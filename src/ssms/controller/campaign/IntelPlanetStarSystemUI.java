package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CampaignEngine;
import com.fs.starfarer.campaign.comms.IntelTabData;
import com.fs.starfarer.campaign.ui.MarketConditionsWidget;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.reflection.*;

import java.util.ArrayList;
import java.util.List;

public class IntelPlanetStarSystemUI extends InputScreenBase {
    public static final String ID = "IntelPlanetStarSystem";
    IntelTabReflector intelTabReflector;
    IntelTabData intelTabData;
    int lastFrameSelectedIndex = -1;
    CampaignScope campaignScope;
    IntelPlanetTabUi.PlanetTabReflector planetTabReflector;
    int selectedIndex = -1;
    Vector2f desiredMousePos = null;
    float mouseMoveFactor = 4.f;

    enum StarSystemTabFocusMode {
        PlanetAttributes,
        Map,
        PlanetList
    };
    enum MapMode {
        MoveCursor,
        MoveMap,
        Zoom
    };
    MapMode currentMapMode = MapMode.MoveCursor;
    StarSystemTabFocusMode currentTabFocus = StarSystemTabFocusMode.PlanetAttributes;

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
            indicators.add(new Pair<>(Indicators.RightStickLeft, "Focus map"));
            indicators.add(new Pair<>(Indicators.RightStickUp, "Focus planet attributes"));
            indicators.add(new Pair<>(Indicators.RightStickDown, "Focus celestials"));
            indicators.add(new Pair<>(Indicators.LeftTrigger, "Select intel tab"));
            indicators.add(new Pair<>(Indicators.RightTrigger, "Select faction tab"));
            indicators.add(new Pair<>(Indicators.X, "Show on map"));
            indicators.add(new Pair<>(Indicators.Y, "Lay in course"));
            if(currentTabFocus == StarSystemTabFocusMode.PlanetAttributes) {
                indicators.add(new Pair<>(Indicators.A, "Open Codex"));
            } else {
                indicators.add(new Pair<>(Indicators.A, "Select"));
            }
            indicators.add(new Pair<>(Indicators.B, "Return to planets list"));
            indicators.addAll(campaignScope.getIndicators());
        }
        return indicators;
    }

    @Override
    public void activate(Object ... args) throws Throwable {
        intelTabReflector = (IntelTabReflector) args[0];
        intelTabData = CampaignEngine.getInstance().getUIData().getIntelData();
        lastFrameSelectedIndex = intelTabData.getSelectedTabIndex();
        currentTabFocus = StarSystemTabFocusMode.PlanetAttributes;
        desiredMousePos = null;
        campaignScope = (CampaignScope) InputScreenManager.getInstance().getCurrentScope();
        currentMapMode = MapMode.MoveCursor;
        planetTabReflector = new IntelPlanetTabUi.PlanetTabReflector(intelTabReflector.getPlanetTabData());
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

    void preInputPlanetAttributes(float amount) {
        var planetPanel = planetTabReflector.getStarSystemDisplay();
        var items = UIPanelReflector.getChildItems(planetPanel);
        List<UIComponentAPI> lst = new ArrayList<>();
        for(var item : items) {
            if(ButtonAPI.class.isAssignableFrom(item.getClass())) {
                lst.add((UIComponentAPI) item);
            }
        }
        lst.sort((UIComponentAPI left, UIComponentAPI right) -> {
            if(left.getPosition().getX() == right.getPosition().getX()) {
                return (int)(left.getPosition().getY() - right.getPosition().getY());
            }
            return (int)(left.getPosition().getX() - right.getPosition().getX());
        });
        //var lst = starSystemReflector.getIntelFilters();
        if(controller.getButtonEvent(LogicalButtons.LeftStickLeft) == 1) {
            selectedIndex--;
            navigateButton(lst);
        } else if(controller.getButtonEvent(LogicalButtons.LeftStickRight) == 1) {
            selectedIndex++;
            navigateButton(lst);
        }
    }

    void preInputMap(float amount) {
        if(currentMapMode == MapMode.Zoom) {
            if(controller.isButtonPressed(LogicalButtons.LeftStickUp)) {
                InputShim.mouseWheel((int) desiredMousePos.getX(), (int)desiredMousePos.getY(), 1);
            } else if(controller.isButtonPressed(LogicalButtons.LeftStickDown)) {
                InputShim.mouseWheel((int) desiredMousePos.getX(), (int)desiredMousePos.getY(), -1);
            }
        } else {
            ReadableVector2f leftStick = controller.getLeftStick();
            if (leftStick.getX() != 0 || leftStick.getY() != 0) {
                if (desiredMousePos == null) {
                    var map = planetTabReflector.getMap();
                    desiredMousePos = new Vector2f((int) map.getPosition().getCenterX(), (int) map.getPosition().getCenterY());
                } else {
                    desiredMousePos.set(desiredMousePos.getX() + (leftStick.getX() * mouseMoveFactor), desiredMousePos.getY() + (leftStick.getY() * mouseMoveFactor));
                }
                InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
            }
        }
        if (controller.getButtonEvent(LogicalButtons.LeftStickButton) == 1) {
            if (currentMapMode == MapMode.MoveCursor) InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
            else if(currentMapMode == MapMode.MoveMap) InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);

            currentMapMode = switch(currentMapMode) {
                case MoveCursor -> MapMode.MoveMap;
                case MoveMap -> MapMode.Zoom;
                case Zoom -> MapMode.MoveCursor;
            };
            indicators = null;
            InputScreenManager.getInstance().refreshIndicators();
        }
        if (currentMapMode == MapMode.MoveCursor) {
            if (desiredMousePos != null) {
                if (controller.getButtonEvent(LogicalButtons.A) == 1) {
                    InputShim.mouseDownUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
                }
            }
        }
    }

    void preInputPlanetList(float amount) {
        var planetPanel = planetTabReflector.getPlanetInfoPanel();
        var items = UIPanelReflector.getChildItems(planetPanel);
        List<UIComponentAPI> lst = new ArrayList<>();
        for(var item : items) {
            if(MarketConditionsWidget.class.isAssignableFrom(item.getClass())) {
                for(var filterBtn : UIPanelReflector.getChildItems((UIPanelAPI) item)) {
                    if (ButtonAPI.class.isAssignableFrom(filterBtn.getClass())) {
                        lst.add((UIComponentAPI) filterBtn);
                    }
                }
            }
        }
        lst.sort((UIComponentAPI left, UIComponentAPI right) -> (int)(left.getPosition().getX() - right.getPosition().getX()));
        //var lst = starSystemReflector.getIntelButtons();
        if(controller.getButtonEvent(LogicalButtons.LeftStickLeft) == 1) {
            selectedIndex--;
            navigateButton(lst);
            //if(selectedIndex != -1) starSystemReflector.ensureIntelButtonVisible(lst.get(selectedIndex));
        } else if(controller.getButtonEvent(LogicalButtons.LeftStickRight) == 1) {
            selectedIndex++;
            navigateButton(lst);
            //if(selectedIndex != -1) starSystemReflector.ensureIntelButtonVisible(lst.get(selectedIndex));
        }
    }

    @Override
    public void preInput(float amount) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.INTEL) InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        else if(intelTabData.getSelectedTabIndex() == 0) InputScreenManager.getInstance().transitionDelayed(IntelTabUI.ID, intelTabReflector);
        else if(intelTabData.getSelectedTabIndex() == 1 && planetTabReflector.getStarSystem() == null) InputScreenManager.getInstance().transitionDelayed(IntelPlanetTabUi.ID, intelTabReflector);
        else if(intelTabData.getSelectedTabIndex() == 2) InputScreenManager.getInstance().transitionDelayed(IntelFactionTabUi.ID, intelTabReflector);
        lastFrameSelectedIndex = intelTabData.getSelectedTabIndex();
        switch(currentTabFocus) {
            case PlanetAttributes -> preInputPlanetList(amount);
            case Map -> preInputMap(amount);
            case PlanetList -> preInputPlanetAttributes(amount);
        }
        if(controller.getButtonEvent(LogicalButtons.B) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_Q, 'q');
        } else if(controller.getButtonEvent(LogicalButtons.X) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_S, 's');
        } else if(controller.getButtonEvent(LogicalButtons.Y) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_A, 'a');
        } else if(controller.getButtonEvent(LogicalButtons.A) == 1) {
            if(currentTabFocus == StarSystemTabFocusMode.PlanetAttributes) {
                InputShim.keyDownUp(Keyboard.KEY_F2, '\0');
            } else if(desiredMousePos != null) {
                InputShim.mouseDownUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
            }
            InputShim.keyDownUp(Keyboard.KEY_A, 'a');
        } else if(controller.getButtonEvent(LogicalButtons.LeftTrigger) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_1, '1');
        } else if(controller.getButtonEvent(LogicalButtons.RightTrigger) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_3, '3');
        } else if(controller.getButtonEvent(LogicalButtons.RightStickLeft) == 1) {
            var map = planetTabReflector.getMap();
            currentMapMode = MapMode.MoveCursor;
            desiredMousePos = new Vector2f((int) map.getPosition().getCenterX(), (int) map.getPosition().getCenterY());
            InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
            currentTabFocus = StarSystemTabFocusMode.Map;
            indicators = null;
            InputScreenManager.getInstance().refreshIndicators();
        } else if(controller.getButtonEvent(LogicalButtons.RightStickRight) == 1) {
            //var lst = starSystemReflector.getIntelFilters();
            selectedIndex = 0;
            //navigateButton(lst);
            currentTabFocus = StarSystemTabFocusMode.PlanetList;
            indicators = null;
            InputScreenManager.getInstance().refreshIndicators();
        } else if(controller.getButtonEvent(LogicalButtons.RightStickUp) == 1) {
            //var lst = starSystemReflector.getIntelButtons();
            selectedIndex = 0;
            //navigateButton(lst);
            currentTabFocus = StarSystemTabFocusMode.PlanetAttributes;
            indicators = null;
            InputScreenManager.getInstance().refreshIndicators();
        }
        campaignScope.handleInput(amount, true);
    }
}
