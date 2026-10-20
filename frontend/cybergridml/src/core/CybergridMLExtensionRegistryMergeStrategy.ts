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

import { DataExtension } from "@eclipse-sirius/sirius-components-core";
import { diagramDialogContributionExtensionPoint } from "@eclipse-sirius/sirius-components-diagrams";
import { SysONExtensionRegistryMergeStrategy } from "@eclipse-syson/syson-components";

export class CybergridMLExtensionRegistryMergeStrategy extends SysONExtensionRegistryMergeStrategy {
  override mergeDataExtensions(
    identifier: string,
    existingValue: DataExtension<any>,
    newValue: DataExtension<any>
  ): DataExtension<any> {
    if (identifier === diagramDialogContributionExtensionPoint.identifier) {
      return {
        identifier: `cybergridml_${identifier}`,
        data: [...newValue.data, ...existingValue.data],
      };
    }
    return super.mergeDataExtensions(identifier, existingValue, newValue);
  }
}
