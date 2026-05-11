package cea.fr.NacreRTE;

import org.eclipse.syson.sysml.AttributeUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.FeatureMembership;
import org.eclipse.syson.sysml.FeatureValue;
import org.eclipse.syson.sysml.LiteralBoolean;
import org.eclipse.syson.sysml.SysmlFactory;

public class NacreHazardService {

    private static final String HAZARD_FEATURE_NAME = "hazard";

    public boolean hasHazard(Element element) {
        return element.getOwnedRelationship().stream()
                .filter(FeatureMembership.class::isInstance)
                .map(FeatureMembership.class::cast)
                .flatMap(fm -> fm.getOwnedRelatedElement().stream())
                .filter(AttributeUsage.class::isInstance)
                .map(AttributeUsage.class::cast)
                .filter(attr -> HAZARD_FEATURE_NAME.equals(attr.getDeclaredName()))
                .anyMatch(attr -> attr.getOwnedMembership().stream()
                        .filter(FeatureValue.class::isInstance)
                        .map(FeatureValue.class::cast)
                        .findFirst()
                        .map(fv -> fv.getValue() instanceof LiteralBoolean lb && lb.isValue())
                        .orElse(false));
    }

    public Element toggleHazard(Element element) {
        var existingHazardAttr = element.getOwnedRelationship().stream()
                .filter(FeatureMembership.class::isInstance)
                .map(FeatureMembership.class::cast)
                .flatMap(fm -> fm.getOwnedRelatedElement().stream())
                .filter(AttributeUsage.class::isInstance)
                .map(AttributeUsage.class::cast)
                .filter(attr -> HAZARD_FEATURE_NAME.equals(attr.getDeclaredName()))
                .findFirst();

        if (existingHazardAttr.isPresent()) {
            existingHazardAttr.get().getOwnedMembership().stream()
                    .filter(FeatureValue.class::isInstance)
                    .map(FeatureValue.class::cast)
                    .findFirst()
                    .ifPresent(fv -> {
                        if (fv.getValue() instanceof LiteralBoolean lb) {
                            lb.setValue(!lb.isValue());
                        }
                    });
        } else {
            var lb = SysmlFactory.eINSTANCE.createLiteralBoolean();
            lb.setValue(true);
            var featureValue = SysmlFactory.eINSTANCE.createFeatureValue();
            featureValue.getOwnedRelatedElement().add(lb);
            var hazardAttr = SysmlFactory.eINSTANCE.createAttributeUsage();
            hazardAttr.setDeclaredName(HAZARD_FEATURE_NAME);
            hazardAttr.getOwnedRelationship().add(featureValue);
            var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
            featureMembership.getOwnedRelatedElement().add(hazardAttr);
            element.getOwnedRelationship().add(featureMembership);
        }
        return element;
    }
}
