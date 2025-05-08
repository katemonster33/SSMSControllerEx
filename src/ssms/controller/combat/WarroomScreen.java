package ssms.controller.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.CombatTaskManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.generic.MessageBoxScreen;
import ssms.controller.reflection.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class WarroomScreen extends InputScreenBase {
    public static final String ID = "Warroom";
    CombatStateReflector csr;
    HandlerController controller;
    boolean isMovingMap = false;
    BattleScope scope;
    WarroomReflector warroomReflector;
    List<Pair<Indicators, String>> indicators;
    DeployedFleetMemberAPI deployedFleetMember = null;
    CombatFleetManagerAPI playerFleetManager;
    CombatTaskManagerAPI playerTaskManager;
    List<DeployedFleetMemberAPI> deployedPlayerShips;
    ViewportAPI viewportAPI;
    Vector2f desiredMousePos = null;
    final float mouseMoveFactor = 4.f;
    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }

    @Override
    public void activate(Object ... args) {
        csr = CombatStateReflector.GetInstance();
        warroomReflector = new WarroomReflector(csr.getWarroom());
        controller = SSMSControllerModPluginEx.controller;
        viewportAPI = Global.getCombatEngine().getViewport();

        playerFleetManager = Global.getCombatEngine().getFleetManager(FleetSide.PLAYER);
        playerTaskManager = playerFleetManager.getTaskManager(false);
        deployedPlayerShips = playerFleetManager.getAllEverDeployedCopy();
        deployedFleetMember = null;

        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.Start, "Pause"));
        indicators.add(new Pair<>(Indicators.Select, "Show video feed"));
        indicators.add(new Pair<>(Indicators.LeftStick, "Move cursor"));
        indicators.add(new Pair<>(Indicators.LeftStick, "(press) Toggle map move"));
        //indicators.add(new Pair<>(Indicators.RightStick, "Move map"));
        indicators.add(new Pair<>(Indicators.A, "Select object/button"));
        indicators.add(new Pair<>(Indicators.X, "Target point/object"));
        indicators.add((new Pair<>(Indicators.BumperLeft, "Select prev allied ship")));
        indicators.add((new Pair<>(Indicators.BumperRight, "Select next allied ship")));
        indicators.add(new Pair<>(Indicators.LeftTrigger, "Select hostile ships"));
        indicators.add(new Pair<>(Indicators.RightTrigger, "Select hostile ships"));
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String[] getScopes() {
        return new String[]{ BattleScope.ID };
    }

    @Override
    public void preInput(float advance) {
        if(!Global.getCombatEngine().getCombatUI().isShowingCommandUI()) {
            InputScreenManager.getInstance().transitionDelayed(BattleSteeringScreen.ID);
            return;
        }
        if(Global.getCombatEngine().getCombatUI().isShowingDeploymentDialog()) {
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, MessageBoxScreen.ID, new Object[]{});
            return;
        }

        if(desiredMousePos != null && !InputShim.hasMouseControl()) {
            desiredMousePos = null;
        }

        ReadableVector2f leftStick = controller.getLeftStick();
        if(leftStick.getX() != 0 || leftStick.getY() != 0) {
            if(desiredMousePos == null) {
                desiredMousePos = new Vector2f(viewportAPI.convertWorldXtoScreenX(viewportAPI.getCenter().getX()), viewportAPI.convertWorldYtoScreenY(viewportAPI.getCenter().getY()));
            } else {
                desiredMousePos.set(desiredMousePos.getX() + (leftStick.getX() * mouseMoveFactor), desiredMousePos.getY() + (leftStick.getY() * mouseMoveFactor));
            }
            InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
        }
        if(desiredMousePos != null) {
            if(controller.getButtonEvent(HandlerController.Buttons.LeftStickButton) == 1 && controller.isButtonLeftStickPressed()) {
                if(!isMovingMap) {
                    InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
                } else {
                    InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
                }
                isMovingMap = !isMovingMap;
            }
            if(controller.getButtonEvent(HandlerController.Buttons.A) == 1) {
                if(controller.isButtonAPressed()) {
                    InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
                } else {
                    InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
                }
            }
            if(controller.getButtonEvent(HandlerController.Buttons.X) == 1) {
                if(controller.isButtonXPressed()) {
                    InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
                } else {
                    InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
                }
            }
        }


        if(controller.getButtonEvent(HandlerController.Buttons.Start) == 1) {
            Global.getCombatEngine().setPaused(!Global.getCombatEngine().isPaused());
        } else if(controller.getButtonEvent(HandlerController.Buttons.Select) == 1) {
            csr.HideWarroom();
            csr.SetVideoFeedToPlayerShip();
            //Global.getCombatEngine().sho
        } else if(controller.getButtonEvent(HandlerController.Buttons.BumperLeft) == 1 && controller.isButtonBumperLeftPressed()) {

            int selectedShipIdx = -1;
            if(deployedFleetMember != null) {
                selectedShipIdx = deployedPlayerShips.indexOf(deployedFleetMember);
            }
        }

    }

    @Override
    public void renderInWorld(ViewportAPI viewport) {
    }
}
