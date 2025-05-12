package ssms.controller.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.CombatTaskManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
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
    int selectedButtonGroup = -1, selectedButton = -1;
    boolean selectingButton = false;
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
    boolean isLeftMouseDown = false;
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

        selectedButtonGroup = selectedButton = -1;
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.Start, "Pause"));
        indicators.add(new Pair<>(Indicators.Select, "Show video feed"));
        indicators.add(new Pair<>(Indicators.LeftStick, "Move cursor"));
        indicators.add(new Pair<>(Indicators.LeftStick, "(press) Toggle map move"));
        //indicators.add(new Pair<>(Indicators.RightStick, "Move map"));
        indicators.add(new Pair<>(Indicators.A, "Select object/button"));
        indicators.add(new Pair<>(Indicators.X, "Target point/object"));
        indicators.add(new Pair<>(Indicators.Y, "Return to deployment screen"));
        indicators.add((new Pair<>(Indicators.BumperLeft, "Select prev button")));
        indicators.add((new Pair<>(Indicators.BumperRight, "Select next button")));
        indicators.add(new Pair<>(Indicators.LeftTrigger, "Select prev button group"));
        indicators.add(new Pair<>(Indicators.RightTrigger, "Select next button group"));
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
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, BattleDeploymentScreen.ID, new Object[]{ csr });
            return;
        }

        if(desiredMousePos != null && !InputShim.hasMouseControl()) {
            desiredMousePos = null;
        }

        ReadableVector2f leftStick = controller.getLeftStick();
        if(leftStick.getX() != 0 || leftStick.getY() != 0) {
            if(selectingButton) {
                selectedButton = selectedButtonGroup = -1;
                desiredMousePos = null;
                selectingButton = false;
            }
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
            if(!isLeftMouseDown && controller.isButtonAPressed()) {
                InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
                isLeftMouseDown = true;
            } else if(isLeftMouseDown && !controller.isButtonAPressed()) {
                InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
                isLeftMouseDown = false;
            }
            if(controller.getButtonEvent(HandlerController.Buttons.X) == 1) {
                InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
                InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
            }
        }

        if(controller.getButtonEvent(HandlerController.Buttons.BumperLeft) == 1 ||
                controller.getButtonEvent(HandlerController.Buttons.BumperRight) == 1 ||
                controller.getButtonEvent(HandlerController.Buttons.LeftTrigger) == 1 ||
                controller.getButtonEvent(HandlerController.Buttons.RightTrigger) == 1) {
            selectingButton = true;
            var buttonGroups = warroomReflector.getButtonGroups();
            if((selectedButtonGroup != -1 && selectedButton != -1) &&
                    (selectedButtonGroup >= buttonGroups.size() || selectedButton >= buttonGroups.get(selectedButtonGroup).size())) {
                selectedButtonGroup = selectedButton = -1;
            }
            if(selectedButtonGroup == -1 || selectedButton == -1) {
                if(!buttonGroups.isEmpty()) {
                    selectedButtonGroup = selectedButton = 0;
                    var btnPos = buttonGroups.get(selectedButtonGroup).get(selectedButton).getPosition();
                    desiredMousePos.set(btnPos.getCenterX(), btnPos.getCenterY());
                    InputShim.mouseMove((int) btnPos.getCenterX(), (int) btnPos.getCenterY());
                }
            } else {
                if(controller.getButtonEvent(HandlerController.Buttons.BumperLeft) == 1) {
                    if(selectedButton > 0) selectedButton--;
                } else if(controller.getButtonEvent(HandlerController.Buttons.BumperRight) == 1) {
                    if(selectedButton < buttonGroups.get(selectedButtonGroup).size() - 1) selectedButton++;
                } else if(controller.getButtonEvent(HandlerController.Buttons.LeftTrigger) == 1 &&
                        controller.isTriggerLeft()) {
                    if(selectedButtonGroup > 0) selectedButtonGroup--;
                    selectedButton = 0;
                } else if(controller.getButtonEvent(HandlerController.Buttons.RightTrigger) == 1 &&
                        controller.isTriggerRight()) {
                    if(selectedButtonGroup < buttonGroups.size() - 1) selectedButtonGroup++;
                    selectedButton = 0;
                }
                var btnPos = buttonGroups.get(selectedButtonGroup).get(selectedButton).getPosition();
                desiredMousePos.set(btnPos.getCenterX(), btnPos.getCenterY());
                InputShim.mouseMove((int) btnPos.getCenterX(), (int) btnPos.getCenterY());
            }
        }

        if(controller.getButtonEvent(HandlerController.Buttons.Start) == 1) {
            Global.getCombatEngine().setPaused(!Global.getCombatEngine().isPaused());
        } else if(controller.getButtonEvent(HandlerController.Buttons.Select) == 1) {
            csr.HideWarroom();
            csr.SetVideoFeedToPlayerShip();
            //Global.getCombatEngine().sho
        } else if(controller.getButtonEvent(HandlerController.Buttons.Y) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_G, 'G');
        }

    }

    @Override
    public void renderInWorld(ViewportAPI viewport) {
    }
}
