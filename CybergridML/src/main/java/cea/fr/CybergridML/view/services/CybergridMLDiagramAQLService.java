package cea.fr.CybergridML.view.services;

import org.eclipse.syson.diagram.services.DiagramQueryAnnotatingService;
import org.eclipse.syson.diagram.services.DiagramQueryElementService;
import org.eclipse.syson.diagram.services.DiagramQueryExposeService;
import org.eclipse.syson.diagram.services.DiagramQueryGraphicalService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryViewService;
import org.eclipse.syson.diagram.services.aql.DiagramQueryAQLService;
import org.springframework.stereotype.Service;
import org.eclipse.syson.sysml.Element;

@Service
public class CybergridMLDiagramAQLService extends DiagramQueryAQLService {

    public CybergridMLDiagramAQLService(DiagramQueryElementService diagramQueryElementService, DiagramQueryLabelService diagramQueryLabelService,
            DiagramQueryExposeService diagramQueryExposeService, DiagramQueryGraphicalService diagramQueryGraphicalService, DiagramQueryViewService diagramQueryViewService,
            DiagramQueryAnnotatingService diagramQueryAnnotatingService) {
        super(diagramQueryElementService, diagramQueryLabelService, diagramQueryExposeService, diagramQueryGraphicalService, diagramQueryViewService, diagramQueryAnnotatingService);
    }

    @Override
    public String getDefaultInitialDirectEditLabel(Element element) {
        return element.getDeclaredName();
    }
}
