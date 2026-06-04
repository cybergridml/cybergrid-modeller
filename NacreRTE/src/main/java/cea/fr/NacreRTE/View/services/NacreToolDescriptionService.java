package cea.fr.NacreRTE.view.services;

import cea.fr.NacreRTE.domain.NacreLibElementType;
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


public class NacreToolDescriptionService extends ToolDescriptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacreToolDescriptionService.class);

    public NacreToolDescriptionService(IDescriptionNameGenerator descriptionNameGenerator) {
        super(descriptionNameGenerator);
    }

    public NodeTool createNodeToolFromDiagramBackground(NodeDescription nodeDescription, NacreLibElementType nacreElementType) {
        NodeTool nodeTool = super.createNodeToolFromDiagramBackground(nodeDescription, nacreElementType.geteClass());
        String nodeToolName = nacreElementType.getName();
        modifyLcaNodeTool(nacreElementType, nodeToolName, nodeTool);
        return nodeTool;
    }

    private void modifyLcaNodeTool(NacreLibElementType nacreElementType, String nodeToolName, NodeTool nodeTool) {
        nodeTool.setName(nodeToolName);
        changeElementCreation(nodeTool.getBody(), nacreElementType);
        LOGGER.debug("Created node tool {}", nodeTool.getName());
    }

    private void changeElementCreation(final EList<Operation> operationList, NacreLibElementType nacreElementType) {
        for (Operation operation : operationList) {
            if (operation instanceof CreateInstance createInstance && createInstance.getTypeName().equals(SysMLMetamodelHelper.buildQualifiedName(nacreElementType.geteClass()))) {
                createInstance.getChildren().stream()
                        .filter(ChangeContext.class::isInstance)
                        .findFirst()
                        .map(ChangeContext.class::cast)
                        .ifPresent(newInstanceChangeContext -> {
                            List<String> parameters = List.of(IEditingContext.EDITING_CONTEXT, AQLUtils.aqlString(nacreElementType.getName()));
                            newInstanceChangeContext.setExpression(AQLUtils.getServiceCallExpression(NEW_INSTANCE, SERVICE_ELEMENT_INITIALIZER, parameters));
                        });
            }
            changeElementCreation(operation.getChildren(), nacreElementType);
        }
    }
}
