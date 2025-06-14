package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.coreui.P;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class WarroomReflector {
    static Class<?> warroomClass;
    UIPanelAPI warroomObject;
    UIPanelAPI buttonGroupPanel;
    public WarroomReflector(UIPanelAPI warroomObj) {
        this.warroomObject = warroomObj;
        this.buttonGroupPanel = (UIPanelAPI) UIPanelReflector.getChildItems(warroomObj).get(0);
    }

    public UIPanelAPI getPrivateObj() {
        return warroomObject;
    }

    public List<List<ButtonAPI>> getButtonGroups() {
        List<List<ButtonAPI>> buttonGroups = new ArrayList<>();
        var groupPanelChildren = UIPanelReflector.getChildItems(buttonGroupPanel);

        for(var grp : groupPanelChildren) {
            if(UIPanelAPI.class.isAssignableFrom(grp.getClass())) {
                var grpChildren = UIPanelReflector.getChildItems((UIPanelAPI) grp);
                List<ButtonAPI> buttons = new ArrayList<>();
                for(var child : grpChildren) {
                    if(ButtonAPI.class.isAssignableFrom(child.getClass())) {
                        buttons.add((ButtonAPI) child);
                    }
                }
                if(!buttons.isEmpty()) {
                    buttonGroups.add(buttons);
                }
            }
        }
        return buttonGroups;
    }

    public List<ButtonAPI> getButtons() {
        return UIPanelReflector.getChildButtons(buttonGroupPanel, true);
    }
}
