package cea.fr.NacreRTE;

import org.eclipse.syson.diagram.services.DiagramQueryElementService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.diagram.services.aql.DiagramQueryAQLService;
import org.eclipse.syson.sysml.Element;

public class NacreDiagramAQLService extends DiagramQueryAQLService {

    public NacreDiagramAQLService(DiagramQueryElementService diagramQueryElementService, DiagramQueryLabelService diagramQueryLabelService) {
        super(diagramQueryElementService, diagramQueryLabelService);
    }

    @Override
    public String getDefaultInitialDirectEditLabel(Element element) {
        return element.getDeclaredName();
    }
}
