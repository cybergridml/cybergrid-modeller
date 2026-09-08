import { loadDevMessages, loadErrorMessages } from "@apollo/client/dev";
import {
  ExtensionRegistry,
  WorkbenchViewContribution,
  workbenchViewContributionExtensionPoint,
} from "@eclipse-sirius/sirius-components-core";
import { diagramDialogContributionExtensionPoint } from "@eclipse-sirius/sirius-components-diagrams";
import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome";
import NumeribisView from "./numeribis/NumeribisView";
import {
  DiagramRepresentationConfiguration,
  SiriusWebApplication,
} from "@eclipse-sirius/sirius-web-application";
import {
  sysONExtensionRegistry,
  sysONNodeTypeRegistry,
} from "@eclipse-syson/syson-components";
import { createRoot } from "react-dom/client";
import { CybergridMLExtensionRegistryMergeStrategy } from "./core/CybergridMLExtensionRegistryMergeStrategy";
import { cybergridMLTheme } from "./core/theme";
import { httpOrigin, wsOrigin } from "./core/URL";
import { xiidmDialogContribution } from "./xiidm/xiidmDialogContribution";

import "./reset.css";
import { NotarizationView } from "./notarization/NotarizationView (1)";

if (process.env.NODE_ENV !== "production") {
  loadDevMessages();
  loadErrorMessages();
}

const extensionRegistry = new ExtensionRegistry();
const mergeStrategy = new CybergridMLExtensionRegistryMergeStrategy();

extensionRegistry.addAll(sysONExtensionRegistry, mergeStrategy);
console.log(
  "AFTER SYSON",
  extensionRegistry.getData(workbenchViewContributionExtensionPoint)
);

const cybergridMLRegistry = new ExtensionRegistry();

cybergridMLRegistry.putData(workbenchViewContributionExtensionPoint, {
  identifier: `cybergridml_${workbenchViewContributionExtensionPoint.identifier}`,
  data: [
    {
      id: "numeribis",
      title: "Numeribis",
      icon: <AutoAwesomeIcon />,
      component: NumeribisView,
    },
  ],
});

cybergridMLRegistry.putData(diagramDialogContributionExtensionPoint, {
  identifier: `cybergridml_${diagramDialogContributionExtensionPoint.identifier}`,
  data: [xiidmDialogContribution],
});

const allViewContributions: WorkbenchViewContribution[] = [
  {
    id: "notarization",
    title: "Notarize",
    icon: null,
    component: NotarizationView,
  },
];

extensionRegistry.putData(workbenchViewContributionExtensionPoint, {
  identifier: `cybergridml_${workbenchViewContributionExtensionPoint.identifier}`,
  data: allViewContributions,
});

extensionRegistry.addAll(cybergridMLRegistry, mergeStrategy);

console.log(
  "AFTER CYBERGRIDML",
  extensionRegistry.getData(workbenchViewContributionExtensionPoint)
);

console.log(
  "FINAL",
  extensionRegistry.getData(workbenchViewContributionExtensionPoint)
);

const container = document.getElementById("root");
const root = createRoot(container!);

console.log(
  "BEFORE APP",
  extensionRegistry
    .getData(workbenchViewContributionExtensionPoint)
    ?.data.map((view) => view.id)
);
root.render(
  <SiriusWebApplication
    httpOrigin={httpOrigin}
    wsOrigin={wsOrigin}
    extensionRegistry={extensionRegistry}
    extensionRegistryMergeStrategy={mergeStrategy}
    theme={cybergridMLTheme}
  >
    <DiagramRepresentationConfiguration
      nodeTypeRegistry={sysONNodeTypeRegistry}
    />
  </SiriusWebApplication>
);
