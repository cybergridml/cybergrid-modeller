package cea.fr.NacreRTE.services;

import cea.fr.NacreRTE.library.util.NacreDefinitionsUtil;
import cea.fr.NacreRTE.library.util.NacrePropertyUtil;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.MetadataUsage;
import org.eclipse.syson.sysml.OwningMembership;
import org.eclipse.syson.sysml.SysmlFactory;

import java.util.Optional;

public class NacreHazardService {

    private static final String HAZARD_USAGE_NAME = "Hazard";

    public boolean hasHazard(Element element) {
        return findHazardOwningMembership(element).isPresent();
    }

    public Element toggleHazard(Element element, IEditingContext editingContext) {
        var existing = findHazardOwningMembership(element);
        if (existing.isPresent()) {
            element.getOwnedRelationship().remove(existing.get());
        } else {
            element.getOwnedRelationship().add(createHazardOwningMembership(editingContext));
        }
        return element;
    }

    private Optional<OwningMembership> findHazardOwningMembership(Element element) {
        return element.getOwnedRelationship().stream()
                .filter(OwningMembership.class::isInstance)
                .map(OwningMembership.class::cast)
                .filter(om -> om.getOwnedRelatedElement().stream()
                        .filter(MetadataUsage.class::isInstance)
                        .map(MetadataUsage.class::cast)
                        .anyMatch(mu -> HAZARD_USAGE_NAME.equals(mu.getDeclaredName())))
                .findFirst();
    }

    private OwningMembership createHazardOwningMembership(IEditingContext editingContext) {
        var metadataUsage = SysmlFactory.eINSTANCE.createMetadataUsage();
        metadataUsage.setDeclaredName(HAZARD_USAGE_NAME);
        metadataUsage.setIsComposite(true);

        NacreDefinitionsUtil.getHazardDefinition(editingContext).ifPresent(hazardDef -> {
            var featureTyping = SysmlFactory.eINSTANCE.createFeatureTyping();
            featureTyping.setType(hazardDef);
            featureTyping.setTypedFeature(metadataUsage);
            metadataUsage.getOwnedRelationship().add(featureTyping);

            NacrePropertyUtil.addAttributesFromDefinition(metadataUsage, hazardDef);
        });

        var owningMembership = SysmlFactory.eINSTANCE.createOwningMembership();
        owningMembership.getOwnedRelatedElement().add(metadataUsage);
        return owningMembership;
    }
}
