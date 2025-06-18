package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.CharacterSheetReflector;
import ssms.controller.reflection.UIPanelReflector;

import java.util.ArrayList;
import java.util.List;

public class CharacterTabUI extends InputScreenBase {
    public static final String ID = "CharacterTab";
    CharacterSheetReflector characterSheetReflector;
    DirectionalUINavigator directionalUINavigator;

    @Override
    public void activate(Object ...args) {
        characterSheetReflector = (CharacterSheetReflector) args[0];

        //buttonRows = characterSheetReflector.getButtonRows();

        var characterSheetObj = characterSheetReflector.getCharacterSheetObj();
        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for(var btn : UIPanelReflector.getChildButtons(characterSheetObj, true)) {
            directionalObjects.add(new Pair<>(btn, null));
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
        addButtonPressHandler("T", LogicalButtons.X, new KeySender(Keyboard.KEY_T, 'T'));
        //indicators.add(new Pair<>(Indicators.Select, "Re-assign skills"));
        addButtonPressHandler("Select fleet tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_F, 'f'));
        ControllerCrosshairRenderer.getControllerRenderer().disable();
    }

    @Override
    public void deactivate() {
        clearHandlers();
        ControllerCrosshairRenderer.getControllerRenderer().enable();
    }

    @Override
    public void preInput(float advance) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.CHARACTER) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
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
