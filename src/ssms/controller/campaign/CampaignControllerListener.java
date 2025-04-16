package ssms.controller.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import ssms.controller.SSMSControllerModPluginEx;
import ssms.controller.InputScopeBase;
import ssms.controller.InputScreenManager;

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
        if (!pluginActive && Global.getCurrentState() == GameState.CAMPAIGN) {
            pluginActive = true;
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, MainCampaignUI.ID, new Object[]{});
        }
        if(pluginActive) {
            SSMSControllerModPluginEx.controller.poll();
            if(Global.getSector().getCampaignUI().isShowingDialog() && !Objects.equals(InputScreenManager.getInstance().getCurrentScreen().getId(), DialogUI.ID)) {
                InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, DialogUI.ID, new Object[]{});
            }
            InputScreenManager.getInstance().preInput(amount);
            InputScreenManager.getInstance().renderUI(Global.getSector().getViewport());
        }
    }
}
