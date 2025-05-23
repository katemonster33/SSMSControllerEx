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
import com.fs.starfarer.campaign.ui.UITable;
import com.fs.starfarer.campaign.ui.intel.PlanetListV2;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.reflection.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class IntelPlanetTabUi extends InputScreenBase {
    public static final String ID = "IntelPlanetTab";
    List<Pair<Indicators, String>> indicators;
    List<List<ButtonAPI>> filterButtonRows = null;
    CampaignScope campaignScope;
    HandlerController controller;
    PlanetTabReflector planetTabReflector;
    IntelTabData intelTabData;
    IntelTabReflector intelTabReflector;
    int selectedRowIndex = -1;
    int selectedColumn = -1;
    List<UIComponentAPI> selectedRowColumns;
    boolean selectingPlanetFilters = false;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String[] getScopes() {
        return new String[]{CampaignScope.ID};
    }

    @Override
    public void activate(Object ... args) {
        intelTabReflector = (IntelTabReflector) args[0];

        planetTabReflector = PlanetTabReflector.tryGet(intelTabReflector);
        intelTabData = CampaignEngine.getInstance().getUIData().getIntelData();
        campaignScope = (CampaignScope) InputScreenManager.getInstance().getCurrentScope();
        controller = SSMSControllerModPluginEx.controller;

        filterButtonRows = new ArrayList<>();
        for (UIPanelAPI buttonGroup : UIPanelReflector.getChildPanels(planetTabReflector.getPlanetTabData(), 1, 5, 0)) {
            for (UIPanelAPI buttonRow : UIPanelReflector.getChildPanels(buttonGroup)) {
                for(UIPanelAPI buttonInnerPanel : UIPanelReflector.getChildPanels(buttonRow)) {
                    var btnList = UIPanelReflector.getChildButtons(buttonInnerPanel);
                    if (!btnList.isEmpty()) filterButtonRows.add(btnList);
                }
            }
        }
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if (indicators == null) {
            indicators = new ArrayList<>();

            indicators.add(new Pair<>(Indicators.LeftStick, "Navigate"));
            indicators.add(new Pair<>(Indicators.A, "Select"));
            indicators.add(new Pair<>(Indicators.B, "Close"));
        }
        return indicators;
    }

    void updateSelectedRowColumns() {
        List<?> rows = selectingPlanetFilters ? filterButtonRows : planetTabReflector.getPlanetsTableRows();

        if(rows.isEmpty()) {
            selectedRowColumns = null;
            return;
        }

        if(selectedRowIndex < 0) selectedRowIndex = 0;
        else if(selectedRowIndex >= rows.size()) selectedRowIndex = rows.size() - 1;

        if(selectingPlanetFilters) {
            selectedRowColumns = new ArrayList<>(filterButtonRows.get(selectedRowIndex));
        } else {
            selectedRowColumns = planetTabReflector.getPlanetSubItems((UIPanelAPI) rows.get(selectedRowIndex));
        }
    }

    void performActionOnHoveredItem() {
        updateSelectedRowColumns();

        if(selectedRowColumns == null || selectedRowColumns.isEmpty() || selectedColumn < 0 || selectedColumn >= selectedRowColumns.size()) return;

        if(!selectingPlanetFilters && selectedColumn > 0 && selectedColumn < (selectedRowColumns.size() - 1)) {
            InputShim.keyDownUp(Keyboard.KEY_F2, '\0');
        } else {
            var selectedCell = selectedRowColumns.get(selectedColumn);
            var pos = selectedCell.getPosition();
            InputShim.mouseDownUp((int) pos.getCenterX(), (int) pos.getCenterY(), InputEventMouseButton.LEFT);
        }
    }

    void hoverSelectedItem() {
        updateSelectedRowColumns();
        if(selectingPlanetFilters && selectedRowColumns == null) return;
        if(selectedRowColumns == null || (selectingPlanetFilters && selectedColumn < 0) || (!selectingPlanetFilters && selectedColumn >= selectedRowColumns.size())) {
            selectedRowIndex = selectedColumn = 0;
            selectingPlanetFilters = !selectingPlanetFilters;
            hoverSelectedItem();
            return;
        }

        if (selectedRowColumns.isEmpty()) return;
        else if(selectedColumn < 0) selectedColumn = 0;
        else if (selectedColumn >= selectedRowColumns.size()) selectedColumn = selectedRowColumns.size() - 1;

        var selectedCell = selectedRowColumns.get(selectedColumn);
        var pos = selectedCell.getPosition();
        InputShim.mouseMove((int) pos.getCenterX(), (int) pos.getCenterY());
    }

    @Override
    public void preInput(float amount) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.INTEL) InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        else if(intelTabData.getSelectedTabIndex() == 0) InputScreenManager.getInstance().transitionDelayed(IntelTabUI.ID, intelTabReflector);
        else if(intelTabData.getSelectedTabIndex() == 2) InputScreenManager.getInstance().transitionDelayed(IntelFactionTabUi.ID, intelTabReflector);
        if (controller.getButtonEvent(HandlerController.Buttons.LeftStickDown) == 1) {
            selectedRowIndex++;
            selectedColumn = 0;
            hoverSelectedItem();
        } else if (controller.getButtonEvent(HandlerController.Buttons.LeftStickUp) == 1) {
            selectedRowIndex--;
            selectedColumn = 0;
            hoverSelectedItem();
        } else if (controller.getButtonEvent(HandlerController.Buttons.LeftStickLeft) == 1) {
            selectedColumn--;
            hoverSelectedItem();
        } else if (controller.getButtonEvent(HandlerController.Buttons.LeftStickRight) == 1) {
            selectedColumn++;
            hoverSelectedItem();
        }
        if (controller.getButtonEvent(HandlerController.Buttons.LeftStickButton) == 1) {
            selectingPlanetFilters = !selectingPlanetFilters;
        } else if (controller.getButtonEvent(HandlerController.Buttons.A) == 1) {
            performActionOnHoveredItem();
        }
        if(controller.getButtonEvent(HandlerController.Buttons.B) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
        }
        campaignScope.handleInput(amount, true);
    }

    public static class PlanetTabReflector {
        final UIPanelAPI planetTabData;
        static MethodHandle getPlanetListV2;
        static MethodHandle getPlanetListTableRows;
        static MethodHandle selectPlanetTableRow;
        static Object getList;
        static Object ensurePlanetVisible;
        static Object planetItemColumnsField;

        static Class<?> tableItemCls;
        static Class<?> tableListCls;

        public PlanetTabReflector(UIPanelAPI planetTabData) {
            this.planetTabData = planetTabData;
        }

        public UIPanelAPI getPlanetTabData() {
            return planetTabData;
        }

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
                    if(children.size() > 1) {
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

        public static PlanetTabReflector tryGet(IntelTabReflector intelTabReflector) {
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

                        return new PlanetTabReflector(planetPanel);
                    } catch (Throwable ex) {
                        Global.getLogger(PlanetTabReflector.class).error("Couldn't reflect into planet tab!", ex);
                        return null;
                    }
                } else {
                    return new PlanetTabReflector(planetPanel);
                }
            }
            return null;
        }
    }
}
