package ssms.controller.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import ssms.controller.InputScreenBase;
import ssms.controller.reflection.ClassReflector;
import ssms.controller.reflection.DeploymentUiReflector;
import ssms.controller.reflection.MethodReflector;

import java.util.List;

public class BattleDeploymentScreen extends InputScreenBase {
    public static final String ID = "BattleDeployment";
    Object deploymentUi;
    DeploymentUiReflector dui; // hehe, dui
    int selectedButton = -1;
    List<ButtonAPI> dialogOptions;
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object ...args) {
        deploymentUi = args[0];
        dui = DeploymentUiReflector.TryGet(deploymentUi);
        if(dui != null) {
            dialogOptions = dui.getAllButtons();
        }

    }



    public void selectNextButton()
    {
        if(dialogOptions != null && !dialogOptions.isEmpty()) {
            int oldSelectedButton = selectedButton;
            if(selectedButton == -1) {
                selectedButton = 0;
            } else if(selectedButton < (dialogOptions.size() - 1)) {
                selectedButton++;
            }
            if(!dialogOptions.get(selectedButton).isEnabled()) {
                selectedButton++;
            }
            if(selectedButton >= dialogOptions.size()) {
                selectedButton = 0;
            }
            if(selectedButton != oldSelectedButton && oldSelectedButton != -1) {
                dialogOptions.get(oldSelectedButton).unhighlight();
            }
            dialogOptions.get(selectedButton).highlight();
        }
    }

    public void selectPrevButton()
    {
        if(dialogOptions != null && !dialogOptions.isEmpty()) {
            int oldSelectedButton = selectedButton;
            if(selectedButton == -1) {
                selectedButton = 0;
            } else if(selectedButton > 0) {
                selectedButton--;
            }
            if(!dialogOptions.get(selectedButton).isEnabled()) {
                selectedButton--;
            }
            if(selectedButton < 0) {
                selectedButton = dialogOptions.size() - 1;
            }
            if(selectedButton != oldSelectedButton && oldSelectedButton != -1) {
                dialogOptions.get(oldSelectedButton).unhighlight();
            }
            dialogOptions.get(selectedButton).highlight();
        }
    }

    public void clickButton()
    {
        if(selectedButton != -1 && dialogOptions != null && selectedButton < dialogOptions.size()) {
            dui.doActionPerformed(null, dialogOptions.get(selectedButton));
        }
    }

}
