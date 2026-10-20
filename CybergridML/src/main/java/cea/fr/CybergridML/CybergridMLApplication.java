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

package cea.fr.CybergridML;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;


@SpringBootApplication
@ComponentScan(
		basePackages = {"cea.fr.CybergridML", "org.eclipse.syson", "org.eclipse.sirius.web",
				"org.eclipse.sirius.components"},
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.REGEX, pattern = {
						".*SysONDefaultLibrariesConfiguration",
						".*SysMLv2ProjectTemplatesProvider",
						".*StateTransitionViewDiagramDescriptionProvider",
						".*ActionFlowViewDiagramDescriptionProvider",
						".*InterconnectionViewDiagramDescriptionProvider"
				})
		}
)
@ConfigurationPropertiesScan
public class CybergridMLApplication {

	public static void main(String[] args) {
		SpringApplication.run(CybergridMLApplication.class, args);
	}
}