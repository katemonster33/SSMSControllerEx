package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.CodexUI;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.ScrollPanelReflector;
import ssms.controller.reflection.UIPanelReflector;

import java.util.ArrayList;
import java.util.List;

public class CommandTabUI  extends InputScreenBase {
    public static final String ID = "CommandTab";
    UIPanelReflector commandPanelReflector;
    List<DirectionalUINavigator.NavigationObject> tabNavItems = new ArrayList<>();
    DirectionalUINavigator directionalUINavigator;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if (indicators == null) {
            indicators = new ArrayList<>();
            addDirectionalUINavigator(directionalUINavigator);
            addButtonPressHandler("Return to campaign view", LogicalButtons.B, new KeySender(Keyboard.KEY_B, 'b'));
            addButtonPressHandler("Select intel tab", LogicalButtons.BumperLeft, new KeySender(Keyboard.KEY_E, 'e'));
            List<DirectionalUINavigator.NavigationObject> tmpNavObjects = new ArrayList<>();
            List<ScrollPanelReflector> scrollers = new ArrayList<>();
            getPanelNavigatables(commandPanelReflector, tmpNavObjects, scrollers);
            directionalUINavigator.setNavigationObjects(tmpNavObjects);
            directionalUINavigator.clearScrollPanels();
            for(var scroller : scrollers) {
                directionalUINavigator.addScrollPanel(scroller);
            }
        }
        return indicators;
    }

    @Override
    public void activate(Object... args) {
        if(args.length > 0) {
            commandPanelReflector = (UIPanelReflector) args[0];
        }
        indicators = null;
        directionalUINavigator = new DirectionalUINavigator(new ArrayList<>()) {
            @Override
            public void onSelect(NavigationObject directionalObject) {
                super.onSelect(directionalObject);
                if (directionalObject.tag instanceof ScrollPanelReflector scrollPanelReflector) {
                    scrollPanelReflector.ensureVisible(directionalObject.component);
                }
            }
        };
    }

    @Override
    public void preInput(float amount) {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.OUTPOSTS) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
        }
        if(isCodexOpen()) {
            InputScreenManager.getInstance().transitionDelayed(CodexUI.ID, getId());
        }

        List<DirectionalUINavigator.NavigationObject> tmpNavObjects = new ArrayList<>();
        getPanelNavigatables(commandPanelReflector, tmpNavObjects, new ArrayList<>());
        if(tmpNavObjects.size() != tabNavItems.size()) {
            directionalUINavigator.setNavigationObjects(tmpNavObjects);
            tabNavItems = tmpNavObjects;
        }

        directionalUINavigator.advance(amount);
    }
}
