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

import { DiagramDialogContribution } from "@eclipse-sirius/sirius-components-diagrams";
import { XiidmImportDialog } from "./XiidmImportDialog";

const CYBERGRIDML_VIEW_SOURCE_ID = "862e39fd-7d88-352b-8fe4-e59d52a39bce";

// Dialogs that use default Sirius selection tree
const EXCLUDED_DIALOG_IDS = [
  "c9e14348-6ce3-3360-b4ec-1b37e9df763b", // Connect in Bulk
];

function extractSourceElementId(dialogDescriptionId: string): string | null {
  const match = dialogDescriptionId.match(/sourceElementId=([a-f0-9-]+)/);
  return match?.[1] ?? null;
}

export const xiidmDialogContribution: DiagramDialogContribution = {
  canHandle: (dialogDescriptionId: string) => {
    if (
      !dialogDescriptionId.startsWith(
        "siriusComponents://selectionDialogDescription"
      ) ||
      !dialogDescriptionId.includes(`sourceId=${CYBERGRIDML_VIEW_SOURCE_ID}`)
    ) {
      return false;
    }

    const sourceElementId = extractSourceElementId(dialogDescriptionId);
    return (
      sourceElementId !== null && !EXCLUDED_DIALOG_IDS.includes(sourceElementId)
    );
  },
  component: XiidmImportDialog,
};
