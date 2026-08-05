package cea.fr.CybergridML.util;

import org.eclipse.syson.util.AQLConstants;

import java.util.List;


public final class CybergridMLAQLUtils {

    private CybergridMLAQLUtils() {
    }

    public static String getSelfServiceCallExpression(String serviceName) {
        return getSelfServiceCallExpression(serviceName, List.of());
    }

    public static String getSelfServiceCallExpression(String serviceName, String parameter) {
        return getSelfServiceCallExpression(serviceName, List.of(parameter));
    }

    public static String getSelfServiceCallExpression(String serviceName, List<String> parameters) {
        return AQLConstants.AQL_SELF + '.' + serviceName + '(' + String.join(",", parameters) + ')';
    }

    public static String getServiceCallExpression(String var, String serviceName) {
        return getServiceCallExpression(var, serviceName, List.of());
    }

    public static String getServiceCallExpression(String var, String serviceName, String parameter) {
        return getServiceCallExpression(var, serviceName, List.of(parameter));
    }

    public static String getServiceCallExpression(String var, String serviceName, List<String> parameters) {
        return AQLConstants.AQL + var + '.' + serviceName + '(' + String.join(",", parameters) + ')';
    }
}
