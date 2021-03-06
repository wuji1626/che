/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.git;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto.Type;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.watcher.FileWatcherManager;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.nio.file.PathMatcher;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.nio.file.Files.isDirectory;
import static java.util.regex.Pattern.compile;
import static org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto.Type.BRANCH;
import static org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto.Type.REVISION;
import static org.eclipse.che.api.vfs.watcher.FileWatcherManager.EMPTY_CONSUMER;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;


public class GitCheckoutDetector {
    private static final Logger LOG = getLogger(GitCheckoutDetector.class);

    private static final String  GIT_DIR         = ".git";
    private static final String  HEAD_FILE       = "HEAD";
    private static final Pattern PATTERN         = compile("ref: refs/heads/");
    private static final String  INCOMING_METHOD = "track/git-checkout";
    private static final String  OUTGOING_METHOD = "event/git-checkout";

    private final VirtualFileSystemProvider vfsProvider;
    private final RequestTransmitter        transmitter;
    private final FileWatcherManager        manager;
    private final ProjectRegistry           projectRegistry;

    private final Set<String> endpointIds = newConcurrentHashSet();

    private int id;

    @Inject
    public GitCheckoutDetector(VirtualFileSystemProvider vfsProvider,
                               RequestTransmitter transmitter,
                               FileWatcherManager manager,
                               ProjectRegistry projectRegistry) {
        this.vfsProvider = vfsProvider;
        this.transmitter = transmitter;
        this.manager = manager;
        this.projectRegistry = projectRegistry;
    }

    @Inject
    public void configureHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName(INCOMING_METHOD)
                    .noParams()
                    .noResult()
                    .withConsumer(endpointIds::add);
    }

    @PostConstruct
    public void startWatcher() {
        id = manager.registerByMatcher(matcher(), createConsumer(), modifyConsumer(), deleteConsumer());
    }

    @PreDestroy
    public void stopWatcher() {
        manager.unRegisterByMatcher(id);
    }


    private PathMatcher matcher() {
        return it -> !isDirectory(it) &&
                     HEAD_FILE.equals(it.getFileName().toString()) &&
                     GIT_DIR.equals(it.getParent().getFileName().toString());
    }

    private Consumer<String> createConsumer() {
        return fsEventConsumer();
    }

    private Consumer<String> modifyConsumer() {
        return fsEventConsumer();
    }

    private Consumer<String> deleteConsumer() {
        return EMPTY_CONSUMER;
    }

    private Consumer<String> fsEventConsumer() {
        return it -> {
            try {
                String content = vfsProvider.getVirtualFileSystem()
                                            .getRoot()
                                            .getChild(Path.of(it))
                                            .getContentAsString();
                Type type = content.contains("ref:") ? BRANCH : REVISION;
                String name = type == REVISION ? content : PATTERN.split(content)[1];

                //Update project attributes with new git values
                projectRegistry.setProjectType(it.split("/")[1], GitProjectType.TYPE_ID, true);

                endpointIds.forEach(transmitConsumer(type, name));

            } catch (ServerException | ForbiddenException e) {
                LOG.error("Error trying to read {} file and broadcast it", it, e);
            } catch (NotFoundException | ConflictException e) {
                LOG.error("Error trying to update project attributes", it, e);
            }
        };
    }

    private Consumer<String> transmitConsumer(Type type, String name) {
        return id -> transmitter.newRequest()
                                .endpointId(id)
                                .methodName(OUTGOING_METHOD)
                                .paramsAsDto(newDto(GitCheckoutEventDto.class).withName(name).withType(type))
                                .sendAndSkipResult();
    }
}
