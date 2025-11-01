package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CampaignEngine;
import com.fs.starfarer.campaign.StarSystem;
import com.fs.starfarer.campaign.comms.IntelTabData;
import com.fs.starfarer.campaign.ui.UITable;
import com.fs.starfarer.campaign.ui.intel.PlanetListV2;
import com.fs.starfarer.campaign.ui.intel.StarSystemDisplay;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.CodexUI;
import ssms.controller.inputhelper.DigitalJoystickHandler;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class IntelPlanetTabUi extends InputScreenBase {
    public static final String ID = "IntelPlanetTab";
    List<List<ButtonAPI>> filterButtonRows = null;
    PlanetTabReflector planetTabReflector;
    IntelTabData intelTabData;
    IntelTabReflector intelTabReflector;
    int selectedRowIndex = -1;
    int selectedColumn = -1;
    List<UIComponentAPI> selectedRowColumns;
    UIPanelAPI selectedPlanet;
    int selectedPlanetX, selectedPlanetY;
    InteractionDialogReflector interactionDialogReflector;
    boolean selectingPlanetFilters = false;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object ... args) {
        if(args.length > 0) {
            intelTabReflector = (IntelTabReflector) args[0];

            planetTabReflector = PlanetTabReflector.tryGet(intelTabReflector);
            intelTabData = CampaignEngine.getInstance().getUIData().getIntelData();
        }

        filterButtonRows = new ArrayList<>();
        for (UIPanelAPI buttonGroup : planetTabReflector.getChildPanels(1, 5, 0)) {
            for (UIPanelAPI buttonRow : new UIPanelReflector(buttonGroup).getChildPanels()) {
                for(UIPanelAPI buttonInnerPanel : new UIPanelReflector(buttonRow).getChildPanels()) {
                    var btnList = new UIPanelReflector(buttonInnerPanel).getChildButtons();
                    if (!btnList.isEmpty()) filterButtonRows.add(btnList);
                }
            }
        }
        indicators = new ArrayList<>();
        interactionDialogReflector = InteractionDialogReflector.getCurrentInstance();

        addDigitalJoystickHandler("Navigate", Joystick.DPad, new DigitalJoystickHandler() {
            @Override
            public void performUpAction(float advance) {
                selectedRowIndex--;
                selectedColumn = 0;
                hoverSelectedItem();
            }

            @Override
            public void performDownAction(float advance) {
                selectedRowIndex++;
                selectedColumn = 0;
                hoverSelectedItem();
            }

            @Override
            public void performLeftAction(float advance) {
                selectedColumn--;
                hoverSelectedItem();
            }

            @Override
            public void performRightAction(float advance) {
                selectedColumn++;
                hoverSelectedItem();
            }
        });
        addButtonPressHandler("Select", LogicalButtons.A, (float advance) -> performActionOnHoveredItem());
        addButtonPressHandler("Close", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        addButtonPressHandler("Select map tab", LogicalButtons.BumperLeft, new KeySender(Keyboard.KEY_TAB));
        addButtonPressHandler("Select command tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_D, 'd'));
        addButtonPressHandler("Select intel tab", LogicalButtons.LeftTrigger, new KeySender(Keyboard.KEY_1, '1'));
        addButtonPressHandler("Select planet tab", LogicalButtons.RightTrigger, new KeySender(Keyboard.KEY_3, '3'));
        selectedPlanetX = selectedPlanetY = -1;
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
            selectedPlanet = null;
            selectedRowColumns = new ArrayList<>(filterButtonRows.get(selectedRowIndex));
        } else {
            selectedPlanet = (UIPanelAPI) rows.get(selectedRowIndex);
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
        if(!selectingPlanetFilters && selectedPlanet != null) {
            planetTabReflector.ensurePlanetVisible(selectedPlanet);
            selectedPlanetX = (int) pos.getX();
            selectedPlanetY = (int) pos.getY();
        } else {
            selectedPlanetX = selectedPlanetY = -1;
        }
        InputShim.mouseMove((int) pos.getCenterX(), (int) pos.getCenterY());
    }

    @Override
    public void preInput(float amount) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.INTEL) InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        else if(intelTabData.getSelectedTabIndex() == 0) InputScreenManager.getInstance().transitionDelayed(IntelTabUI.ID, intelTabReflector);
        else if(intelTabData.getSelectedTabIndex() == 1 && planetTabReflector.getStarSystem() != null) InputScreenManager.getInstance().transitionDelayed(IntelPlanetStarSystemUI.ID, intelTabReflector);
        else if(intelTabData.getSelectedTabIndex() == 2) InputScreenManager.getInstance().transitionDelayed(IntelFactionTabUi.ID, intelTabReflector);

        if(isCodexOpen()) {
            InputScreenManager.getInstance().transitionDelayed(CodexUI.ID, getId());
        }

        if(interactionDialogReflector != null && !interactionDialogReflector.isCoreUiOpen()) {
            InputScreenManager.getInstance().transitionDelayed(DialogUI.ID);
        }

        if(selectedPlanet != null) {
            int selPlanetCurX = (int) selectedPlanet.getPosition().getX(), selPlanetCurY = (int) selectedPlanet.getPosition().getY();
            if(selPlanetCurX != selectedPlanetX || selPlanetCurY != selectedPlanetY) {
                hoverSelectedItem();
            }
        }
    }

    public static class PlanetTabReflector extends UIPanelReflector {
            static MethodHandle getPlanetListV2;
            static MethodHandle getPlanetListTableRows;
            static MethodHandle selectPlanetTableRow;
            static MethodReflector getList;
            static MethodReflector ensurePlanetVisible;
            static FieldReflector planetItemColumnsField;
            static FieldReflector starSystemField;
            static Class<?> tableItemCls;
            static Class<?> tableListCls;
            static FieldReflector mapField;
            static FieldReflector starSystemDisplayField;
            static FieldReflector planetInfoPanelField;

            public PlanetTabReflector(UIPanelAPI planetTabData) {
                super(planetTabData);
            }

            public PlanetListV2 getPlanetList() {
                try {
                    return (PlanetListV2) getPlanetListV2.invoke(getPanel());
                } catch (Throwable ex) {
                    Global.getLogger(getClass()).error("Couldn't get planet list!", ex);
                    return null;
                }
            }

            public void ensurePlanetVisible(Object planetObj) {
                try {
                    var buttonField = new ClassReflector(planetObj.getClass().getSuperclass().getSuperclass()).getDeclaredField("button");

                    var btn = (ButtonAPI) buttonField.get(planetObj);

                    var lst = getList.invoke(getPlanetList().getTable());
                    ensurePlanetVisible.invoke(lst, btn);
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

            public StarSystem getStarSystem() {
                return (StarSystem) starSystemField.get(getPanel());
            }

        public UIComponentAPI getMap() {
            return (UIComponentAPI) mapField.get(getPanel());
        }

            public List<UIComponentAPI> getPlanetSubItems(UIPanelAPI planetItem) {
                List<UIComponentAPI> output = new ArrayList<>();
                try {
                    List<?> columns = (List<?>) planetItemColumnsField.get(planetItem);
                    if (columns != null && columns.size() > 5 && columns.get(0) instanceof UIComponentAPI planetSpriteItem) {
                        output.add(planetSpriteItem);

                        var children = new UIPanelReflector((UIPanelAPI) columns.get(1)).getChildItems();
                        if (children.size() > 1 && children.get(0) instanceof UIPanelAPI subItemsPanel) {;

                            for (var child : new UIPanelReflector(subItemsPanel).getChildItems()) {
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

        public StarSystemDisplay getStarSystemDisplay() {
            return (StarSystemDisplay) starSystemDisplayField.get(getPanel());
        }

        public UIPanelAPI getPlanetInfoPanel() {
            return (UIPanelAPI) planetInfoPanelField.get(getPanel());
        }

        public static PlanetTabReflector tryGet(IntelTabReflector intelTabReflector) {
            var planetPanel = intelTabReflector.getPlanetTabData();
            if (planetPanel != null) {
                if (planetItemColumnsField == null) {
                    try {
                        getPlanetListV2 = MethodHandles.lookup().findVirtual(planetPanel.getPanel().getClass(), "getPlanetList2", MethodType.methodType(PlanetListV2.class));

                        getPlanetListTableRows = MethodHandles.lookup().findVirtual(UITable.class, "getRows", MethodType.methodType(List.class));

                        for(var field : new ClassReflector(planetPanel.getPanel().getClass()).getDeclaredFields()) {
                            var fieldCls = field.getType();
                            if(StarSystem.class.isAssignableFrom(fieldCls)) {
                                starSystemField = field;
                            } else if(SectorMapAPI.class.isAssignableFrom(fieldCls)) {
                                mapField = field;
                            } else if(StarSystemDisplay.class.isAssignableFrom(fieldCls)) {
                                starSystemDisplayField = field;
                            } else if(planetInfoPanelField == null && UIPanelAPI.class.isAssignableFrom(fieldCls)) {
                                planetInfoPanelField = field;
                            }
                        }
                        var uiTableReflector = new ClassReflector(UITable.class);
                        var getSelected = uiTableReflector.getDeclaredMethod("getSelected");
                        tableItemCls = getSelected.getReturnType();

                        getList = uiTableReflector.getDeclaredMethod("getList");

                        tableListCls = getList.getReturnType();

                        ensurePlanetVisible =  new ClassReflector(tableListCls).findDeclaredMethod("ensureVisible");
                        planetItemColumnsField = new ClassReflector(tableItemCls).getDeclaredField("columns");

                        return new PlanetTabReflector(planetPanel.getPanel());
                    } catch (Throwable ex) {
                        Global.getLogger(PlanetTabReflector.class).error("Couldn't reflect into planet tab!", ex);
                        return null;
                    }
                } else {
                    return new PlanetTabReflector(planetPanel.getPanel());
                }
            }
            return null;
        }
    }
}
