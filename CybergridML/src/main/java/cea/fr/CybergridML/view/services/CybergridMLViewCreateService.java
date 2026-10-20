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
import cea.fr.CybergridML.services.CybergridMLElementInitializerSwitch;
import cea.fr.CybergridML.services.xiidm.XiidmParseException;
import cea.fr.CybergridML.services.xiidm.XiidmParser;
import cea.fr.CybergridML.services.xiidm.XiidmToCybergridMLMapper;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.representations.Message;
import org.eclipse.sirius.components.representations.MessageLevel;
import org.springframework.stereotype.Service;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IReadOnlyObjectPredicate;
import org.eclipse.syson.diagram.common.view.services.ShowDiagramsInheritedMembersService;
import org.eclipse.syson.diagram.common.view.services.ViewCreateService;
import org.eclipse.syson.sysml.*;
import org.eclipse.syson.sysml.Package;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
public class CybergridMLViewCreateService extends ViewCreateService {

    private final IFeedbackMessageService feedbackMessageService;

    private final XiidmParser xiidmParser;

    private final XiidmToCybergridMLMapper xiidmMapper;

    public CybergridMLViewCreateService(IObjectSearchService objectSearchService,
                                        IReadOnlyObjectPredicate readOnlyObjectPredicate,
                                        ShowDiagramsInheritedMembersService showDiagramsInheritedMembersService,
                                        IFeedbackMessageService feedbackMessageService,
                                        XiidmParser xiidmParser,
                                        XiidmToCybergridMLMapper xiidmMapper) {
        super(objectSearchService, readOnlyObjectPredicate, showDiagramsInheritedMembersService, feedbackMessageService);
        this.feedbackMessageService = Objects.requireNonNull(feedbackMessageService);
        this.xiidmParser = Objects.requireNonNull(xiidmParser);
        this.xiidmMapper = Objects.requireNonNull(xiidmMapper);
    }

    public List<Element> importXiidm(Element self, String xiidmContent, String xiidmFileName, IEditingContext editingContext) {
        if (xiidmContent == null || xiidmContent.isBlank()) {
            return findSubstations(self);
        }
        try {
            var network = this.xiidmParser.parse(xiidmContent);
            var result = this.xiidmMapper.importInto(self, network, editingContext);
            this.addMessage("%s %s".formatted(fileLabel(xiidmFileName), result.summary()), MessageLevel.SUCCESS);
            result.warnings().forEach(warning -> this.addMessage(warning, MessageLevel.WARNING));
            return result.createdRootElements();
        } catch (XiidmParseException exception) {
            this.addMessage("%s %s".formatted(fileLabel(xiidmFileName), exception.getMessage()), MessageLevel.ERROR);
            return List.of();
        }
    }

    private static String fileLabel(String xiidmFileName) {
        return xiidmFileName == null || xiidmFileName.isBlank() ? "XIIDM import:" : "'%s':".formatted(xiidmFileName);
    }

    private List<Element> findSubstations(Element parent) {
        return parent.getOwnedElement().stream()
                .filter(element -> CybergridMLLibElementType.getCybergridMLElementFromUsage((Feature) element) == CybergridMLLibElementType.SUBSTATION)
                .toList();
    }

    private void addMessage(String body, MessageLevel level) {
        this.feedbackMessageService.addFeedbackMessage(new Message(body, level));
    }

    @Override
    public boolean canCreateDiagram(Element element) {
        return element instanceof Package || element instanceof ViewUsage;
    }

    public Element elementInitializer(Element element, IEditingContext editingContext, String lcaElementTypeName) {
        CybergridMLElementInitializerSwitch elementInitializerSwitch = new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.getCybergridMLElementFromName(lcaElementTypeName));
        return elementInitializerSwitch.doSwitch(element);
    }

    public PartUsage createMainGrid(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.MAINGRID).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createZone(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.ZONE).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createSubstation(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.SUBSTATION).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createCentralController(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.CENTRALCONTROLLER).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createZonalController(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.ZONALCONTROLLER).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createCommunicationMedia(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.COMMUNICATIONMEDIA).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createComputingResource(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.COMPUTINGRESOURCE).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createSoftwareResource(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.SOFTWARERESOURCE).doSwitch(newPartUsage);
        return newPartUsage;
    }
    public PartUsage createRedundancyCluster(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.REDUNDANCYCLUSTER).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public Element createAbstractAllocateEdge(Element source, Element target, IEditingContext editingContext) {
        Element owner = this.findCfraView(source);
        if (owner == null) {
            owner = source.getOwner();
        }

        var ownerMembership = SysmlFactory.eINSTANCE.createOwningMembership();
        owner.getOwnedRelationship().add(ownerMembership);
        var allocation = SysmlFactory.eINSTANCE.createAllocationUsage();
        ownerMembership.getOwnedRelatedElement().add(allocation);

        this.addEndToAbstractAllocateEdge(allocation, source);
        this.addEndToAbstractAllocateEdge(allocation, target);
        return source;
    }

    private void addEndToAbstractAllocateEdge(AllocationUsage allocation, Element end) {
        if (end instanceof Usage usage) {
            var featureMembership = SysmlFactory.eINSTANCE.createEndFeatureMembership();
            allocation.getOwnedRelationship().add(featureMembership);
            var feature = SysmlFactory.eINSTANCE.createFeature();
            featureMembership.getOwnedRelatedElement().add(feature);
            var reference = SysmlFactory.eINSTANCE.createReferenceSubsetting();
            feature.getOwnedRelationship().add(reference);
            reference.setReferencedFeature(usage);
        }
    }

    /** Walks up the ownership chain of the given element looking for the CFRA view, either as an ancestor or as a sibling of one. */
    private Element findCfraView(Element element) {
        Element current = element.getOwner();
        while (current != null) {
            if (current instanceof ViewUsage viewUsage && "CFRA".equals(viewUsage.getDeclaredName())) {
                return viewUsage;
            }
            if (current instanceof Namespace namespace) {
                Optional<Element> sibling = namespace.getOwnedMember().stream()
                        .filter(ViewUsage.class::isInstance)
                        .filter(member -> "CFRA".equals(member.getDeclaredName()))
                        .findFirst();
                if (sibling.isPresent()) {
                    return sibling.get();
                }
            }
            current = current.getOwner();
        }
        return null;
    }
}
