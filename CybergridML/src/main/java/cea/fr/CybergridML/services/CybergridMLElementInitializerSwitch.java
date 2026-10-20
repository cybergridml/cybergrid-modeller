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

package cea.fr.CybergridML.services;

import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import cea.fr.CybergridML.domain.CybergridMLDefinitionsUtil;
import cea.fr.CybergridML.domain.CybergridMLPropertyUtil;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.sysml.metamodel.services.ElementInitializerSwitch;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.*;


public class CybergridMLElementInitializerSwitch extends ElementInitializerSwitch {

    private final UtilService utilService;
    private final IEditingContext editingContext;
    private final CybergridMLLibElementType cybergridMLLibElementType;

    public CybergridMLElementInitializerSwitch(IEditingContext editingContext, CybergridMLLibElementType cybergridMLLibElementType) {
        super();
        this.utilService = new UtilService();
        this.editingContext = editingContext;
        this.cybergridMLLibElementType = cybergridMLLibElementType;
    }

    @Override
    public Element caseUsage(Usage usage) {
        if (cybergridMLLibElementType != null) {
            long existingElements = this.countElements(usage, cybergridMLLibElementType);
            usage.setDeclaredName(cybergridMLLibElementType.getName().toLowerCase() + existingElements);
        }
        usage.setIsComposite(true);
        return usage;
    }

    @Override
    public Element casePartUsage(PartUsage usage) {
        if (cybergridMLLibElementType != null) {
            Definition definition = CybergridMLDefinitionsUtil.getDefinition(editingContext, cybergridMLLibElementType);
            utilService.setFeatureTyping(usage, definition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, definition);
        }
        caseUsage(usage);
        return usage;
    }

    @Override
    public Element caseDefinition(Definition definition) {
        if (cybergridMLLibElementType != null) {
            long elementsNb = this.countElements(definition, cybergridMLLibElementType);
            definition.setDeclaredName(cybergridMLLibElementType.getName().toLowerCase() + elementsNb);
        }
        return definition;
    }

    private long countElements(Element element, CybergridMLLibElementType cybergridMLLibElementType) {
        Namespace owningNamespace = element.getOwningNamespace();
        boolean doFilterDefinitions = element instanceof Definition;
        if (owningNamespace != null) {
            return owningNamespace.getOwnedMember().stream()
                    .filter(namespaceElt -> doFilterDefinitions ? namespaceElt instanceof Definition : namespaceElt instanceof Usage)
                    .filter(namespaceElt -> namespaceElt instanceof Feature f && cybergridMLLibElementType == CybergridMLLibElementType.getCybergridMLElementFromUsage(f))
                    .count();
        }
        return 0;
    }
}

