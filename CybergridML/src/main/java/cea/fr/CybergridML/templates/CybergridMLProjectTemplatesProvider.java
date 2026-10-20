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

package cea.fr.CybergridML.templates;

import org.eclipse.sirius.web.application.project.services.api.IProjectTemplateProvider;
import org.eclipse.sirius.web.application.project.services.api.ProjectTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CybergridMLProjectTemplatesProvider implements IProjectTemplateProvider {

    public static final String CYBERGRIDML_TEMPLATE_ID = "CyberGridML-template";

    @Override
    public List<ProjectTemplate> getProjectTemplates() {
        var cybergridMLTemplate = new ProjectTemplate(CYBERGRIDML_TEMPLATE_ID, "CyberGridML", "/images/Cybergrid.png", List.of());

        return List.of(cybergridMLTemplate);
    }
}

