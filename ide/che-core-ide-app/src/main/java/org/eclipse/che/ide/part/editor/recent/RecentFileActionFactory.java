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
package org.eclipse.che.ide.part.editor.recent;

import org.eclipse.che.ide.api.resources.File;

/**
 * Factory for the recent files action.
 *
 * @author Vlad Zhukovskiy
 */
public interface RecentFileActionFactory {
    /**
     * Creates new recent file action to show in main menu.
     *
     * @param file
     *         file associated with
     * @return action
     */
    RecentFileAction newRecentFileAction(File file);
}
