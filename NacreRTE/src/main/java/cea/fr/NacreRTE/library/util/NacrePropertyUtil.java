package cea.fr.NacreRTE.library.util;


import org.eclipse.syson.sysml.*;
import org.eclipse.syson.sysml.textual.SysMLElementSerializer;
import org.eclipse.syson.sysml.textual.utils.FileNameDeresolver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Utils to get the value of an attribute.
 */
public class NacrePropertyUtil {

    private NacrePropertyUtil() {
    }

    public static String getStringAttributeValue(AttributeUsage usage) {
        Expression expression = getExpression(usage);

        if (expression == null) {
            return null;
        }

        if (expression.eClass().equals(SysmlPackage.eINSTANCE.getLiteralString())) {
            return ((LiteralString) expression).getValue();
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

    /**
     * Get the nested properties as well as the inherited properties of a usage.
     */
    public static List<AttributeUsage> getUsageProperties(Usage usage) {
        List<AttributeUsage> properties = new ArrayList<>();
        List<AttributeUsage> nestedProperties = usage.getNestedAttribute()
                .stream()
                .toList();
        List<AttributeUsage> inheritedProperties = usage.getInheritedFeature()
                .stream()
                .filter(AttributeUsage.class::isInstance)
                .map(AttributeUsage.class::cast)
                .toList();
        properties.addAll(nestedProperties);
        properties.addAll(inheritedProperties);
        return properties;
    }
}
