package ssms.controller.campaign;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.ui.trade.CargoStackView;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.MessageBoxScreen;
import ssms.controller.inputhelper.ButtonPressOrHoldHandler;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.*;

import java.util.ArrayList;
import java.util.List;

public class TradeScreen extends InputScreenBase {
    public static final String ID = "Trade";
    TradeUiReflector tradeUiReflector;
    InteractionDialogAPI interactionDialogAPI;
    UIPanelReflector coreUiPanelReflector;
    InteractionDialogReflector interactionDialogReflector;
    CargoDataGridViewReflector playerDataGrid;
    CargoDataGridViewReflector otherDataGrid;
    CargoTransferHandlerReflector cargoTransferHandler;
    DirectionalUINavigator directionalUINavigator;
    int lastFrameChildCount = 0;
    boolean isCargoTab = false;

    @Override
    public void activate(Object ... args) {
        if(args.length > 0) {
            tradeUiReflector = (TradeUiReflector) args[0];
            coreUiPanelReflector = new UIPanelReflector((UIPanelAPI) tradeUiReflector.getCoreUIAPI());
        }
        indicators = new ArrayList<>();
        isCargoTab = Global.getSector().getCampaignUI().getCurrentCoreTab() == CoreUITabId.CARGO;
        if(isCargoTab) {
            addButtonPressHandler("Select refit tab", LogicalButtons.BumperLeft, new KeySender(Keyboard.KEY_R, 'r'));
            addButtonPressHandler("Select map tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_TAB));
        }
        directionalUINavigator = new DirectionalUINavigator(new ArrayList<>());
        addDigitalJoystickHandler("Navigate", Joystick.DPad, directionalUINavigator);

        addButtonPressOrHoldHandler("Press button / take partial stack", "Take whole stack", LogicalButtons.A, new ButtonPressOrHoldHandler() {
            @Override
            public void performHoldAction(float advance) {
                clickSelected(true);
            }

            @Override
            public void performPressAction(float advance) {
                clickSelected(false);
            }
        });
        addButtonPressHandler("Cancel / Close", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        playerDataGrid = tradeUiReflector.getPlayerCargoView();
        otherDataGrid = tradeUiReflector.getOtherCargoView();
        cargoTransferHandler = tradeUiReflector.getCargoTransferHandler();
        interactionDialogAPI = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        if(interactionDialogAPI != null) {
            interactionDialogReflector = new InteractionDialogReflector(interactionDialogAPI);
        }
        lastFrameChildCount = 0;
    }

    void clickSelected(boolean takeAllIfStack) {
        var selected = directionalUINavigator.getSelected();
        if(selected != null) {
            if(selected.one instanceof CargoStackView cargoStackView && selected.two instanceof CargoDataGridViewReflector cargoDataGridViewReflector && !takeAllIfStack) {
                clickStack(cargoDataGridViewReflector, cargoStackView);
            } else {
                var pos = selected.one.getPosition();
                InputShim.mouseMove((int) pos.getCenterX(), (int) pos.getCenterY());
                InputShim.mouseDownUp((int) pos.getCenterX(), (int) pos.getCenterY(), InputEventMouseButton.LEFT);
            }
        }
    }

    void clickStack(CargoDataGridViewReflector gridView, CargoStackView stackView) {
        var stackPos = ((UIComponentAPI)stackView).getPosition();
        var mousePos = new Vector2f(stackPos.getCenterX(), stackPos.getCenterY());
        if(stackView.getStack().getSize() >= 4.f) {
            InputShim.keyDown(Keyboard.KEY_LSHIFT, '\0');
        }
        InputShim.mouseDown((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
        if(stackView.getStack().getSize() < 4.f) {
            InputShim.mouseUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
        } else if(stackView.getStack().getSize() >= 4.f) {
            InputShim.keyUp(Keyboard.KEY_LSHIFT, '\0');
        }
    }

    @Override
    public void preInput(float advance) {
        if(cargoTransferHandler != null) {
            if(cargoTransferHandler.getScrollbar() != null || cargoTransferHandler.getPickedUpStack() != null) {
                InputScreenManager.getInstance().transitionToScreen(CargoStackPickerScreen.ID, tradeUiReflector);
            }
        }
        if(!Global.getSector().getCampaignUI().isShowingDialog() || (isCargoTab && Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.CARGO)) {
            InputScreenManager.getInstance().transitionToScreen(MainCampaignUI.ID);
        } else if(tradeUiReflector.getCoreUIAPI().getTradeMode() != null){
            if(interactionDialogAPI != null) {
                var tradePanelChildren = interactionDialogReflector.getChildItems();
                if (!tradePanelChildren.contains(tradeUiReflector.getCoreUIAPI())) {
                    InputScreenManager.getInstance().transitionToScreen(DialogUI.ID);
                }
            }
            List<?> coreUiChildren = null;
            if(interactionDialogAPI != null && Global.getCurrentState() == GameState.COMBAT) coreUiChildren = coreUiPanelReflector.getChildItems();
            else if(coreUiPanelReflector.getParent() != null) coreUiChildren = new UIPanelReflector(coreUiPanelReflector.getParent()).getChildItems();
            if(coreUiChildren != null) {
                int numChildren = coreUiChildren.size();
                if(numChildren > lastFrameChildCount) {
                    for(int i = lastFrameChildCount; i < numChildren; i++) {
                        var child = coreUiChildren.get(i);
                        if(child instanceof UIPanelAPI uiPanelAPI && InputScreenManager.getInstance().getDisplayPanel() != null && child != InputScreenManager.getInstance().getDisplayPanel().getSubpanel()) {
                            if(MessageBoxReflector.isMsgBox(uiPanelAPI)) {
                                InputScreenManager.getInstance().transitionToScreen(MessageBoxScreen.ID, new MessageBoxReflector(uiPanelAPI), TradeScreen.ID);
                                return;
                            }
                        }
                    }
                }
                lastFrameChildCount = numChildren;
            }
        }

        var buttons = tradeUiReflector.getChildButtons(true);
        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for(var btn : buttons) {
            if(btn.isEnabled()) {
                directionalObjects.add(new Pair<>(btn, null));
            }
        }
        for(var stack : playerDataGrid.getStacks()) {
            directionalObjects.add(new Pair<>(stack, playerDataGrid));
        }
        for(var stack : otherDataGrid.getStacks()) {
            directionalObjects.add(new Pair<>(stack, otherDataGrid));
        }
        directionalUINavigator.setNavigationObjects(directionalObjects);
    }

    @Override
    public String getId() {
        return ID;
    }
}
