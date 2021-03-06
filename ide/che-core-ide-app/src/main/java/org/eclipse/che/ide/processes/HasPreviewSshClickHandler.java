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
package org.eclipse.che.ide.processes;

/**
 * An object that implements this interface provides registration for {@link PreviewSshClickHandler} instances.
 *
 * @author Vlad Zhukovskyi
 * @see PreviewSshClickHandler
 * @since 5.11.0
 */
public interface HasPreviewSshClickHandler {

    /**
     * Adds a {@link PreviewSshClickHandler} handler.
     *
     * @param handler
     *         the preview ssh click handler
     */
    void addPreviewSshClickHandler(PreviewSshClickHandler handler);
}
