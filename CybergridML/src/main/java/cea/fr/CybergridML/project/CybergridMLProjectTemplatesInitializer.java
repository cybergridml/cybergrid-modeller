package cea.fr.CybergridML.project;


import cea.fr.CybergridML.domain.CybergridMLViewType;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.sirius.components.core.RepresentationMetadata;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.events.ICause;
import org.eclipse.sirius.web.application.project.services.api.IProjectTemplateInitializer;
import org.eclipse.syson.application.sysmlv2.api.IDefaultSysMLv2ResourceProvider;
import org.eclipse.syson.sysml.*;
import org.eclipse.syson.sysml.Package;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class CybergridMLProjectTemplatesInitializer implements IProjectTemplateInitializer {

    private static final String Nacre_DOCUMENT_NAME = "CyberGridML.sysml";


    private final IDefaultSysMLv2ResourceProvider defaultSysMLv2ResourceProvider;
    private final CybergridMLModelMutationElementService NacreModelMutationElementService;
    private final CybergridMLElementUtil elementUtil;
    private final CybergridMLDiagramCreationService diagramMutationDiagramService;

    public CybergridMLProjectTemplatesInitializer(IDefaultSysMLv2ResourceProvider defaultSysMLv2ResourceProvider,
                                                  CybergridMLModelMutationElementService NacreModelMutationElementService,
                                                  CybergridMLDiagramCreationService diagramMutationDiagramService) {
        this.defaultSysMLv2ResourceProvider = Objects.requireNonNull(defaultSysMLv2ResourceProvider);
        this.NacreModelMutationElementService = NacreModelMutationElementService;
        this.diagramMutationDiagramService = Objects.requireNonNull(diagramMutationDiagramService);
        this.elementUtil = new CybergridMLElementUtil();
    }

    @Override
    public boolean canHandle(String templateId) {
        return CybergridMLProjectTemplatesProvider.NACRE_TEMPLATE_ID.equals(templateId);
    }

    @Override
    public Optional<RepresentationMetadata> handle(ICause cause, String projectTemplateId, IEditingContext editingContext) {
        if (!canHandle(projectTemplateId)) {
            return Optional.empty();
        }
        return initializeNacreProject(editingContext);
    }

    private Optional<RepresentationMetadata> initializeNacreProject(IEditingContext editingContext) {
        Optional<ResourceSet> resourceSetOpt = this.getResourceSet(editingContext);
        if (resourceSetOpt.isPresent()) {
            ResourceSet resourceSet = resourceSetOpt.get();
            Resource resource = defaultSysMLv2ResourceProvider.getDefaultSysMLv2Resource(UUID.randomUUID(), Nacre_DOCUMENT_NAME);
            resourceSet.getResources().add(resource);

            Optional<ViewUsage> viewUsageOpt = Optional.empty();
            Optional<ViewUsage> viewUsageOpt1 = Optional.empty();
            Optional<ViewUsage> viewUsageOpt2 = Optional.empty();
            Optional<Package> packageOpt = elementUtil.getRootPackage(resource);
            if (packageOpt.isPresent()) {
                Package packageElt = packageOpt.get();
                packageElt.setDeclaredName("CyberGridML");
                viewUsageOpt = NacreModelMutationElementService.createViewUsage(packageElt, "CRA");
                viewUsageOpt.ifPresent(viewUsage -> NacreModelMutationElementService.typeViewUsage(packageElt, viewUsage, CybergridMLViewType.CRA_VIEW));
                viewUsageOpt1 = NacreModelMutationElementService.createViewUsage(packageElt, "CFRA");
                viewUsageOpt1.ifPresent(viewUsage -> NacreModelMutationElementService.typeViewUsage(packageElt, viewUsage, CybergridMLViewType.CFRA_VIEW));
                viewUsageOpt2 = NacreModelMutationElementService.createViewUsage(packageElt, "CFA");
                viewUsageOpt2.ifPresent(viewUsage -> NacreModelMutationElementService.typeViewUsage(packageElt, viewUsage, CybergridMLViewType.CFA_VIEW));
            }

            if (viewUsageOpt.isPresent()) {
                diagramMutationDiagramService.createDiagram(viewUsageOpt.get(), editingContext, CybergridMLRepresentationDescriptionIdentifiers.CRA_VIEW_DIAGRAM_DESCRIPTION_ID);
                diagramMutationDiagramService.createDiagram(viewUsageOpt1.get(), editingContext, CybergridMLRepresentationDescriptionIdentifiers.CFRA_VIEW_DIAGRAM_DESCRIPTION_ID);
                diagramMutationDiagramService.createDiagram(viewUsageOpt2.get(), editingContext, CybergridMLRepresentationDescriptionIdentifiers.CFA_VIEW_DIAGRAM_DESCRIPTION_ID);
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

