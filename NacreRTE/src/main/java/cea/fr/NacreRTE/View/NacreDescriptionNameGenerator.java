package cea.fr.NacreRTE.view;

import org.eclipse.syson.util.DescriptionNameGenerator;

public class NacreDescriptionNameGenerator extends DescriptionNameGenerator {

    private static final String DIAGRAM_PREFIX = "NACRE";

    public NacreDescriptionNameGenerator() {
        super(DIAGRAM_PREFIX);
    }

    public NacreDescriptionNameGenerator(String diagramPrefix) {
        super(diagramPrefix);
    }
}
