package ssms.controller.campaign;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import ssms.controller.InputScreenBase;
import ssms.controller.InputScreenManager;
import ssms.controller.reflection.CampaignStateReflector;

// this is dumb, but there's a few frames where the game is "loaded" but Global.getCurrentState() still returns TITLE. so we show this UI for a few frames.
public class CampaignTransitionUI extends InputScreenBase {
    public static final String ID = "CampaignTransition";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void preInput(float amount) {
        if(Global.getCurrentState() == GameState.CAMPAIGN) {
            CampaignStateReflector.GetInstance().refreshInstance();
            InputScreenManager.getInstance().transitionToScreen(MainCampaignUI.ID);
        }
    }
}
