package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.util.List;

public class MapReflector {
    CoreUIAPI coreUIAPI;
    Object mapObj;

    private MapReflector(CoreUIAPI coreUi, Object mapObj) {
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
        try {
            var children = UIPanelReflector.getChildItems((UIPanelAPI) coreUIAPI);
            if(!children.isEmpty()) {
                var btnPanel = children.get(children.size() - 1);
                return UIPanelReflector.getChildButtons((UIPanelAPI) btnPanel);
            }
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't get buttons from map!");
        }
        return null;
    }
}
