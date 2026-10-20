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

package cea.fr.CybergridML.services;

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

