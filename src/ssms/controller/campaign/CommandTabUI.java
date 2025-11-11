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
import ssms.controller.reflection.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandTabUI  extends InputScreenBase {
    public static final String ID = "CommandTab";
    UIPanelReflector commandPanelReflector;
    UIPanelReflector campaignStatePanelReflector;
    List<DirectionalUINavigator.NavigationObject> tabNavItems = new ArrayList<>();
    List<ScrollPanelReflector> scrollPanelReflectors = new ArrayList<>();
    List<UIPanelAPI> mapReflectors = new ArrayList<>();
    DirectionalUINavigator directionalUINavigator;
    InteractionDialogReflector interactionDialogReflector;

    Pair<Integer, Character>[] tabInputs = new Pair[] {
            new Pair<>(Keyboard.KEY_C, 'c'),
            new Pair<>(Keyboard.KEY_F, 'f'),
            new Pair<>(Keyboard.KEY_R, 'r'),
            new Pair<>(Keyboard.KEY_I, 'i'),
            new Pair<>(Keyboard.KEY_TAB, '\0'),
            new Pair<>(Keyboard.KEY_E, 'e'),
            new Pair<>(Keyboard.KEY_D, 'd'),
    };

    List<CoreUITabId> tabs = Arrays.asList(CoreUITabId.values());

    @Override
    public String getId() {
        return ID;
    }

    void selectNextTab(float advance) {
        var selectedTab = Global.getSector().getCampaignUI().getCurrentCoreTab();
        if(selectedTab != null) {
            int selectedTabIdx = tabs.indexOf(selectedTab);
            selectedTabIdx++;
            if(selectedTabIdx > tabs.size()) selectedTabIdx = 0;
            InputShim.keyDownUp(tabInputs[selectedTabIdx].one, tabInputs[selectedTabIdx].two);
        }
    }

    void selectPrevTab(float advance) {
        var selectedTab = Global.getSector().getCampaignUI().getCurrentCoreTab();
        if(selectedTab != null) {
            int selectedTabIdx = tabs.indexOf(selectedTab);
            selectedTabIdx--;
            if(selectedTabIdx < 0) selectedTabIdx = tabs.size() - 1;
            InputShim.keyDownUp(tabInputs[selectedTabIdx].one, tabInputs[selectedTabIdx].two);
        }
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if (indicators == null) {
            indicators = new ArrayList<>();
            addDirectionalUINavigator(directionalUINavigator);
            addButtonPressHandler("Return to campaign view", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
            addButtonPressHandler("Select previous campaign tab", LogicalButtons.BumperLeft, this::selectPrevTab);
            addButtonPressHandler("Select next campaign tab", LogicalButtons.BumperRight, this::selectNextTab);
            tabNavItems.clear();
            scrollPanelReflectors.clear();
            mapReflectors.clear();
            getPanelNavigatables(campaignStatePanelReflector, tabNavItems, scrollPanelReflectors, mapReflectors);
            directionalUINavigator.setNavigationObjects(tabNavItems);
            directionalUINavigator.clearScrollPanels();
            for(var scroller : scrollPanelReflectors) {
                directionalUINavigator.addScrollPanel(scroller);
            }
            if(!mapReflectors.isEmpty()) {
                directionalUINavigator.setMapComponent(mapReflectors.get(0));
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
        interactionDialogReflector = InteractionDialogReflector.getCurrentInstance();
        campaignStatePanelReflector = new UIPanelReflector(CampaignStateReflector.GetInstance().getScreenPanel());
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
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() == null) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
        } else if(Global.getSector().getCampaignUI().getCurrentCoreTab() == CoreUITabId.CARGO) {
            CoreUIReflector coreUiReflector;
            if(interactionDialogReflector != null) {
                coreUiReflector = new CoreUIReflector(interactionDialogReflector.getCoreUI(Global.getSector().getCampaignUI().getCurrentInteractionDialog()));
            } else {
                coreUiReflector = new CoreUIReflector(CampaignStateReflector.GetInstance().getCoreUI());
            }
            InputScreenManager.getInstance().transitionDelayed(TradeScreen.ID, TradeUiReflector.TryGet(coreUiReflector.getCoreUIAPI(), new UIPanelReflector(coreUiReflector.getCurrentTab())));
            return;
        }

        if(interactionDialogReflector != null && !interactionDialogReflector.isCoreUiOpen()) {
            InputScreenManager.getInstance().transitionDelayed(DialogUI.ID);
        }

        List<DirectionalUINavigator.NavigationObject> tmpNavObjects = new ArrayList<>();
        List<ScrollPanelReflector> scrollersTmp = new ArrayList<>();
        List<UIPanelAPI> mapsTmp = new ArrayList<>();
        getPanelNavigatables(campaignStatePanelReflector, tmpNavObjects, scrollersTmp, mapsTmp);
        if(tmpNavObjects.size() != tabNavItems.size() || mapsTmp.size() != mapReflectors.size() || scrollersTmp.size() != scrollPanelReflectors.size() ) {
            directionalUINavigator.setNavigationObjects(tmpNavObjects);
            directionalUINavigator.clearScrollPanels();
            scrollPanelReflectors = scrollersTmp;
            mapReflectors = mapsTmp;
            for(var scroller : scrollPanelReflectors) {
                directionalUINavigator.addScrollPanel(scroller);
            }
            if(!mapReflectors.isEmpty()) {
                directionalUINavigator.setMapComponent(mapReflectors.get(0));
            }
            tabNavItems = tmpNavObjects;
        }

        directionalUINavigator.advance(amount);
    }
}
