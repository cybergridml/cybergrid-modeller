/*******************************************************************************
 * Copyright (c) 2026 CEA LIST - RTE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     CEA LIST - Initial API and implementation
 *******************************************************************************/

package cea.fr.CybergridML.services.xiidm;

/** Thrown when XIIDM parsing fails. */
public class XiidmParseException extends Exception {

    public XiidmParseException(String message) {
        super(message);
    }

    public XiidmParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
