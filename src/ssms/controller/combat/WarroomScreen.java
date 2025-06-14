package ssms.controller.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.reflection.*;

import java.util.ArrayList;

public class WarroomScreen extends InputScreenBase {
    public static final String ID = "Warroom";
    CombatStateReflector csr;
    boolean isMovingMap = false;
    int selectedButtonGroup = -1, selectedButton = -1;
    boolean selectingButton = false;
    WarroomReflector warroomReflector;
    ViewportAPI viewportAPI;
    Vector2f desiredMousePos = null;
    final float mouseMoveFactor = 4.f;

    @Override
    public void activate(Object ... args) {
        csr = CombatStateReflector.GetInstance();
        warroomReflector = new WarroomReflector(csr.getWarroom());
        viewportAPI = Global.getCombatEngine().getViewport();

        selectedButtonGroup = selectedButton = -1;
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.Start, "Pause"));
        indicators.add(new Pair<>(Indicators.Select, "Show video feed"));
        addMapHandler(viewportAPI);
        indicators.add(new Pair<>(Indicators.X, "Target point/object"));
        indicators.add(new Pair<>(Indicators.Y, "Return to deployment screen"));
        addDigitalJoystickHandler("Select buttons", Joystick.DPad, new DirectionalUINavigator());
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

        ReadableVector2f leftStick = controller.getJoystick(Joystick.Left);
        if(leftStick.getX() != 0 || leftStick.getY() != 0) {
            if(selectingButton) {
                selectedButton = selectedButtonGroup = -1;
                desiredMousePos = null;
                selectingButton = false;
            }
            if(desiredMousePos == null) {
                desiredMousePos = new Vector2f(viewportAPI.convertWorldXtoScreenX(viewportAPI.getCenter().getX()), viewportAPI.convertWorldYtoScreenY(viewportAPI.getCenter().getY()));
            } else {
                desiredMousePos.set(desiredMousePos.getX() + (leftStick.getX() * mouseMoveFactor), desiredMousePos.getY() - (leftStick.getY() * mouseMoveFactor));
            }
            InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
        }
        if(desiredMousePos != null) {
            if(controller.getButtonEvent(LogicalButtons.LeftStickButton) == 1) {
                if(!isMovingMap) {
                    InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
                } else {
                    InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
                }
                isMovingMap = !isMovingMap;
            }
            if(controller.getButtonEvent(LogicalButtons.A) == 1) {
                InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
            } else if(controller.getButtonEvent(LogicalButtons.A) == -1) {
                InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
            }
            if(controller.getButtonEvent(LogicalButtons.X) == 1) {
                InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
                InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
            }
        }

        if(controller.getButtonEvent(LogicalButtons.BumperLeft) == 1 ||
                controller.getButtonEvent(LogicalButtons.BumperRight) == 1 ||
                controller.getButtonEvent(LogicalButtons.LeftTrigger) == 1 ||
                controller.getButtonEvent(LogicalButtons.RightTrigger) == 1) {
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
                if(controller.getButtonEvent(LogicalButtons.BumperLeft) == 1) {
                    if(selectedButton > 0) selectedButton--;
                } else if(controller.getButtonEvent(LogicalButtons.BumperRight) == 1) {
                    if(selectedButton < buttonGroups.get(selectedButtonGroup).size() - 1) selectedButton++;
                } else if(controller.getButtonEvent(LogicalButtons.LeftTrigger) == 1) {
                    if(selectedButtonGroup > 0) selectedButtonGroup--;
                    selectedButton = 0;
                } else if(controller.getButtonEvent(LogicalButtons.RightTrigger) == 1) {
                    if(selectedButtonGroup < buttonGroups.size() - 1) selectedButtonGroup++;
                    selectedButton = 0;
                }
                var btnPos = buttonGroups.get(selectedButtonGroup).get(selectedButton).getPosition();
                desiredMousePos.set(btnPos.getCenterX(), btnPos.getCenterY());
                InputShim.mouseMove((int) btnPos.getCenterX(), (int) btnPos.getCenterY());
            }
        }

        if(controller.getButtonEvent(LogicalButtons.Start) == 1) {
            Global.getCombatEngine().setPaused(!Global.getCombatEngine().isPaused());
        } else if(controller.getButtonEvent(LogicalButtons.Select) == 1) {
            csr.HideWarroom();
            csr.SetVideoFeedToPlayerShip();
            //Global.getCombatEngine().sho
        } else if(controller.getButtonEvent(LogicalButtons.Y) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_G, 'G');
        }
    }
}
