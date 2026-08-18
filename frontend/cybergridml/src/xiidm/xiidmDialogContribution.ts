import { DiagramDialogContribution } from "@eclipse-sirius/sirius-components-diagrams";
import { XiidmImportDialog } from "./XiidmImportDialog";

/**
 * Identifier of the EMF resource holding the CybergridML views (CFA, CFRA and CRA all declare the same view id).
 *
 * Sirius derives it from the view id with `UUID.nameUUIDFromBytes("NacreViewDiagram".getBytes())`, and every
 * dialog description declared in those views carries it in its `sourceId`. Matching on it keeps this contribution
 * from hijacking the selection dialogs that SysON declares in its own views.
 *
 * `CybergridMLViewIdTest` asserts that the backend still produces this value.
 */
const CYBERGRIDML_VIEW_SOURCE_ID = "862e39fd-7d88-352b-8fe4-e59d52a39bce";

/**
 * The XIIDM import tool is the only tool of the CybergridML views declaring a dialog, so any dialog coming from
 * those views is the file picker.
 */
export const xiidmDialogContribution: DiagramDialogContribution = {
  canHandle: (dialogDescriptionId: string) =>
    dialogDescriptionId.startsWith(
      "siriusComponents://selectionDialogDescription"
    ) && dialogDescriptionId.includes(`sourceId=${CYBERGRIDML_VIEW_SOURCE_ID}`),
  component: XiidmImportDialog,
};
