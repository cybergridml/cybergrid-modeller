package cea.fr.CybergridML.xiidm.dto;

import org.eclipse.sirius.components.core.api.IPayload;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ImportXiidmSuccessPayload(
        @NotNull UUID id,
        int createdElementCount
) implements IPayload {
}
