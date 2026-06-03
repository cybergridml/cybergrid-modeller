package cea.fr.NacreRTE;

import cea.fr.NacreRTE.View.nodes.NacreDescriptionNameGenerator;
import cea.fr.NacreRTE.library.NacreLibElementType;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.diagram.common.view.services.AbstractViewNodeToolsWithoutSectionSwitch;
import org.eclipse.syson.sysml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NacreViewNodeToolsWithoutSectionSwitch extends AbstractViewNodeToolsWithoutSectionSwitch {
    private static final Logger LOGGER = LoggerFactory.getLogger(NacreViewNodeToolsWithoutSectionSwitch.class);

    private final NacreLibElementType nacreElementType;
    private final List<NodeDescription> allNodeDescriptions;
    private final IViewDiagramElementFinder cache;
    private final NacreDescriptionNameGenerator nacreNameGenerator;


    public NacreViewNodeToolsWithoutSectionSwitch(NacreLibElementType nacreElementType,
                                                  NacreDescriptionNameGenerator descriptionNameGenerator,
                                                  List<NodeDescription> allNodeDescriptions,
                                                  IViewDiagramElementFinder cache) {
        super(descriptionNameGenerator);
        this.nacreElementType = nacreElementType;
        this.allNodeDescriptions = allNodeDescriptions;
        this.cache = cache;
        this.nacreNameGenerator = descriptionNameGenerator;
    }

    @Override
    public List<NodeTool> doSwitch(EClass eClass) {
        LOGGER.info("Creating node tools for {}", nacreElementType);
        return super.doSwitch(eClass);
    }


    @Override
    public List<NodeTool> casePartUsage(PartUsage object) {
        List<NodeTool> nodeTools = new ArrayList<>();

        if (NacreLibElementType.MAINGRID.equals(nacreElementType)) {
            addContainerTools(nodeTools, SysmlPackage.eINSTANCE.getUsage_NestedAttribute());
        }
        return nodeTools;
    }




    @Override
    protected List<NodeDescription> getAllNodeDescriptions() {
        return this.allNodeDescriptions;
    }

    protected NodeDescription getNodeDescription(String nodeName) {
        return this.getAllNodeDescriptions().stream()
                .filter(nd -> Objects.equals(this.descriptionNameGenerator.getNodeName(nodeName), nd.getName())
                        || Objects.equals(this.descriptionNameGenerator.getBorderNodeName(nodeName), nd.getName()))
                .findFirst()
                .orElseGet(() -> {
                    LOGGER.warn("{} not found in allNodeDescriptions", nodeName);
                    return null;
                });
    }

    private void addContainerTools(List<NodeTool> nodeTools, EReference nestedEReference) {}}
