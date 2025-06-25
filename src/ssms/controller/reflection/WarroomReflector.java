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

public class WarroomReflector extends UIPanelReflector {
    static Class<?> warroomClass;
    UIPanelReflector buttonGroupPanel;
    public WarroomReflector(UIPanelAPI warroomObj) {
        super(warroomObj);
        this.buttonGroupPanel = new UIPanelReflector(getChildPanels().get(0));
    }
}
