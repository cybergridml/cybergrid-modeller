package cea.fr.CybergridML.project;

import cea.fr.CybergridML.domain.CybergridMLViewType;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.IInputPostProcessor;
import org.eclipse.sirius.components.collaborative.api.IInputPreProcessor;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationInput;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextPersistenceService;
import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.ViewDefinition;
import org.eclipse.syson.sysml.ViewUsage;
import org.eclipse.syson.sysml.util.ElementUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.Objects;
import java.util.Optional;


@Service
public class CybergridMLCreateRepresentationInputProcessor implements IInputPreProcessor, IInputPostProcessor {

    private final IObjectSearchService objectSearchService;
    private final IEditingContextPersistenceService editingContextPersistenceService;
    private final CybergridMLModelMutationElementService cybergridMLModelMutationElementService;
    private final ElementUtil elementUtil;

    public CybergridMLCreateRepresentationInputProcessor(IObjectSearchService objectSearchService,
                                                         IEditingContextPersistenceService editingContextPersistenceService,
                                                         CybergridMLModelMutationElementService cybergridMLModelMutationElementService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.editingContextPersistenceService = Objects.requireNonNull(editingContextPersistenceService);
        this.cybergridMLModelMutationElementService = Objects.requireNonNull(cybergridMLModelMutationElementService);
        this.elementUtil = new ElementUtil();
    }

    @Override
    public IInput preProcess(IEditingContext editingContext, IInput input, Sinks.Many<ChangeDescription> changeDescriptionSink) {
        if (!(input instanceof CreateRepresentationInput createRepresentationInput) || !this.canHandle(createRepresentationInput)) {
            return input;
        }

        Optional<Element> optElement = this.getElement(editingContext, createRepresentationInput);
        if (optElement.isEmpty()) {
            return input;
        }

        Element containerElement = optElement.get();
        ViewUsage viewUsage;
        if (containerElement instanceof ViewUsage containerVU) {
            viewUsage = containerVU;
        } else {
            viewUsage = this.createViewUsage(input, containerElement, createRepresentationInput.representationName());
        }

        if (viewUsage != null) {
            return new CreateRepresentationInput(
                    input.id(),
                    createRepresentationInput.editingContextId(),
                    createRepresentationInput.representationDescriptionId(),
                    viewUsage.getElementId(),
                    createRepresentationInput.representationName()
            );
        }
        return input;
    }

    @Override
    public void postProcess(IEditingContext editingContext, IInput input, Sinks.Many<ChangeDescription> changeDescriptionSink) {
        if (input instanceof CreateRepresentationInput createRepresentationInput && this.canHandle(createRepresentationInput)) {
            this.editingContextPersistenceService.persist(createRepresentationInput, editingContext);
        }
    }

    private boolean canHandle(CreateRepresentationInput input) {
        String id = input.representationDescriptionId();
        return Objects.equals(CybergridMLRepresentationDescriptionIdentifiers.CFA_VIEW_DIAGRAM_DESCRIPTION_ID, id)
                || Objects.equals(CybergridMLRepresentationDescriptionIdentifiers.CRA_VIEW_DIAGRAM_DESCRIPTION_ID, id)
                || Objects.equals(CybergridMLRepresentationDescriptionIdentifiers.CFRA_VIEW_DIAGRAM_DESCRIPTION_ID, id);
    }

    private Optional<Element> getElement(IEditingContext editingContext, CreateRepresentationInput input) {
        return this.objectSearchService.getObject(editingContext, input.objectId())
                .filter(Element.class::isInstance)
                .map(Element.class::cast);
    }

    private ViewUsage createViewUsage(IInput input, Element containerElement, String viewUsageName) {
        Optional<ViewUsage> viewUsageOpt = this.cybergridMLModelMutationElementService.createViewUsage(containerElement, viewUsageName);
        if (viewUsageOpt.isEmpty()) {
            return null;
        }
        ViewUsage viewUsage = viewUsageOpt.get();
        this.setViewDefinition(containerElement, viewUsage, input);
        return viewUsage;
    }

    private void setViewDefinition(Element containerElement, ViewUsage viewUsage, IInput input) {
        Optional<ViewDefinition> viewDefinitionOpt = this.getViewDefinition(containerElement, (CreateRepresentationInput) input);
        viewDefinitionOpt.ifPresent(viewDefinition -> {
            var featureTyping = SysmlFactory.eINSTANCE.createFeatureTyping();
            viewUsage.getOwnedRelationship().add(featureTyping);
            featureTyping.setType(viewDefinition);
            featureTyping.setTypedFeature(viewUsage);
        });
    }

    private Optional<ViewDefinition> getViewDefinition(Element containerElement, CreateRepresentationInput input) {
        String id = input.representationDescriptionId();
        CybergridMLViewType cybergridMLViewType = null;
        if (Objects.equals(CybergridMLRepresentationDescriptionIdentifiers.CFA_VIEW_DIAGRAM_DESCRIPTION_ID, id)) {
            cybergridMLViewType = CybergridMLViewType.CFA_VIEW;
        } else if (Objects.equals(CybergridMLRepresentationDescriptionIdentifiers.CRA_VIEW_DIAGRAM_DESCRIPTION_ID, id)) {
            cybergridMLViewType = CybergridMLViewType.CRA_VIEW;
        } else if (Objects.equals(CybergridMLRepresentationDescriptionIdentifiers.CFRA_VIEW_DIAGRAM_DESCRIPTION_ID, id)) {
            cybergridMLViewType = CybergridMLViewType.CFRA_VIEW;
        }
        if (cybergridMLViewType == null) {
            return Optional.empty();
        }
        ViewDefinition viewDef = this.elementUtil.findByNameAndType(containerElement, cybergridMLViewType.getCybergridMLLibDefinitionName(), ViewDefinition.class);
        return Optional.ofNullable(viewDef);
    }
}

