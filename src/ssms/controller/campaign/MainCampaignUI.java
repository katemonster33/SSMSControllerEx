package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.InputScreenBase;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ssms.controller.Indicators;
import ssms.controller.reflection.CampaignStateReflector;

public class MainCampaignUI  extends InputScreenBase {
    public static final String ID = "MainCampaign";
    Vector2f mousePos = new Vector2f(-1.f, -1.f);
    Robot inputSender;
    HandlerController handler;

    ArrayList<Pair<Indicators, String>> indicators;


    public MainCampaignUI() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.LeftStick, "Set ship heading"));
        indicators.add(new Pair<>(Indicators.A, "Navigate"));
        indicators.add(new Pair<>(Indicators.Start, "Pause"));
        //indicators.add(new Pair<>(Indicators.Select, "Reset keybindings"));
    }

    @Override
    public void activate(Object... args) {
        handler = SSMSControllerModPluginEx.controller;
        try {
            inputSender = new Robot();
        } catch(AWTException ex) {
            Global.getLogger(getClass()).fatal("Couldn't create the input sending robot!", ex);
        }
    }

    @Override
    public void renderUI(ViewportAPI viewport) {
        var pf = Global.getSector().getPlayerFleet();
        if(pf == null) {
            return;
        }
        ReadableVector2f desiredHeading = handler.getLeftStick();
        if ( desiredHeading.getX() == 0 && desiredHeading.getY() == 0 ) {
            mousePos.x = mousePos.y = -1.f;
            //desiredHeading = Util_Steering.getHeadingFromFacing(pf.getFacing());
            return;
        }
        //shipLocation.y += pf.getRadius();
//        CombatState cs = (CombatState) AppDriver.getInstance().getState(CombatState.STATE_ID);
//        if ( cs.getWidgetPanel() == null ) return;
//        float zoom = cs.getZoomFactor();
        float zoom = CampaignStateReflector.GetInstance().getZoomFactor();

        //a pentagon that points in the direction the ship ship wants to head into, useful since the ship turns slowly
        //and this way the user immediately has feedback on where he is steering.
        Vector2f vHeadingNormalised = new Vector2f(desiredHeading);
        vHeadingNormalised.normalise();

        float shipRadius = pf.getRadius();
        //adjusting the size of the marker based on ship size but also constraining it to avoid silly dimensions.
        float radius = shipRadius * 0.05f;
        if ( radius < 5f ) radius = 5f;
        else if ( radius > 20f ) radius = 20f;
        radius *= zoom;
        Vector2f shipLocation = new Vector2f(viewport.convertWorldXtoScreenX(pf.getLocation().x), viewport.convertWorldYtoScreenY(pf.getLocation().y));
        Vector2f pentagonCenter = new Vector2f(shipLocation.x + vHeadingNormalised.x * shipRadius * (4f + 2f * radius / shipRadius), shipLocation.y - vHeadingNormalised.y * shipRadius * (4f + 2f * radius / shipRadius));

        int windowPosX = Display.getX(), windowPosY = Display.getY();

        mousePos.x = pentagonCenter.x + windowPosX;
        mousePos.y = pentagonCenter.y + windowPosY;
        InputShim.mouseMove((int)pentagonCenter.x, (int)pentagonCenter.y);
        ControllerCrosshairRenderer.setSize((int)(58 / zoom));
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
        if(Global.getSector().getCampaignUI().isShowingDialog() && Global.getSector().getCampaignUI().getCurrentInteractionDialog() != null) {
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, DialogUI.ID, new Object[]{});
        }
        ReadableVector2f vDesiredHeading = handler.getLeftStick();
        if (vDesiredHeading.getX() != 0 || vDesiredHeading.getY() != 0) {
//            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
//            playerFleet.setMoveDestinationOverride(vDesiredHeading.getX(), vDesiredHeading.getY());
//            isMoving = true;
//            CampaignState cs = (CampaignState) AppDriver.getInstance().getCurrentState();
//            CampaignEngine ce = (CampaignEngine)Global.getSector();
        } else if (isMoving) {
            isMoving = false;
            // CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            // playerFleet.setMoveDestination(playerFleet.getLocation().getX(), playerFleet.getLocation().getY());
        }
        if(handler.isButtonAPressed() && !isMouseDown) {
            inputSender.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            isMouseDown = true;
        } else if(!handler.isButtonAPressed() && isMouseDown) {
            inputSender.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            isMouseDown = false;
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
