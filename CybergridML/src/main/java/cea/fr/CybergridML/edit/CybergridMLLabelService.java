package cea.fr.CybergridML.edit;
import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import org.eclipse.sirius.components.core.api.ILabelServiceDelegate;
import org.eclipse.sirius.components.core.api.labels.StyledString;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CybergridMLLabelService implements ILabelServiceDelegate {

    @Override
    public boolean canHandle(Object object) {
        return object instanceof CybergridMLLibElementType;
    }

    public String getLabel(Object object) {
        return ((CybergridMLLibElementType) object).getName();
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

