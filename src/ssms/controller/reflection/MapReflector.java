package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.comms.v2.EventsPanel;

import java.util.ArrayList;
import java.util.List;

public class MapReflector extends UIPanelReflector {
    static Class<?> mapType;
    static MethodReflector getMap;
    static MethodReflector getScroller;

    static {
        ClassReflector eventsCls = new ClassReflector(EventsPanel.class);
        var getMapTab = eventsCls.getDeclaredMethod("getMap");

        var clsReflector = new ClassReflector(getMapTab.getReturnType());

        getMap = clsReflector.getDeclaredMethod("getMap");

        getScroller = clsReflector.getDeclaredMethod("getScroller");

        mapType = getMapTab.getReturnType();
    }

    public MapReflector(UIPanelAPI mapObj) {
        super(mapObj);
    }

    public static boolean isAssignableFrom(Class<?> cls) {
        return mapType.isAssignableFrom(cls);
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
