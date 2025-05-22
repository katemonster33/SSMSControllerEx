package ssms.controller.campaign;

import ssms.controller.InputScreenBase;
import ssms.controller.InputScreenManager;
import ssms.controller.reflection.IntelTabReflector;

public class IntelFactionTabUi extends InputScreenBase {
    public static final String ID = "IntelFactionTab";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String[] getScopes() {
        return new String[] { CampaignScope.ID };
    }

    @Override
    public void activate(Object ... args) {
        IntelTabReflector intelTabReflector = (IntelTabReflector) args[0];
    }
}
