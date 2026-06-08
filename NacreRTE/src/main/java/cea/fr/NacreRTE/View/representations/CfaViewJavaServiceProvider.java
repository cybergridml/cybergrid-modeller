package cea.fr.NacreRTE.view.representations;


import cea.fr.NacreRTE.view.services.NacreDiagramAQLService;
import cea.fr.NacreRTE.view.services.NacreViewCreateService;
import cea.fr.NacreRTE.view.services.NacreViewNodeService;
import cea.fr.NacreRTE.view.services.NacreViewToolService;
import org.eclipse.emf.common.util.EList;
import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.eclipse.syson.diagram.common.view.services.ViewEdgeService;
import org.eclipse.syson.diagram.common.view.services.ViewLabelService;
import org.eclipse.syson.diagram.services.aql.DiagramMutationAQLService;
import org.eclipse.syson.model.services.aql.ModelMutationAQLService;
import org.eclipse.syson.model.services.aql.ModelQueryAQLService;
import org.eclipse.syson.representation.services.aql.RepresentationMutationAQLService;
import org.eclipse.syson.representation.services.aql.RepresentationQueryAQLService;
import org.eclipse.syson.services.DeleteService;
import org.eclipse.syson.services.UtilService;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;


@Configuration
public class CfaViewJavaServiceProvider implements IJavaServiceProvider {

    @Override
    public List<Class<?>> getServiceClasses(View view) {
        EList<RepresentationDescription> descriptions = view.getDescriptions();
        Optional<RepresentationDescription> optDescription = descriptions.stream()
                .filter(desc -> CfaRepresentationDescriptionProvider.DESCRIPTION_NAME.equals(desc.getName())).findFirst();
        if (optDescription.isPresent()) {
            return List.of(
                    UtilService.class,
                    DeleteService.class,
                    ViewLabelService.class,
                    ViewEdgeService.class,
                    DiagramMutationAQLService.class,
                    ModelMutationAQLService.class,
                    ModelQueryAQLService.class,
                    RepresentationMutationAQLService.class,
                    RepresentationQueryAQLService.class,
                    NacreViewToolService.class,
                    NacreViewCreateService.class,
                    NacreViewNodeService.class,
                    NacreDiagramAQLService.class
                   );
        }
        return List.of();
    }
}