package cea.fr.CybergridML.project;

import org.eclipse.sirius.web.application.project.services.api.IProjectTemplateProvider;
import org.eclipse.sirius.web.application.project.services.api.ProjectTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CybergridMLProjectTemplatesProvider implements IProjectTemplateProvider {

    public static final String CYBERGRIDML_TEMPLATE_ID = "CyberGridML-template";

    @Override
    public List<ProjectTemplate> getProjectTemplates() {
        var nacreTemplate = new ProjectTemplate(CYBERGRIDML_TEMPLATE_ID, "CyberGridML", "/images/Cybergrid.png", List.of());

        return List.of(nacreTemplate);
    }
}

