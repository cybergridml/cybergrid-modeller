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

package cea.fr.CybergridML.services.xiidm;

import java.util.List;

import org.eclipse.syson.sysml.Element;

/**
 * Outcome comments of an XIIDM import.
 */
public record XiidmImportResult(
        List<Element> createdRootElements,
        int substations,
        int buses,
        int generators,
        int loads,
        int terminals,
        int localControllers,
        int computingResources,
        int softwareResources,
        List<String> warnings
) {

    public String summary() {
        return "Imported %d substation(s), %d bus(es), %d generator(s), %d load(s), %d terminal(s), %d local controller(s), %d computing resource(s), %d software resource(s)."
                .formatted(this.substations, this.buses, this.generators, this.loads, this.terminals,
                        this.localControllers, this.computingResources, this.softwareResources);
    }
}
