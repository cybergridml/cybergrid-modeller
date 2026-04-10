package cea.fr.NacreRTE.Template;
import org.eclipse.sirius.web.application.document.dto.Stereotype;
import org.eclipse.sirius.web.application.document.services.api.IStereotypeProvider;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NacreStereotypeProvider implements IStereotypeProvider {

    public static final String NACRE_TEMPLATE_ID = "nacre_template";

    @Override
    public List<Stereotype> getStereotypes(IEditingContext editingContext) {
        return List.of(
                new Stereotype(NACRE_TEMPLATE_ID, "Nacre Template")
        );
    }
}