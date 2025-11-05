package ssms.controller.reflection;


import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.coreui.refit.wgd2.WeaponGroupDialogV2;

public final class WeaponGroupRowReflector {
    static Class<?> weaponGroupRowClass;
    static {
        ClassReflector classReflector = new ClassReflector(WeaponGroupDialogV2.class);
        var tableRowSelected = classReflector.findDeclaredMethod("tableRowSelected");

        weaponGroupRowClass = tableRowSelected.getParameterTypes()[1];
    }

    public static boolean isWeaponGroupRow(UIPanelAPI uiPanelAPI) {
        return weaponGroupRowClass.isAssignableFrom(uiPanelAPI.getClass());
    }
}
