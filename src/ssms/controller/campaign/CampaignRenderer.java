package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.listeners.CampaignUIRenderingListener;
import com.fs.starfarer.api.combat.ViewportAPI;
import ssms.controller.ControllerCrosshairRenderer;
import ssms.controller.InputScreenManager;
import ssms.controller.InputShim;

public class CampaignRenderer implements CampaignUIRenderingListener {
    @Override
    public void renderInUICoordsBelowUI(ViewportAPI viewport) {

    }

    @Override
    public void renderInUICoordsAboveUIBelowTooltips(ViewportAPI viewport) {
    }

    @Override
    public void renderInUICoordsAboveUIAndTooltips(ViewportAPI viewport) {
        InputScreenManager.getInstance().renderUI(Global.getSector().getViewport());
    }
}
