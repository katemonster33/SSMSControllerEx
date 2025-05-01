package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.coreui.P;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class WarroomReflector {
    static Class<?> warroomClass;
    UIPanelAPI warroomObject;

    public WarroomReflector(UIPanelAPI warroomObj) {
        this.warroomObject = warroomObj;
    }

    public UIPanelAPI getPrivateObj() {
        return warroomObject;
    }
}
