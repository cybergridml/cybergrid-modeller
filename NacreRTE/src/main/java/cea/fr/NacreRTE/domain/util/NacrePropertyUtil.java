package cea.fr.NacreRTE.domain.util;


import org.eclipse.syson.sysml.*;

public class NacrePropertyUtil {

    private NacrePropertyUtil() {
    }

    public static void addAttributesFromDefinition(Element parent, Namespace definition) {
        definition.getOwnedRelationship().stream()
                .filter(FeatureMembership.class::isInstance)
                .map(FeatureMembership.class::cast)
                .flatMap(fm -> fm.getOwnedRelatedElement().stream())
                .filter(AttributeUsage.class::isInstance)
                .map(AttributeUsage.class::cast)
                .forEach(defAttr -> addAttributeFromDefinition(parent, defAttr));
    }

    private static void addAttributeFromDefinition(Element parent, AttributeUsage defAttr) {
        AttributeUsage attr = SysmlFactory.eINSTANCE.createAttributeUsage();
        attr.setDeclaredName(defAttr.getDeclaredName());
        attr.setIsComposite(defAttr.isIsComposite());

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
                        } else if (expr instanceof LiteralRational lr) {
                            var copy = SysmlFactory.eINSTANCE.createLiteralRational();
                            copy.setValue(lr.getValue());
                            fv.getOwnedRelatedElement().add(copy);
                        } else if (expr instanceof LiteralString ls) {
                            var copy = SysmlFactory.eINSTANCE.createLiteralString();
                            copy.setValue(ls.getValue());
                            fv.getOwnedRelatedElement().add(copy);
                        }
                    });
                    attr.getOwnedRelationship().add(fv);
                });

        if (defAttr.isIsComposite()) {
            defAttr.getOwnedRelationship().stream()
                    .filter(FeatureTyping.class::isInstance)
                    .map(FeatureTyping.class::cast)
                    .map(FeatureTyping::getType)
                    .map(Namespace.class::cast)
                    .forEach(type -> addAttributesFromDefinition(attr, type));
        }

        FeatureMembership fm = SysmlFactory.eINSTANCE.createFeatureMembership();
        fm.getOwnedRelatedElement().add(attr);
        parent.getOwnedRelationship().add(fm);
    }
}

