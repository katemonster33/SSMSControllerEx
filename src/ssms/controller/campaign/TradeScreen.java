package ssms.controller.campaign;

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
        ControllerCrosshairRenderer.getControllerRenderer().setSize(100);
        interactionDialogAPI = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        lastFrameChildCount = UIPanelReflector.getChildItems((UIPanelAPI) tradeUiReflector.getCoreUIAPI()).size();
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
        var stackPos = stackView.getPosition();
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
            InputScreenManager.getInstance().transitionToScope(CampaignScope.ID, new Object[]{}, MainCampaignUI.ID, new Object[]{});
        } else if(tradeUiReflector.getCoreUIAPI().getTradeMode() != null && interactionDialogAPI != null){
            var tradePanelChildren = UIPanelReflector.getChildItems((UIPanelAPI) interactionDialogAPI);
            if(!tradePanelChildren.contains(tradeUiReflector.getCoreUIAPI())) {
                InputScreenManager.getInstance().transitionToScreen(DialogUI.ID);
            } else {
                var coreUiChildren = UIPanelReflector.getChildItems((UIPanelAPI) tradeUiReflector.getCoreUIAPI());
                int numChildren = coreUiChildren.size();
                if(numChildren > lastFrameChildCount) {
                    for(int i = lastFrameChildCount; i < numChildren; i++) {
                        var child = coreUiChildren.get(i);
                        if(UIPanelAPI.class.isAssignableFrom(child.getClass()) && InputScreenManager.getInstance().getDisplayPanel() != null && child != InputScreenManager.getInstance().getDisplayPanel().getSubpanel()) {
                            MessageBoxReflector messageBoxReflector = MessageBoxReflector.TryGet((UIPanelAPI) child);
                            if(messageBoxReflector != null) {
                                InputScreenManager.getInstance().transitionToScreen(MessageBoxScreen.ID, messageBoxReflector, TradeScreen.ID);
                                return;
                            }
                        }
                    }
                }
                lastFrameChildCount = numChildren;
            }
        }

        var buttons = UIPanelReflector.getChildButtons((UIPanelAPI) tradeUiReflector.getTradePanel(), true);
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
