
package cea.fr.NacreRTE;

import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.syson.common.view.api.IViewDescriptionProvider;
import org.springframework.stereotype.Service;

@Service
public class NacreViewDescriptionProviderCRA implements IViewDescriptionProvider {

	@Override
	public String getViewId() {
		return "NacreViewDiagram";
	}

	@Override
	public IRepresentationDescriptionProvider getRepresentationDescriptionProvider() {
		return new NacreRepresentationDescriptionProviderCRA();
	}

}