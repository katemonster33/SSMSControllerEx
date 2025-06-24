package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import ssms.controller.*;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.MessageBoxScreen;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.CharacterSheetReflector;
import ssms.controller.reflection.MessageBoxReflector;
import ssms.controller.reflection.UIPanelReflector;

import java.util.ArrayList;
import java.util.List;

public class CharacterTabUI extends InputScreenBase {
    public static final String ID = "CharacterTab";
    CharacterSheetReflector characterSheetReflector;
    DirectionalUINavigator directionalUINavigator;
    int lastFrameNumChildren;

    @Override
    public void activate(Object ...args) {
        // if no args, means we are returning from message box
        if(args.length > 0) {
            characterSheetReflector = (CharacterSheetReflector) args[0];
        }

        //buttonRows = characterSheetReflector.getButtonRows();

        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for(var btn : characterSheetReflector.getChildButtons(true)) {
            if (btn.getPosition().getX() >= 0 && btn.getPosition().getX() <= Display.getWidth() &&
                    btn.getPosition().getY() >= 0 && btn.getPosition().getY() <= Display.getHeight()) {
                directionalObjects.add(new Pair<>(btn, null));
            }
        }
        indicators = new ArrayList<>();
        directionalUINavigator = new DirectionalUINavigator(directionalObjects);
        addDigitalJoystickHandler("Navigate", Joystick.DPad, directionalUINavigator);
        addButtonPressHandler("Select", LogicalButtons.A, (float advance) -> {
            if(directionalUINavigator.getSelected() != null) {
                var pos = directionalUINavigator.getSelected().one.getPosition();
                InputShim.mouseMove((int) pos.getCenterX(), (int) pos.getCenterY());
                InputShim.mouseDownUp((int) pos.getCenterX(), (int) pos.getCenterY(), InputEventMouseButton.LEFT);
            }
        });
        addButtonPressHandler("Close", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        //indicators.add(new Pair<>(Indicators.Start, "Confirm"));
        addButtonPressHandler("Reset", LogicalButtons.Y, new KeySender(Keyboard.KEY_G, 'G'));
        addButtonPressHandler("Open Codex", LogicalButtons.Y, new KeySender(Keyboard.KEY_F2));
        //indicators.add(new Pair<>(Indicators.Select, "Re-assign skills"));
        addButtonPressHandler("Select fleet tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_F, 'f'));
        ControllerCrosshairRenderer.getControllerRenderer().disable();
        lastFrameNumChildren = new UIPanelReflector((UIPanelAPI) characterSheetReflector.getCoreUIAPI()).getChildItems().size();
    }

    @Override
    public void deactivate() {
        clearHandlers();
        //ControllerCrosshairRenderer.getControllerRenderer().enable();
    }

    @Override
    public void preInput(float advance) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.CHARACTER) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
        } else {
            var coreUiChildren = new UIPanelReflector((UIPanelAPI) characterSheetReflector.getCoreUIAPI()).getChildItems();
            if(coreUiChildren.size() > lastFrameNumChildren) {
                for(int index = lastFrameNumChildren; index < coreUiChildren.size(); index++) {
                    if(coreUiChildren.get(index) instanceof  UIPanelAPI childPanel) {
                        MessageBoxReflector mbr = MessageBoxReflector.TryGet(childPanel);
                        if(mbr != null) {
                            InputScreenManager.getInstance().transitionToScreen(MessageBoxScreen.ID, mbr, getId());
                            return;
                        }
                    }
                }
            }
            lastFrameNumChildren = coreUiChildren.size();
        }
    }

    @Override
    public UIPanelAPI getPanelForIndicators() {
        return (UIPanelAPI) characterSheetReflector.getCoreUIAPI();
    }

    @Override
    public String getId() {
        return ID;
    }
}
