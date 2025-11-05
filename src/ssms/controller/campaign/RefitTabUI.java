package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.CodexUI;
import ssms.controller.generic.MessageBoxScreen;
import ssms.controller.inputhelper.ButtonPressOrHoldHandler;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.*;

import java.util.ArrayList;
import java.util.List;

public class RefitTabUI extends InputScreenBase {
    public static final String ID = "RefitTab";
    UIPanelReflector refitPanel;
    List<DirectionalUINavigator.NavigationObject> directionalObjects;
    DirectionalUINavigator refitNavigator;
    InteractionDialogReflector interactionDialogReflector;
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = new ArrayList<>();
            List<ScrollPanelReflector> scrollers = new ArrayList<>();
            directionalObjects = new ArrayList<>();
            getPanelNavigatables(refitPanel, directionalObjects, scrollers);
            //directionalObjects = new ArrayList<>(refitPanel.getChildButtons(true).stream().map(DirectionalUINavigator.NavigationObject::new).toList());
            for(var scroller : scrollers) {
                refitNavigator.addScrollPanel(scroller);
            }
            refitNavigator.setNavigationObjects(directionalObjects);
            addDirectionalUINavigator(refitNavigator);
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
        if(args.length > 0) {
            refitPanel = (UIPanelReflector) args[0];
        }
        refitNavigator = new DirectionalUINavigator(new ArrayList<>()){
            @Override
            public void onSelect(NavigationObject selectedObj) {
                super.onSelect(selectedObj);
                if(selectedObj.tag instanceof  ScrollPanelReflector scrollPanelReflector) {
                    scrollPanelReflector.ensureVisible(selectedObj.component);
                }
            }
        };
        interactionDialogReflector = InteractionDialogReflector.getCurrentInstance();
        indicators = null;
    }

    @Override
    public void preInput(float amount) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.REFIT) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
        }
        if(isCodexOpen()) {
            InputScreenManager.getInstance().transitionDelayed(CodexUI.ID, getId());
        }
        if(interactionDialogReflector != null && !interactionDialogReflector.isCoreUiOpen()) {
            InputScreenManager.getInstance().transitionDelayed(DialogUI.ID);
        }
        if(isMessageBoxShown(refitPanel)) return;
        List<DirectionalUINavigator.NavigationObject> directionalObjectsTmp = new ArrayList<>(); //new ArrayList<>(refitPanel.getChildButtons(true).stream().map(DirectionalUINavigator.NavigationObject::new).toList());
        getPanelNavigatables(refitPanel, directionalObjectsTmp, new ArrayList<>());
        if(directionalObjectsTmp.size() != directionalObjects.size()) {
            directionalObjects = directionalObjectsTmp;
            refitNavigator.setNavigationObjects(directionalObjects);
        }
        refitNavigator.advance(amount);
    }
}
