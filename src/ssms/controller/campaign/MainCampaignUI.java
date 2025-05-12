package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.InputScreenBase;

import java.util.ArrayList;
import java.util.List;

import ssms.controller.Indicators;
import ssms.controller.reflection.CampaignStateReflector;
import ssms.controller.reflection.TradeUiReflector;

public class MainCampaignUI  extends InputScreenBase {
    public static final String ID = "MainCampaign";
    Vector2f lastHeading = null;
    Vector2f mousePos = new Vector2f(-1.f, -1.f);
    HandlerController handler;

    ArrayList<Pair<Indicators, String>> indicators;


    public MainCampaignUI() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.LeftStick, "Set ship heading"));
        indicators.add(new Pair<>(Indicators.LeftStickButton, "Toggle free look"));
        indicators.add(new Pair<>(Indicators.A, "Navigate"));
        indicators.add(new Pair<>(Indicators.Start, "Pause"));
        //indicators.add(new Pair<>(Indicators.Select, "Reset keybindings"));
    }

    @Override
    public void activate(Object... args) {
        handler = SSMSControllerModPluginEx.controller;
    }

    @Override
    public void renderUI(ViewportAPI viewport) {
        var pf = Global.getSector().getPlayerFleet();
        if(pf == null) {
            return;
        }
        ReadableVector2f desiredHeading = handler.getLeftStick();
        if ( desiredHeading.getX() == 0 && desiredHeading.getY() == 0) {
            if(lastHeading != null && (desiredHeading.getX() != lastHeading.getX() || desiredHeading.getY() != lastHeading.getY())) {
                mousePos.x = pf.getLocation().getX();
                mousePos.y = pf.getLocation().getY();
                InputShim.mouseMove((int) mousePos.getX(), (int) mousePos.getY());
            } else {
                return;
            }
        }
        if(lastHeading == null) {
            lastHeading = new Vector2f(desiredHeading.getX(), desiredHeading.getY());
        } else {
            lastHeading.set(desiredHeading.getX(), desiredHeading.getY());
        }
        float minX = viewport.getLLX(), minY = viewport.getLLY();
        float maxX = viewport.getVisibleWidth() + minX, maxY = viewport.getVisibleHeight() + minY;
        var shipPos = pf.getLocation();
        float xpos, ypos;
        if(desiredHeading.getX() < 0) {
            xpos = shipPos.x + desiredHeading.getX() * (shipPos.x - minX);
        } else {
            xpos = shipPos.x + desiredHeading.getX() * (maxX - shipPos.x);
        }
        mousePos.x = viewport.convertWorldXtoScreenX(xpos);
        if(desiredHeading.getY() < 0) {
            ypos = shipPos.y + desiredHeading.getY() * (shipPos.y - minY);
        } else {
            ypos = shipPos.y + desiredHeading.getY() * (maxY - shipPos.y);
        }
        mousePos.y = viewport.convertWorldYtoScreenY(ypos);
        InputShim.mouseMove((int)mousePos.x, (int)mousePos.y);
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }

    boolean isMouseDown = false;
    boolean isMoving = false;
    boolean startButtonHandled = false;
    @Override
    public void preInput(float advance) {
        float zoom = CampaignStateReflector.GetInstance().getZoomFactor();
        ControllerCrosshairRenderer.setSize((int)(58 / zoom));
        if(Global.getSector().getCampaignUI().isShowingDialog()) {
            if(Global.getSector().getCampaignUI().getCurrentInteractionDialog() != null) {
                InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, DialogUI.ID, new Object[]{});
            } else if(Global.getSector().getCampaignUI().getCurrentCoreTab() == CoreUITabId.CARGO) {
                var coreui = CampaignStateReflector.GetInstance().getCoreUI();
                if(coreui != null) {
                    var tradeui = TradeUiReflector.TryGet(coreui);
                    if(tradeui != null) {
                        InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, TradeScreen.ID, new Object[]{ tradeui });
                    }
                }
            }
        }
        if(mousePos.x != -1.f && mousePos.y != -1.f) {
            if (handler.isButtonAPressed() && !isMouseDown) {
                InputShim.mouseDown((int) mousePos.x, (int) mousePos.y, 0);
                isMouseDown = true;
            } else if (!handler.isButtonAPressed() && isMouseDown) {
                InputShim.mouseUp((int) mousePos.x, (int) mousePos.y, 0);
                isMouseDown = false;
            }
        }
        if(handler.getButtonEvent(HandlerController.Buttons.LeftStickButton) == 1) {
            if(mousePos.x == -1.f || mousePos.y == -1.f) {
                if(Global.getSector().getPlayerFleet() != null) {
                    var shipLoc = Global.getSector().getPlayerFleet().getLocation();
                    mousePos.x = shipLoc.getX();
                    mousePos.y = shipLoc.getY();
                }
            }
            InputShim.mouseMove((int) mousePos.x, (int) mousePos.y);
            InputShim.mouseDownUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.RIGHT);
        }
        if(handler.isButtonXPressed()) {
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            playerFleet.goSlowOneFrame();
        }
        if(!startButtonHandled && handler.isButtonStartPressed()) {
            Global.getSector().setPaused(!Global.getSector().isPaused());
            startButtonHandled = true;
        } else if(!handler.isButtonStartPressed()) {
            startButtonHandled = false;
        }
        if(handler.isButtonSelectPressed()) {
            if(Global.getSector().getCampaignUI().isHideUI()) {
                Global.getSector().getCampaignUI().setHideUI(false);
            } else {
                Global.getSector().getCampaignUI().showCoreUITab(CoreUITabId.MAP);
            }
        }
        if(handler.isDpadRight()) {
            //Global.getSector().
        }
    }

    @Override
    public String getId() {
        return ID;
    }
}
