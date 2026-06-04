package cea.fr.NacreRTE.project;

import org.eclipse.sirius.web.application.project.services.api.IProjectTemplateProvider;
import org.eclipse.sirius.web.application.project.services.api.ProjectTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class NacreProjectTemplatesProvider implements IProjectTemplateProvider {

    public static final String NACRE_TEMPLATE_ID = "nacre-template";

    @Override
    public List<ProjectTemplate> getProjectTemplates() {
        var nacreTemplate = new ProjectTemplate(NACRE_TEMPLATE_ID, "CYBERGRIDML", "/images/rte.png", List.of());

        return List.of(nacreTemplate);
    }
}

