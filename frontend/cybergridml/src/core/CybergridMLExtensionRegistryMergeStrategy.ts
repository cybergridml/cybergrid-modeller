import {
  DataExtension,
  workbenchViewContributionExtensionPoint,
} from "@eclipse-sirius/sirius-components-core";
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
    if (identifier === workbenchViewContributionExtensionPoint.identifier) {

      return {
        identifier: `cybergridml_${identifier}`,
        data: [...existingValue.data, ...newValue.data],
      };

    }
    return super.mergeDataExtensions(identifier, existingValue, newValue);
  }
}
