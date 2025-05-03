package ssms.controller.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import ssms.controller.InputShim;
import ssms.controller.SSMSControllerModPluginEx;
import ssms.controller.InputScopeBase;
import ssms.controller.InputScreenManager;
import ssms.controller.reflection.InteractionDialogReflector;

import java.util.Objects;

public class CampaignControllerListener implements EveryFrameScript {
    public static boolean pluginActive = false;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        InputShim.advance(amount);
        if (!pluginActive && Global.getCurrentState() == GameState.CAMPAIGN) {
            pluginActive = true;
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, MainCampaignUI.ID, new Object[]{});
        }
        if(pluginActive) {
            SSMSControllerModPluginEx.controller.poll();
            InputScreenManager.getInstance().preInput(amount);
            InputScreenManager.getInstance().renderUI(Global.getSector().getViewport());
        }
    }
}
