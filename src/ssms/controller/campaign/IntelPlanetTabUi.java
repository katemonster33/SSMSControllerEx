package ssms.controller.campaign;

import ssms.controller.InputScreenBase;

public class IntelPlanetTabUi extends InputScreenBase {
    public static final String ID = "IntelPlanetTab";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String[] getScopes() {
        return new String[] {};
    }
}
