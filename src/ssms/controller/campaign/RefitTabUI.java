package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.ButtonPressOrHoldHandler;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.UIPanelReflector;

import java.util.ArrayList;
import java.util.List;

public class RefitTabUI extends InputScreenBase {
    public static final String ID = "RefitTab";
    List<Pair<Indicators, String>> indicators;
    ViewportAPI viewportAPI;
    UIPanelReflector refitPanel;
    Vector2f desiredMousePos = null;
    float mouseMoveFactor = 4.f;
    List<Pair<UIComponentAPI, Object>> directionalObjects;
    DirectionalUINavigator refitNavigator;
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = new ArrayList<>();
            directionalObjects = new ArrayList<>();
            for(var pnl : refitPanel.getChildButtons(true)) {
                directionalObjects.add(new Pair<>(pnl, null));
            }
            refitNavigator = new DirectionalUINavigator(directionalObjects);
            addDigitalJoystickHandler("Navigate", Joystick.DPad, refitNavigator);
            addButtonPressHandler("Select", LogicalButtons.A, (float advance) -> {
                if(refitNavigator.getSelected() != null) {
                    var pos = refitNavigator.getSelected().one.getPosition();
                    InputShim.mouseDownUp((int) pos.getX(), (int) pos.getY(), InputEventMouseButton.LEFT);
                }
            });
            addButtonPressOrHoldHandler("More Info", "Open Codex", LogicalButtons.Y, new ButtonPressOrHoldHandler() {
                @Override
                public void performHoldAction(float advance) {
                    InputShim.keyDownUp(Keyboard.KEY_F2, '\0');
                }

                @Override
                public void performPressAction(float advance) {
                    InputShim.keyDownUp(Keyboard.KEY_F1, '\0');
                }
            });
            addButtonPressHandler("Return to campaign view", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
            addButtonPressHandler("Select fleet tab", LogicalButtons.BumperLeft, new KeySender(Keyboard.KEY_F, 'f'));
            addButtonPressHandler("Select cargo tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_I, 'i'));
        }
        return indicators;
    }

    @Override
    public void activate(Object ... args) {
        viewportAPI = Global.getSector().getViewport();
        refitPanel = (UIPanelReflector) args[0];
        indicators = null;
    }

    @Override
    public void preInput(float amount) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.REFIT) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
        }

        List<Pair<UIComponentAPI, Object>> directionalObjectsTmp = new ArrayList<>();
        for(var pnl : refitPanel.getChildButtons(true)) {
            directionalObjectsTmp.add(new Pair<>(pnl, null));
        }
        if(directionalObjectsTmp.size() != directionalObjects.size()) {
            directionalObjects = directionalObjectsTmp;
            refitNavigator.setNavigationObjects(directionalObjects);
        }
    }
}
