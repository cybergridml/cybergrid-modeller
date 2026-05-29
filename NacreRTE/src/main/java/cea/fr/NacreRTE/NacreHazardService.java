package cea.fr.NacreRTE;

import cea.fr.NacreRTE.library.util.NacreDefinitionsUtil;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.sysml.AttributeUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.FeatureMembership;
import org.eclipse.syson.sysml.FeatureValue;
import org.eclipse.syson.sysml.LiteralInteger;
import org.eclipse.syson.sysml.LiteralString;
import org.eclipse.syson.sysml.MetadataDefinition;
import org.eclipse.syson.sysml.MetadataUsage;
import org.eclipse.syson.sysml.OwningMembership;
import org.eclipse.syson.sysml.SysmlFactory;

import java.util.Optional;
import java.util.stream.Stream;

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

            getDefinitionAttributes(hazardDef)
                    .forEach(defAttr -> addAttributeUsage(metadataUsage, defAttr));
        });

        var owningMembership = SysmlFactory.eINSTANCE.createOwningMembership();
        owningMembership.getOwnedRelatedElement().add(metadataUsage);
        return owningMembership;
    }

    private Stream<AttributeUsage> getDefinitionAttributes(MetadataDefinition hazardDef) {
        return hazardDef.getOwnedRelationship().stream()
                .filter(FeatureMembership.class::isInstance)
                .map(FeatureMembership.class::cast)
                .flatMap(fm -> fm.getOwnedRelatedElement().stream())
                .filter(AttributeUsage.class::isInstance)
                .map(AttributeUsage.class::cast);
    }

    private void addAttributeUsage(MetadataUsage parent, AttributeUsage defAttr) {
        AttributeUsage attr = SysmlFactory.eINSTANCE.createAttributeUsage();
        attr.setDeclaredName(defAttr.getDeclaredName());
        attr.setIsComposite(true);

        // Copy FeatureValue from library definition if present (e.g. start = 0)
        defAttr.getOwnedRelationship().stream()
                .filter(FeatureValue.class::isInstance)
                .map(FeatureValue.class::cast)
                .findFirst()
                .ifPresent(defFv -> {
                    var fv = SysmlFactory.eINSTANCE.createFeatureValue();
                    defFv.getOwnedRelatedElement().stream().findFirst().ifPresent(expr -> {
                        if (expr instanceof LiteralInteger li) {
                            var copy = SysmlFactory.eINSTANCE.createLiteralInteger();
                            copy.setValue(li.getValue());
                            fv.getOwnedRelatedElement().add(copy);
                        } else if (expr instanceof LiteralString ls) {
                            var copy = SysmlFactory.eINSTANCE.createLiteralString();
                            copy.setValue(ls.getValue());
                            fv.getOwnedRelatedElement().add(copy);
                        }
                    });
                    attr.getOwnedRelationship().add(fv);
                });

        FeatureMembership fm = SysmlFactory.eINSTANCE.createFeatureMembership();
        fm.getOwnedRelatedElement().add(attr);
        parent.getOwnedRelationship().add(fm);
    }
}
