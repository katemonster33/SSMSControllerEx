package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.ui.trade.CargoStackView;
import org.lwjgl.input.Mouse;
import ssms.controller.HandlerController;
import ssms.controller.Indicators;
import ssms.controller.InputScreenBase;
import ssms.controller.SSMSControllerModPluginEx;
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
    List<CargoStackView> playerGridItems;
    List<CargoStackView> otherGridItems;
    boolean playerGridSelected;
    int gridStackIndexSelected = -1;
    HandlerController controller;

    public TradeScreen() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.LeftStick, "Navigate cargo list"));
        indicators.add(new Pair<>(Indicators.X, "Select some"));
        controller = SSMSControllerModPluginEx.controller;
    }

    @Override
    public void activate(Object ... args) {
        tradeUiReflector = (TradeUiReflector) args[0];

        playerDataGrid = tradeUiReflector.getPlayerCargoView();
        otherDataGrid = tradeUiReflector.getOtherCargoView();
    }

    void mouseOverStack(CargoDataGridViewReflector gridView, CargoStackView cargoStackView) {

        List<InputEventAPI> eventList = new ArrayList<>();
        try {
            Object getPosition = ClassReflector.GetInstance().findDeclaredMethod(cargoStackView.getClass(), "getPosition");

            PositionAPI positionAPI = (PositionAPI) MethodReflector.GetInstance().invoke(getPosition, cargoStackView);
            eventList.add(InputEventReflector.GetInstance().createMouseMoveEvent((int)positionAPI.getCenterX(), (int) positionAPI.getCenterY()));
            Mouse.setCursorPosition((int)positionAPI.getCenterX(), (int) positionAPI.getCenterY());
        }catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Failed to get stack's position!", ex);
        }
    }

    @Override
    public void preInput(float advance) {
        if(gridStackIndexSelected == -1) {
            if(playerGridSelected) {
                if(playerGridItems == null) {
                    playerGridItems = playerDataGrid.getStacks();
                }
                if(playerGridItems != null && !playerGridItems.isEmpty()) {
                    //gridStackIndexSelected = 0;
                    //mouseOverStack(playerDataGrid, playerGridItems.get(gridStackIndexSelected));
                    mouseOverStack(playerDataGrid, playerGridItems.get(gridStackIndexSelected));
                }
            } else {
                if(otherGridItems == null) {
                    otherGridItems = otherDataGrid.getStacks();
                }
                if(otherGridItems != null && !otherGridItems.isEmpty()) {
                    //gridStackIndexSelected = 0;
                    //mouseOverStack(otherDataGrid, otherGridItems.get(gridStackIndexSelected));
                    mouseOverStack(otherDataGrid, otherGridItems.get(0));
                }
            }
        }
        if(controller.isLeftStickUp()) {

        } else if(controller.isLeftStickDown()) {

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
