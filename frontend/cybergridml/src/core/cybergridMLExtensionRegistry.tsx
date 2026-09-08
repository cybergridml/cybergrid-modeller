import {
  ExtensionRegistry,
  WorkbenchViewContribution,
  workbenchViewContributionExtensionPoint,
} from "@eclipse-sirius/sirius-components-core";
import { diagramDialogContributionExtensionPoint } from "@eclipse-sirius/sirius-components-diagrams";
import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome";
import { chatbotViewContribution } from "../chatbot/chatbotViewContribution";
import NumeribisView from "../numeribis/NumeribisView";
import { xiidmDialogContribution } from "../xiidm/xiidmDialogContribution";

const cybergridMLExtensionRegistry: ExtensionRegistry = new ExtensionRegistry();

const allViewContributions: WorkbenchViewContribution[] = [
  {
    id: "numeribis",
    title: "Numeribis",
    icon: <AutoAwesomeIcon />,
    component: NumeribisView,
  },
  chatbotViewContribution,
];

cybergridMLExtensionRegistry.putData(workbenchViewContributionExtensionPoint, {
  identifier: `cybergridml_${workbenchViewContributionExtensionPoint.identifier}`,
  data: allViewContributions,
});

cybergridMLExtensionRegistry.putData(diagramDialogContributionExtensionPoint, {
  identifier: `cybergridml_${diagramDialogContributionExtensionPoint.identifier}`,
  data: [xiidmDialogContribution],
});

export { cybergridMLExtensionRegistry };
