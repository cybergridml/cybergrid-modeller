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

package cea.fr.CybergridML.domain;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.sysml.Definition;
import org.eclipse.syson.sysml.MetadataDefinition;
import org.eclipse.syson.sysml.SysmlFactory;

import java.util.Optional;

public class CybergridMLDefinitionsUtil {

    private CybergridMLDefinitionsUtil() {
    }

    public static Definition getDefinition(IEditingContext editingContext, CybergridMLLibElementType cybergridMLLibElementType) {
        Resource nResource = CybergridMLLibraryUtil.getCybergridMLLibraryResource(editingContext);
        return getCybergridMLDefinition(nResource, cybergridMLLibElementType, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }

    public static Optional<Definition> getCybergridMLDefinition(Resource resource, CybergridMLLibElementType cybergridMLLibElementType, boolean isSubclassifier) {
        if (resource == null || cybergridMLLibElementType == null) {
            return Optional.empty();
        }
        TreeIterator<EObject> contentsIter = resource.getAllContents();
        while (contentsIter.hasNext()) {
            EObject eObject = contentsIter.next();
            if (eObject instanceof Definition definition && CybergridMLLibraryUtil.typeHasCybergridMLLibraryDefName(definition, cybergridMLLibElementType, isSubclassifier)) {
                return Optional.of(definition);
            }
        }
        return Optional.empty();
    }

    public static Optional<MetadataDefinition> getHazardDefinition(IEditingContext editingContext) {
        Resource resource = CybergridMLLibraryUtil.getCybergridMLLibraryResource(editingContext);
        if (resource == null) {
            return Optional.empty();
        }
        TreeIterator<EObject> it = resource.getAllContents();
        while (it.hasNext()) {
            EObject obj = it.next();
            if (obj instanceof MetadataDefinition md && "CyberGridMLDatatypes::Hazard".equals(md.getQualifiedName())) {
                return Optional.of(md);
            }
        }
        return Optional.empty();
    }
}
