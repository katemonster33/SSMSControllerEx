package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import java.util.ArrayList;
import java.util.List;

public class DeploymentUiReflector {
    UIPanelAPI deploymentUiObj;

    public DeploymentUiReflector(UIPanelAPI deploymentUiObject) {
        this.deploymentUiObj = deploymentUiObject;
    }

    public List<ButtonAPI> getAllButtons() {
        List<ButtonAPI> output = new ArrayList<>();
        try {
            var getInnerPanel = ClassReflector.GetInstance().findDeclaredMethod(deploymentUiObj.getClass(), "getInnerPanel");
            var innerPanel = (UIPanelAPI) MethodReflector.GetInstance().invoke(getInnerPanel, deploymentUiObj);
            output.addAll(UIPanelReflector.getChildButtons(innerPanel, true));
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't fetch buttons of dialog!");
        }
        return output;
    }

    public Object getDialogObject() {
        return deploymentUiObj;
    }
}
