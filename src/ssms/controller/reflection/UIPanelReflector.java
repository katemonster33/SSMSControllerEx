package ssms.controller.reflection;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.ui.UIComponentAPI;
import org.apache.log4j.Level;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import ssms.controller.InputScreenManager;

public class UIPanelReflector extends UIComponentReflector {
    static Class<?> panelType;
    static MethodReflector getChildItemsHandle;
    UIPanelAPI panel;
    static
    {
        var testPnl = Global.getSettings().createCustom(1.f, 1.f, null);
        var reflector = new ClassReflector(testPnl.getClass().getSuperclass());
        var meth = reflector.getDeclaredMethod("getChildrenCopy");
        if (meth != null) {
            getChildItemsHandle = meth;
            UIPanelReflector.panelType = testPnl.getClass().getSuperclass();
        }
    }

    public UIPanelReflector(UIPanelAPI panel) {
        super(panel);
        this.panel = panel;
    }

    public UIPanelAPI getPanel() {
        return panel;
    }

    public List<?> getChildItems() {
        try {
            return (List<?>) getChildItemsHandle.invoke(panel);
        } catch(Throwable ex) {
            Global.getLogger(UIPanelReflector.class).fatal("Could not get child items of UIPanel! ", ex);
            return new ArrayList<>();
        }
    }

    public UIComponentAPI getLastChild() {

        var items = getChildItems();
        if (!items.isEmpty()) {
            var lastChild = items.get(items.size() - 1);
            if(InputScreenManager.getInstance().getDisplayPanel() != null && lastChild == InputScreenManager.getInstance().getDisplayPanel().getSubpanel()) {
                lastChild = items.get(items.size() - 2);
            }
            if(lastChild instanceof  UIComponentAPI componentAPI) {
                return componentAPI;
            }
        }
        return null;
    }

    public List<UIPanelAPI> getChildPanels(int ... args) {
        try {
            List<?> childrenTmp = getChildItems();
            for(int arg : args) {
                if(childrenTmp.size() > arg && childrenTmp.get(arg) instanceof UIPanelAPI uiPanelAPI) {
                    childrenTmp = new UIPanelReflector(uiPanelAPI).getChildItems();
                } else return new ArrayList<>();
            }
            List<UIPanelAPI> output = new ArrayList<>();
            for(Object child : childrenTmp) {
                if(child instanceof  UIPanelAPI panelAPI) {
                    output.add(panelAPI);
                }
            }
            return output;
        } catch(Throwable ex) {
            Global.getLogger(UIPanelReflector.class).fatal("Could not get nested child panels of UIPanel! ", ex);
            return new ArrayList<>();
        }
    }

    public List<ButtonAPI> getChildButtons()  {
        return getChildButtons(false);
    }

    public List<ButtonAPI> getChildButtons(boolean recursive)  {
        ArrayList<ButtonAPI> output = new ArrayList<>();
        List<?> childItems = getChildItems();
        for(Object childItem : childItems) {
            if (recursive && childItem instanceof UIPanelAPI uiPanelAPI) {
                output.addAll(new UIPanelReflector(uiPanelAPI).getChildButtons(true));
            } else if (childItem instanceof ButtonAPI buttonAPI) {
                output.add(buttonAPI);
            }
        }
        return output;
    }
}
