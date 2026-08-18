package cea.fr.CybergridML.services.xiidm;

import java.util.List;

import org.eclipse.syson.sysml.Element;

/**
 * Outcome of an XIIDM import.
 *
 * @param createdRootElements the elements created directly under the target grid (the substations). They are the
 *        ones the diagram tool exposes, so that they show up in the CFA diagram.
 * @param substations         number of Substation parts created
 * @param buses               number of Bus parts created
 * @param generators          number of Generator parts created
 * @param loads               number of Load parts created
 * @param terminals           number of Terminal parts created
 * @param warnings            human readable messages about what could not be mapped
 */
public record XiidmImportResult(List<Element> createdRootElements, int substations, int buses, int generators, int loads,
        int terminals, List<String> warnings) {

    public String summary() {
        return "Imported %d substation(s), %d bus(es), %d generator(s), %d load(s) and %d terminal(s)."
                .formatted(this.substations, this.buses, this.generators, this.loads, this.terminals);
    }
}
