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
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
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
import java.util.stream.Collectors;

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
    InteractionDialogReflector interactDialog;

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = new ArrayList<>();
            if(isCargoTab) {
                addButtonPressHandler("Select refit tab", LogicalButtons.BumperLeft, new KeySender(Keyboard.KEY_R, 'r'));
                addButtonPressHandler("Select map tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_TAB));
            }
            addDirectionalUINavigator(directionalUINavigator);

            addButtonPressHandler("Take partial stack", LogicalButtons.X, advance -> {
                var selectedStack = getSelectedCargoStack();
                if(selectedStack == null) {
                    return;
                }
                var stackPos = ((UIComponentAPI)selectedStack).getPosition();
                var mousePos = new Vector2f(stackPos.getCenterX(), stackPos.getCenterY());
                if(selectedStack.getStack().getSize() >= 4.f) {
                    InputShim.keyDown(Keyboard.KEY_LSHIFT, '\0');
                }
                InputShim.mouseDown((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
                if(selectedStack.getStack().getSize() < 4.f) {
                    InputShim.mouseUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
                } else if(selectedStack.getStack().getSize() >= 4.f) {
                    InputShim.keyUp(Keyboard.KEY_LSHIFT, '\0');
                }
            });
            addButtonPressHandler("Cancel / Close", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        }
        return indicators;
    }

    boolean mouseOverStack(CargoStackView cargoStackView) {
        return InputShim.getMouseX() >= cargoStackView.getX() && InputShim.getMouseX() < cargoStackView.getX() + cargoStackView.getWidth() &&
                InputShim.getMouseY() >= cargoStackView.getY() && InputShim.getMouseY() < cargoStackView.getY() + cargoStackView.getHeight();
    }

    @Override
    public void activate(Object ... args) {
        if(args.length > 0) {
            tradeUiReflector = (TradeUiReflector) args[0];
            coreUiPanelReflector = new UIPanelReflector((UIPanelAPI) tradeUiReflector.getCoreUIAPI());
        }
        interactDialog = InteractionDialogReflector.getCurrentInstance();
        isCargoTab = Global.getSector().getCampaignUI().getCurrentCoreTab() == CoreUITabId.CARGO;
        directionalUINavigator = new DirectionalUINavigator(new ArrayList<>())
        {
            @Override
            public void onSelect(NavigationObject navigationObject) {
                super.onSelect(navigationObject);
                if(navigationObject.tag instanceof  ScrollPanelReflector scrollPanelReflector) {
                    scrollPanelReflector.ensureVisible(navigationObject.component);
                }
            }

            @Override
            public void handleAButton(float advance, boolean btnVal) {
                var selectedStack = getSelectedCargoStack();
                if(selectedStack != null) {
                    if(!btnVal) {
                        InputShim.mouseMove((int) selectedStack.getCenterX(), (int) selectedStack.getCenterY());
                        InputShim.mouseDownUp((int) selectedStack.getCenterX(), (int) selectedStack.getCenterY(), InputEventMouseButton.LEFT);
                    }
                } else {
                    super.handleAButton(advance, btnVal);
                }
            }
        };
        playerDataGrid = tradeUiReflector.getPlayerCargoView();
        directionalUINavigator.addScrollPanel(new ScrollPanelReflector(playerDataGrid.getScroller()));
        otherDataGrid = tradeUiReflector.getOtherCargoView();
        directionalUINavigator.addScrollPanel(new ScrollPanelReflector(otherDataGrid.getScroller()));
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
        indicators = null;
        updateDirectionalObjects();
    }


    @Nullable
    private CargoStackView getSelectedCargoStack() {
        CargoStackView selectedStack = null;
        if(directionalUINavigator.getSelected() != null) {
            if(directionalUINavigator.getSelected().component instanceof CargoStackView stack) {
                selectedStack = stack;
            }
        } else if(InputShim.getMouseX() != null && InputShim.getMouseY() != null) {
            List<CargoStackView> allStacks = new ArrayList<>(otherDataGrid.getStacks());
            allStacks.addAll(playerDataGrid.getStacks());
            for(var stack : allStacks) {
                if(mouseOverStack(stack)) {
                    selectedStack = stack;
                    break;
                }
            }
        }
        return selectedStack;
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
            if(interactDialog != null && !interactDialog.isCoreUiOpen()) {
                InputScreenManager.getInstance().transitionDelayed(DialogUI.ID);
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
        if(directionalUINavigator != null) {
            directionalUINavigator.advance(advance);
        }
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
            var playerGridScroller = new ScrollPanelReflector(playerDataGrid.getScroller());
            directionalObjects.addAll(playerDataGrid.getStacks().stream().map(stack -> new DirectionalUINavigator.NavigationObject(stack, playerGridScroller)).toList());
            var otherGridScroller = new ScrollPanelReflector(otherDataGrid.getScroller());
            directionalObjects.addAll(otherDataGrid.getStacks().stream().map(stack -> new DirectionalUINavigator.NavigationObject(stack, otherGridScroller)).toList());
        }
        directionalUINavigator.setNavigationObjects(directionalObjects);
    }

    @Override
    public String getId() {
        return ID;
    }
}
