package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.util.Pair;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.reflection.*;

import java.util.ArrayList;

public class CargoStackPickerScreen  extends InputScreenBase {
    public static final String ID = "CargoStackPicker";
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
        InputShim.mouseUp(mouseX, mouseY, InputEventMouseButton.LEFT);
    }

    void cancel() {
        InputShim.mouseDownUp(mouseX, mouseY, InputEventMouseButton.RIGHT);
        InputShim.mouseUp(mouseX, mouseY, InputEventMouseButton.LEFT);
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
        if(controller.getButtonEvent(LogicalButtons.LeftStickLeft) == 1) {
            // the 10 is hard-coded in the scrollbar source code
            mouseX -= 10;
            mouseX = Math.max((int) scrollbar.getPrivateObj().getPosition().getX(), mouseX);
            updateMousePos();
        } else if(controller.getButtonEvent(LogicalButtons.LeftStickRight) == 1) {
            mouseX += 10;
            mouseX = Math.min((int)(scrollbar.getPrivateObj().getPosition().getX() + scrollbar.getPrivateObj().getPosition().getWidth()), mouseX);
            updateMousePos();
        } else if(controller.getButtonEvent(LogicalButtons.LeftStickDown) == 1) {
            mouseX = (int)scrollbar.getPrivateObj().getPosition().getX();
            updateMousePos();
        } else if(controller.getButtonEvent(LogicalButtons.LeftStickUp) == 1) {
            mouseX = (int)(scrollbar.getPrivateObj().getPosition().getX() + scrollbar.getPrivateObj().getPosition().getWidth());
            updateMousePos();
        } else if(controller.getButtonEvent(LogicalButtons.B) == 1) {
            cancel();
        } else if(controller.getButtonEvent(LogicalButtons.A) == 1) {
            confirm();
        }
    }

    @Override
    public String getId() {
        return ID;
    }
}
