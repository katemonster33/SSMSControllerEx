package ssms.controller.campaign;

import ssms.controller.InputScopeBase;

public class IntelScope extends InputScopeBase {
    public String ID = "IntelScope";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDefaultScreen() {
        return IntelTabUI.ID;
    }
}
