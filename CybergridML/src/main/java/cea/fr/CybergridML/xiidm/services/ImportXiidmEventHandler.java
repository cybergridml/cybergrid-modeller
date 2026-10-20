/*******************************************************************************
 * Copyright (c) 2026 CEA LIST - RTE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     CEA LIST - Initial API and implementation
 *******************************************************************************/

package cea.fr.CybergridML.xiidm.services;

import cea.fr.CybergridML.view.services.CybergridMLViewCreateService;
import cea.fr.CybergridML.xiidm.dto.ImportXiidmInput;
import cea.fr.CybergridML.xiidm.dto.ImportXiidmSuccessPayload;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.api.IEditingContextEventHandler;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.syson.sysml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
public class ImportXiidmEventHandler implements IEditingContextEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ImportXiidmEventHandler.class);

    private final IObjectSearchService objectSearchService;
    private final CybergridMLViewCreateService createService;

    public ImportXiidmEventHandler(
            IObjectSearchService objectSearchService,
            CybergridMLViewCreateService createService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.createService = Objects.requireNonNull(createService);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, IInput input) {
        return input instanceof ImportXiidmInput;
    }

    @Override
    public void handle(Sinks.One<IPayload> payloadSink, Sinks.Many<ChangeDescription> changeDescriptionSink,
                       IEditingContext editingContext, IInput input) {

        if (!(input instanceof ImportXiidmInput importInput)) {
            return;
        }

        logger.info("Processing XIIDM import: targetElementId={}, fileName={}",
                importInput.targetElementId(), importInput.file().getName());

        IPayload payload;
        ChangeDescription changeDescription = new ChangeDescription(ChangeKind.NOTHING, editingContext.getId(), input);

        try {
            var optionalTargetElement = this.objectSearchService.getObject(editingContext, importInput.targetElementId());
            if (optionalTargetElement.isEmpty()) {
                logger.warn("Target element not found: {}", importInput.targetElementId());
                payload = new ErrorPayload(input.id(), "Target element not found: " + importInput.targetElementId());
                payloadSink.tryEmitValue(payload);
                changeDescriptionSink.tryEmitNext(changeDescription);
                return;
            }

            Object targetObject = optionalTargetElement.get();
            if (!(targetObject instanceof Element targetElement)) {
                logger.warn("Target is not a SysML Element: {}", targetObject.getClass().getName());
                payload = new ErrorPayload(input.id(), "Target element is not a valid SysML Element");
                payloadSink.tryEmitValue(payload);
                changeDescriptionSink.tryEmitNext(changeDescription);
                return;
            }

            String xiidmContent = new String(
                    importInput.file().getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            String fileName = importInput.file().getName();
            var createdElements = this.createService.importXiidm(
                    targetElement, xiidmContent, fileName, editingContext);

            logger.info("XIIDM import successful: {} elements created", createdElements.size());

            payload = new ImportXiidmSuccessPayload(input.id(), createdElements.size());
            changeDescription = new ChangeDescription(ChangeKind.SEMANTIC_CHANGE, editingContext.getId(), input);

        } catch (IOException e) {
            logger.error("Failed to read XIIDM file", e);
            payload = new ErrorPayload(input.id(), "Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("XIIDM import failed", e);
            payload = new ErrorPayload(input.id(), "Import failed: " + e.getMessage());
        }

        payloadSink.tryEmitValue(payload);
        changeDescriptionSink.tryEmitNext(changeDescription);
    }
}
