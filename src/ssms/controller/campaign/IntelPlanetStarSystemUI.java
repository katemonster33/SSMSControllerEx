package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CampaignEngine;
import com.fs.starfarer.campaign.StarSystem;
import com.fs.starfarer.campaign.comms.IntelTabData;
import com.fs.starfarer.campaign.ui.UITable;
import com.fs.starfarer.campaign.ui.intel.PlanetListV2;
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

public class IntelPlanetStarSystemUI extends InputScreenBase {
    public static final String ID = "IntelPlanetStarSystem";
    IntelTabReflector intelTabReflector;
    IntelTabData intelTabData;
    List<Pair<Indicators, String>> indicators = null;
    int lastFrameSelectedIndex = -1;
    CampaignScope campaignScope;
    HandlerController controller;
    StarSystemReflector starSystemReflector;
    int selectedIndex = -1;
    Vector2f desiredMousePos = null;
    float mouseMoveFactor = 4.f;

    enum StarSystemTabFocusMode {
        IntelList,
        Map,
        FilterButtons
    };
    enum MapMode {
        MoveCursor,
        MoveMap,
        Zoom
    };
    MapMode currentMapMode = MapMode.MoveCursor;
    StarSystemTabFocusMode currentTabFocus = StarSystemTabFocusMode.IntelList;

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
            if(currentTabFocus == StarSystemTabFocusMode.Map) {
                switch(currentMapMode) {
                    case MoveCursor -> indicators.add(new Pair<>(Indicators.LeftStickButton, "Toggle move map"));
                    case MoveMap -> indicators.add(new Pair<>(Indicators.LeftStickButton, "Toggle zoom map"));
                    case Zoom -> indicators.add(new Pair<>(Indicators.LeftStickButton, "Toggle move cursor"));
                }
            }
            indicators.add(new Pair<>(Indicators.RightStickUp, "Focus map"));
            indicators.add(new Pair<>(Indicators.RightStickLeft, "Focus intel list"));
            indicators.add(new Pair<>(Indicators.RightStickDown, "Focus intel tag buttons"));
            indicators.add(new Pair<>(Indicators.RightTrigger, "Select planet tab"));
            if(currentTabFocus == StarSystemTabFocusMode.IntelList) {
                indicators.add(new Pair<>(Indicators.Y, "Show on map"));
            } else if(currentTabFocus == StarSystemTabFocusMode.Map) {
                indicators.add(new Pair<>(Indicators.X, "Toggle sector/system view"));
                indicators.add(new Pair<>(Indicators.Y, "Show fuel range"));
            }
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
        currentTabFocus = StarSystemTabFocusMode.IntelList;
        desiredMousePos = null;
        campaignScope = (CampaignScope) InputScreenManager.getInstance().getCurrentScope();
        controller = SSMSControllerModPluginEx.controller;
        currentMapMode = MapMode.MoveCursor;
        var planetTabReflector = new IntelPlanetTabUi.PlanetTabReflector(intelTabReflector.getPlanetTabData());
        // this can throw an exception, we will just pass the exception upstream so that the screen doesn't get activated
        starSystemReflector = new StarSystemReflector(planetTabReflector.getStarSystem());
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
        var lst = starSystemReflector.getIntelFilters();
        if(controller.getButtonEvent(HandlerController.Buttons.LeftStickLeft) == 1) {
            selectedIndex--;
            navigateButton(lst);
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickRight) == 1) {
            selectedIndex++;
            navigateButton(lst);
        }
    }

    void preInputMap(float amount) {
        if(currentMapMode == IntelTabUI.MapMode.Zoom) {
            if(controller.isLeftStickUp()) {
                InputShim.mouseWheel((int) desiredMousePos.getX(), (int)desiredMousePos.getY(), 1);
                //InputShim.mouseDownUp((int) desiredMousePos.getX(), (int)desiredMousePos.getY(), InputEventMouseButton.);
            } else if(controller.isLeftStickDown()) {
                InputShim.mouseWheel((int) desiredMousePos.getX(), (int)desiredMousePos.getY(), -1);
            }
        } else {
            ReadableVector2f leftStick = controller.getLeftStick();
            var map = starSystemReflector.getMap();
            if (leftStick.getX() != 0 || leftStick.getY() != 0) {
                if (desiredMousePos == null)
                    desiredMousePos = new Vector2f((int) map.getPosition().getCenterX(), (int) map.getPosition().getCenterY());
                else
                    desiredMousePos.set(desiredMousePos.getX() + (leftStick.getX() * mouseMoveFactor), desiredMousePos.getY() + (leftStick.getY() * mouseMoveFactor));

                InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
            }
        }
        if (controller.getButtonEvent(HandlerController.Buttons.LeftStickButton) == 1) {
            if (currentMapMode == IntelTabUI.MapMode.MoveCursor) InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
            else if(currentMapMode == IntelTabUI.MapMode.MoveMap) InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);

            currentMapMode = switch(currentMapMode) {
                case MoveCursor -> IntelTabUI.MapMode.MoveMap;
                case MoveMap -> IntelTabUI.MapMode.Zoom;
                case Zoom -> IntelTabUI.MapMode.MoveCursor;
            };
            indicators = null;
            InputScreenManager.getInstance().refreshIndicators();
        }
        if (currentMapMode == IntelTabUI.MapMode.MoveCursor) {
            if (desiredMousePos != null) {
                if (controller.getButtonEvent(HandlerController.Buttons.A) == 1) {
                    InputShim.mouseDownUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
                }
            }
        }

        if (controller.getButtonEvent(HandlerController.Buttons.Y) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_W, 'w');
        }
    }

    void preInputIntelList(float amount) {
        var lst = starSystemReflector.getIntelButtons();
        if(controller.getButtonEvent(HandlerController.Buttons.LeftStickUp) == 1) {
            selectedIndex++;
            navigateButton(lst);
            if(selectedIndex != -1) starSystemReflector.ensureIntelButtonVisible(lst.get(selectedIndex));
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickDown) == 1) {
            selectedIndex--;
            navigateButton(lst);
            if(selectedIndex != -1) starSystemReflector.ensureIntelButtonVisible(lst.get(selectedIndex));
        } else if(controller.getButtonEvent(HandlerController.Buttons.Y) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_S, 's');
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
        } else if(controller.getButtonEvent(HandlerController.Buttons.RightTrigger) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_2, '2');
        } else if(controller.getButtonEvent(HandlerController.Buttons.RightStickLeft) == 1) {
            var lst = starSystemReflector.getIntelButtons();
            selectedIndex = 0;
            navigateButton(lst);
            currentTabFocus = IntelTabUI.IntelTabFocusMode.IntelList;
            indicators = null;
            InputScreenManager.getInstance().refreshIndicators();
        } else if(controller.getButtonEvent(HandlerController.Buttons.RightStickDown) == 1) {
            var lst = starSystemReflector.getIntelFilters();
            selectedIndex = 0;
            navigateButton(lst);
            currentTabFocus = IntelTabUI.IntelTabFocusMode.FilterButtons;
            indicators = null;
            InputScreenManager.getInstance().refreshIndicators();
        } else if(controller.getButtonEvent(HandlerController.Buttons.RightStickUp) == 1) {
            var map = starSystemReflector.getMap();
            currentMapMode = IntelTabUI.MapMode.MoveCursor;
            desiredMousePos = new Vector2f((int) map.getPosition().getCenterX(), (int) map.getPosition().getCenterY());
            InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
            currentTabFocus = IntelTabUI.IntelTabFocusMode.Map;
            indicators = null;
            InputScreenManager.getInstance().refreshIndicators();
        }
        campaignScope.handleInput(amount, true);
    }

    public record StarSystemReflector(StarSystem starSystem) {
        static MethodHandle getPlanetListV2;
        static MethodHandle getPlanetListTableRows;
        static MethodHandle selectPlanetTableRow;
        static Object getList;
        static Object ensurePlanetVisible;
        static Object planetItemColumnsField;
        static Object starSystemField;
        static Class<?> tableItemCls;
        static Class<?> tableListCls;

        public PlanetListV2 getPlanetList() {
            try {
                return (PlanetListV2) getPlanetListV2.invoke(planetTabData);
            } catch (Throwable ex) {
                Global.getLogger(getClass()).error("Couldn't get planet list!", ex);
                return null;
            }
        }

        public void ensurePlanetVisible(Object planetObj) {
            try {
                var buttonField = ClassReflector.GetInstance().getDeclaredField(planetObj.getClass().getSuperclass().getSuperclass(), "button");

                var btn = (ButtonAPI) FieldReflector.GetInstance().GetVariable(buttonField, planetObj);

                var lst = MethodReflector.GetInstance().invoke(getList, getPlanetList().getTable());
                MethodReflector.GetInstance().invoke(ensurePlanetVisible, lst, btn);
            } catch (Throwable ex) {
                Global.getLogger(getClass()).error("Couldn't ensure that the desired planet is visible in the scroller!", ex);
            }
        }

        public List<UIPanelAPI> getPlanetsTableRows() {
            var planetListUi = getPlanetList();
            List<UIPanelAPI> planets = new ArrayList<>();
            if (planetListUi != null) {
                try {
                    var table = planetListUi.getTable();

                    var planetsListRaw = (List<?>) getPlanetListTableRows.invoke(table);
                    for (Object planet : planetsListRaw) {
                        if (UIPanelAPI.class.isAssignableFrom(planet.getClass())) {
                            planets.add((UIPanelAPI) planet);
                        }
                    }
                } catch (Throwable ex) {
                    Global.getLogger(getClass()).error("Couldn't fetch current list of planets!", ex);
                }
            }
            return planets;
        }

        public List<UIComponentAPI> getPlanetSubItems(UIPanelAPI planetItem) {
            List<UIComponentAPI> output = new ArrayList<>();
            try {
                List<?> columns = (List<?>) FieldReflector.GetInstance().GetVariable(planetItemColumnsField, planetItem);
                if (columns != null && columns.size() > 5) {
                    output.add((UIComponentAPI) columns.get(0));

                    var children = UIPanelReflector.getChildItems((UIPanelAPI) columns.get(1));
                    if (children.size() > 1) {
                        children = UIPanelReflector.getChildItems((UIPanelAPI) children.get(1));

                        for (var child : children) {
                            output.add((UIComponentAPI) child);
                        }
                    }

                    output.add((UIComponentAPI) columns.get(2));
                    output.sort((UIComponentAPI left, UIComponentAPI right) -> (int) (left.getPosition().getX() - right.getPosition().getX()));
                }
            } catch (Throwable ex) {
                Global.getLogger(getClass()).error("Couldn't get clickable sub-items of planet!", ex);
            }
            return output;
        }

        public static IntelPlanetTabUi.PlanetTabReflector tryGet(IntelTabReflector intelTabReflector) {
            var planetPanel = intelTabReflector.getPlanetTabData();
            if (planetPanel != null) {
                if (planetItemColumnsField == null) {
                    try {
                        getPlanetListV2 = MethodHandles.lookup().findVirtual(planetPanel.getClass(), "getPlanetList2", MethodType.methodType(PlanetListV2.class));

                        getPlanetListTableRows = MethodHandles.lookup().findVirtual(UITable.class, "getRows", MethodType.methodType(List.class));

                        var getSelected = ClassReflector.GetInstance().getDeclaredMethod(UITable.class, "getSelected");
                        tableItemCls = MethodReflector.GetInstance().getReturnType(getSelected);

                        getList = ClassReflector.GetInstance().getDeclaredMethod(UITable.class, "getList");

                        tableListCls = MethodReflector.GetInstance().getReturnType(getList);
                        ensurePlanetVisible = ClassReflector.GetInstance().findDeclaredMethod(tableListCls, "ensureVisible");
                        planetItemColumnsField = ClassReflector.GetInstance().getDeclaredField(tableItemCls, "columns");

                        return new IntelPlanetTabUi.PlanetTabReflector(planetPanel);
                    } catch (Throwable ex) {
                        Global.getLogger(IntelPlanetTabUi.PlanetTabReflector.class).error("Couldn't reflect into planet tab!", ex);
                        return null;
                    }
                } else {
                    return new IntelPlanetTabUi.PlanetTabReflector(planetPanel);
                }
            }
            return null;
        }
    }
}
