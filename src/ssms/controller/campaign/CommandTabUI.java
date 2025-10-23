package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.ScrollPanelReflector;
import ssms.controller.reflection.ScrollbarUiReflector;
import ssms.controller.reflection.UIComponentReflector;
import ssms.controller.reflection.UIPanelReflector;

import java.util.ArrayList;
import java.util.List;

public class CommandTabUI  extends InputScreenBase {
    public static final String ID = "CommandTab";
    ViewportAPI viewportAPI;
    Vector2f desiredMousePos = null;
    float mouseMoveFactor = 4.f;
    UIPanelReflector commandPanelReflector;
    List<ButtonAPI> tabButtons;
    List<UIPanelReflector> commandTabs;
    UIPanelReflector activeTab;
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
        viewportAPI = Global.getSector().getViewport();
        commandPanelReflector = (UIPanelReflector) args[0];
        tabButtons = commandPanelReflector.getChildButtons();
        commandTabs = commandPanelReflector.getChildPanels().stream().map(UIPanelReflector::new).toList();
        for(var pnl : commandTabs) {
            if(pnl.getFader().getBrightness() == 1.f) {
                activeTab = pnl;
                break;
            }
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

    void getPanelNavigatables(UIPanelReflector pnl, List<DirectionalUINavigator.NavigationObject> directionalObjects, List<ScrollPanelReflector> scrollers) {
        if( ScrollPanelAPI.class.isAssignableFrom(pnl.getPanel().getClass())) {
            ScrollPanelReflector scroller = new ScrollPanelReflector((ScrollPanelAPI) pnl.getPanel());
            scrollers.add(scroller);
            UIPanelReflector container = new UIPanelReflector(scroller.getContentContainer());
            if(scroller.getFader().getBrightness() != 1.f) {
                return;
            }
            for(var item : container.getChildItems()) {
                if(UIPanelAPI.class.isAssignableFrom(item.getClass()) && TagDisplayAPI.class.isAssignableFrom(item.getClass())) {
                    getPanelNavigatables(new UIPanelReflector((UIPanelAPI) item), directionalObjects, scrollers);
                } else if(UIComponentAPI.class.isAssignableFrom(item.getClass())) {
                    directionalObjects.add(new DirectionalUINavigator.NavigationObject((UIComponentAPI) item, scroller));
                }
            }
            for(var item : scroller.getChildItems()) {
                if(UIComponentAPI.class.isAssignableFrom(item.getClass()) && item != container.getPanel()) {
                    UIComponentReflector comp = new UIComponentReflector((UIComponentAPI) item);
                    if(((UIComponentAPI)item).getPosition().getWidth() > 0 && comp.getFader().getBrightness() == 1.f) {
                        directionalObjects.add(new DirectionalUINavigator.NavigationObject((UIComponentAPI)item));
                    }
                }
            }
        } else {
            for (var item : pnl.getChildItems()) {
                if (ButtonAPI.class.isAssignableFrom(item.getClass())) {
                    directionalObjects.add(new DirectionalUINavigator.NavigationObject((ButtonAPI) item));
                } else if (UIPanelAPI.class.isAssignableFrom(item.getClass())) {
                    UIPanelReflector reflectorTmp = new UIPanelReflector((UIPanelAPI) item);
                    if (reflectorTmp.getFader().getBrightness() == 1.f) {
                        getPanelNavigatables(reflectorTmp, directionalObjects, scrollers);
                    }
                }
            }
        }
    }

    @Override
    public void preInput(float amount) {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.OUTPOSTS) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
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
