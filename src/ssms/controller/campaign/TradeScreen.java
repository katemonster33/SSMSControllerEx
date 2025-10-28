package ssms.controller.campaign;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.ui.trade.CargoStackView;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.CodexUI;
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
    UIPanelReflector colonyInfoWidget;
    boolean isColonyInfoShown;

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

        var tradeUiChildren = tradeUiReflector.getChildItems();
        if(tradeUiChildren.size() == 12) {
            colonyInfoWidget = new UIPanelReflector((UIPanelAPI) tradeUiChildren.get(7));
            isColonyInfoShown = colonyInfoWidget.getFader().getBrightness() == 1.f;
        } else {
            colonyInfoWidget = null;
        }
        updateDirectionalObjects();
    }

    void clickSelected(boolean takeAllIfStack) {
        var selected = directionalUINavigator.getSelected();
        if(selected != null) {
            if(selected.component instanceof CargoStackView cargoStackView && selected.tag instanceof CargoDataGridViewReflector cargoDataGridViewReflector && !takeAllIfStack) {
                clickStack(cargoDataGridViewReflector, cargoStackView);
            } else {
                InputShim.mouseMove((int) selected.getCenterX(), (int) selected.getCenterY());
                InputShim.mouseDownUp((int) selected.getCenterX(), (int) selected.getCenterY(), InputEventMouseButton.LEFT);
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
        if(isCodexOpen()) {
            InputScreenManager.getInstance().transitionDelayed(CodexUI.ID, getId());
        }

        if(colonyInfoWidget != null) {
            var isMarketInfoShownTmp = colonyInfoWidget.getFader().getBrightness() == 1.f;
            if (isMarketInfoShownTmp == isColonyInfoShown && !isMarketInfoShownTmp) {
                return;
            }
            isColonyInfoShown = isMarketInfoShownTmp;
        }
        updateDirectionalObjects();
    }

    private void updateDirectionalObjects() {
        var tradeUiChildren = tradeUiReflector.getChildItems();
        List<ButtonAPI> buttons = new ArrayList<>();
//        if(marketInfoWidget != null) {
//            if(isMarketInfoShown) {
//                buttons.addAll(marketInfoWidget.getChildButtons(true));
//            } else {
//                for( var item : tradeUiChildren) {
//                    if (item != marketInfoWidget.getPanel()) {
//                        if (item instanceof UIPanelAPI uiPanelAPI) {
//                            buttons.addAll(new UIPanelReflector(uiPanelAPI).getChildButtons(true));
//                        } else if (item instanceof ButtonAPI buttonAPI) {
//                            buttons.add(buttonAPI);
//                        }
//                    }
//                }
//            }
//        } else {
//            buttons.addAll(tradeUiReflector.getChildButtons(true));
//        }

        for( var item : tradeUiChildren) {
            if(UIComponentReflector.isComponent(item.getClass())) {
                UIComponentReflector uiComponentReflector = new UIComponentReflector((UIComponentAPI) item);
                if(uiComponentReflector.getFader() != null && uiComponentReflector.getFader().getBrightness() == 1.f) {
                    if (item instanceof UIPanelAPI uiPanelAPI) {
                        buttons.addAll(new UIPanelReflector(uiPanelAPI).getChildButtons(true));
                    } else if (item instanceof ButtonAPI buttonAPI) {
                        buttons.add(buttonAPI);
                    }
                }
            }
        }
        List<DirectionalUINavigator.NavigationObject> directionalObjects = new ArrayList<>(buttons.stream().filter(ButtonAPI::isEnabled).map(DirectionalUINavigator.NavigationObject::new).toList());
        if(colonyInfoWidget == null || !isColonyInfoShown) {
            directionalObjects.addAll(playerDataGrid.getStacks().stream().map(stack -> new DirectionalUINavigator.NavigationObject(stack, playerDataGrid)).toList());
            directionalObjects.addAll(otherDataGrid.getStacks().stream().map(stack -> new DirectionalUINavigator.NavigationObject(stack, otherDataGrid)).toList());
        }
        directionalUINavigator.setNavigationObjects(directionalObjects);
    }

    @Override
    public String getId() {
        return ID;
    }
}
