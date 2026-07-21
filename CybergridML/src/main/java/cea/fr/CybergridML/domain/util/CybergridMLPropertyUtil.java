package cea.fr.CybergridML.domain.util;


import org.eclipse.syson.sysml.*;
import org.eclipse.syson.sysml.textual.SysMLElementSerializer;
import org.eclipse.syson.sysml.textual.utils.FileNameDeresolver;

import java.math.BigDecimal;
import java.util.Optional;

public class CybergridMLPropertyUtil {

    private static final String NFP_LATENCY_QUALIFIED_NAME = "CyberGridMLDatatypes::NFP_Latency";
    private static final String NFP_CAPACITY_QUALIFIED_NAME = "CyberGridMLDatatypes::NFP_Capacity";

    private CybergridMLPropertyUtil() {
    }

    public static void addAttributesFromDefinition(Element parent, Namespace definition) {
        definition.getOwnedRelationship().stream()
                .filter(FeatureMembership.class::isInstance)
                .map(FeatureMembership.class::cast)
                .flatMap(fm -> fm.getOwnedRelatedElement().stream())
                .forEach(feature -> {
                    if (feature instanceof AttributeUsage defAttr) {
                        addAttributeFromDefinition(parent, defAttr);
                    } else if (feature instanceof ReferenceUsage defRef) {
                        addReferenceFromDefinition(parent, defRef);
                    }
                });
    }

