package cea.fr.CybergridML.view.services;

import org.eclipse.syson.diagram.services.DiagramQueryElementService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.diagram.services.aql.DiagramQueryAQLService;
import org.springframework.stereotype.Service;
import org.eclipse.syson.sysml.Element;

@Service
public class CybergridMLDiagramAQLService extends DiagramQueryAQLService {

    public CybergridMLDiagramAQLService(DiagramQueryElementService diagramQueryElementService, DiagramQueryLabelService diagramQueryLabelService) {
        super(diagramQueryElementService, diagramQueryLabelService);
    }

    @Override
    public String getDefaultInitialDirectEditLabel(Element element) {
        return element.getDeclaredName();
    }
}
