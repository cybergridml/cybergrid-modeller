import { DataExtension } from "@eclipse-sirius/sirius-components-core";
import { diagramDialogContributionExtensionPoint } from "@eclipse-sirius/sirius-components-diagrams";
import { SysONExtensionRegistryMergeStrategy } from "@eclipse-syson/syson-components";

/**
 * Data extension points hold a single value, so the default strategy has the last registry win. Syson already
 * ships `SysONExtensionRegistryMergeStrategy`, which concatenates the extension points *it* contributes to
 * (apolloClientOptionsConfigurers, widgetContribution, paletteTool, ...) instead of letting its own single-entry
 * contributions silently replace Sirius Web's built-in ones - e.g. dropping Sirius Web's "ReferenceWidget" form
 * widget contribution and its Apollo document transform, which broke every reference widget ("Typed by",
 * "References", "Children", ...) with no visible error. We extend it rather than `DefaultExtensionRegistryMergeStrategy`
 * so we keep inheriting that handling, and only add our own case on top for the diagram dialog contributions:
 * dropping it would lose Sirius Web's own selection dialog when we register the XIIDM import dialog.
 *
 * Sirius Web merges its default registry with ours (`defaultExtensionRegistry.addAll(ours, strategy)`), meaning
 * `existingValue` holds the built-in contributions and `newValue` ours. Ours come first so that the XIIDM dialog
 * is matched before the built-in selection dialog, which claims every `selectionDialogDescription`.
 */
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
