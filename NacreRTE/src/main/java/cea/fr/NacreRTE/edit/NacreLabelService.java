package cea.fr.NacreRTE.edit;
import cea.fr.NacreRTE.domain.NacreLibElementType;
import org.eclipse.sirius.components.core.api.ILabelServiceDelegate;
import org.eclipse.sirius.components.core.api.labels.StyledString;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NacreLabelService implements ILabelServiceDelegate {

    @Override
    public boolean canHandle(Object object) {
        return object instanceof NacreLibElementType;
    }

    public String getLabel(Object object) {
        return ((NacreLibElementType) object).getName();
    }

    @Override
    public StyledString getStyledLabel(Object object) {
        return StyledString.of(this.getLabel(object));
    }

    @Override
    public List<String> getImagePaths(Object object) {
        return List.of();
    }
}

