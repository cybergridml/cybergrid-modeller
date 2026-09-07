package cea.fr.CybergridML.xiidm.controllers;

import cea.fr.CybergridML.xiidm.dto.ImportXiidmInput;
import cea.fr.CybergridML.xiidm.dto.ImportXiidmSuccessPayload;
import org.eclipse.sirius.components.collaborative.api.IEditingContextEventProcessorRegistry;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.graphql.api.UploadFile;
import org.eclipse.sirius.components.representations.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/xiidm")
public class XiidmUploadController {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final IEditingContextEventProcessorRegistry eventProcessorRegistry;

    public XiidmUploadController(IEditingContextEventProcessorRegistry eventProcessorRegistry) {
        this.eventProcessorRegistry = Objects.requireNonNull(eventProcessorRegistry);
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importXiidm(
            @RequestParam String editingContextId,
            @RequestParam String targetElementId,
            @RequestParam MultipartFile file) throws IOException {

        var uploadFile = new UploadFile(file.getOriginalFilename(), file.getInputStream());
        var input = new ImportXiidmInput(UUID.randomUUID(), editingContextId, targetElementId, uploadFile);
        var payload = this.eventProcessorRegistry.dispatchEvent(editingContextId, input).block(TIMEOUT);

        if (payload instanceof ImportXiidmSuccessPayload success) {
            return ResponseEntity.ok(Map.of("success", true, "createdElementCount", success.createdElementCount()));
        } else if (payload instanceof ErrorPayload error) {
            var msg = error.messages().stream().findFirst().map(Message::body).orElse("Unknown error");
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", msg));
        }
        return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "Unexpected response"));
    }
}
