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

        SSMSControllerModPluginEx.controller.poll();
        InputScreenManager.getInstance().startFrame();
        InputScreenManager.getInstance().preInput(amount);
        InputScreenManager.getInstance().processControllerEvents(amount, SSMSControllerModPluginEx.controller.getButtonEvents(), SSMSControllerModPluginEx.controller.getAxisEvents());
    }
}
