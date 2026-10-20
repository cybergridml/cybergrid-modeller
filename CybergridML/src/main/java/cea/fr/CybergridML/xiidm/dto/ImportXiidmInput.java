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
