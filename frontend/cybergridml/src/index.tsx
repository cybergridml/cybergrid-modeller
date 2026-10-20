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

import { loadDevMessages, loadErrorMessages } from "@apollo/client/dev";
import { ExtensionRegistry } from "@eclipse-sirius/sirius-components-core";
import { diagramDialogContributionExtensionPoint } from "@eclipse-sirius/sirius-components-diagrams";
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

if (process.env.NODE_ENV !== "production") {
  loadDevMessages();
  loadErrorMessages();
}

const extensionRegistry = new ExtensionRegistry();
const mergeStrategy = new CybergridMLExtensionRegistryMergeStrategy();
extensionRegistry.addAll(sysONExtensionRegistry, mergeStrategy);
extensionRegistry.putData(diagramDialogContributionExtensionPoint, {
  identifier: `cybergridml_${diagramDialogContributionExtensionPoint.identifier}`,
  data: [xiidmDialogContribution],
});

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
