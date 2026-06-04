package cea.fr.NacreRTE.project;

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
public class NacreDiagramMutationDiagramService {


    private final IDiagramCreationService diagramCreationService;
    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;
    private final IRepresentationMetadataPersistenceService representationMetadataPersistenceService;
    private final IRepresentationPersistenceService representationPersistenceService;

    public NacreDiagramMutationDiagramService(IDiagramCreationService diagramCreationService, IRepresentationDescriptionSearchService representationDescriptionSearchService,
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

                        Diagram diagram = this.diagramCreationService.create(viewUsage, diagramDescription, editingContext);
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

