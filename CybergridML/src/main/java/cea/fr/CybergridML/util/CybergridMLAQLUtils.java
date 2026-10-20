/*******************************************************************************
 * Copyright (c) 2026 CEA LIST - RTE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     CEA LIST - Initial API and implementation
 *******************************************************************************/

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
