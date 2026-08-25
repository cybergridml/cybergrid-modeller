import { DiagramDialogContribution } from "@eclipse-sirius/sirius-components-diagrams";
import { XiidmImportDialog } from "./XiidmImportDialog";

// Derived from "NacreViewDiagram" - see CybergridMLViewIdTest
const CYBERGRIDML_VIEW_SOURCE_ID = "862e39fd-7d88-352b-8fe4-e59d52a39bce";

export const xiidmDialogContribution: DiagramDialogContribution = {
  canHandle: (dialogDescriptionId: string) =>
    dialogDescriptionId.startsWith(
      "siriusComponents://selectionDialogDescription"
    ) && dialogDescriptionId.includes(`sourceId=${CYBERGRIDML_VIEW_SOURCE_ID}`),
  component: XiidmImportDialog,
};
