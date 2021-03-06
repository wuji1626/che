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
package org.eclipse.che.ide.commons.exception;

/**
 * Notifies about unmarshalling error accured.
 *
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 */
@SuppressWarnings("serial")
public class UnmarshallerException extends Exception {

    /**
     * Creates an Instance of {@link UnauthorizedException} with message and root cause
     *
     * @param message
     * @param cause
     */
    public UnmarshallerException(String message, Throwable cause) {
        super(message, cause);
    }

}
