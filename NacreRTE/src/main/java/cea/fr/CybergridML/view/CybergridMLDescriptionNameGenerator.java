package cea.fr.CybergridML.view;

import org.eclipse.syson.util.DescriptionNameGenerator;

public class CybergridMLDescriptionNameGenerator extends DescriptionNameGenerator {

    private static final String DIAGRAM_PREFIX = "NACRE";

    public CybergridMLDescriptionNameGenerator() {
        super(DIAGRAM_PREFIX);
    }

    public CybergridMLDescriptionNameGenerator(String diagramPrefix) {
        super(diagramPrefix);
    }
}
