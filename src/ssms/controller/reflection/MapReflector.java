package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.util.ArrayList;
import java.util.List;

public class MapReflector extends UIPanelReflector {
    CoreUIAPI coreUIAPI;
    static Class<?> mapType;
    static MethodReflector getMap;
    static MethodReflector getScroller;

    private MapReflector(CoreUIAPI coreUi, UIPanelAPI mapObj) {
        super(mapObj);
        this.coreUIAPI = coreUi;
    }

    public static MapReflector TryGet(CoreUIAPI coreUIAPI, BorderedPanelReflector borderedPanelReflector) {
        var mapObj = borderedPanelReflector.getInnerPanel();
        if (mapType == null) {
            var clsReflector = new ClassReflector(mapObj.getPanel().getClass());
            getMap = clsReflector.getDeclaredMethod("getMap");

            getScroller = clsReflector.getDeclaredMethod("getScroller");
            if(getMap != null && getScroller != null) {
                mapType = mapObj.getClass();
                return new MapReflector(coreUIAPI, mapObj.getPanel());
            }
        } else if (mapType.isAssignableFrom(mapObj.getClass())) {
            return new MapReflector(coreUIAPI, mapObj.getPanel());
        }
        Global.getLogger(MapReflector.class).error("Couldn't fetch map UI!");
        return null;
    }

    // gets the buttons visible when you click and hold a location on the map.
    public List<ButtonAPI> getButtons() {
        List<ButtonAPI> buttons = new ArrayList<>();
        try {
            buttons.addAll(getChildButtons(true));
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't get buttons from map!");
        }
        return buttons;
    }

    public UIPanelAPI getMap() {
        return (UIPanelAPI) getMap.invoke(panel);
    }

    public UIPanelAPI getScroller() {
        return (UIPanelAPI) getScroller.invoke(panel);
    }
}
