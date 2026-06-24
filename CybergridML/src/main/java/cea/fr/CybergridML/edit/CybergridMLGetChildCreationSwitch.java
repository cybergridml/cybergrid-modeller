package cea.fr.CybergridML.edit;

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

