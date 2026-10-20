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

import org.eclipse.sirius.components.collaborative.api.IRepresentationMetadataPersistenceService;
import org.eclipse.sirius.components.collaborative.api.IRepresentationPersistenceService;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramCreationService;
import org.eclipse.sirius.components.core.RepresentationMetadata;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.description.DiagramDescription;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.ViewUsage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
@Service
public class CybergridMLDiagramCreationService {


    private final IDiagramCreationService diagramCreationService;
    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;
    private final IRepresentationMetadataPersistenceService representationMetadataPersistenceService;
    private final IRepresentationPersistenceService representationPersistenceService;

    public CybergridMLDiagramCreationService(IDiagramCreationService diagramCreationService, IRepresentationDescriptionSearchService representationDescriptionSearchService,
                                             IRepresentationMetadataPersistenceService representationMetadataPersistenceService, IRepresentationPersistenceService representationPersistenceService) {
        this.diagramCreationService = Objects.requireNonNull(diagramCreationService);
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
        this.representationMetadataPersistenceService = Objects.requireNonNull(representationMetadataPersistenceService);
        this.representationPersistenceService = Objects.requireNonNull(representationPersistenceService);
    }

    public Element createDiagram(Element element, IEditingContext editingContext, String representationDescriptionIdentifiers) {
        if (element instanceof ViewUsage viewUsage) {
            this.representationDescriptionSearchService.findById(editingContext, representationDescriptionIdentifiers)
                    .filter(DiagramDescription.class::isInstance)
                    .map(DiagramDescription.class::cast)
                    .ifPresent(diagramDescription -> {
                        var variableManager = new VariableManager();
                        variableManager.put(VariableManager.SELF, viewUsage);
                        variableManager.put(DiagramDescription.LABEL, viewUsage.getDeclaredName());
                        String label = diagramDescription.getLabelProvider().apply(variableManager);
                        List<String> iconURLs = diagramDescription.getIconURLsProvider().apply(variableManager);

                        Diagram diagram = this.diagramCreationService.create(editingContext, diagramDescription, viewUsage);
                        var representationMetadata = RepresentationMetadata.newRepresentationMetadata(diagram.getId())
                                .kind(diagram.getKind())
                                .label(label)
                                .descriptionId(diagram.getDescriptionId())
                                .iconURLs(iconURLs)
                                .build();
                        this.representationMetadataPersistenceService.save(null, editingContext, representationMetadata, diagram.getTargetObjectId());
                        this.representationPersistenceService.save(null, editingContext, diagram);
                    });
        }
        return element;
    }
}
