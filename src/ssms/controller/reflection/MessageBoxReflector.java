package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.save.LoadGameDialog;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageBoxReflector extends DialogBaseReflector {
    // The class representing the highest-order message dialog type
    static Class<?> messageBoxClass;

    static MethodHandle actionPerformed;
    static MethodReflector getOptionMap;
    static MethodReflector getInnerPanel;

    static {
        try {
            Class<?> clsTmp = LoadGameDialog.class.getSuperclass();
            ClassReflector msgBoxReflector = new ClassReflector(clsTmp);
            getOptionMap = msgBoxReflector.getDeclaredMethod("getOptionMap");

            getInnerPanel = msgBoxReflector.getDeclaredMethod("getInnerPanel");

            actionPerformed = MethodHandles.lookup().findVirtual(clsTmp, "actionPerformed", MethodType.methodType(void.class, Object.class, Object.class));

            messageBoxClass = clsTmp;
        } catch (Throwable ex) {
            throw new RuntimeException("Could not get a !", ex);
        }
    }

    public static boolean isMsgBox(UIPanelAPI obj) {
        return messageBoxClass.isAssignableFrom(obj.getClass());
    }

    public MessageBoxReflector(UIPanelAPI dialogObject) {
        super(dialogObject);
    }

    public List<ButtonAPI> getDialogButtons() {
        return getInnerPanel().getChildButtons(true);
    }

    public UIPanelReflector getInnerPanel() {
        return new UIPanelReflector((UIPanelAPI) getInnerPanel.invoke(panel));
    }

    public Object getDialogObject() {
        return panel;
    }

    public void doActionPerformed(Object obj1, Object obj2) {
        try {
            actionPerformed.invoke(panel, obj1, obj2);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Could not invoke actionPerformed on dialog!", ex);
        }
    }
}
