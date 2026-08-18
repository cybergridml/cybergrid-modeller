package cea.fr.CybergridML.services.xiidm;

import java.util.List;

/**
 * In-memory representation of the subset of an XIIDM (PowSyBl IIDM) network that CybergridML maps.
 *
 * <p>Only the concepts that have a counterpart in the CFA package of the CybergridML library are kept:
 * substations, voltage levels, buses, generators, loads, lines and two windings transformers. Everything
 * else in the file (switches, dangling lines, extensions, ...) is ignored by {@link XiidmParser}.
 */
public record XiidmNetwork(String id, List<Substation> substations, List<Branch> branches) {

    /**
     * An {@code iidm:substation}. Becomes a Substation part under the imported grid.
     */
    public record Substation(String id, List<VoltageLevel> voltageLevels) {
    }

    /**
     * An {@code iidm:voltageLevel}. It has no counterpart in CybergridML: its buses are attached directly to the
     * owning substation, and its {@code nominalV} is carried over to each of those buses.
     */
    public record VoltageLevel(String id, Double nominalV, List<Bus> buses, List<Equipment> equipments) {
    }

    /**
     * An {@code iidm:bus}. Becomes a Bus part under the substation.
     */
    public record Bus(String id, String name, Double v, Double angle) {
    }

    /**
     * A generator, a load or a shunt compensator attached to a bus of the owning voltage level.
     */
    public record Equipment(EquipmentKind kind, String id, String busId, Double p, Double q) {
    }

    /**
     * A line or a two windings transformer. Both are mapped the same way: a pair of Terminal parts, one under
     * each of the two buses they connect.
     */
    public record Branch(BranchKind kind, String id, String bus1, String bus2) {
    }

    public enum EquipmentKind {
        GENERATOR, LOAD, SHUNT_COMPENSATOR
    }

    public enum BranchKind {
        LINE, TWO_WINDINGS_TRANSFORMER
    }
}
