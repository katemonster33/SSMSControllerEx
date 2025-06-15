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
    ScrollbarUiReflector scrollbar;
    public CargoStackPickerScreen() {
        indicators = new ArrayList<>();
        addButtonPressHandler("Select less", LogicalButtons.DpadLeft, (float advance) -> updateMousePos(mouseX - 10));
        addButtonPressHandler("Select more", LogicalButtons.DpadRight, (float advance) -> updateMousePos(mouseX + 10));
        addButtonPressHandler("Select all", LogicalButtons.DpadUp, (float advance) -> updateMousePos(Integer.MAX_VALUE));
        addButtonPressHandler("Select none", LogicalButtons.DpadDown, (float advance) -> updateMousePos(Integer.MIN_VALUE));
        addButtonPressHandler("Cancel", LogicalButtons.B, (float advance) -> {
            InputShim.mouseDownUp(mouseX, mouseY, InputEventMouseButton.RIGHT);
            InputShim.mouseUp(mouseX, mouseY, InputEventMouseButton.LEFT);
        });
        addButtonPressHandler("Confirm", LogicalButtons.A, (float advance) -> InputShim.mouseUp(mouseX, mouseY, InputEventMouseButton.LEFT));
    }

    @Override
    public void activate(Object ... args) {
        tradeUiReflector = (TradeUiReflector) args[0];
        cargoTransferHandler = tradeUiReflector.getCargoTransferHandler();
        mouseX = mouseY = -1;
    }

    void updateMousePos(int newVal) {
        if(scrollbar == null) return;
        var scrollbarPos = scrollbar.getPrivateObj().getPosition();
        mouseX = newVal;
        if(mouseX < scrollbarPos.getX()) mouseX = (int)scrollbarPos.getX();
        else if(mouseX > scrollbarPos.getX() + scrollbarPos.getWidth()) mouseX = (int)(scrollbarPos.getX() + scrollbarPos.getWidth());
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
        scrollbar = tryGetScrollbar();
        if (scrollbar == null) {
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, TradeScreen.ID, new Object[]{tradeUiReflector});
            return;
        } else if (mouseX == -1 || mouseY == -1) {
            mouseX = (int) scrollbar.getPrivateObj().getPosition().getX();
            mouseY = (int) scrollbar.getPrivateObj().getPosition().getCenterY();
        }
    }

    @Override
    public String getId() {
        return ID;
    }
}
