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
package org.eclipse.che.api.agent;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.impl.BasicAgent;

import java.io.IOException;

/**
 * Terminal agent.
 *
 * @see Agent
 *
 * @author Garagatyi Alexander
 */
@Singleton
public class TerminalAgent extends BasicAgent {
    private static final String AGENT_DESCRIPTOR = "org.eclipse.che.terminal.json";
    private static final String AGENT_SCRIPT     = "org.eclipse.che.terminal.script.sh";

    @Inject
    public TerminalAgent() throws IOException {
        super(AGENT_DESCRIPTOR, AGENT_SCRIPT);
    }
}
