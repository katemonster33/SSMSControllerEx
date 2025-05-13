package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.ui.trade.CargoStackView;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.reflection.*;

import java.util.ArrayList;
import java.util.List;

public class TradeScreen extends InputScreenBase {
    public static final String ID = "Trade";
    List<Pair<Indicators, String>> indicators;
    TradeUiReflector tradeUiReflector;
    InteractionDialogAPI interactionDialogAPI;
    CargoDataGridViewReflector playerDataGrid;
    CargoDataGridViewReflector otherDataGrid;
    CargoTransferHandlerReflector cargoTransferHandler;
    boolean playerGridSelected;
    int gridStackIndexSelected = -1;
    int selectedRow = -1, selectedCol = -1;
    HandlerController controller;

    public TradeScreen() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.LeftStick, "Navigate list"));
        indicators.add(new Pair<>(Indicators.X, "Pick up stack"));
        indicators.add(new Pair<>(Indicators.Y, "Take all"));
        indicators.add(new Pair<>(Indicators.B, "Abort"));
        indicators.add(new Pair<>(Indicators.A, "Confirm"));
        indicators.add(new Pair<>(Indicators.Select, "Toggle hangar"));
        indicators.add(new Pair<>(Indicators.Start, "Close"));
        controller = SSMSControllerModPluginEx.controller;
    }

    @Override
    public void activate(Object ... args) {
        tradeUiReflector = (TradeUiReflector) args[0];

        playerDataGrid = tradeUiReflector.getPlayerCargoView();
        otherDataGrid = tradeUiReflector.getOtherCargoView();
        cargoTransferHandler = tradeUiReflector.getCargoTransferHandler();
        playerGridSelected = true;
        selectedCol = selectedRow = -1;
        ControllerCrosshairRenderer.setSize(100);
        interactionDialogAPI = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
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
        InputShim.mouseDown((int) mousePos.x, (int) mousePos.y, 0);
        if(mousedOverStack == null || mousedOverStack.getStack().getSize() < 4.f) {
            InputShim.mouseUp((int) mousePos.x, (int) mousePos.y, 0);
        } else if(mousedOverStack != null && mousedOverStack.getStack().getSize() >= 4.f) {
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
        if(!Global.getSector().getCampaignUI().isShowingDialog()) {
            InputScreenManager.getInstance().transitionToScreen(MainCampaignUI.ID);
        } else if(tradeUiReflector.getCoreUIAPI().getTradeMode() != null && interactionDialogAPI != null){
            var tradePanelChildren = UIPanelReflector.getChildItems((UIPanelAPI) interactionDialogAPI);
            if(!tradePanelChildren.contains(tradeUiReflector.getCoreUIAPI())) {
                InputScreenManager.getInstance().transitionToScreen(DialogUI.ID);
            }
        }
        CargoDataGridViewReflector curGrid = playerGridSelected ? playerDataGrid : otherDataGrid;
        if(selectedCol == -1 || selectedRow == -1) {
            selectedCol = selectedRow = 0;
            mouseOverGrid(curGrid);
        }
        if(controller.getButtonEvent(HandlerController.Buttons.LeftStickUp) == 1 && controller.isLeftStickUp()) {
            moveGridSelection(curGrid, -1, 0);
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickDown) == 1 && controller.isLeftStickDown()) {
            moveGridSelection(curGrid, 1, 0);
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickLeft) == 1 && controller.isLeftStickLeft()) {
            //selectStack(curGrid, 0, -1);
            moveGridSelection(curGrid, 0, -1);
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickRight) == 1 && controller.isLeftStickRight()) {
            //selectStack(curGrid, 0, 1);
            moveGridSelection(curGrid, 0, 1);
        } else if(controller.getButtonEvent(HandlerController.Buttons.X) == 1 && controller.isButtonXPressed()) {
            clickStack(curGrid);
        } else if(controller.getButtonEvent(HandlerController.Buttons.Y) == 1 && controller.isButtonYPressed()) {
            InputShim.keyDownUp(Keyboard.KEY_R, 'r');
        } else if(controller.getButtonEvent(HandlerController.Buttons.A) == 1 && controller.isButtonAPressed()) {
            InputShim.keyDownUp(Keyboard.KEY_G, 'g');
        } else if(controller.getButtonEvent(HandlerController.Buttons.B) == 1 && controller.isButtonBPressed()) {
            InputShim.keyDownUp(Keyboard.KEY_T, 't');
        } else if(controller.getButtonEvent(HandlerController.Buttons.Select) == 1 && controller.isButtonSelectPressed()) {
            playerGridSelected = !playerGridSelected;
            selectedRow = selectedCol = -1;
        } else if(controller.getButtonEvent(HandlerController.Buttons.Start) == 1 && controller.isButtonStartPressed()) {
            InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
        }
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }

    @Override
    public String getId() {
        return ID;
    }
}
