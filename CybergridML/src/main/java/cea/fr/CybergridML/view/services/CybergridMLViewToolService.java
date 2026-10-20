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

import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.springframework.stereotype.Service;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.view.emf.IViewRepresentationDescriptionSearchService;
import org.eclipse.syson.diagram.common.view.services.ViewToolService;
import org.eclipse.syson.services.api.ISysMLMoveElementService;
import org.eclipse.syson.sysml.*;
import org.eclipse.syson.tree.explorer.services.api.ISysONExplorerService;

import java.util.ArrayList;
import java.util.List;

@Service
public class CybergridMLViewToolService extends ViewToolService {

    private final CybergridMLUtilService cybergridMLUtilService;

    public CybergridMLViewToolService(IIdentityService identityService, IObjectSearchService objectSearchService, IViewRepresentationDescriptionSearchService viewRepresentationDescriptionSearchService, IFeedbackMessageService feedbackMessageService, ISysMLMoveElementService moveService, ISysONExplorerService sysONExplorerService, ILabelService labelService) {
        super(identityService, objectSearchService, viewRepresentationDescriptionSearchService, feedbackMessageService, moveService, sysONExplorerService, labelService);
        cybergridMLUtilService = new CybergridMLUtilService();
    }

    @Override
    public boolean toolShouldBeAvailable(Element element, IEditingContext editingContext, DiagramContext diagramContext, EClass newElementType) {
        if (!(element instanceof ViewUsage)) {
            return true;
        }
        CybergridMLViewDefinitionKind viewDefinitionKind = this.cybergridMLUtilService.getCybergridMLViewDefinitionKind(element, List.of(), editingContext);
        if (viewDefinitionKind == null) {
            return true;
        }
        return switch (viewDefinitionKind) {
            case CFA_VIEW -> this.toolShouldBeAvailableOnCFAView(newElementType);
            case CRA_VIEW -> this.toolShouldBeAvailableOnCRAView(newElementType);
            case CFRA_VIEW -> this.toolShouldBeAvailableOnCFRAView(newElementType);
        };
    }

    private boolean toolShouldBeAvailableOnCFAView(EClass newElementType) {
        return SysmlPackage.eINSTANCE.getPartUsage().equals(newElementType);
    }

    private boolean toolShouldBeAvailableOnCFRAView(EClass newElementType) {
        return SysmlPackage.eINSTANCE.getActionUsage().equals(newElementType) ||
                SysmlPackage.eINSTANCE.getPartUsage().equals(newElementType);
    }

    private boolean toolShouldBeAvailableOnCRAView(EClass newElementType) {
        return SysmlPackage.eINSTANCE.getActionUsage().equals(newElementType) ||
                SysmlPackage.eINSTANCE.getPartUsage().equals(newElementType);
    }

    public List<Element> getComputingResourceSelectionDialogElements(IEditingContext editingContext) {
        List<Element> computingResources = new ArrayList<>();
        if (editingContext instanceof IEMFEditingContext emfEditingContext) {
            for (Resource resource : emfEditingContext.getDomain().getResourceSet().getResources()) {
                var contents = resource.getAllContents();
                while (contents.hasNext()) {
                    var eObject = contents.next();
                    if (eObject instanceof PartUsage partUsage
                            && CybergridMLLibElementType.COMPUTINGRESOURCE == CybergridMLLibElementType.getCybergridMLElementFromUsage(partUsage)) {
                        computingResources.add(partUsage);
                    }
                }
            }
        }
        return computingResources;
    }

    public List<Object> getComputingResourceSelectionDialogChildren(Object selectionDialogTreeElement) {
        return List.of();
    }
}
