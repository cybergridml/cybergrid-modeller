package cea.fr.NacreRTE.edit;

import cea.fr.NacreRTE.domain.NacreLibElementType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.sysml.*;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.util.SysmlEClassSwitch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class NacreGetChildCreationSwitch extends SysmlEClassSwitch<List<NacreLibElementType>> {

    @Override
    public List<NacreLibElementType> defaultCase(EObject object) {
        return Collections.emptyList();
    }

    @Override
    public List<NacreLibElementType> casePackage(Package object) {
        List<NacreLibElementType> childrenCandidates = new ArrayList<>();
        childrenCandidates.add(NacreLibElementType.MAINGRID);
        //childrenCandidates.add(NacreLibElementType.ZONE);
        //childrenCandidates.add(NacreLibElementType.ZONALCONTROLLER);
        childrenCandidates.add(NacreLibElementType.CENTRALCONTROLLER);
        return childrenCandidates;
    }


    @Override
    public List<NacreLibElementType> casePartDefinition(PartDefinition object) {
        List<NacreLibElementType> childrenCandidates = new ArrayList<>();
        childrenCandidates.add(NacreLibElementType.MAINGRID);
       // childrenCandidates.add(NacreLibElementType.ZONE);
       // childrenCandidates.add(NacreLibElementType.ZONALCONTROLLER);
        childrenCandidates.add(NacreLibElementType.CENTRALCONTROLLER);
        return childrenCandidates;
    }



}

