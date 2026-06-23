package cea.fr.CybergridML.view;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.sysml.util.SysmlSwitch;

public class CybergridMLViewFilterSwitch extends SysmlSwitch<Boolean> {

    @Override
    public Boolean defaultCase(EObject object) {
        return Boolean.TRUE;
    }
}
