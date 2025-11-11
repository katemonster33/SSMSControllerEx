package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import org.apache.log4j.Level;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.CodexUI;
import ssms.controller.generic.MessageBoxScreen;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.reflection.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DialogUI extends InputScreenBase {
    UIPanelReflector optionsPanel;
    MethodHandle doButtonClick;
    InteractionDialogReflector  interactReflector;
    InteractionDialogAPI interactionDialogAPI;
    public static final String ID = "Dialog";
    DirectionalUINavigator directionalUINavigator;

    public DialogUI() {
    }

    @Override
    public void activate(Object... args) {
        interactionDialogAPI = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        interactReflector = new InteractionDialogReflector(interactionDialogAPI);
        indicators = new ArrayList<>();
        directionalUINavigator = new DirectionalUINavigator(new ArrayList<>());
        addDigitalJoystickHandler("Navigate menu", Joystick.DPad, directionalUINavigator);
        addButtonPressHandler("Confirm", LogicalButtons.A, (float advance) -> clickButton());
        if(interactionDialogAPI != null) {
            optionsPanel = new UIPanelReflector((UIPanelAPI) interactionDialogAPI.getOptionPanel());
            try {
                doButtonClick = MethodHandles.lookup().findVirtual(optionsPanel.getPanel().getClass(), "actionPerformed", MethodType.methodType(void.class, Object.class, Object.class));
            } catch(Throwable ex) {
                Global.getLogger(getClass()).log(Level.FATAL, "Couldn't get the main menu buttons!");
            }
        }
    }

    public void clickButton() {
        if(directionalUINavigator.getSelected() != null && directionalUINavigator.getSelected().component instanceof ButtonAPI selectedButton) {
            try {
                doButtonClick.invoke(optionsPanel.getPanel(), null, selectedButton);
            } catch(Throwable ex) {
                Global.getLogger(getClass()).log(Level.ERROR, "couldn't fire button event!");
            }
        }
    }

    @Override
    public void preInput(float advance) {
        if (!Global.getSector().getCampaignUI().isShowingDialog()) {
            InputScreenManager.getInstance().transitionToScreen(MainCampaignUI.ID);
        } else {
            var interactionCoreUi = interactReflector.getCoreUI(interactionDialogAPI);
            var coreUiReflector = new UIPanelReflector((UIPanelAPI) interactionCoreUi);
            if (interactionCoreUi != null && interactionCoreUi.getTradeMode() != null && coreUiReflector.getParent() != null && openCoreUiTab(new CoreUIReflector(interactionCoreUi))) {
                return;
//                var dialogChildren = interactReflector.getChildItems();
//                if (dialogChildren.contains(interactionCoreUi)) {
//                    for (var coreUiChild : new UIPanelReflector((UIPanelAPI) interactionCoreUi).getChildPanels()) {
//                        BorderedPanelReflector borderPanel = BorderedPanelReflector.TryGet(interactionCoreUi, coreUiChild);
//                        if (borderPanel != null) {
//                            var tradeUi = TradeUiReflector.TryGet(interactionCoreUi, borderPanel);
//                            if (tradeUi != null && tradeUi.getPanel().getOpacity() != 0.f) {
//                                InputScreenManager.getInstance().transitionToScreen(TradeScreen.ID, tradeUi);
//                            }
//                        }
//                    }
//                }
            } else {
                for (var child : interactReflector.getChildPanels()) {
                    if (MessageBoxReflector.isMsgBox(child)) {
                        InputScreenManager.getInstance().transitionToScreen(MessageBoxScreen.ID, new MessageBoxReflector(child), getId());
                    }
                }
            }
        }
        if(isCodexOpen()) {
            InputScreenManager.getInstance().transitionDelayed(CodexUI.ID, getId());
        }
        List<DirectionalUINavigator.NavigationObject> directionalObjects = new ArrayList<>(optionsPanel.getChildButtons().stream().map(DirectionalUINavigator.NavigationObject::new).toList());
        directionalUINavigator.setNavigationObjects(directionalObjects);
    }

    @Override
    public String getId() {
        return ID;
    }
}
