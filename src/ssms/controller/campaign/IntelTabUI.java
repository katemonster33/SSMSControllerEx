package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.TagDisplayAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CampaignEngine;
import com.fs.starfarer.campaign.comms.IntelTabData;
import com.fs.starfarer.campaign.comms.v2.EventsPanel;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.reflection.*;

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
    int selectedIndex = -1;
    Vector2f desiredMousePos = null;
    boolean mouseDown = false, movingMap = false;
    float mouseMoveFactor = 4.f;

    enum IntelTabFocusMode {
        IntelList,
        Map,
        FilterButtons
    };
    IntelTabFocusMode currentTabFocus = IntelTabFocusMode.IntelList;
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
        currentTabFocus = IntelTabFocusMode.IntelList;
        desiredMousePos = null;
        campaignScope = (CampaignScope) InputScreenManager.getInstance().getCurrentScope();
        controller = SSMSControllerModPluginEx.controller;
        // this can throw an exception, we will just pass the exception upstream so that the screen doesn't get activated
        eventsTabReflector = new EventsTabReflector(intelTabReflector.getEventsPanel());
    }

    void navigateButton(List<UIComponentAPI> eventsButtons) {
        if(eventsButtons == null || eventsButtons.isEmpty()) {
            selectedIndex = -1;
            return;
        }
        else if(selectedIndex < 0) selectedIndex = 0;
        else if(selectedIndex >= eventsButtons.size()) selectedIndex = eventsButtons.size() - 1;

        var pos = eventsButtons.get(selectedIndex).getPosition();
        InputShim.mouseMove((int) pos.getCenterX(), (int) pos.getCenterY());
    }

    void preInputFilterButtons(float amount) {
        var lst = eventsTabReflector.getIntelFilters();
        if(controller.getButtonEvent(HandlerController.Buttons.LeftStickLeft) == 1) {
            selectedIndex--;
            navigateButton(lst);
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickRight) == 1) {
            selectedIndex++;
            navigateButton(lst);
        }
    }

    void preInputMap(float amount) {
        ReadableVector2f leftStick = controller.getLeftStick();
        var map = eventsTabReflector.getMap();
        if (leftStick.getX() != 0 || leftStick.getY() != 0) {
            if (desiredMousePos == null) desiredMousePos = new Vector2f((int) map.getPosition().getCenterX(), (int) map.getPosition().getCenterY());
            else desiredMousePos.set(desiredMousePos.getX() + (leftStick.getX() * mouseMoveFactor), desiredMousePos.getY() + (leftStick.getY() * mouseMoveFactor));

            InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
        }
        if(!mouseDown && controller.getButtonEvent(HandlerController.Buttons.LeftStickButton) == 1) {
            if(!movingMap) InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
            else InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);

            movingMap = !movingMap;
        }
        if(!movingMap) {
            if(desiredMousePos != null) {
                if (controller.isButtonAPressed() && !mouseDown) {
                    InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
                    mouseDown = true;
                } else if (!controller.isButtonAPressed() && mouseDown) {
                    InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
                    mouseDown = false;
                }
            }
        }
    }

    void preInputIntelList(float amount) {
        var lst = eventsTabReflector.getIntelButtons();
        if(controller.getButtonEvent(HandlerController.Buttons.LeftStickUp) == 1) {
            selectedIndex++;
            navigateButton(lst);
            if(selectedIndex != -1) eventsTabReflector.ensureIntelButtonVisible(lst.get(selectedIndex));
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickDown) == 1) {
            selectedIndex--;
            navigateButton(lst);
            if(selectedIndex != -1) eventsTabReflector.ensureIntelButtonVisible(lst.get(selectedIndex));
        }
    }

    @Override
    public void preInput(float amount) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.INTEL) InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        else if(intelTabData.getSelectedTabIndex() == 1) InputScreenManager.getInstance().transitionDelayed(IntelPlanetTabUi.ID, intelTabReflector);
        else if(intelTabData.getSelectedTabIndex() == 2) InputScreenManager.getInstance().transitionDelayed(IntelFactionTabUi.ID, intelTabReflector);
        lastFrameSelectedIndex = intelTabData.getSelectedTabIndex();
        switch(currentTabFocus) {
            case IntelList -> preInputIntelList(amount);
            case Map -> preInputMap(amount);
            case FilterButtons -> preInputFilterButtons(amount);
        }
        if(controller.getButtonEvent(HandlerController.Buttons.B) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
        } else if(controller.getButtonEvent(HandlerController.Buttons.RightStickLeft) == 1) {
            var lst = eventsTabReflector.getIntelButtons();
            selectedIndex = 0;
            navigateButton(lst);
            currentTabFocus = IntelTabFocusMode.IntelList;
        } else if(controller.getButtonEvent(HandlerController.Buttons.RightStickDown) == 1) {
            var lst = eventsTabReflector.getIntelFilters();
            selectedIndex = 0;
            navigateButton(lst);
            currentTabFocus = IntelTabFocusMode.FilterButtons;
        } else if(controller.getButtonEvent(HandlerController.Buttons.RightStickUp) == 1) {
            var map = eventsTabReflector.getMap();
            movingMap = mouseDown = false;
            desiredMousePos = new Vector2f((int) map.getPosition().getCenterX(), (int) map.getPosition().getCenterY());
            InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
            currentTabFocus = IntelTabFocusMode.Map;
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

        static MethodHandle getGroups;
        static Object intelTagGroupList;
        static Class<?> intelTagGroupListCls;
        public EventsTabReflector(EventsPanel eventsPanel) throws Throwable {
            this.eventsPanel = eventsPanel;

            if(intelTagGroupList == null) {
                getList = ClassReflector.GetInstance().getDeclaredMethod(EventsPanel.class, "getList");

                getMap = ClassReflector.GetInstance().getDeclaredMethod(EventsPanel.class, "getMap");

                Class<?> listType = MethodReflector.GetInstance().getReturnType(getList);
                ensureVisible = ClassReflector.GetInstance().findDeclaredMethod(listType, "ensureVisible");

                getItems = MethodHandles.lookup().findVirtual(listType, "getItems", MethodType.methodType(List.class));

                for (var field : ClassReflector.GetInstance().getDeclaredFields(EventsPanel.class)) {
                    intelTagGroupListCls = FieldReflector.GetInstance().GetVariableType(field);

                    if(TagDisplayAPI.class.isAssignableFrom(intelTagGroupListCls)) {
                        intelTagGroupList = field;
                        break;
                    }
                }
                if(intelTagGroupList == null) {
                    intelTagGroupListCls = null;
                    throw new InstantiationException("Couldn't find any field in EventsPanel that implements TagDisplayAPI");
                }
                getGroups = MethodHandles.lookup().findVirtual(intelTagGroupListCls, "getGroups", MethodType.methodType(List.class));

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

        public List<UIComponentAPI> getIntelButtons() {
            List<UIComponentAPI> intelButtons = new ArrayList<>();
            try {
                var listPanel = MethodReflector.GetInstance().invoke(getList, eventsPanel);

                var items = (List<?>) getItems.invoke(listPanel);

                for(Object item : items) {
                    if(UIComponentAPI.class.isAssignableFrom(item.getClass())) {
                        intelButtons.add((UIComponentAPI) item);
                    }
                }
            } catch(Throwable ex) {
                Global.getLogger(getClass()).error("Couldn't fetch list of intel items from EventsPanel!", ex);
            }
            return intelButtons;
        }

        public List<UIComponentAPI> getIntelFilters() {
            List<UIComponentAPI> filterButtons = new ArrayList<>();
            try {
                var filterGroups = FieldReflector.GetInstance().GetVariable(intelTagGroupList, eventsPanel);

                var groupPanels = (List<?>) getGroups.invoke(filterGroups);
                for(var pnl : groupPanels) {
                    if(UIPanelAPI.class.isAssignableFrom(pnl.getClass())) {
                        for(var btn : UIPanelReflector.getChildItems((UIPanelAPI) pnl)) {
                            if(UIComponentAPI.class.isAssignableFrom(btn.getClass())) {
                                filterButtons.add((UIComponentAPI) btn);
                            }
                        }
                    }
                }
            } catch(Throwable ex) {
                Global.getLogger(getClass()).error("Couldn't get filter buttons!", ex);
            }
            return filterButtons;
        }

        public void ensureIntelButtonVisible(UIComponentAPI intelButton) {
            try {
                var listPanel = MethodReflector.GetInstance().invoke(getList, eventsPanel);

                MethodReflector.GetInstance().invoke(ensureVisible, listPanel, intelButton);
            } catch(Throwable ex) {
                Global.getLogger(getClass()).error("Couldn't ensure intel item visible in EventsPanel!", ex);
            }
        }
    }
}
