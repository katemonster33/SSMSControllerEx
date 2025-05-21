package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CampaignEngine;
import com.fs.starfarer.campaign.comms.IntelTabData;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.reflection.IntelTabReflector;
import ssms.controller.reflection.UIPanelReflector;

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
    int selectedRowIndex = -1;
    int selectedColumn = -1;
    boolean selectingPlanetFilters = false;
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String[] getScopes() {
        return new String[] { CampaignScope.ID };
    }

    List<Pair<Indicators, String>> getIntelIndicators() {
        List<Pair<Indicators, String>> output = new ArrayList<>();
        output.add(new Pair<>(Indicators.LeftStick, "Navigate"));
        output.add(new Pair<>(Indicators.LeftTrigger, "Select prev mission"));
        output.add(new Pair<>(Indicators.RightTrigger, "Select next mission"));
        output.add(new Pair<>(Indicators.B, "Close"));
        return output;
    }

    List<Pair<Indicators, String>> getPlanetIndicators() {
        List<Pair<Indicators, String>> output = new ArrayList<>();
        output.add(new Pair<>(Indicators.LeftStick, "Navigate"));
        output.add(new Pair<>(Indicators.A, "Select"));
        output.add(new Pair<>(Indicators.B, "Close"));
        return output;
    }

    List<Pair<Indicators, String>> getFactionIndicators() {
        List<Pair<Indicators, String>> output = new ArrayList<>();
        output.add(new Pair<>(Indicators.LeftStick, "Navigate"));
        output.add(new Pair<>(Indicators.A, "Select"));
        output.add(new Pair<>(Indicators.B, "Close"));
        return output;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = switch (intelTabData.getSelectedTabIndex()) {
                case 0 -> getIntelIndicators();
                case 1 -> getPlanetIndicators();
                case 2 -> getFactionIndicators();
                default -> new ArrayList<>();
            };
        }
        return indicators;
    }

    @Override
    public void activate(Object ... args) {
        intelTabReflector = (IntelTabReflector) args[0];
        intelTabData = CampaignEngine.getInstance().getUIData().getIntelData();
        lastFrameSelectedIndex = intelTabData.getSelectedTabIndex();
        campaignScope = (CampaignScope) InputScreenManager.getInstance().getCurrentScope();
        controller = SSMSControllerModPluginEx.controller;
    }

    UIPanelAPI getSelectedPlanetRow() {
        var rows = intelTabReflector.getPlanetsTableRows();
        if(rows.isEmpty()) {
            selectedRowIndex = selectedColumn = -1;
            return null;
        }

        if(selectedRowIndex < 0) selectedRowIndex = 0;
        else if(selectedRowIndex >= rows.size()) selectedRowIndex = rows.size() - 1;

        return rows.get(selectedRowIndex);
    }

    List<UIComponentAPI> getPlanetColumns(UIPanelAPI planet) {
        var rowColumns = intelTabReflector.getPlanetSubItems(planet);

        if(selectedColumn < 0) selectedColumn = 0;
        else if(selectedColumn >= rowColumns.size()) selectedColumn = rowColumns.size() - 1;

        return rowColumns;
    }

    UIComponentAPI getSelectedCell() {

        var rows = getPlanetRows();
        if (rows.isEmpty()) {
            return;
        }

        var selectedRow = rows.get(selectedRowIndex);
        intelTabReflector.ensurePlanetVisible(selectedRow);
        var columns = getPlanetColumns(selectedRow);
        if (columns.isEmpty()) {
            return;
        }
    }

    void hoverSelectedItem() {

        var pos = columns.get(selectedColumn).getPosition();
        InputShim.mouseMove((int) pos.getCenterX(), (int) pos.getCenterY());
    }

    void performActionOnHoveredItem() {
        var rows = getPlanetRows();
        if(rows.isEmpty()) {
            return;
        }

        var columns = getRowColumns(rows.get(selectedRowIndex));
        if(columns.isEmpty()) {
            return;
        }

        var pos = columns.get(selectedColumn).getPosition();
        if(selectingPlanetFilters || selectedColumn == 0 || selectedColumn == columns.size() - 1) {
            InputShim.mouseDownUp((int) pos.getCenterX(), (int) pos.getCenterY(), InputEventMouseButton.LEFT);
        } else {
            InputShim.keyDownUp(Keyboard.KEY_F2, '\0');
        }
    }

    void preInputIntelTab(float amount) {

    }

    void preInputPlanetTab(float amount) {

        var planets = intelTabReflector.getPlanetsTableRows();
        List<List<UIComponentAPI>> buttonRows = new ArrayList<>();
//        var children = UIPanelReflector.getChildItems(intelTabReflector.getPlanetTabData());
//        if(children.size() == 2) {
//            children = UIPanelReflector.getChildItems((UIPanelAPI) children.get(1));
//            if (children.size() == 6) {
//                children = UIPanelReflector.getChildItems((UIPanelAPI) children.get(5));
//                if (children.size() == 2) {
//                    children = UIPanelReflector.getChildItems((UIPanelAPI) children.get(0));
//                    if (children.size() > 1) {
//                        for (int i = 1; i < children.size(); i++) {
//                            for (var grp : UIPanelReflector.getChildItems((UIPanelAPI) children.get(i))) {
//                                if(UIPanelAPI.class.isAssignableFrom(grp.getClass())) {
//                                    for (var rowPanel : UIPanelReflector.getChildItems((UIPanelAPI) grp)) {
//                                        if (UIPanelAPI.class.isAssignableFrom(rowPanel.getClass())) {
//                                            buttonRows.add(new ArrayList<>(UIPanelReflector.getChildButtons((UIPanelAPI) rowPanel)));
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }

        for(UIPanelAPI buttonGroup : UIPanelReflector.getChildPanels(intelTabReflector.getPlanetTabData(), 1, 5, 0).stream().flatMap((UIPanelAPI child) -> UIPanelReflector.getChildPanels(child).stream()).toList()) {
            for(UIPanelAPI buttonRow : UIPanelReflector.getChildPanels(buttonGroup)) {
                var btnList = UIPanelReflector.getChildButtons(buttonRow);
                if(!btnList.isEmpty()) buttonRows.add(btnList);
            }
        }
        if(!planets.isEmpty()) {
            try {
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
                } if(controller.getButtonEvent(HandlerController.Buttons.LeftStickButton) == 1) {
                    selectingPlanetFilters = !selectingPlanetFilters;
                } else if(controller.getButtonEvent(HandlerController.Buttons.A) == 1) {
                    performActionOnHoveredItem();
                }
            } catch(Throwable ex) {
                Global.getLogger(getClass()).warn("Error!", ex);
            }
            //intelTabReflector.setSelectPlanetTableRow(planets.get(0), null, true);
        } else selectedRowIndex = -1;
    }

    void preInputFactionTab(float amount) {

    }

    @Override
    public void preInput(float amount) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.INTEL) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
        }
        switch(intelTabData.getSelectedTabIndex()) {
            case 0 -> preInputIntelTab(amount);
            case 1 -> preInputPlanetTab(amount);
            case 2 -> preInputFactionTab(amount);
        }
        if(intelTabData.getSelectedTabIndex() != lastFrameSelectedIndex) {
            indicators = null;
            InputScreenManager.getInstance().refreshIndicators();
        }
        lastFrameSelectedIndex = intelTabData.getSelectedTabIndex();
        if(controller.getButtonEvent(HandlerController.Buttons.B) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
        }
        campaignScope.handleInput(amount, true);
    }
}
