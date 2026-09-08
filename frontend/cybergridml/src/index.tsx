import { loadDevMessages, loadErrorMessages } from "@apollo/client/dev";
import { ExtensionRegistry } from "@eclipse-sirius/sirius-components-core";
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
import { cybergridMLExtensionRegistry } from "./core/cybergridMLExtensionRegistry";
import { cybergridMLTheme } from "./core/theme";
import { httpOrigin, wsOrigin } from "./core/URL";

import "./reset.css";

if (process.env.NODE_ENV !== "production") {
  loadDevMessages();
  loadErrorMessages();
}

const extensionRegistry = new ExtensionRegistry();
const mergeStrategy = new CybergridMLExtensionRegistryMergeStrategy();
extensionRegistry.addAll(sysONExtensionRegistry, mergeStrategy);
extensionRegistry.addAll(cybergridMLExtensionRegistry, mergeStrategy);

const container = document.getElementById("root");
const root = createRoot(container!);
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
