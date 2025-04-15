package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CampaignEngine;
import com.fs.starfarer.campaign.CampaignState;
import com.fs.starfarer.campaign.fleet.CampaignFleet;
import com.fs.starfarer.campaign.fleet.CampaignFleetView;
import com.fs.starfarer.combat.CombatState;
import com.fs.state.AppDriver;
import lunalib.lunaUtil.campaign.SectorUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.XRandR;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.inputScreens.InputScope_360;
import ssms.controller.inputScreens.InputScreen;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import ssms.controller.Indicators;

public class MainCampaignUI  implements InputScreen {
    public static final String ID = "MainCampaign";
    Vector2f mousePos = new Vector2f(-1.f, -1.f);
    Robot inputSender;
    HandlerController handler;

    ArrayList<Pair<Indicators, String>> indicators;


    public MainCampaignUI() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.LeftStickUp, "Up"));
        indicators.add(new Pair<>(Indicators.LeftStickDown, "Down"));
        indicators.add(new Pair<>(Indicators.LeftStickLeft, "Left"));
        indicators.add(new Pair<>(Indicators.LeftStickRight, "Right"));
        indicators.add(new Pair<>(Indicators.A, "Confirm"));
        indicators.add(new Pair<>(Indicators.B, "Cancel"));
        indicators.add(new Pair<>(Indicators.Select, "Reset keybindings"));
    }
    @Override
    public void deactivate() {

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
        var ps = Global.getCombatEngine().getPlayerShip();
        if(pf == null) {
            return;
        }
        Vector2f shipLocation = new Vector2f(pf.getLocation().x, pf.getLocation().y);
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
        CampaignState cs = (CampaignState) AppDriver.getInstance().getCurrentState();
        float zoom = cs.getZoomFactor();

        //a pentagon that points in the direction the ship ship wants to head into, useful since the ship turns slowly
        //and this way the user immediately has feedback on where he is steering.
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        //HUD color for own ship
        final float cr = 155.0f/255.0f, cg = 255.0f/255.0f, cb = 0f/255.0f;
        GL11.glColor3f(cr, cg, cb);
        GL11.glLineWidth(1f);
        Vector2f vHeadingNormalised = new Vector2f(desiredHeading);
        vHeadingNormalised.normalise();

        float shipRadius = pf.getRadius();
        //adjusting the size of the marker based on ship size but also constraining it to avoid silly dimensions.
        float radius = shipRadius * 0.05f;
        if ( radius < 5f ) radius = 5f;
        else if ( radius > 20f ) radius = 20f;
        radius *= zoom;
        Vector2f pentagonCenter = new Vector2f(shipLocation.x + vHeadingNormalised.x * shipRadius * (4f + 2f * radius / shipRadius), shipLocation.y - vHeadingNormalised.y * shipRadius * (4f + 2f * radius / shipRadius));

        float angleIncrement = (float) Math.toRadians(360.0f / 5f);
        //rotating the pentagon so that it points in the right direction and the missing slice is opposite to that point.
        float angle = (float) Math.toRadians(Util_Steering.getFacingFromHeading(new Vector2f(desiredHeading))) + 3f * angleIncrement;

        //int innerX = Display.getParent().getX();
        int windowPosX = Display.getX(), windowPosY = Display.getY();
        mousePos.x = viewport.convertWorldXtoScreenX(pentagonCenter.x) + windowPosX;
        mousePos.y = viewport.convertWorldYtoScreenY(pentagonCenter.y) + windowPosY;
        inputSender.mouseMove((int)mousePos.x, (int)mousePos.y);
        //viewport.getLLX()
    }

    @Override
    public void renderInWorld(ViewportAPI viewport) {
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
    public void postInput(float advance) {

    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String[] getScopes() {
        return new String[] { InputScope_360.ID };
    }
}
