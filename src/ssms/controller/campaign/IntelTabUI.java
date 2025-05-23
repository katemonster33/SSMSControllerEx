package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CampaignEngine;
import com.fs.starfarer.campaign.comms.IntelTabData;
import com.fs.starfarer.campaign.comms.v2.EventsPanel;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.reflection.ClassReflector;
import ssms.controller.reflection.IntelTabReflector;
import ssms.controller.reflection.MethodReflector;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class IntelTabUI extends InputScreenBase {
    public static final String ID = "IntelTab";
    IntelTabReflector intelTabReflector;
    IntelTabData intelTabData;
    IntelManagerAPI intelManagerAPI;
    List<Pair<Indicators, String>> indicators = null;
    int lastFrameSelectedIndex = -1;
    CampaignScope campaignScope;
    HandlerController controller;
    EventsTabReflector eventsTabReflector;
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String[] getScopes() {
        return new String[]{CampaignScope.ID};
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = new ArrayList<>();
            indicators.add(new Pair<>(Indicators.LeftStick, "Navigate"));
            indicators.add(new Pair<>(Indicators.LeftStickButton, "Toggle free look (map only)"));
            indicators.add(new Pair<>(Indicators.RightStickUp, "Focus map"));
            indicators.add(new Pair<>(Indicators.RightStickLeft, "Focus intel list"));
            indicators.add(new Pair<>(Indicators.RightStickDown, "Focus intel tag buttons"));
            indicators.add(new Pair<>(Indicators.RightStick, "Navigate to different table"));
            indicators.add(new Pair<>(Indicators.RightTrigger, "Select planet tab"));
            indicators.add(new Pair<>(Indicators.B, "Return to campaign view"));
            indicators.addAll(campaignScope.getIndicators());
        }
        return indicators;
    }

    @Override
    public void activate(Object ... args) throws Throwable {
        intelTabReflector = (IntelTabReflector) args[0];
        intelTabData = CampaignEngine.getInstance().getUIData().getIntelData();
        lastFrameSelectedIndex = intelTabData.getSelectedTabIndex();
        campaignScope = (CampaignScope) InputScreenManager.getInstance().getCurrentScope();
        controller = SSMSControllerModPluginEx.controller;
        // this can throw an exception, we will just pass the exception upstream so that the screen doesn't get activated
        eventsTabReflector = new EventsTabReflector(intelTabReflector.getEventsPanel());
    }

    @Override
    public void preInput(float amount) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.INTEL) InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        else if(intelTabData.getSelectedTabIndex() == 1) InputScreenManager.getInstance().transitionDelayed(IntelPlanetTabUi.ID, intelTabReflector);
        else if(intelTabData.getSelectedTabIndex() == 2) InputScreenManager.getInstance().transitionDelayed(IntelFactionTabUi.ID, intelTabReflector);
        lastFrameSelectedIndex = intelTabData.getSelectedTabIndex();
        if(controller.getButtonEvent(HandlerController.Buttons.B) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
        }
        campaignScope.handleInput(amount, true);
    }

    public static class EventsTabReflector
    {
        EventsPanel eventsPanel;
        static Object getList;
        static Object ensureVisible;
        static Object getMap;
        static MethodHandle getItems;
        public EventsTabReflector(EventsPanel eventsPanel) throws Throwable {
            this.eventsPanel = eventsPanel;

            if(ensureVisible == null) {
                getList = ClassReflector.GetInstance().getDeclaredMethod(EventsPanel.class, "getList");

                getMap = ClassReflector.GetInstance().getDeclaredMethod(EventsPanel.class, "getMap");

                Class<?> listType = MethodReflector.GetInstance().getReturnType(getList);
                ensureVisible = ClassReflector.GetInstance().findDeclaredMethod(listType, "ensureVisible");

                getItems = MethodHandles.lookup().findVirtual(listType, "getItems", MethodType.methodType(List.class));
            }
        }

        public UIComponentAPI getMap() {
            try {
                return (UIComponentAPI) MethodReflector.GetInstance().invoke(getMap, eventsPanel);
            } catch(Throwable ex) {
                Global.getLogger(getClass()).error("Couldn't get map from EventsPanel!", ex);
                return null;
            }
        }

        public List<ButtonAPI> getIntelButtons() {
            List<ButtonAPI> intelButtons = new ArrayList<>();
            try {
                var listPanel = MethodReflector.GetInstance().invoke(getList, eventsPanel);

                var items = (List<?>) MethodReflector.GetInstance().invoke(getItems, listPanel);

                for(Object item : items) {
                    if(ButtonAPI.class.isAssignableFrom(item.getClass())) {
                        intelButtons.add((ButtonAPI) item);
                    }
                }
            } catch(Throwable ex) {
                Global.getLogger(getClass()).error("Couldn't fetch list of intel items from EventsPanel!", ex);
            }
            return intelButtons;
        }

        public void ensureIntelButtonVisible(ButtonAPI intelButton) {
            try {
                var listPanel = MethodReflector.GetInstance().invoke(getList, eventsPanel);

                MethodReflector.GetInstance().invoke(ensureVisible, listPanel, intelButton);
            } catch(Throwable ex) {
                Global.getLogger(getClass()).error("Couldn't ensure intel item visible in EventsPanel!", ex);
            }
        }
    }
}
