package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventMouseButton;
 import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.ui.trade.CargoDataGridView;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CargoStackPickerScreen  extends InputScreenBase {
    public static final String ID = "CargoStackPicker";
    TradeUiReflector tradeUiReflector;
    CargoTransferHandlerReflector cargoTransferHandler;
    int mouseX = -1, mouseY = -1;
    ScrollbarUiReflector scrollbar;
    boolean wasScrollerVisible = false;
    boolean movedNewCargoStack = false;

    @Override
    public void activate(Object ... args) {
        tradeUiReflector = (TradeUiReflector) args[0];
        cargoTransferHandler = tradeUiReflector.getCargoTransferHandler();
        mouseX = mouseY = -1;
        scrollbar = tryGetScrollbar();
        wasScrollerVisible = scrollbar != null;
        movedNewCargoStack = false;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        indicators = new ArrayList<>();
        if(scrollbar != null) {
            addButtonPressHandler("Select less", LogicalButtons.DpadLeft, (float advance) -> updateMousePos(mouseX - 10));
            addButtonPressHandler("Select more", LogicalButtons.DpadRight, (float advance) -> updateMousePos(mouseX + 10));
            addButtonPressHandler("Select all", LogicalButtons.DpadUp, (float advance) -> updateMousePos(Integer.MAX_VALUE));
            addButtonPressHandler("Select none", LogicalButtons.DpadDown, (float advance) -> updateMousePos(Integer.MIN_VALUE));
            addButtonPressHandler("Cancel", LogicalButtons.B, (float advance) -> {
                InputShim.mouseDownUp(mouseX, mouseY, InputEventMouseButton.RIGHT);
                InputShim.mouseUp(mouseX, mouseY, InputEventMouseButton.LEFT);
            });
            addButtonPressHandler("Confirm", LogicalButtons.A, (float advance) -> InputShim.mouseUp(mouseX, mouseY, InputEventMouseButton.LEFT));
        } else {
            addButtonPressHandler("Cancel", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
            addButtonPressHandler("Confirm", LogicalButtons.A, (float advance) -> InputShim.mouseDownUp(mouseX, mouseY, InputEventMouseButton.LEFT));
        }
        return indicators;
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
                scrollbarUiReflector = new ScrollbarUiReflector(scrollbarInternal);
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
        if(scrollbar == null && wasScrollerVisible) {
            wasScrollerVisible = false;
            refreshIndicators();
        }
        if (scrollbar == null) {
            if (cargoTransferHandler.getPickedUpStack() == null) {
                InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, TradeScreen.ID, new Object[]{tradeUiReflector});
            } else if(!movedNewCargoStack) {
                movedNewCargoStack = true;
                var cargoGridSrc = cargoTransferHandler.getOrigStackSource();
                if(cargoGridSrc == null) return;
                CargoDataGridView stackView = null;
                try {
                    MethodHandle getCargoDataView = MethodHandles.lookup().findVirtual(cargoGridSrc.getClass(), "getCargoDataView", MethodType.methodType(CargoDataGridView.class));
                    stackView = (CargoDataGridView) getCargoDataView.invoke(cargoGridSrc);
                } catch(Throwable ex) {
                    Global.getLogger(getClass()).error("Couldn't fetch source cargo data grid!", ex);
                }
                if(stackView == null) return;
                var gridToMoveTo = stackView == tradeUiReflector.getPlayerCargoView().getPrivateObject() ? tradeUiReflector.getOtherCargoView() : tradeUiReflector.getPlayerCargoView();
                var pickedUpStack = cargoTransferHandler.getPickedUpStack();
                for(var item : gridToMoveTo.getStacks()) {
                    if(Objects.equals(item.getStack().getCommodityId(), pickedUpStack.getCommodityId())) {
                        var pos = ((UIComponentAPI)item).getPosition();
                        mouseX = (int) pos.getCenterX();
                        mouseY = (int) pos.getCenterY();
                        InputShim.mouseMove((int) pos.getCenterX(), (int) pos.getCenterY());
                        return;
                    }
                }
                var gridPos = ((UIComponentAPI)gridToMoveTo.getPrivateObject()).getPosition();
                for(int row = 0; row < gridToMoveTo.getPrivateObject().getRows(); row++) {
                    for(int col = 0; col < gridToMoveTo.getPrivateObject().getCols(); col++) {
                        int xPos = (int)gridPos.getX() + (col * 100);
                        int yPos = (int)gridPos.getY() + (int)gridPos.getHeight() - 100 - (row * 100);

                        boolean cellFilled = false;
                        for(var item : gridToMoveTo.getStacks()) {
                            if(((UIComponentAPI)item).getPosition().getX() == xPos && ((UIComponentAPI)item).getPosition().getY() == yPos) {
                                cellFilled = true;
                                break;
                            }
                        }
                        if(!cellFilled) {
                            mouseX = xPos + 50;
                            mouseY = yPos + 50;
                            InputShim.mouseMove(mouseX, mouseY);
                            return;
                        }
                    }
                }
            }
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
