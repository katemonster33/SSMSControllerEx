package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class DeploymentUiReflector {
    MethodHandle dismiss;
    Object isBeingDismissed;
    MethodHandle actionPerformed;
    MethodHandle shipActionPerformed;
    Object deploymentUiObj;
    Object shipPanel;
    private DeploymentUiReflector(Object deploymentUiObject) throws Throwable {
        this.deploymentUiObj = deploymentUiObject;
        var lookup = MethodHandles.lookup();
        dismiss = lookup.findVirtual(deploymentUiObj.getClass(), "dismiss", MethodType.methodType(void.class, int.class));
        actionPerformed = lookup.findVirtual(deploymentUiObj.getClass(), "actionPerformed", MethodType.methodType(void.class, Object.class, Object.class));
        isBeingDismissed = ClassReflector.GetInstance().getDeclaredMethod(deploymentUiObj.getClass().getSuperclass().getSuperclass(), "isBeingDismissed");
    }

    public boolean isBeingDismissed(){
        try {
            MethodReflector.GetInstance().setAccessible(isBeingDismissed, true);

            return (boolean) MethodReflector.GetInstance().invoke(isBeingDismissed, deploymentUiObj);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't tell if deployment UI is being dismissed!");
        }
        return false;
    }

    public List<ButtonAPI> getShips() {
        List<ButtonAPI> output = new ArrayList<>();
        try {
            var getInnerPanel = ClassReflector.GetInstance().findDeclaredMethod(deploymentUiObj.getClass(), "getInnerPanel");
            var innerPanel = MethodReflector.GetInstance().invoke(getInnerPanel, deploymentUiObj);
            var childItems = UIPanelReflector.getChildItems((UIPanelAPI) innerPanel);
            shipPanel = childItems.get(2);
            shipActionPerformed = MethodHandles.lookup().findVirtual(shipPanel.getClass(), "actionPerformed", MethodType.methodType(void.class, Object.class, Object.class));
            var toDeployItemsChildren = UIPanelReflector.getChildItems((UIPanelAPI) shipPanel);
            if(toDeployItemsChildren.size() == 1) {
                var tableViewPanel = UIPanelReflector.getChildItems((UIPanelAPI) toDeployItemsChildren.get(0));
                if(toDeployItemsChildren.size() == 1) {
                    var scrollableViewPanel = UIPanelReflector.getChildItems((UIPanelAPI) tableViewPanel.get(0));
                    if(!scrollableViewPanel.isEmpty()) {
                        var ships = UIPanelReflector.getChildItems((UIPanelAPI) scrollableViewPanel.get(0));
                        for(var ship : ships) {
                            if(ButtonAPI.class.isAssignableFrom(ship.getClass())) {
                                output.add((ButtonAPI) ship);
                            }
                        }
                    }
                }
            }
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't fetch buttons of dialog!");
        }
        return output;
    }

    public List<ButtonAPI> getAllButtons() {
        List<ButtonAPI> output = new ArrayList<>();
        try {
            var getInnerPanel = ClassReflector.GetInstance().findDeclaredMethod(deploymentUiObj.getClass(), "getInnerPanel");
            var innerPanel = MethodReflector.GetInstance().invoke(getInnerPanel, deploymentUiObj);
            output.addAll(UIPanelReflector.getChildButtons((UIPanelAPI) innerPanel));
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't fetch buttons of dialog!");
        }
        return output;
    }

    public static DeploymentUiReflector TryGet(Object obj) {
        // here we determine whether our object is the deployment UI based on the methods it contains, what is my life
        try {
            return new DeploymentUiReflector(obj);
        } catch(Throwable ex) {
            Global.getLogger(DeploymentUiReflector.class).fatal("Error while attempting to create DialogReflector from existing object!", ex);
        }
        return null;
    }

    public Object getDialogObject() {
        return deploymentUiObj;
    }

    public void doActionPerformedShip(ButtonAPI ship) {
        try {
            shipActionPerformed.invoke(shipPanel, null, ship);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Could not invoke actionPerformed on dialog!", ex);
        }
    }

    public void doActionPerformed(Object obj1, Object obj2) {
        try {
            actionPerformed.invoke(deploymentUiObj, obj1, obj2);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Could not invoke actionPerformed on dialog!", ex);
        }
    }

    public void doDismiss() {
        try {
            dismiss.invoke(deploymentUiObj, 1);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Could not invoke dismiss on dialog!", ex);
        }
    }
}
