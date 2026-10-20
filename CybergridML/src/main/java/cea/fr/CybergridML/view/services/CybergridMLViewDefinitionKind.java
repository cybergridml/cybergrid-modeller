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

package cea.fr.CybergridML.view.services;

public enum CybergridMLViewDefinitionKind {
    CRA_VIEW,
    CFA_VIEW,
    CFRA_VIEW;

    public static CybergridMLViewDefinitionKind getKind(String viewDefinitionName) {
        if ("CyberGridML::CRA".equals(viewDefinitionName)) {
            return CRA_VIEW;
        }
        if ("CyberGridML::CFA".equals(viewDefinitionName)) {
            return CFA_VIEW;
        }
        if ("CyberGridML::CFRA".equals(viewDefinitionName)) {
            return CFRA_VIEW;
        }
        return null;
    }
}
