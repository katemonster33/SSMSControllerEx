package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
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
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.inputhelper.MapInputHandler;
import ssms.controller.reflection.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IntelTabUI extends InputScreenBase {
    public static final String ID = "IntelTab";
    IntelTabReflector intelTabReflector;
    IntelTabData intelTabData;
    int lastFrameSelectedIndex = -1;
    EventsTabReflector eventsTabReflector;
    MapInputHandler mapInputHandler;
    List<UIComponentAPI> filterButtons;
    List<UIComponentAPI> intelButtons;
    UIComponentAPI mapComponent;
    DirectionalUINavigator directionalUINavigator;

    enum IntelTabFocusMode {
        Buttons,
        Map
    };
    IntelTabFocusMode currentTabFocus = IntelTabFocusMode.Buttons;
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = new ArrayList<>();
            if(currentTabFocus == IntelTabFocusMode.Map) {
                mapInputHandler = addMapHandler(eventsTabReflector.getMap());
            } else {
                addButtonPressHandler("Select planet tab", LogicalButtons.RightTrigger, new KeySender(Keyboard.KEY_2, '2'));
            }
            if(currentTabFocus == IntelTabFocusMode.Buttons) {
                addButtonPressHandler("Show on map", LogicalButtons.Y, new KeySender(Keyboard.KEY_S, 's'));
            } else if(currentTabFocus == IntelTabFocusMode.Map) {
                //indicators.add(new Pair<>(Indicators.X, "Toggle sector/system view"));
                addButtonPressHandler("Show fuel range", LogicalButtons.Y, new KeySender(Keyboard.KEY_W, 'w'));
            }
            addButtonPressHandler("Return to campaign view", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
            addButtonChangeHandler("Select", LogicalButtons.A, (float advance, boolean state) -> {
                if(InputShim.getMouseX() != null && InputShim.getMouseY() != null) {
                    if (state) InputShim.mouseDown(InputShim.getMouseX(), InputShim.getMouseY(), InputEventMouseButton.LEFT);
                    else InputShim.mouseUp(InputShim.getMouseX(), InputShim.getMouseY(), InputEventMouseButton.LEFT);
                }
            });
            addButtonPressHandler("Select map tab", LogicalButtons.BumperLeft, new KeySender(Keyboard.KEY_TAB));
            addButtonPressHandler("Select command tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_D, 'd'));
            if(directionalUINavigator == null) {
                directionalUINavigator = new DirectionalUINavigator(new ArrayList<>()) {
                    @Override
                    public void onSelect(Pair<UIComponentAPI, Object> objPair) {
                        super.onSelect(objPair);
                        if(currentTabFocus != IntelTabFocusMode.Map && objPair.one == mapComponent) {
                            currentTabFocus = IntelTabFocusMode.Map;
                            clearHandlers();
                            InputScreenManager.getInstance().refreshIndicators();
                        } else if(currentTabFocus == IntelTabFocusMode.Map && objPair.one != mapComponent) {
                            currentTabFocus = IntelTabFocusMode.Buttons;
                            clearHandlers();
                            InputScreenManager.getInstance().refreshIndicators();
                        }
                    }
                };
            }
            addDigitalJoystickHandler("Navigate", Joystick.DPad, directionalUINavigator);
            refreshDirectionalUi();
        }
        return indicators;
    }

    @Override
    public void activate(Object ... args) throws Throwable {
        intelTabReflector = (IntelTabReflector) args[0];
        intelTabData = CampaignEngine.getInstance().getUIData().getIntelData();
        lastFrameSelectedIndex = intelTabData.getSelectedTabIndex();
        // this can throw an exception, we will just pass the exception upstream so that the screen doesn't get activated
        eventsTabReflector = new EventsTabReflector(intelTabReflector.getEventsPanel());
        filterButtons = eventsTabReflector.getIntelFilters();
        intelButtons = eventsTabReflector.getIntelButtons();
        mapComponent = eventsTabReflector.getMap();
        indicators = null;
    }

    void refreshDirectionalUi() {
        List<UIComponentAPI> buttons = new ArrayList<>(filterButtons);
        buttons.addAll(intelButtons);
        buttons.add(mapComponent);
        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for(var btn : buttons) {
            directionalObjects.add(new Pair<>(btn, null));
        }
        directionalUINavigator.setNavigationObjects(directionalObjects);
    }

    @Override
    public void preInput(float amount) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.INTEL) InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        else if(intelTabData.getSelectedTabIndex() == 1) InputScreenManager.getInstance().transitionDelayed(IntelPlanetTabUi.ID, intelTabReflector);
        else if(intelTabData.getSelectedTabIndex() == 2) InputScreenManager.getInstance().transitionDelayed(IntelFactionTabUi.ID, intelTabReflector);
        lastFrameSelectedIndex = intelTabData.getSelectedTabIndex();
        if(mapInputHandler != null && currentTabFocus == IntelTabFocusMode.Map) {
            mapInputHandler.advance(amount);
        }
    }

    public static class EventsTabReflector extends UIPanelReflector
    {
        EventsPanel eventsPanel;
        static MethodReflector getList;
        static MethodReflector ensureVisible;
        static MethodReflector getMap;
        static MethodHandle getItems;

        static MethodHandle getGroups;
        static FieldReflector intelTagGroupList;
        static Class<?> intelTagGroupListCls;
        public EventsTabReflector(EventsPanel eventsPanel) throws Throwable {
            super(eventsPanel);
            this.eventsPanel = eventsPanel;

            if(intelTagGroupList == null) {
                ClassReflector eventsPanelReflector = new ClassReflector(EventsPanel.class);
                getList = eventsPanelReflector.getDeclaredMethod("getList");

                getMap = eventsPanelReflector.getDeclaredMethod("getMap");

                Class<?> listType = getList.getReturnType();
                ensureVisible = new ClassReflector(listType).findDeclaredMethod("ensureVisible");

                getItems = MethodHandles.lookup().findVirtual(listType, "getItems", MethodType.methodType(List.class));

                for (var field : eventsPanelReflector.getDeclaredFields()) {
                    intelTagGroupListCls = field.getType();

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
            return (UIComponentAPI) getMap.invoke(eventsPanel);
        }

        public List<UIComponentAPI> getIntelButtons() {
            List<UIComponentAPI> intelButtons = new ArrayList<>();
            try {
                var listPanel = getList.invoke(eventsPanel);

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
                var filterGroups = intelTagGroupList.get(eventsPanel);

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
