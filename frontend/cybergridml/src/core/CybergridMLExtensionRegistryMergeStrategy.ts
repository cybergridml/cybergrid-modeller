import { DataExtension } from "@eclipse-sirius/sirius-components-core";
import { diagramDialogContributionExtensionPoint } from "@eclipse-sirius/sirius-components-diagrams";
import { DefaultExtensionRegistryMergeStrategy } from "@eclipse-sirius/sirius-web-application";

/**
 * Data extension points hold a single value, so the default strategy has the last registry win. That would drop
 * Sirius Web's own selection dialog when we register the XIIDM import dialog, so the diagram dialog contributions
 * are concatenated instead.
 *
 * Sirius Web merges its default registry with ours (`defaultExtensionRegistry.addAll(ours, strategy)`), meaning
 * `existingValue` holds the built-in contributions and `newValue` ours. Ours come first so that the XIIDM dialog
 * is matched before the built-in selection dialog, which claims every `selectionDialogDescription`.
 */
export class CybergridMLExtensionRegistryMergeStrategy extends DefaultExtensionRegistryMergeStrategy {
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
