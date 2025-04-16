package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import org.apache.log4j.Level;
import ssms.controller.HandlerController;
import ssms.controller.Indicators;
import ssms.controller.SSMSControllerModPluginEx;
import ssms.controller.InputScopeBase;
import ssms.controller.InputScreenBase;
import ssms.controller.reflection.UIPanelReflector;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class DialogUI extends InputScreenBase {
    UIPanelAPI optionsPanel;
    List<ButtonAPI> dialogOptions;
    MethodHandle doButtonClick;
    int selectedButton = -1;
    HandlerController controller;
    public static final String ID = "Dialog";
    public List<Pair<Indicators, String>> indicators;

    public DialogUI() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.LeftStick, "Navigate Menu"));
        indicators.add(new Pair<>(Indicators.A, "Confirm"));
    }

    @Override
    public void activate(Object... args) {
        var dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        dialogOptions = null;
        if(dialog != null) {
            optionsPanel = (UIPanelAPI) dialog.getOptionPanel();
            try {
                doButtonClick = MethodHandles.lookup().findVirtual(optionsPanel.getClass(), "actionPerformed", MethodType.methodType(void.class, Object.class, Object.class));
            } catch(Throwable ex) {
                Global.getLogger(getClass()).log(Level.FATAL, "Couldn't get the main menu buttons!");
            }
        }
        controller = SSMSControllerModPluginEx.controller;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
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
            try {
                doButtonClick.invoke(optionsPanel, null, dialogOptions.get(selectedButton));
            } catch(Throwable ex) {
                Global.getLogger(getClass()).log(Level.ERROR, "couldn't fire button event!");
            }
            //titleScreenButtons.get(selectedButton).
        }
    }

    @Override
    public void preInput(float advance) {
        if(dialogOptions == null) {
            selectedButton = -1;
            dialogOptions = new ArrayList<>(UIPanelReflector.getChildButtons(optionsPanel));
            if(!dialogOptions.isEmpty()) {
                selectNextButton();
            }
        }
        if(controller.getButtonEvent(HandlerController.Buttons.LeftStickDown) == 1) {
            selectNextButton();
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickUp) == 1) {
            selectPrevButton();
        } else if(controller.getButtonEvent(HandlerController.Buttons.A) == 1) {
            clickButton();
            dialogOptions = null;
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String[] getScopes() {
        return new String[] { InputScopeBase.ID };
    }
}
