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

import cea.fr.CybergridML.view.colors.CybergridMLColorPaletteProvider;
import org.eclipse.sirius.components.view.ColorPalette;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.syson.common.view.api.IViewDescriptionProvider;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CfraViewDescriptionProvider implements IViewDescriptionProvider {

	@Override
	public String getViewId() {
		return "NacreViewDiagram";
	}

	@Override
	public IRepresentationDescriptionProvider getRepresentationDescriptionProvider() {
		return new CfraRepresentationDescriptionProvider();
	}

	@Override
	public Optional<ColorPalette> buildColorPalette() {
		return Optional.of(CybergridMLColorPaletteProvider.build());
	}

}