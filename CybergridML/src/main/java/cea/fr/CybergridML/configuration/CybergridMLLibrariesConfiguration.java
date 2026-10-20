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

package cea.fr.CybergridML.configuration;

import cea.fr.CybergridML.domain.CybergridMLLibraryUtil;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.emfjson.resource.JsonResourceFactoryImpl;
import org.eclipse.syson.application.configuration.SysONDefaultLibrariesConfiguration;
import org.eclipse.syson.application.configuration.SysONLoadDefaultLibrariesOnApplicationStartConfiguration;
import org.eclipse.syson.application.libraries.SysONLibraryLoadingDefinition;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;


@Configuration
public class CybergridMLLibrariesConfiguration extends SysONDefaultLibrariesConfiguration {

    public static final SysONLibraryLoadingDefinition CYBERGRIDML = new SysONLibraryLoadingDefinition(
            "SysML CyberGridML Library",
            CybergridMLLibraryUtil.CYBERGRIDML_LIBRARY_SCHEME,
            "sysml.cybergridML.libraries",
            Collections.singletonList(JsonResourceFactoryImpl.EXTENSION),
            new JSONResourceFactory()::createResource);

    public CybergridMLLibrariesConfiguration(SysONLoadDefaultLibrariesOnApplicationStartConfiguration startConfiguration) {
        super(startConfiguration);
    }

    @Override
    protected List<SysONLibraryLoadingDefinition> getDefaultLibraries() {
        return Stream.of(KERML, SYSML, CYBERGRIDML ).toList();
    }

}

