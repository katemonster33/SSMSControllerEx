package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.CodexUI;
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
    List<DirectionalUINavigator.NavigationObject> directionalObjects = new ArrayList<>();

    @Override
    public void activate(Object ...args) {
        // if no args, means we are returning from message box or codex
        if(args.length > 0) {
            characterSheetReflector = (CharacterSheetReflector) args[0];
        }
        indicators = null;

        lastFrameNumChildren = new UIPanelReflector((UIPanelAPI) characterSheetReflector.getCoreUIAPI()).getChildItems().size();
        getPanelNavigatables(characterSheetReflector, directionalObjects, new ArrayList<>());
        directionalUINavigator = new DirectionalUINavigator(directionalObjects);
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = new ArrayList<>();
            addDirectionalUINavigator(directionalUINavigator);
            addButtonPressHandler("Close", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
            //indicators.add(new Pair<>(Indicators.Start, "Confirm"));
            addButtonPressHandler("Reset", LogicalButtons.X, new KeySender(Keyboard.KEY_T, 't'));
            addButtonPressHandler("Open Codex", LogicalButtons.Y, new KeySender(Keyboard.KEY_F2));
            //indicators.add(new Pair<>(Indicators.Select, "Re-assign skills"));
            addButtonPressHandler("Select fleet tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_F, 'f'));
        }
        return indicators;
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
        } else {
            var coreUiChildren = new UIPanelReflector((UIPanelAPI) characterSheetReflector.getCoreUIAPI()).getChildPanels();
            if(coreUiChildren.size() != lastFrameNumChildren) {
                for (UIPanelAPI childPanel : coreUiChildren) {
                    if (MessageBoxReflector.isMsgBox(childPanel)) {
                        InputScreenManager.getInstance().transitionToScreen(MessageBoxScreen.ID, new MessageBoxReflector(childPanel), getId());
                        return;
                    }
                }
            }
            lastFrameNumChildren = coreUiChildren.size();
        }
        if(isCodexOpen()) {
            InputScreenManager.getInstance().transitionDelayed(CodexUI.ID, getId());
        }

        List<DirectionalUINavigator.NavigationObject> directionalObjectsTmp = new ArrayList<>();
        getPanelNavigatables(characterSheetReflector, directionalObjectsTmp, new ArrayList<>());
        if(directionalObjectsTmp.size() != directionalObjects.size()) {
            directionalObjects = directionalObjectsTmp;
            directionalUINavigator.setNavigationObjects(directionalObjects);
        }
        directionalUINavigator.advance(advance);
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
