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
