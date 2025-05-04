package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.input.InputEventClass;
import com.fs.starfarer.api.input.InputEventType;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.ui.trade.CargoStackView;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import ssms.controller.*;
import ssms.controller.reflection.*;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class TradeScreen extends InputScreenBase {
    public static final String ID = "Trade";
    List<Pair<Indicators, String>> indicators;
    TradeUiReflector tradeUiReflector;
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
    }

    void mouseOverGrid(CargoDataGridViewReflector gridView) {
        //var gridPos = gridView.getPosition();
        var gridPos = ((UIPanelAPI)gridView.getPrivateObject()).getPosition();
        float xpos = gridPos.getX() + (100 * selectedCol) + 43;
        float ypos = gridPos.getY() + gridPos.getHeight() - (100 * selectedRow) - 58;
        InputShim.mouseMove((int) xpos, (int) ypos);
    }

    void clickStack(CargoDataGridViewReflector gridView) {
        var gridPos = ((UIPanelAPI)gridView.getPrivateObject()).getPosition();
        float xpos = gridPos.getX() + (100 * selectedCol) + 43;
        float ypos = gridPos.getY() + gridPos.getHeight()  - (100 * selectedRow) - 58;
        CargoStackView mousedOverStack = null;
        for(var stack : gridView.getStacks()) {
            var stackPos = ((UIPanelAPI)stack).getPosition();
            if(xpos >= stackPos.getX() && xpos <= (stackPos.getX() + stackPos.getWidth()) &&
             ypos >= stackPos.getY() && ypos <= (stackPos.getY() + stackPos.getHeight())) {
                mousedOverStack = stack;
                break;
            }
        }
        if(mousedOverStack != null && mousedOverStack.getStack().getSize() >= 4.f) {
            InputShim.keyDown(Keyboard.KEY_LSHIFT, '\0');
        }
        InputShim.mouseDown((int) xpos, (int) ypos, 0);
        if(mousedOverStack == null || mousedOverStack.getStack().getSize() < 4.f) {
            InputShim.mouseUp((int) xpos, (int) ypos, 0);
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
                InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, CargoStackPickerScreen.ID, new Object[]{ tradeUiReflector });
            }
        }
        if(!Global.getSector().getCampaignUI().isShowingDialog()) {
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, MainCampaignUI.ID, new Object[]{});
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
