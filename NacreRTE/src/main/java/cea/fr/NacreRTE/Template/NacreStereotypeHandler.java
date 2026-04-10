package cea.fr.NacreRTE.Template;
import org.eclipse.sirius.web.application.document.dto.DocumentDTO;
import org.eclipse.sirius.web.application.document.services.api.IStereotypeHandler;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.syson.application.sysmlv2.api.IDefaultSysMLv2ResourceProvider;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class NacreStereotypeHandler implements IStereotypeHandler {

    private static final String ACV_STEREOTYPE_ID = "nare_template";
    private static final String DOCUMENT_TYPE = "nacre Document";

    private final IDefaultSysMLv2ResourceProvider resourceProvider;

    public NacreStereotypeHandler(IDefaultSysMLv2ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, String stereotypeId) {
        return ACV_STEREOTYPE_ID.equals(stereotypeId);
    }

    @Override
    public Optional<DocumentDTO> handle(IEditingContext editingContext, String stereotypeId, String name) {
        if (!(editingContext instanceof IEMFEditingContext emfEditingContext)) {
            return Optional.empty();
        }

        return createAcvTemplateDocument(emfEditingContext, name);
    }

    private Optional<DocumentDTO> createAcvTemplateDocument(IEMFEditingContext editingContext, String name) {
        UUID documentId = UUID.randomUUID();
        var resource = resourceProvider.getDefaultSysMLv2Resource(documentId, name);

        editingContext.getDomain()
                .getResourceSet()
                .getResources()
                .add(resource);

        return Optional.of(new DocumentDTO(documentId, name, DOCUMENT_TYPE));
    }
}
