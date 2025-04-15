package ssms.controller.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.listeners.CampaignUIRenderingListener;
import com.fs.starfarer.api.combat.ViewportAPI;
import ssms.controller.SSMSControllerModPluginEx;
import ssms.controller.inputScreens.InputScope_360;
import ssms.controller.inputScreens.InputScreenManager;

public class CampaignControllerListener implements EveryFrameScript {
    public static boolean pluginActive = false;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (!pluginActive && Global.getCurrentState() == GameState.CAMPAIGN) {
            pluginActive = true;
            InputScreenManager.getInstance().transitionToScope(InputScope_360.ID, new Object[]{}, MainCampaignUI.ID, new Object[]{});
        }
        if(pluginActive) {
            InputScreenManager.getInstance().preInput(amount);
            InputScreenManager.getInstance().renderUI(Global.getSector().getViewport());
        }
    }
}
