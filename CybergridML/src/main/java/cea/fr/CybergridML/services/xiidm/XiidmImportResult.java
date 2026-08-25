package cea.fr.CybergridML.services.xiidm;

import java.util.List;

import org.eclipse.syson.sysml.Element;

/** Result of an XIIDM import with counts and warnings. */
public record XiidmImportResult(List<Element> createdRootElements, int substations, int buses, int generators, int loads,
        int terminals, int localControllers, List<String> warnings) {

    public String summary() {
        return "Imported %d substation(s), %d bus(es), %d generator(s), %d load(s), %d terminal(s) and %d local controller(s)."
                .formatted(this.substations, this.buses, this.generators, this.loads, this.terminals, this.localControllers);
    }
}
