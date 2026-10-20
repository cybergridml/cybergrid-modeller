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

/** Subset of an XIIDM network that CybergridML can map. */
public record XiidmNetwork(String id, List<Substation> substations, List<Branch> branches) {

    /** Maps to a Substation part. */
    public record Substation(String id, List<VoltageLevel> voltageLevels) {
    }

    /** No direct CybergridML counterpart; buses attach to owning substation with nominalV. */
    public record VoltageLevel(String id, Double nominalV, List<Bus> buses, List<Equipment> equipments) {
    }

    /** Maps to a Bus part under the substation. */
    public record Bus(String id, String name, Double v, Double angle) {
    }

    /** Generator, load, or shunt compensator attached to a bus. */
    public record Equipment(EquipmentKind kind, String id, String busId, Double p, Double q) {
    }

    /** Line or transformer; maps to a Terminal part on each connected bus. */
    public record Branch(BranchKind kind, String id, String bus1, String bus2) {
    }

    public enum EquipmentKind {
        GENERATOR, LOAD, SHUNT_COMPENSATOR
    }

    public enum BranchKind {
        LINE, TWO_WINDINGS_TRANSFORMER
    }
}
