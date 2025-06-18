package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.util.ArrayList;
import java.util.List;

public class MapReflector {
    CoreUIAPI coreUIAPI;
    UIComponentAPI mapObj;

    private MapReflector(CoreUIAPI coreUi, UIComponentAPI mapObj) {
        this.coreUIAPI = coreUi;
        this.mapObj = mapObj;
    }

    public static MapReflector TryGet(CoreUIAPI coreUIAPI, BorderedPanelReflector borderedPanelReflector) {
        try {
            var mapObj = borderedPanelReflector.getPanel();
            return new MapReflector(coreUIAPI, mapObj);
        } catch(Throwable ex) {
            Global.getLogger(MapReflector.class).error("Couldn't fetch map UI!");
            return null;
        }
    }

    // gets the buttons visible when you click and hold a location on the map.
    public List<ButtonAPI> getButtons() {
        List<ButtonAPI> buttons = new ArrayList<>();
        try {
            buttons.addAll(UIPanelReflector.getChildButtons((UIPanelAPI) mapObj, true));
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't get buttons from map!");
        }
        return buttons;
    }

    public UIComponentAPI getMap() {
        return mapObj;
    }
}
