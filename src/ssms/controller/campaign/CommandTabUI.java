package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
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

import java.util.ArrayList;
import java.util.List;

public class CommandTabUI  extends InputScreenBase {
    public static final String ID = "CommandTab";
    List<Pair<Indicators, String>> indicators;
    ViewportAPI viewportAPI;
    Vector2f desiredMousePos = null;
    float mouseMoveFactor = 4.f;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if (indicators == null) {
            indicators = new ArrayList<>();
            indicators.add(new Pair<>(Indicators.LeftStick, "Navigate"));
            indicators.add(new Pair<>(Indicators.A, "Select"));
            indicators.add(new Pair<>(Indicators.B, "Return to campaign view"));
            indicators.add(new Pair<>(Indicators.BumperLeft, "Select intel tab"));
        }
        return indicators;
    }

    @Override
    public String[] getScopes() {
        return new String[]{CampaignScope.ID};
    }

    @Override
    public void activate(Object... args) {
        viewportAPI = Global.getSector().getViewport();
    }

    @Override
    public void preInput(float amount) {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.OUTPOSTS) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
        }

        ReadableVector2f leftStick = controller.getJoystick(Joystick.Left);
        if (leftStick.getX() != 0 || leftStick.getY() != 0) {
            if (desiredMousePos == null) {
                desiredMousePos = new Vector2f((int) viewportAPI.convertWorldXtoScreenX(viewportAPI.getCenter().getX()), (int) viewportAPI.convertWorldYtoScreenY(viewportAPI.getCenter().getY()));
            } else {
                desiredMousePos.set(desiredMousePos.getX() + (leftStick.getX() * mouseMoveFactor), desiredMousePos.getY() + (leftStick.getY() * mouseMoveFactor));
            }
            InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
        }

        if (desiredMousePos != null && controller.getButtonEvent(LogicalButtons.A) == 1) {
            InputShim.mouseDownUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
        } else if (controller.getButtonEvent(LogicalButtons.B) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
        } else if (controller.getButtonEvent(LogicalButtons.BumperLeft) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_E, 'e');
        }
    }
}
