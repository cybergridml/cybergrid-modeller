package cea.fr.CybergridML.util;

import org.eclipse.syson.util.AQLConstants;

import java.util.List;

/**
 * {@code org.eclipse.syson.util.AQLUtils#getSelfServiceCallExpression}/{@code #getServiceCallExpression} were
 * removed in Syson 2026.7.0 in favor of the type-safe {@code ServiceMethod} builder, which requires a resolvable
 * Java method reference for every call site. Some CybergridML expressions (e.g. {@code createAllocateEdge}) don't
 * have a locatable declaration in either CybergridML or Syson, so the exact string-building behavior of the
 * removed methods is reproduced here rather than guessing a {@code ServiceMethod} reference.
 */
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
