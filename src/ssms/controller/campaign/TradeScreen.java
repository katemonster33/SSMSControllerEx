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
    }

    @Override
    public void deactivate() {
        InputShim.clearAll();
    }

    void mouseOverStack(CargoStackView cargoStackView) {
        PositionAPI positionAPI = ((UIComponentAPI)cargoStackView).getPosition();
        InputShim.mouseMove((int)positionAPI.getCenterX(), (int) positionAPI.getCenterY());
    }

    void clickStack(CargoDataGridViewReflector gridView) {
        CargoStackView cargoStackView = gridView.getStacks().get(gridStackIndexSelected);
        PositionAPI positionAPI = ((UIComponentAPI)cargoStackView).getPosition();
        List<InputEventAPI> events = new ArrayList<>();
        //cargoStackView.getStack()
        try {
            //events.add(InputEventReflector.GetInstance().createMouseDownEvent((int) positionAPI.getCenterX(), (int) positionAPI.getCenterY(), 0));
            // if there's more than 4 in the stack, the scrollbar control will be created, thus we should do a shift-click. Otherwise, we will single click to select the whole stack.
            if(cargoStackView.getStack().getSize() > 4.0F) {
                InputShim.keyDown(Keyboard.KEY_LSHIFT, '\0');
                //InputEventReflector.GetInstance().setShiftDown(events.get(0), true);
            }
            InputShim.mouseDown((int) positionAPI.getCenterX(), (int) positionAPI.getCenterY(), 0);

            if(cargoStackView.getStack().getSize() > 4.0F) {
                //InputShim.keyUp(Keyboard.KEY_LSHIFT, '\0');
                //InputEventReflector.GetInstance().setShiftDown(events.get(0), true);
            }
            if(cargoStackView.getStack().getSize() <= 4.0F) {
                InputShim.mouseUp((int) positionAPI.getCenterX(), (int) positionAPI.getCenterY(), 0);
            }
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Failed to create a mouse left-click event!", ex);
        }
        gridView.getPrivateObject().processInput(events);
    }

    int clamp(int val, int max) {
        if(val < 0) return 0;
        else return Math.min(val, max - 1);
    }

    public void selectStack(CargoDataGridViewReflector curGrid, int rowDelta, int colDelta) {
        if(rowDelta < -1 || rowDelta > 1 || colDelta < -1 || colDelta > 1) {
            throw new IllegalArgumentException("Can't currently move more than 1 row in any direction!");
        }
        if(gridStackIndexSelected == -1) {
            return;
        }
        int numRows = curGrid.getPrivateObject().getRows(), numCols = curGrid.getPrivateObject().getCols();
        int curRow = gridStackIndexSelected / numCols, curCol = gridStackIndexSelected % numCols;
        if(colDelta != 0) {
            curCol = clamp(colDelta + curCol, numCols);
        }
        if(rowDelta != 0) {
            curRow = clamp(rowDelta + curRow, numRows);
        }
        gridStackIndexSelected = curRow * numCols + curCol;
        List<CargoStackView> curStacks = curGrid.getStacks();
        gridStackIndexSelected = clamp(gridStackIndexSelected, curStacks.size());
        mouseOverStack(curStacks.get(gridStackIndexSelected));
    }

    void trySelectFirstStack(CargoDataGridViewReflector curGrid) {
        List<CargoStackView> curStacks = curGrid.getStacks();
        if(curStacks == null || curStacks.isEmpty()) {
            return;
        }
        gridStackIndexSelected = 0;
        mouseOverStack(curStacks.get(gridStackIndexSelected));
    }

    @Override
    public void preInput(float advance) {
        if(cargoTransferHandler != null) {
            var scrollbar = cargoTransferHandler.getScrollbar();
            if(scrollbar != null) {
                InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, CargoStackPickerScreen.ID, new Object[]{ tradeUiReflector });
            }
        }
        CargoDataGridViewReflector curGrid = playerGridSelected ? playerDataGrid : otherDataGrid;
        if(gridStackIndexSelected == -1) {
            trySelectFirstStack(curGrid);
        }
        if(controller.getButtonEvent(HandlerController.Buttons.LeftStickUp) == 1 && controller.isLeftStickUp()) {
            selectStack(curGrid, -1, 0);
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickDown) == 1 && controller.isLeftStickDown()) {
            selectStack(curGrid, 1, 0);
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickLeft) == 1 && controller.isLeftStickLeft()) {
            selectStack(curGrid, 0, -1);
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickRight) == 1 && controller.isLeftStickRight()) {
            selectStack(curGrid, 0, 1);
        } else if(controller.getButtonEvent(HandlerController.Buttons.A) == 1 && controller.isButtonAPressed()) {
            if(gridStackIndexSelected != -1) {
                clickStack(curGrid);
            }
        } else if(controller.getButtonEvent(HandlerController.Buttons.Select) == 1 && controller.isButtonSelectPressed()) {
            playerGridSelected = !playerGridSelected;
            gridStackIndexSelected = -1;
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
