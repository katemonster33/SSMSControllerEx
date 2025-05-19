package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.reflection.MapReflector;

import java.util.ArrayList;
import java.util.List;

public class MapTabUI extends InputScreenBase {
    public final static String ID = "MapTab";
    ViewportAPI viewportAPI;
    HandlerController controller;
    List<Pair<Indicators, String>> indicators = null;
    boolean mouseDown = false;
    boolean movingMap = false;

    MapReflector mapReflector;
    Vector2f desiredMousePos;
    final float mouseMoveFactor = 4.f;

    @Override
    public String getId() {
        return ID;
    }

    public String[] getScopes() {
        return new String[] { CampaignScope.ID };
    }

    @Override
    public void activate(Object ... args) {
        controller = SSMSControllerModPluginEx.controller;
        viewportAPI = Global.getSector().getViewport();
        mapReflector = (MapReflector) args[0];
        desiredMousePos = null;
        mouseDown = movingMap = false;
    }

    @Override
    public void preInput(float amount) {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.MAP) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
        }

        ReadableVector2f leftStick = controller.getLeftStick();
        if (leftStick.getX() != 0 || leftStick.getY() != 0) {
            if (desiredMousePos == null) {
                desiredMousePos = new Vector2f(viewportAPI.convertWorldXtoScreenX(viewportAPI.getCenter().getX()), viewportAPI.convertWorldYtoScreenY(viewportAPI.getCenter().getY()));
            } else {
                desiredMousePos.set(desiredMousePos.getX() + (leftStick.getX() * mouseMoveFactor), desiredMousePos.getY() + (leftStick.getY() * mouseMoveFactor));
            }
            InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
        }
        if(!mouseDown && controller.getButtonEvent(HandlerController.Buttons.LeftStickButton) == 1) {
            if(!movingMap) {
                InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
            } else {
                InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
            }
            movingMap = !movingMap;
        }
        if(!movingMap) {
            if (controller.isButtonAPressed() && !mouseDown) {
                InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
                mouseDown = true;
            } else if (!controller.isButtonAPressed() && mouseDown) {
                InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
                mouseDown = false;
            }
            if (controller.getButtonEvent(HandlerController.Buttons.B) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
            } else if (controller.getButtonEvent(HandlerController.Buttons.LeftTrigger) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_Q, 'q');
            } else if (controller.getButtonEvent(HandlerController.Buttons.RightTrigger) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_W, 'w');
            }
        }

    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if (indicators == null) {
            indicators = new ArrayList<>();
            indicators.add(new Pair<>(Indicators.LeftStick, "Move cursor"));
            indicators.add(new Pair<>(Indicators.LeftStick, "(press) Toggle map move"));
            indicators.add(new Pair<>(Indicators.A, "Select point"));
            indicators.add(new Pair<>(Indicators.B, "Close dialog"));
            indicators.add(new Pair<>(Indicators.LeftTrigger, "Open sector view"));
            indicators.add(new Pair<>(Indicators.B, "Open system view"));
        }
        return indicators;
    }
}
