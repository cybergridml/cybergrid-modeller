
package cea.fr.CybergridML.view.representations;

import cea.fr.CybergridML.view.CybergridMLColorPaletteProvider;
import org.eclipse.sirius.components.view.ColorPalette;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.syson.common.view.api.IViewDescriptionProvider;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CfraViewDescriptionProvider implements IViewDescriptionProvider {

	@Override
	public String getViewId() {
		return "NacreViewDiagram";
	}

	@Override
	public IRepresentationDescriptionProvider getRepresentationDescriptionProvider() {
		return new CfraRepresentationDescriptionProvider();
	}

	@Override
	public Optional<ColorPalette> buildColorPalette() {
		return Optional.of(CybergridMLColorPaletteProvider.build());
	}

}