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

package cea.fr.CybergridML.templates;

import cea.fr.CybergridML.domain.CybergridMLViewType;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.sysml.*;
import org.eclipse.syson.sysml.util.ElementUtil;
import org.eclipse.syson.util.GetIntermediateContainerCreationSwitch;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CybergridMLModelMutationElementService {

    private final ElementUtil elementUtil;

    public CybergridMLModelMutationElementService() {
        this.elementUtil = new ElementUtil();
    }

    public Optional<ViewUsage> createViewUsage(Element ownerElement, String viewName) {
        Optional<ViewUsage> viewUsageOpt = Optional.empty();

        Optional<EClass> membershipClassOpt = new GetIntermediateContainerCreationSwitch(ownerElement).doSwitch(ownerElement.eClass());
        if (membershipClassOpt.isPresent()) {
            EObject viewUsageMembership = SysmlFactory.eINSTANCE.create(membershipClassOpt.get());
            if (viewUsageMembership instanceof Relationship viewUsageRelationship) {
                ViewUsage viewUsage = SysmlFactory.eINSTANCE.createViewUsage();
                ownerElement.getOwnedRelationship().add(viewUsageRelationship);
                viewUsageRelationship.getOwnedRelatedElement().add(viewUsage);
                viewUsage.setDeclaredName(viewName);
                viewUsageOpt = Optional.of(viewUsage);
            }
        }

        return viewUsageOpt;
    }

    public void typeViewUsage(Element ownerElement, ViewUsage viewUsage, CybergridMLViewType cybergridMLViewType) {
        FeatureTyping featureTyping = SysmlFactory.eINSTANCE.createFeatureTyping();
        viewUsage.getOwnedRelationship().add(featureTyping);
        var generalViewViewDef = this.elementUtil.findByNameAndType(ownerElement, cybergridMLViewType.getCybergridMLLibDefinitionName(), ViewDefinition.class);
        featureTyping.setType(generalViewViewDef);
        featureTyping.setTypedFeature(viewUsage);
    }
}

