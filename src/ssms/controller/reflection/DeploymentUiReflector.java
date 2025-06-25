package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import java.util.ArrayList;
import java.util.List;

public class DeploymentUiReflector {
    UIPanelAPI deploymentUiObj;
    static MethodReflector getInnerPanel;

    public DeploymentUiReflector(UIPanelAPI deploymentUiObject) {
        this.deploymentUiObj = deploymentUiObject;
        if(getInnerPanel == null) {
            getInnerPanel = new ClassReflector(deploymentUiObject.getClass()).findDeclaredMethod("getInnerPanel");
        }
    }

    public List<ButtonAPI> getAllButtons() {
        var innerPanel = new UIPanelReflector((UIPanelAPI) getInnerPanel.invoke(deploymentUiObj));
        return innerPanel.getChildButtons(true);
    }

    public Object getDialogObject() {
        return deploymentUiObj;
    }
}
