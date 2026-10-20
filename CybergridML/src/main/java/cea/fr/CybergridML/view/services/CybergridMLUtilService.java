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

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Type;
import org.eclipse.syson.sysml.ViewUsage;

import java.util.List;

public class CybergridMLUtilService {

    public CybergridMLViewDefinitionKind getCybergridMLViewDefinitionKind(Element element, List<Object> ancestors, IEditingContext editingContext) {
        CybergridMLViewDefinitionKind kind = CybergridMLViewDefinitionKind.CFA_VIEW;
        if (element instanceof ViewUsage viewUsage) {
            var types = viewUsage.getType();
            if (types != null && !types.isEmpty()) {
                Type type = types.get(0);
                if (type != null) {
                    CybergridMLViewDefinitionKind resolved = CybergridMLViewDefinitionKind.getKind(type.getQualifiedName());
                    if (resolved != null) {
                        kind = resolved;
                    }
                }
            }
        } else {
            var viewUsageContainingElement = ancestors.stream().filter(ViewUsage.class::isInstance).map(ViewUsage.class::cast).findFirst();
            if (viewUsageContainingElement.isPresent()) {
                ViewUsage viewUsage = viewUsageContainingElement.get();
                kind = this.getCybergridMLViewDefinitionKind(viewUsage, List.of(), editingContext);
            }
        }
        return kind;
    }
}
