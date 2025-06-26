package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

public class FleetTabReflector {
    CoreUIAPI coreUIAPI;
    UIPanelReflector fleetListObj, sidePanelObj, fleetGridPanelObj;

    static MethodHandle getItemHeight;
    static MethodHandle getItemWidth;
    static MethodHandle getItems;
    static MethodHandle getColumns;

    static Class<?> fleetListObjCls;

    private FleetTabReflector(CoreUIAPI parentCoreUi, UIPanelReflector fleetListObj, UIPanelReflector sidePanelObj, UIPanelReflector fleetGridPanelObj) {
        this.coreUIAPI = parentCoreUi;
        this.fleetListObj = fleetListObj;
        this.sidePanelObj = sidePanelObj;
        this.fleetGridPanelObj = fleetGridPanelObj;
    }

    public List<ButtonAPI> getButtons() {
        var buttons = fleetListObj.getChildButtons(true);
        buttons.addAll(sidePanelObj.getChildButtons(true));
        return buttons;
    }

    public float getItemHeight() {
        try {
            return (float) getItemHeight.invoke(fleetGridPanelObj);
        } catch (Throwable ex) {
            Global.getLogger(getClass()).error("Failed to get height of items!", ex);
            return -1.f;
        }
    }

    public float getItemWidth() {
        try {
            return (float) getItemWidth.invoke(fleetGridPanelObj);
        } catch (Throwable ex) {
            Global.getLogger(getClass()).error("Failed to get width of items!", ex);
            return -1.f;
        }
    }

    public List<?> getItems() {
        try {
            return (List<?>) getItems.invoke(fleetGridPanelObj);
        } catch (Throwable ex) {
            Global.getLogger(getClass()).error("Failed to get items!", ex);
            return null;
        }
    }

    public int getColumns() {
        try {
            return (int) getColumns.invoke(fleetGridPanelObj);
        } catch (Throwable ex) {
            Global.getLogger(getClass()).error("Failed to get number of rows!", ex);
            return -1;
        }
    }

    public static FleetTabReflector TryGet(CoreUIAPI coreUIAPI, BorderedPanelReflector borderedPanelReflector) {
        var children = borderedPanelReflector.getInnerPanel().getChildPanels();
        if (children.size() == 2) {
            UIPanelReflector fleetListObj = new UIPanelReflector(children.get(0));
            UIPanelReflector sidePanelObj = new UIPanelReflector(children.get(1));
            var fleetLstChildren = fleetListObj.getChildItems();
            if (fleetLstChildren.size() == 2 && fleetLstChildren.get(1) instanceof UIPanelAPI fleetGridPanel) {

                if (fleetListObjCls == null) {
                    try {
                        var lookup = MethodHandles.lookup();
                        getItemHeight = lookup.findVirtual(fleetGridPanel.getClass(), "getItemHeight", MethodType.methodType(float.class));

                        getItemWidth = lookup.findVirtual(fleetGridPanel.getClass(), "getItemWidth", MethodType.methodType(float.class));

                        getItems = lookup.findVirtual(fleetGridPanel.getClass(), "getItems", MethodType.methodType(List.class));

                        getColumns = lookup.findVirtual(fleetGridPanel.getClass(), "getColumns", MethodType.methodType(int.class));

                        fleetListObjCls = fleetListObj.getPanel().getClass();

                        return new FleetTabReflector(coreUIAPI, fleetListObj, sidePanelObj, new UIPanelReflector(fleetGridPanel));
                    } catch (Throwable ex) {
                        Global.getLogger(FleetTabReflector.class).error("Couldn't reflect into FleetTab's UI!", ex);
                        return null;
                    }
                } else if (fleetListObjCls.isAssignableFrom(fleetListObj.getPanel().getClass())) {
                    return new FleetTabReflector(coreUIAPI, fleetListObj, sidePanelObj, new UIPanelReflector(fleetGridPanel));
                }
            }
        }
        return null;
    }
}
