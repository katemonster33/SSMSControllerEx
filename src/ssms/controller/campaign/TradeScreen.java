package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.ui.trade.CargoStackView;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.MessageBoxScreen;
import ssms.controller.inputhelper.ButtonInputHandler;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.*;

import java.util.ArrayList;

public class TradeScreen extends InputScreenBase {
    public static final String ID = "Trade";
    TradeUiReflector tradeUiReflector;
    InteractionDialogAPI interactionDialogAPI;
    CargoDataGridViewReflector playerDataGrid;
    CargoDataGridViewReflector otherDataGrid;
    CargoTransferHandlerReflector cargoTransferHandler;
    boolean playerGridSelected;
    int selectedRow = -1, selectedCol = -1;
    int lastFrameChildCount = 0;
    boolean isCargoTab = false;

    @Override
    public void activate(Object ... args) {
        if(args.length > 0) {
            tradeUiReflector = (TradeUiReflector) args[0];
        }
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.LeftStick, "Navigate list"));

        addHandler("Pick up stack", new ButtonInputHandler(LogicalButtons.X, (float advance) ->
                clickStack(playerGridSelected ? playerDataGrid : otherDataGrid)));

        addHandler("Take all", new KeySender(LogicalButtons.Y, Keyboard.KEY_R, 'r'));

        addHandler("Abort", new KeySender(LogicalButtons.B, Keyboard.KEY_T, 't'));

        addHandler("Confirm", new KeySender(LogicalButtons.A, Keyboard.KEY_G, 'g'));

        addHandler("Toggle hangar", new ButtonInputHandler(LogicalButtons.Select, (float advance) -> {
            playerGridSelected = !playerGridSelected;
            selectedRow = selectedCol = -1;
        }));

        addHandler("Close", new KeySender(LogicalButtons.Start, Keyboard.KEY_ESCAPE));
        isCargoTab = Global.getSector().getCampaignUI().getCurrentCoreTab() == CoreUITabId.CARGO;
        if(isCargoTab) {
            addHandler("Select refit tab", new KeySender(LogicalButtons.BumperLeft, Keyboard.KEY_R, 'r'));
            addHandler("Select map tab", new KeySender(LogicalButtons.BumperRight, Keyboard.KEY_TAB));
        }

        playerDataGrid = tradeUiReflector.getPlayerCargoView();
        otherDataGrid = tradeUiReflector.getOtherCargoView();
        cargoTransferHandler = tradeUiReflector.getCargoTransferHandler();
        playerGridSelected = true;
        selectedCol = selectedRow = -1;
        ControllerCrosshairRenderer.getControllerRenderer().setSize(100);
        interactionDialogAPI = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        lastFrameChildCount = UIPanelReflector.getChildItems((UIPanelAPI) tradeUiReflector.getCoreUIAPI()).size();
    }

    Vector2f getMousePosForSelection(CargoDataGridViewReflector gridView) {
        var gridPos = ((UIPanelAPI)gridView.getPrivateObject()).getPosition();
        float xpos = gridPos.getX() + (100 * selectedCol) + 43;
        float ypos = gridPos.getY() + gridPos.getHeight() - (100 * selectedRow) - 58;
        return new Vector2f(xpos, ypos);
    }

    void mouseOverGrid(CargoDataGridViewReflector gridView) {
        var mousePos = getMousePosForSelection(gridView);
        InputShim.mouseMove((int) mousePos.x, (int) mousePos.y);
    }

    void clickStack(CargoDataGridViewReflector gridView) {
        var mousePos = getMousePosForSelection(gridView);
        CargoStackView mousedOverStack = null;
        for(var stack : gridView.getStacks()) {
            var stackPos = ((UIPanelAPI)stack).getPosition();
            if(mousePos.x >= stackPos.getX() && mousePos.x <= (stackPos.getX() + stackPos.getWidth()) &&
                    mousePos.y >= stackPos.getY() && mousePos.y <= (stackPos.getY() + stackPos.getHeight())) {
                mousedOverStack = stack;
                break;
            }
        }
        if(mousedOverStack != null && mousedOverStack.getStack().getSize() >= 4.f) {
            InputShim.keyDown(Keyboard.KEY_LSHIFT, '\0');
        }
        InputShim.mouseDown((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
        if(mousedOverStack == null || mousedOverStack.getStack().getSize() < 4.f) {
            InputShim.mouseUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
        } else if(mousedOverStack.getStack().getSize() >= 4.f) {
            InputShim.keyUp(Keyboard.KEY_LSHIFT, '\0');
        }
    }

    int clamp(int val, int max) {
        if(val < 0) return 0;
        else return Math.min(val, max - 1);
    }

    public void moveGridSelection(CargoDataGridViewReflector curGrid, int rowDelta, int colDelta) {
        if(rowDelta < -1 || rowDelta > 1 || colDelta < -1 || colDelta > 1) {
            throw new IllegalArgumentException("Can't currently move more than 1 row in any direction!");
        }
        int numRows = curGrid.getPrivateObject().getRows(), numCols = curGrid.getPrivateObject().getCols();
        if(colDelta != 0) {
            selectedCol = clamp(colDelta + selectedCol, numCols);
        }
        if(rowDelta != 0) {
            selectedRow = clamp(rowDelta + selectedRow, numRows);
        }
        mouseOverGrid(curGrid);
    }

    @Override
    public void preInput(float advance) {
        if(cargoTransferHandler != null) {
            var scrollbar = cargoTransferHandler.getScrollbar();
            if(scrollbar != null) {
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
        CargoDataGridViewReflector curGrid = playerGridSelected ? playerDataGrid : otherDataGrid;
        if(selectedCol == -1 || selectedRow == -1) {
            selectedCol = selectedRow = 0;
            mouseOverGrid(curGrid);
        }
        if(controller.getButtonEvent(LogicalButtons.LeftStickUp) == 1) {
            moveGridSelection(curGrid, -1, 0);
        } else if(controller.getButtonEvent(LogicalButtons.LeftStickDown) == 1) {
            moveGridSelection(curGrid, 1, 0);
        } else if(controller.getButtonEvent(LogicalButtons.LeftStickLeft) == 1) {
            //selectStack(curGrid, 0, -1);
            moveGridSelection(curGrid, 0, -1);
        } else if(controller.getButtonEvent(LogicalButtons.LeftStickRight) == 1) {
            //selectStack(curGrid, 0, 1);
            moveGridSelection(curGrid, 0, 1);
        } else if(controller.getButtonEvent(LogicalButtons.Select) == 1) {
            playerGridSelected = !playerGridSelected;
            selectedRow = selectedCol = -1;
        }
    }

    @Override
    public String getId() {
        return ID;
    }
}
