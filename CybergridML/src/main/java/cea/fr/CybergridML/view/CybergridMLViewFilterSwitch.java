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

package cea.fr.CybergridML.view;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.sysml.util.SysmlSwitch;

public class CybergridMLViewFilterSwitch extends SysmlSwitch<Boolean> {

    @Override
    public Boolean defaultCase(EObject object) {
        return Boolean.TRUE;
    }
}
