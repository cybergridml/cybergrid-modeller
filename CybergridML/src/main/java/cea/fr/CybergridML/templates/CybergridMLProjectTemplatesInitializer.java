package cea.fr.CybergridML.templates;

import cea.fr.CybergridML.domain.CybergridMLViewType;
import cea.fr.CybergridML.view.CybergridMLRepresentationDescriptionIdentifiers;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextPersistenceService;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.events.ICause;
import org.eclipse.sirius.web.application.project.services.api.ISemanticDataInitializer;
import org.eclipse.syson.application.sysmlv2.api.IDefaultSysMLv2ResourceProvider;
import org.eclipse.syson.sysml.*;
import org.eclipse.syson.sysml.Package;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class CybergridMLProjectTemplatesInitializer implements ISemanticDataInitializer {

    private static final String CYBERGRIDML_DOCUMENT_NAME = "CyberGridML.sysml";


    private final IDefaultSysMLv2ResourceProvider defaultSysMLv2ResourceProvider;
    private final CybergridMLModelMutationElementService cybergridMLModelMutationElementService;
    private final CybergridMLDiagramCreationService diagramMutationDiagramService;
    private final IEditingContextPersistenceService editingContextPersistenceService;

    public CybergridMLProjectTemplatesInitializer(IDefaultSysMLv2ResourceProvider defaultSysMLv2ResourceProvider,
                                                  CybergridMLModelMutationElementService CybergridMLModelMutationElementService,
                                                  CybergridMLDiagramCreationService diagramMutationDiagramService,
                                                  IEditingContextPersistenceService editingContextPersistenceService) {
        this.defaultSysMLv2ResourceProvider = Objects.requireNonNull(defaultSysMLv2ResourceProvider);
        this.cybergridMLModelMutationElementService = CybergridMLModelMutationElementService;
        this.diagramMutationDiagramService = Objects.requireNonNull(diagramMutationDiagramService);
        this.editingContextPersistenceService = Objects.requireNonNull(editingContextPersistenceService);
    }

    @Override
    public boolean canHandle(String templateId) {
        return CybergridMLProjectTemplatesProvider.CYBERGRIDML_TEMPLATE_ID.equals(templateId);
    }

    @Override
    public void handle(ICause cause, IEditingContext editingContext, String projectTemplateId) {
        if (canHandle(projectTemplateId)) {
            initializeNacreProject(editingContext);
            this.editingContextPersistenceService.persist(cause, editingContext);
        }
    }

    private void initializeNacreProject(IEditingContext editingContext) {
        Optional<ResourceSet> resourceSetOpt = this.getResourceSet(editingContext);
        if (resourceSetOpt.isPresent()) {
            ResourceSet resourceSet = resourceSetOpt.get();
            Resource resource = defaultSysMLv2ResourceProvider.getDefaultSysMLv2Resource(UUID.randomUUID(), CYBERGRIDML_DOCUMENT_NAME);
            resourceSet.getResources().add(resource);

            Optional<ViewUsage> viewUsageOpt = Optional.empty();
            Optional<ViewUsage> viewUsageOpt1 = Optional.empty();
            Optional<ViewUsage> viewUsageOpt2 = Optional.empty();
            Optional<Package> packageOpt = this.getRootPackage(resource);
            if (packageOpt.isPresent()) {
                Package packageElt = packageOpt.get();
                packageElt.setDeclaredName("CyberGridML");
                viewUsageOpt2 = cybergridMLModelMutationElementService.createViewUsage(packageElt, "CFA");
                viewUsageOpt2.ifPresent(viewUsage -> cybergridMLModelMutationElementService.typeViewUsage(packageElt, viewUsage, CybergridMLViewType.CFA_VIEW));
                viewUsageOpt = cybergridMLModelMutationElementService.createViewUsage(packageElt, "CRA");
                viewUsageOpt.ifPresent(viewUsage -> cybergridMLModelMutationElementService.typeViewUsage(packageElt, viewUsage, CybergridMLViewType.CRA_VIEW));
                viewUsageOpt1 = cybergridMLModelMutationElementService.createViewUsage(packageElt, "CFRA");
                viewUsageOpt1.ifPresent(viewUsage -> cybergridMLModelMutationElementService.typeViewUsage(packageElt, viewUsage, CybergridMLViewType.CFRA_VIEW));

            }

            if (viewUsageOpt.isPresent()) {
                diagramMutationDiagramService.createDiagram(viewUsageOpt2.get(), editingContext, CybergridMLRepresentationDescriptionIdentifiers.CFA_VIEW_DIAGRAM_DESCRIPTION_ID);
                diagramMutationDiagramService.createDiagram(viewUsageOpt.get(), editingContext, CybergridMLRepresentationDescriptionIdentifiers.CRA_VIEW_DIAGRAM_DESCRIPTION_ID);
                diagramMutationDiagramService.createDiagram(viewUsageOpt1.get(), editingContext, CybergridMLRepresentationDescriptionIdentifiers.CFRA_VIEW_DIAGRAM_DESCRIPTION_ID);

            }
        }
    }


    private Optional<Package> getRootPackage(Resource resource) {
        Object rootElement = resource.getContents().get(0);
        if (rootElement instanceof Namespace rootNamespace) {
            Element rootMember = rootNamespace.getOwnedMember().get(0);
            if (rootMember instanceof Package rootPackage) {
                return Optional.of(rootPackage);
            }
        }
        return Optional.empty();
    }

    private Optional<ResourceSet> getResourceSet(IEditingContext editingContext) {
        return Optional.of(editingContext)
                .filter(IEMFEditingContext.class::isInstance)
                .map(IEMFEditingContext.class::cast)
                .map(IEMFEditingContext::getDomain)
                .map(AdapterFactoryEditingDomain::getResourceSet);
    }
}

