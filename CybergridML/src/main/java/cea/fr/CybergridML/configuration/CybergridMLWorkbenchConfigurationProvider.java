package cea.fr.CybergridML.configuration;

import org.eclipse.sirius.components.collaborative.workbenchconfiguration.api.IWorkbenchConfigurationProviderDelegate;
import org.eclipse.sirius.components.collaborative.workbenchconfiguration.dto.DefaultViewConfiguration;
import org.eclipse.sirius.components.collaborative.workbenchconfiguration.dto.WorkbenchConfiguration;
import org.eclipse.sirius.components.collaborative.workbenchconfiguration.dto.WorkbenchMainPanelConfiguration;
import org.eclipse.sirius.components.collaborative.workbenchconfiguration.dto.WorkbenchSidePanelConfiguration;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CybergridMLWorkbenchConfigurationProvider implements IWorkbenchConfigurationProviderDelegate {

    @Override
    public boolean canHandle(String editingContextId) {
        return true;
    }

    @Override
    public WorkbenchConfiguration getWorkbenchConfiguration(String editingContextId) {
        return new WorkbenchConfiguration(
                new WorkbenchMainPanelConfiguration("main", List.of()),
                List.of(
                        new WorkbenchSidePanelConfiguration("left", true, List.of(
                                new DefaultViewConfiguration("explorer", true),
                                new DefaultViewConfiguration("views-explorer", false),
                                new DefaultViewConfiguration("validation", false),
                                new DefaultViewConfiguration("search", false)
                        )),
                        new WorkbenchSidePanelConfiguration("right", true, List.of(
                                new DefaultViewConfiguration("details", true),
                                new DefaultViewConfiguration("query", false),
                                new DefaultViewConfiguration("related-views", false),
                                new DefaultViewConfiguration("related-elements", false),
                                new DefaultViewConfiguration("cybergridml-chatbot", false)
                        ))
                )
        );
    }
}
