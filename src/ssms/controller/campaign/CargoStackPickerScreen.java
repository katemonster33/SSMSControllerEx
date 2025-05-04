package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.fleet.CargoData;
import com.fs.starfarer.campaign.ui.trade.CargoDataGridView;
import com.fs.starfarer.campaign.ui.trade.CargoItemStack;
import com.fs.starfarer.campaign.ui.trade.CargoStackView;
import com.fs.starfarer.coreui.P;
import ssms.controller.*;
import ssms.controller.reflection.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class CargoStackPickerScreen  extends InputScreenBase {
    public static final String ID = "CargoStackPicker";
    List<Pair<Indicators, String>> indicators;
    HandlerController controller;
    TradeUiReflector tradeUiReflector;
    CargoTransferHandlerReflector cargoTransferHandler;
    int mouseX = -1, mouseY = -1;

    public CargoStackPickerScreen() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.LeftStickLeft, "Select less"));
        indicators.add(new Pair<>(Indicators.LeftStickRight, "Select more"));
        indicators.add(new Pair<>(Indicators.LeftStickUp, "Select all"));
        indicators.add(new Pair<>(Indicators.LeftStickDown, "Select none"));
        indicators.add(new Pair<>(Indicators.B, "Cancel"));
        indicators.add(new Pair<>(Indicators.A, "Confirm"));
        controller = SSMSControllerModPluginEx.controller;
    }

    @Override
    public void activate(Object ... args) {
        tradeUiReflector = (TradeUiReflector) args[0];
        cargoTransferHandler = tradeUiReflector.getCargoTransferHandler();
        mouseX = mouseY = -1;
    }

    @Override
    public void deactivate() {
        InputShim.clearAll();
    }

    void confirm() {
        InputShim.mouseUp(mouseX, mouseY, 0);
    }

    void cancel() {
        InputShim.mouseDown(mouseX, mouseY, 1);
        InputShim.mouseUp(mouseX, mouseY, 1);
        InputShim.mouseUp(mouseX, mouseY, 0);
    }

    void updateMousePos() {
        InputShim.mouseMove(mouseX, mouseY);
    }

    public ScrollbarUiReflector tryGetScrollbar() {
        ScrollbarUiReflector scrollbarUiReflector = null;
        try {
            var scrollbarInternal = cargoTransferHandler.getScrollbar();
            if(scrollbarInternal != null) {
                scrollbarUiReflector = new ScrollbarUiReflector(cargoTransferHandler.getScrollbar());
            }
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get data grid view for the upper cargo view of the trade UI!", ex);
        }
        return scrollbarUiReflector;
    }

    @Override
    public void preInput(float advance) {
        // sometimes when trade UI calls us, the trade UI is not actually transferring cargo so we need to pass control back to TradeScreen
        // OR user ended trade themselves with mouse
        ScrollbarUiReflector scrollbar = tryGetScrollbar();
        if(scrollbar == null) {
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, TradeScreen.ID, new Object[]{ tradeUiReflector });
            return;
        } else if(mouseX == -1 || mouseY == -1) {
            mouseX = (int) scrollbar.getPrivateObj().getPosition().getX();
            mouseY = (int) scrollbar.getPrivateObj().getPosition().getCenterY();
        }
        if(controller.getButtonEvent(HandlerController.Buttons.LeftStickLeft) == 1 && controller.isLeftStickLeft()) {
            // the 10 is hard-coded in the scrollbar source code
            mouseX -= 10;
            mouseX = Math.max((int) scrollbar.getPrivateObj().getPosition().getX(), mouseX);
            updateMousePos();
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickRight) == 1 && controller.isLeftStickRight()) {
            mouseX += 10;
            mouseX = Math.min((int)(scrollbar.getPrivateObj().getPosition().getX() + scrollbar.getPrivateObj().getPosition().getWidth()), mouseX);
            updateMousePos();
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickDown) == 1 && controller.isLeftStickDown()) {
            mouseX = (int)scrollbar.getPrivateObj().getPosition().getX();
            updateMousePos();
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickUp) == 1 && controller.isLeftStickUp()) {
            mouseX = (int)(scrollbar.getPrivateObj().getPosition().getX() + scrollbar.getPrivateObj().getPosition().getWidth());
            updateMousePos();
        } else if(controller.getButtonEvent(HandlerController.Buttons.B) == 1 && controller.isButtonBPressed()) {
            cancel();
        } else if(controller.getButtonEvent(HandlerController.Buttons.A) == 1 && controller.isButtonAPressed()) {
            confirm();
        }
    }

    @Override
    public String getId() {
        return ID;
    }
}
