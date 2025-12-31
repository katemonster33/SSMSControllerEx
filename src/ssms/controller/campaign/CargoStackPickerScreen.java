package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventMouseButton;
 import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.ui.trade.CargoDataGridView;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
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
    float mouseX = -1, mouseY = -1;
    ScrollbarUiReflector scrollbar;
    boolean wasScrollerVisible = false;
    boolean movedNewCargoStack = false;
    final float mouseMoveFactor = 400.f;

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
            addButtonPressHandler("Select less", LogicalButtons.DpadLeft, (float advance) -> updateMouseXPos((int) mouseX - 10));
            addButtonPressHandler("Select more", LogicalButtons.DpadRight, (float advance) -> updateMouseXPos((int) mouseX + 10));
            addButtonPressHandler("Select all", LogicalButtons.DpadUp, (float advance) -> updateMouseXPos(Integer.MAX_VALUE));
            addButtonPressHandler("Select none", LogicalButtons.DpadDown, (float advance) -> updateMouseXPos(Integer.MIN_VALUE));
            addButtonPressHandler("Cancel", LogicalButtons.B, (float advance) -> {
                InputShim.mouseDownUp((int) mouseX, (int) mouseY, InputEventMouseButton.RIGHT);
                InputShim.mouseUp((int) mouseX, (int) mouseY, InputEventMouseButton.LEFT);
            });
            addButtonPressHandler("Confirm", LogicalButtons.A, (float advance) -> InputShim.mouseUp((int) mouseX, (int) mouseY, InputEventMouseButton.LEFT));
        } else {
            addButtonPressHandler("Cancel", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
            addButtonPressHandler("Confirm", LogicalButtons.A, (float advance) -> InputShim.mouseDownUp((int) mouseX, (int) mouseY, InputEventMouseButton.LEFT));
        }
        return indicators;
    }

    void updateMouseXPos(float newVal) {
        if(scrollbar == null) return;
        var scrollbarPos = scrollbar.getPrivateObj().getPosition();
        mouseX = newVal;
        if(mouseX < scrollbarPos.getX()) mouseX = (int)scrollbarPos.getX();
        else if(mouseX > scrollbarPos.getX() + scrollbarPos.getWidth()) mouseX = (int)(scrollbarPos.getX() + scrollbarPos.getWidth());
        InputShim.mouseMove((int) mouseX, (int) mouseY);
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
        if (scrollbar == null) {
            if(wasScrollerVisible) {
                wasScrollerVisible = false;
                refreshIndicators();
            }
            if (cargoTransferHandler.getPickedUpStack() == null) {
                InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, TradeScreen.ID, new Object[]{tradeUiReflector});
            } else if(!movedNewCargoStack) {
                movedNewCargoStack = true;
                moveNewCargoStackToOtherGrid();
            }
        } else if (mouseX == -1 || mouseY == -1) {
            mouseX = (int) scrollbar.getPrivateObj().getPosition().getX();
            mouseY = (int) scrollbar.getPrivateObj().getPosition().getCenterY();
        }
        if(mouseX != -1 && mouseY != -1) {
            var joystickVal = SSMSControllerModPluginEx.controller.getJoystick(Joystick.Left);
            if(joystickVal.x != 0.f || joystickVal.y != 0.f) {
                if (scrollbar != null) {
                    updateMouseXPos(mouseX + (joystickVal.getX() * mouseMoveFactor * advance));
                } else {
                    mouseX += (joystickVal.getX() * mouseMoveFactor * advance);
                    mouseY -= (joystickVal.getY() * mouseMoveFactor * advance);
                    InputShim.mouseMove((int) mouseX, (int) mouseY);
                }
            }
        }
    }

    void mouseOverGridCellAt(float xPos, float yPos, CargoDataGridViewReflector gridView) {
        var gridScroller = new ScrollPanelReflector(gridView.getScroller());
        float scrollVal = gridScroller.getScrollPanel().getYOffset();
        //gridScroller.ensureVisible(item);
        gridScroller.scrollToY(((UIComponentAPI)gridView.getPrivateObject()).getPosition().getHeight() - yPos +  100);

        mouseX = xPos + 50;
        var gridViewObj = (UIComponentAPI)gridView.getPrivateObject();
        mouseY = yPos + 50 - scrollVal + gridScroller.getScrollPanel().getYOffset();
        InputShim.mouseMove((int) mouseX, (int) mouseY);
    }

    private void moveNewCargoStackToOtherGrid() {
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
            if(Objects.equals(item.getStack().getCommodityId(), pickedUpStack.getCommodityId()) &&
                Objects.equals(item.getStack().getDisplayName(), pickedUpStack.getDisplayName())) {
                var pos = ((UIComponentAPI)item).getPosition();
                mouseOverGridCellAt(pos.getX(), pos.getY(), gridToMoveTo);
                return;
            }
        }
        var gridPos = ((UIComponentAPI)gridToMoveTo.getPrivateObject()).getPosition();
        for(int row = 0; row < gridToMoveTo.getPrivateObject().getRows(); row++) {
            for(int col = 0; col < gridToMoveTo.getPrivateObject().getCols(); col++) {
                int xPos = (int)gridPos.getX() + (col * 100);
                int yPos = (int)gridPos.getY() + (int)gridPos.getHeight() - 100 - (row * 100);

                if(gridToMoveTo.getStacks().stream().noneMatch((item) ->
                        ((UIComponentAPI)item).getPosition().getX() == xPos && ((UIComponentAPI)item).getPosition().getY() == yPos)) {
                    mouseOverGridCellAt(xPos, yPos, gridToMoveTo);
                    //InputShim.mouseMove((int)xPos, (int)yPos);
                    return;
                }
            }
        }
    }

    @Override
    public String getId() {
        return ID;
    }
}
