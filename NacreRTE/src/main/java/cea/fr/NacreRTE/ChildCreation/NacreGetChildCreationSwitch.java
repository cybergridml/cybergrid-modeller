package cea.fr.NacreRTE.ChildCreation;

import cea.fr.NacreRTE.library.NacreLibElementType;
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
        childrenCandidates.add(NacreLibElementType.ZONE);
        childrenCandidates.add(NacreLibElementType.ZONALCONTROlLER);
        childrenCandidates.add(NacreLibElementType.CENTRALCONTROLLER);
        childrenCandidates.add(NacreLibElementType.SUBSTATION);
        return childrenCandidates;
    }
//
//    @Override
//    public List<NacreLibElementType> caseActionDefinition(ActionDefinition object) {
//        List<NacreLibElementType> childrenCandidates = new ArrayList<>();
//        childrenCandidates.add(NacreLibElementType.TECHNO_PORT);
//        childrenCandidates.add(NacreLibElementType.BIO_PORT);
//        childrenCandidates.add(NacreLibElementType.PROPERTY);
//        return childrenCandidates;
//    }

//    @Override
//    public List<NacreLibElementType> casePartDefinition(PartDefinition object) {
//        List<NacreLibElementType> childrenCandidates = new ArrayList<>();
//        childrenCandidates.add(NacreLibElementType.ACTIVITY);
//        childrenCandidates.add(NacreLibElementType.BACKGROUND_ACTIVITY);
//        childrenCandidates.add(NacreLibElementType.CONTAINER);
//        childrenCandidates.add(NacreLibElementType.TECHNO_PORT);
//        childrenCandidates.add(NacreLibElementType.BIO_PORT);
//        childrenCandidates.add(NacreLibElementType.PROPERTY);
//        return childrenCandidates;
//    }



    @Override
    public List<NacreLibElementType> casePartDefinition(PartDefinition object) {
        List<NacreLibElementType> childrenCandidates = new ArrayList<>();
        childrenCandidates.add(NacreLibElementType.MAINGRID);
        childrenCandidates.add(NacreLibElementType.ZONE);
        childrenCandidates.add(NacreLibElementType.ZONALCONTROlLER);
        childrenCandidates.add(NacreLibElementType.CENTRALCONTROLLER);
        childrenCandidates.add(NacreLibElementType.SUBSTATION);
        return childrenCandidates;
    }



}
