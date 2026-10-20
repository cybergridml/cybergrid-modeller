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

package cea.fr.CybergridML.domain;

import java.util.Objects;

public enum CybergridMLViewType {

    CRA_VIEW("CyberGridML::CRA"),
    CFA_VIEW("CyberGridML::CFA"),
    CFRA_VIEW("CyberGridML::CFRA");

    private final String CybergridMLLibDefinitionName;

    CybergridMLViewType(String CybergridMLLibDefinitionName) {
        this.CybergridMLLibDefinitionName = Objects.requireNonNull(CybergridMLLibDefinitionName);
    }

    public String getCybergridMLLibDefinitionName() {
        return CybergridMLLibDefinitionName;
    }
}

