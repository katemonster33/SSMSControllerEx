package ssms.controller.reflection;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

public class UIPanelReflector {
    static Class<? extends  UIPanelAPI> panelType;
    static Object getChildItemsHandle;
    public static void initialize(Class<? extends UIPanelAPI> panelType)
    {
        try {
            if(UIPanelAPI.class.isAssignableFrom(panelType)) {
                var meth = ClassReflector.GetInstance().findDeclaredMethod(panelType, "getChildrenCopy");
                if(meth != null) {
                    getChildItemsHandle = meth;
                    UIPanelReflector.panelType = panelType;
                }
            }
        } catch(Throwable ex) {
            Global.getLogger(UIPanelReflector.class).warn("Can't reflect panel type", ex );
        }
    }

    public static List<?> getChildItems(UIPanelAPI panel) {
        try {
            return (List<?>) MethodReflector.GetInstance().invoke(getChildItemsHandle, panel);
        } catch(Throwable ex) {
            Global.getLogger(UIPanelReflector.class).fatal("Could not get child items of UIPanel! ", ex);
            return new ArrayList<>();
        }
    }

    public static List<UIPanelAPI> getChildPanels(UIPanelAPI panel, int ... args) {
        try {
            List<?> childrenTmp = getChildItems(panel);
            for(int i = 0; i < args.length; i++) {
                if(childrenTmp.size() > args[i] && UIPanelAPI.class.isAssignableFrom(childrenTmp.get(args[i]).getClass()) ) {
                    childrenTmp = getChildItems((UIPanelAPI) childrenTmp.get(args[i]));
                } else return new ArrayList<>();
            }
            List<UIPanelAPI> output = new ArrayList<>();
            for(Object child : childrenTmp) {
                if((UIPanelAPI.class.isAssignableFrom(child.getClass()))) {
                    output.add((UIPanelAPI) child);
                }
            }
            return output;
        } catch(Throwable ex) {
            Global.getLogger(UIPanelReflector.class).fatal("Could not get nested child panels of UIPanel! ", ex);
            return new ArrayList<>();
        }
    }

    public static List<ButtonAPI> getChildButtons(UIPanelAPI panel) {
        return getChildButtons(panel, false);
    }

    public static List<ButtonAPI> getChildButtons(UIPanelAPI panel, boolean recursive) {
        ArrayList<ButtonAPI> output = new ArrayList<>();
        List<?> childItems = getChildItems(panel);
        for(Object childItem : childItems) {
            if (recursive && UIPanelAPI.class.isAssignableFrom(childItem.getClass())) {
                output.addAll(getChildButtons((UIPanelAPI) childItem, true));
            } else if (ButtonAPI.class.isAssignableFrom(childItem.getClass())) {
                output.add((ButtonAPI) childItem);
            }
        }
        return output;
    }
}
