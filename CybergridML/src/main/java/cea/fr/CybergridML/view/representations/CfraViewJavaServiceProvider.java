/*******************************************************************************
 * Copyright (c) 2026 CEA LIST - RTE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     CEA LIST - Initial API and implementation
 *******************************************************************************/

package cea.fr.CybergridML.view.representations;


import cea.fr.CybergridML.services.CybergridMLHazardService;
import cea.fr.CybergridML.view.services.CybergridMLDiagramAQLService;
import cea.fr.CybergridML.view.services.CybergridMLViewCreateService;
import cea.fr.CybergridML.view.services.CybergridMLViewNodeService;
import cea.fr.CybergridML.view.services.CybergridMLViewToolService;
import org.eclipse.emf.common.util.EList;
import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.eclipse.syson.diagram.common.view.services.ViewEdgeService;
import org.eclipse.syson.diagram.common.view.services.ViewLabelService;
import org.eclipse.syson.diagram.services.aql.DiagramMutationAQLService;
import org.eclipse.syson.model.services.aql.ModelMutationAQLService;
import org.eclipse.syson.model.services.aql.ModelQueryAQLService;
import org.eclipse.syson.representation.services.aql.RepresentationMutationAQLService;
import org.eclipse.syson.representation.services.aql.RepresentationQueryAQLService;
import org.eclipse.syson.services.DeleteService;
import org.eclipse.syson.services.UtilService;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;


@Configuration
public class CfraViewJavaServiceProvider implements IJavaServiceProvider {

    @Override
    public List<Class<?>> getServiceClasses(View view) {
        EList<RepresentationDescription> descriptions = view.getDescriptions();
        Optional<RepresentationDescription> optDescription = descriptions.stream()
                .filter(desc -> CfraRepresentationDescriptionProvider.DESCRIPTION_NAME.equals(desc.getName())).findFirst();
        if (optDescription.isPresent()) {
            return List.of(
                    UtilService.class,
                    DeleteService.class,
                    ViewLabelService.class,
                    ViewEdgeService.class,
                    DiagramMutationAQLService.class,
                    ModelMutationAQLService.class,
                    ModelQueryAQLService.class,
                    RepresentationMutationAQLService.class,
                    RepresentationQueryAQLService.class,
                    CybergridMLViewToolService.class,
                    CybergridMLViewCreateService.class,
                    CybergridMLViewNodeService.class,
                    CybergridMLDiagramAQLService.class,
                    CybergridMLHazardService.class
                   );
        }
        return List.of();
    }
}