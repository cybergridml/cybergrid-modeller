package cea.fr.CybergridML.view.services;

import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import org.eclipse.emf.common.util.EList;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.view.ChangeContext;
import org.eclipse.sirius.components.view.CreateInstance;
import org.eclipse.sirius.components.view.Operation;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.diagram.common.view.services.description.ToolDescriptionService;
import org.eclipse.syson.util.AQLUtils;
import org.eclipse.syson.util.IDescriptionNameGenerator;
import org.eclipse.syson.util.SysMLMetamodelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class CybergridMLToolDescriptionService extends ToolDescriptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CybergridMLToolDescriptionService.class);

    public CybergridMLToolDescriptionService(IDescriptionNameGenerator descriptionNameGenerator) {
        super(descriptionNameGenerator);
    }

    public NodeTool createNodeToolFromDiagramBackground(NodeDescription nodeDescription, CybergridMLLibElementType cybergridMLElementType) {
        NodeTool nodeTool = super.createNodeToolFromDiagramBackground(nodeDescription, cybergridMLElementType.geteClass());
        String nodeToolName = cybergridMLElementType.getName();
        modifyLcaNodeTool(cybergridMLElementType, nodeToolName, nodeTool);
        return nodeTool;
    }

    private void modifyLcaNodeTool(CybergridMLLibElementType cybergridMLElementType, String nodeToolName, NodeTool nodeTool) {
        nodeTool.setName(nodeToolName);
        changeElementCreation(nodeTool.getBody(), cybergridMLElementType);
        LOGGER.debug("Created node tool {}", nodeTool.getName());
    }

    private void changeElementCreation(final EList<Operation> operationList, CybergridMLLibElementType cybergridMLElementType) {
        for (Operation operation : operationList) {
            if (operation instanceof CreateInstance createInstance && createInstance.getTypeName().equals(SysMLMetamodelHelper.buildQualifiedName(cybergridMLElementType.geteClass()))) {
                createInstance.getChildren().stream()
                        .filter(ChangeContext.class::isInstance)
                        .findFirst()
                        .map(ChangeContext.class::cast)
                        .ifPresent(newInstanceChangeContext -> {
                            List<String> parameters = List.of(IEditingContext.EDITING_CONTEXT, AQLUtils.aqlString(cybergridMLElementType.getName()));
                            newInstanceChangeContext.setExpression(AQLUtils.getServiceCallExpression(NEW_INSTANCE, SERVICE_ELEMENT_INITIALIZER, parameters));
                        });
            }
            changeElementCreation(operation.getChildren(), cybergridMLElementType);
        }
    }
}
