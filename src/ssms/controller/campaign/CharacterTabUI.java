package ssms.controller.campaign;

import com.fs.starfarer.api.combat.ViewportAPI;
import ssms.controller.InputScreenBase;

public class CharacterTabUI extends InputScreenBase {
    public static final String ID = "CharacterTab";

    public void activate(Object ...args) {
    }

    public void preInput(float advance) {
    }


    @Override
    public String getId() {
        return ID;
    }
}
