//package cea.fr.NacreRTE.View;
//
//import cea.fr.NacreRTE.Template.TemplateViewCreateService;
//import org.eclipse.emf.common.util.EList;
//import org.eclipse.sirius.components.view.RepresentationDescription;
//import org.eclipse.sirius.components.view.View;
//import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
//import org.eclipse.syson.services.UtilService;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.List;
//import java.util.Optional;
//
//@Configuration
//public class NacreCFRAViewJavaServiceProvider implements IJavaServiceProvider {
//
//    @Override
//    public List<Class<?>> getServiceClasses(View view) {
//        EList<RepresentationDescription> descriptions = view.getDescriptions();
//        Optional<RepresentationDescription> optDescription = descriptions.stream()
//                .filter(desc -> NacreCFRAViewDescriptionProvider.DESCRIPTION_NAME.equals(desc.getName())).findFirst();
//        if (optDescription.isPresent()) {
//            return List.of(UtilService.class,
//                    TemplateViewCreateService.class);
//        }
//        return List.of();
//    }
//}