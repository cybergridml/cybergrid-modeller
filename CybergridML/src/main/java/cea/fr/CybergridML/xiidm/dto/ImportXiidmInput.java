package cea.fr.CybergridML.xiidm.dto;

import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.graphql.api.UploadFile;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ImportXiidmInput(
        @NotNull UUID id,
        @NotNull String editingContextId,
        @NotNull String targetElementId,
        @NotNull UploadFile file
) implements IInput {
}
