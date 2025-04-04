package ssms.controller.reflection;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

public class UIPanelReflector {
    static Class<?> panelType;
    static Object getChildItemsHandle;
    public static void initialize(Class<?> panelType)
    {
        try {
            Object[] methods = ClassReflector.GetInstance().getDeclaredMethods(panelType);
            for(var method : methods) {
                if(MethodReflector.GetInstance().getName(method) == "getChildrenCopy") {
                    getChildItemsHandle = method;
                    break;
                }
            }
        } catch(Throwable ex) {
            Global.getLogger(UIPanelReflector.class).log(Level.WARN, "Can't reflect panel type: " + panelType + ", " + ex.getMessage() );
        }
    }

    public static List<?> getChildItems(UIPanelAPI panel) {
        try {
            return (List<?>) MethodReflector.GetInstance().invoke(getChildItemsHandle, panel);
        } catch(Throwable ex) {
            Global.getLogger(UIPanelReflector.class).log(Level.FATAL, "Could not get child items of UIPanel! " + ex);
            return new ArrayList<>();
        }
    }

    public static List<ButtonAPI> getChildButtons(UIPanelAPI panel) {
        ArrayList<ButtonAPI> output = new ArrayList<>();
        List<?> childItems = getChildItems(panel);
        for(Object childItem : childItems) {
            if(ButtonAPI.class.isAssignableFrom(childItem.getClass())) {
                output.add((ButtonAPI)childItem);
            }
        }
        return output;
    }
}
