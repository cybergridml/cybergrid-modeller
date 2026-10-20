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
import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.sysml.*;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.util.SysmlEClassSwitch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CybergridMLGetChildCreationSwitch extends SysmlEClassSwitch<List<CybergridMLLibElementType>> {

    @Override
    public List<CybergridMLLibElementType> defaultCase(EObject object) {
        return Collections.emptyList();
    }

    @Override
    public List<CybergridMLLibElementType> casePackage(Package object) {
        List<CybergridMLLibElementType> childrenCandidates = new ArrayList<>();
        //childrenCandidates.add(CybergridMLLibElementType.MAINGRID);
        //childrenCandidates.add(CybergridMLLibElementType.CENTRALCONTROLLER);
        return childrenCandidates;
    }


    @Override
    public List<CybergridMLLibElementType> casePartDefinition(PartDefinition object) {
        List<CybergridMLLibElementType> childrenCandidates = new ArrayList<>();
        childrenCandidates.add(CybergridMLLibElementType.MAINGRID);
        childrenCandidates.add(CybergridMLLibElementType.CENTRALCONTROLLER);
        return childrenCandidates;
    }



}

