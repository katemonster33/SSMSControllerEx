package ssms.controller.reflection;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.campaign.save.LoadGameDialog;
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
        var panelClass = LoadGameDialog.class.getSuperclass().getSuperclass().getSuperclass();
        if(!UIPanelAPI.class.isAssignableFrom(panelClass)) {
            throw new RuntimeException("Panel class given did not match requirements!");
        }
        var reflector = new ClassReflector(panelClass);
        var meth = reflector.getDeclaredMethod("getChildrenCopy");
        if (meth != null) {
            getChildItemsHandle = meth;
            UIPanelReflector.panelType = panelClass;
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
        return (List<?>) getChildItemsHandle.invoke(panel);
    }

    public List<?> getChildItems(int ... args) {
        List<?> childrenTmp = getChildItems();
        for (int arg : args) {
            if (childrenTmp.size() > arg && childrenTmp.get(arg) instanceof UIPanelAPI uiPanelAPI) {
                childrenTmp = new UIPanelReflector(uiPanelAPI).getChildItems();
            } else return new ArrayList<>();
        }
        return childrenTmp;
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
        List<?> childrenTmp = getChildItems(args);
        List<UIPanelAPI> output = new ArrayList<>();
        for (Object child : childrenTmp) {
            if (child instanceof UIPanelAPI panelAPI) {
                output.add(panelAPI);
            }
        }
        return output;
    }

    List<UIPanelAPI> getPanelsOnTopOf(UIPanelReflector pnl) {
        var parent = pnl.getParent();
        List<UIPanelAPI> panelsOnTop = new ArrayList<>();
        if(parent != null) {
            var parentReflector = new UIPanelReflector(parent);
            var parentItems = parentReflector.getChildPanels();
            int pnlIndex = parentItems.indexOf(pnl.getPanel());
            if(pnlIndex != -1) {
                for(pnlIndex++;pnlIndex < parentItems.size(); pnlIndex++) {
                    panelsOnTop.add(parentItems.get(pnlIndex));
                }
            }
            panelsOnTop.addAll(getPanelsOnTopOf(parentReflector));
        }
        return panelsOnTop;
    }

    public List<UIPanelAPI> getPanelsOnTopOfMe() {
        return getPanelsOnTopOf(this);
    }

    public List<ButtonAPI> getChildButtons()  {
        return getChildButtons(false);
    }

    public List<ButtonAPI> getChildButtons(boolean recursive)  {
        ArrayList<ButtonAPI> output = new ArrayList<>();
        List<?> childItems = getChildItems();
        for(Object childItem : childItems) {
            if (recursive && childItem instanceof UIPanelAPI uiPanelAPI) {
                var reflector = new UIPanelReflector(uiPanelAPI);
                if(reflector.getFader().getBrightness() != 0.f) {
                    output.addAll(reflector.getChildButtons(true));
                }
            } else if (childItem instanceof ButtonAPI buttonAPI) {
                output.add(buttonAPI);
            }
        }
        return output;
    }
}
