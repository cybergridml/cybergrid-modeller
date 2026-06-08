package cea.fr.NacreRTE.project;

import cea.fr.NacreRTE.domain.NacreViewType;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.sysml.*;
import org.eclipse.syson.sysml.util.ElementUtil;
import org.eclipse.syson.util.GetIntermediateContainerCreationSwitch;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NacreModelMutationElementService {

    private final ElementUtil elementUtil;

    public NacreModelMutationElementService() {
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

    public void typeViewUsage(Element ownerElement, ViewUsage viewUsage, NacreViewType nacreViewType) {
        FeatureTyping featureTyping = SysmlFactory.eINSTANCE.createFeatureTyping();
        viewUsage.getOwnedRelationship().add(featureTyping);
        var generalViewViewDef = this.elementUtil.findByNameAndType(ownerElement, nacreViewType.getNacreLibDefinitionName(), ViewDefinition.class);
        featureTyping.setType(generalViewViewDef);
        featureTyping.setTypedFeature(viewUsage);
    }
}