    private static void addAttributeFromDefinition(Element parent, AttributeUsage defAttr) {
        AttributeUsage attr = SysmlFactory.eINSTANCE.createAttributeUsage();
        attr.setDeclaredName(defAttr.getDeclaredName());
        attr.setIsComposite(defAttr.isIsComposite());

        defAttr.getOwnedRelationship().stream()
                .filter(FeatureTyping.class::isInstance)
                .map(FeatureTyping.class::cast)
                .map(FeatureTyping::getType)
                .filter(EnumerationDefinition.class::isInstance)
                .findFirst()
                .ifPresent(enumDefinition -> {
                    FeatureTyping featureTyping = SysmlFactory.eINSTANCE.createFeatureTyping();
                    attr.getOwnedRelationship().add(featureTyping);
                    featureTyping.setType(enumDefinition);
                    featureTyping.setTypedFeature(attr);
                });

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
                        } else if (expr instanceof FeatureReferenceExpression fre) {
                            String enumLiteralName = resolveEnumDefaultLiteralName(fre, defAttr);
                            if (enumLiteralName != null) {
                                var copy = SysmlFactory.eINSTANCE.createLiteralString();
                                copy.setValue(enumLiteralName);
                                fv.getOwnedRelatedElement().add(copy);
                            }
                        }
                    });
                    if (!fv.getOwnedRelatedElement().isEmpty()) {
                        attr.getOwnedRelationship().add(fv);
                    }
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

    private static void addReferenceFromDefinition(Element parent, ReferenceUsage defRef) {
        ReferenceUsage ref = SysmlFactory.eINSTANCE.createReferenceUsage();
        ref.setDeclaredName(defRef.getDeclaredName());
        ref.setIsComposite(defRef.isIsComposite());

        defRef.getOwnedRelationship().stream()
                .filter(FeatureTyping.class::isInstance)
                .map(FeatureTyping.class::cast)
                .map(FeatureTyping::getType)
                .findFirst()
                .ifPresent(type -> {
                    FeatureTyping featureTyping = SysmlFactory.eINSTANCE.createFeatureTyping();
                    ref.getOwnedRelationship().add(featureTyping);
                    featureTyping.setType(type);
                    featureTyping.setTypedFeature(ref);
                });

        copyMultiplicity(ref, defRef);

        FeatureMembership fm = SysmlFactory.eINSTANCE.createFeatureMembership();
        fm.getOwnedRelatedElement().add(ref);
        parent.getOwnedRelationship().add(fm);
    }

    private static void copyMultiplicity(Feature target, Feature source) {
        if (!(source.getMultiplicity() instanceof MultiplicityRange sourceRange)) {
            return;
        }
        OwningMembership membership = SysmlFactory.eINSTANCE.createOwningMembership();
        target.getOwnedRelationship().add(membership);
        MultiplicityRange targetRange = SysmlFactory.eINSTANCE.createMultiplicityRange();
        membership.getOwnedRelatedElement().add(targetRange);

        sourceRange.getBound().forEach(bound -> {
            Expression copy = null;
            if (bound instanceof LiteralInteger li) {
                var c = SysmlFactory.eINSTANCE.createLiteralInteger();
                c.setValue(li.getValue());
                copy = c;
            } else if (bound instanceof LiteralInfinity) {
                copy = SysmlFactory.eINSTANCE.createLiteralInfinity();
            }
            if (copy != null) {
                OwningMembership boundMembership = SysmlFactory.eINSTANCE.createOwningMembership();
                targetRange.getOwnedRelationship().add(boundMembership);
                boundMembership.getOwnedRelatedElement().add(copy);
            }
        });
    }

    private static String resolveEnumDefaultLiteralName(FeatureReferenceExpression defaultExpression, AttributeUsage defAttr) {
        Feature referent = defaultExpression.getReferent();
        if (referent != null) {
            return referent.getDeclaredName();
        }
        // CybergridMLLibrary.json currently ships with an unresolved reference for the "unit" defaults of
        // NFP_Latency/NFP_Capacity (the FeatureReferenceExpression's Membership has no memberElement), so the
        // enum literal declared in CybergridML.sysml ("default := Milliseconds"/"default := Kilobytes") can't
        // be read from the model. Fall back to the values declared in the library until the compiled JSON is
        // regenerated with a resolvable reference.
        if (!"unit".equals(defAttr.getDeclaredName())) {
            return null;
        }
        String owningTypeQualifiedName = Optional.ofNullable(defAttr.getOwningType())
                .map(Type::getQualifiedName)
                .orElse(null);
        if (NFP_LATENCY_QUALIFIED_NAME.equals(owningTypeQualifiedName)) {
            return "Milliseconds";
        }
        if (NFP_CAPACITY_QUALIFIED_NAME.equals(owningTypeQualifiedName)) {
            return "Kilobytes";
        }
        return null;
    }

    public static Object getAttributeValue(AttributeUsage usage) {
        Expression expression = getExpression(usage);

        if (expression == null) {
            return null;
        }
        if (expression.eClass().equals(SysmlPackage.eINSTANCE.getLiteralRational())) {
            return BigDecimal.valueOf(((LiteralRational) expression).getValue());
        } else if (expression.eClass().equals(SysmlPackage.eINSTANCE.getLiteralInteger())) {
            return BigDecimal.valueOf(((LiteralInteger) expression).getValue());
        } else if (expression.eClass().equals(SysmlPackage.eINSTANCE.getLiteralString())) {
            return ((LiteralString) expression).getValue();
        } else if (expression.eClass().equals(SysmlPackage.eINSTANCE.getOperatorExpression())
                || expression.eClass().equals(SysmlPackage.eINSTANCE.getFeatureChainExpression())
                || expression.eClass().equals(SysmlPackage.eINSTANCE.getFeatureReferenceExpression())) {
            String operationAsString = new SysMLElementSerializer(
                    "\n",
                    "\t",
                    new FileNameDeresolver(),
                    s -> {
                    })
                    .doSwitch(expression);
            return "=" + operationAsString;
        }
        return null;
    }
    public static Expression getExpression(AttributeUsage usage) {
        FeatureValue featureValue = getValuation(usage);
        if (featureValue == null || featureValue.getValue() == null) {
            return null;
        }
        return featureValue.getValue();
    }
    private static FeatureValue getValuation(Feature feature) {
        return feature.getOwnedMembership().stream()
                .filter(FeatureValue.class::isInstance)
                .map(FeatureValue.class::cast)
                .findFirst()
                .orElse(null);
    }
}

