package cea.fr.NacreRTE;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.util.SysmlSwitch;

public class NacreRevealCompartmentSwitch extends SysmlSwitch<Boolean> {

    @Override
    public Boolean defaultCase(EObject object) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean casePortUsage(PortUsage object) {
        return Boolean.FALSE;
    }
}
