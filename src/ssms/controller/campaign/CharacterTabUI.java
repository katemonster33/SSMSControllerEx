package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.reflection.CharacterSheetReflector;

import java.util.ArrayList;
import java.util.List;

public class CharacterTabUI extends InputScreenBase {
    public static final String ID = "CharacterTab";
    CharacterSheetReflector characterSheetReflector;
    CampaignScope campaignScope;
    List<List<ButtonAPI>> buttonRows;
    HandlerController controller;
    int rowSelected = -1, buttonSelected = -1;
    ButtonAPI selectedBtn;
    List<Pair<Indicators, String>> indicators = new ArrayList<>();

    public CharacterTabUI() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.LeftStick, "Navigate list"));
        indicators.add(new Pair<>(Indicators.A, "Select"));
        indicators.add(new Pair<>(Indicators.B, "Close"));
        indicators.add(new Pair<>(Indicators.Start, "Confirm"));
        indicators.add(new Pair<>(Indicators.Y, "Reset"));
        indicators.add(new Pair<>(Indicators.Select, "Re-assign skills"));
        indicators.add(new Pair<>(Indicators.BumperRight, "Select fleet tab"));
    }

    @Override
    public void activate(Object ...args) {
        characterSheetReflector = (CharacterSheetReflector) args[0];

        buttonRows = characterSheetReflector.getButtonRows();

        controller = SSMSControllerModPluginEx.controller;

        campaignScope = (CampaignScope) InputScreenManager.getInstance().getCurrentScope();
        campaignScope.refreshSelectedIndex();
        ControllerCrosshairRenderer.getControllerRenderer().disable();
    }

    @Override
    public void deactivate() {
        ControllerCrosshairRenderer.getControllerRenderer().enable();
    }

    void selectNewBtn() {
        if(selectedBtn != null) {
            selectedBtn.unhighlight();
        }
        selectedBtn = buttonRows.get(rowSelected).get(buttonSelected);
        selectedBtn.highlight();
        float mouseX = selectedBtn.getPosition().getCenterX(), mouseY = selectedBtn.getPosition().getCenterY();
        InputShim.mouseMove((int) mouseX, (int) mouseY);
    }

    @Override
    public void preInput(float advance) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.CHARACTER) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
        }
        if(rowSelected == -1 || buttonSelected == -1) {
            if(!buttonRows.isEmpty()) {
                rowSelected = buttonSelected = 0;
                selectedBtn = buttonRows.get(0).get(0);
                selectedBtn.highlight();
            }
        }
        if(controller.getButtonEvent(HandlerController.Buttons.LeftStickDown) == 1) {
            if(rowSelected < buttonRows.size() - 1) {
                rowSelected++;
            }
            if(buttonSelected > buttonRows.get(rowSelected).size()) {
                buttonSelected = buttonRows.get(rowSelected).size() - 1;
            }
            selectNewBtn();
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickUp) == 1) {
            if(rowSelected > 0) {
                rowSelected--;
            }
            if(buttonSelected > buttonRows.get(rowSelected).size()) {
                buttonSelected = buttonRows.get(rowSelected).size() - 1;
            }
            selectNewBtn();
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickLeft) == 1) {
            if(buttonSelected > 0) {
                buttonSelected--;
            }
            selectNewBtn();
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickRight) == 1) {
            if(buttonSelected < (buttonRows.get(rowSelected).size() - 1)) {
                buttonSelected++;
            }
            selectNewBtn();
        } else if(controller.getButtonEvent(HandlerController.Buttons.Y) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_G, 'G');
        } else if(controller.getButtonEvent(HandlerController.Buttons.X) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_T, 'T');
        } else if(controller.getButtonEvent(HandlerController.Buttons.A) == 1) {
            if(selectedBtn != null) {
                float mouseX = selectedBtn.getPosition().getCenterX(), mouseY = selectedBtn.getPosition().getCenterY();
                InputShim.mouseMove((int) mouseX, (int) mouseY);
                InputShim.mouseDownUp((int) mouseX, (int) mouseY, InputEventMouseButton.LEFT);
            }
        } else if(controller.getButtonEvent(HandlerController.Buttons.B) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
        } else if(controller.getButtonEvent(HandlerController.Buttons.BumperRight) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_F, 'f');
        }
    }

    @Override
    public UIPanelAPI getPanelForIndicators() {
        return (UIPanelAPI) characterSheetReflector.getCoreUIAPI();
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String[] getScopes() {
        return new String[]{ CampaignScope.ID };
    }
}
