package cea.fr.NacreRTE.view;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.sysml.util.SysmlSwitch;

public class NacreViewFilterSwitch extends SysmlSwitch<Boolean> {

    @Override
    public Boolean defaultCase(EObject object) {
        return Boolean.TRUE;
    }
}
