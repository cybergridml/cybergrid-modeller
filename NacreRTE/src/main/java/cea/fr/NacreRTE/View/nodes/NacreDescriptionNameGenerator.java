
package cea.fr.NacreRTE.View.nodes;

import cea.fr.NacreRTE.library.NacreLibElementType;
import org.eclipse.syson.util.DescriptionNameGenerator;

public class NacreDescriptionNameGenerator extends DescriptionNameGenerator {

    private static final String DIAGRAM_PREFIX = "NACRE";

    public NacreDescriptionNameGenerator() {
        super(DIAGRAM_PREFIX);
    }

    public NacreDescriptionNameGenerator(String diagramPrefix) {
        super(diagramPrefix);
    }

    public String getCreationToolName(NacreLibElementType nacreElementType) {
        return nacreElementType.getName();
    }

    public String getNacreNodeName(NacreLibElementType nacreElementType, String nestedReference) {
        return this.getNodeName(DIAGRAM_PREFIX, nacreElementType.getServiceSuffix() + SPACE + nestedReference);
    }

    public String getNacreBorderNodeName(NacreLibElementType nacreElementType, String nestedReference) {
        return this.getBorderNodeName(DIAGRAM_PREFIX, nacreElementType.getServiceSuffix() + SPACE + nestedReference);
    }

    public String getNacreCompartmentItemName(NacreLibElementType nacreElementType, String nestedReference) {
        return this.getCompartmentItemName(DIAGRAM_PREFIX, nacreElementType.getServiceSuffix() + SPACE + nestedReference);
    }

    public String getNacreCompartmentName(NacreLibElementType nacreElementType, String nestedReference) {
        return this.getCompartmentName(DIAGRAM_PREFIX, nacreElementType.getServiceSuffix() + SPACE + nestedReference);
    }

    public String getNacreInheritedCompartmentItemName(NacreLibElementType nacreElementType, String nestedReference) {
        return this.getInheritedCompartmentItemName(DIAGRAM_PREFIX, nacreElementType.getServiceSuffix() + SPACE + nestedReference);
    }

    public String getNacreInheritedBorderNodeName(NacreLibElementType nacreElementType, String nestedReference) {
        return this.getInheritedBorderNodeName(DIAGRAM_PREFIX, nacreElementType.getServiceSuffix() + SPACE + nestedReference);
    }
}
