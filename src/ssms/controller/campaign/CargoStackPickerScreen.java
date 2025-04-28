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
    CargoDataGridViewReflector curGrid;
    CargoStackView curStack;
    CargoTransferHandlerAPI cargoTransferHandler;
    Object scrollbarField;
    UIPanelAPI scrollbar;
    MethodHandle getScrollbarValue;
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
        curGrid = (CargoDataGridViewReflector) args[1];
        curStack = (CargoStackView) args[2];
        scrollbar = null;
        mouseX = mouseY = -1;
    }

    @Override
    public void deactivate() {
        InputEventReflector.GetInstance().GetShim().stopOverrideMousePos();
    }

    void confirm() {
        try {
            List<InputEventAPI> events = new ArrayList<>();
            events.add(InputEventReflector.GetInstance().createMouseUpEvent(mouseX, mouseY, 0));
            tradeUiReflector.getParent().processInput(events);
        }catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Failed send left-click mouse event!", ex);
        }
    }

    void cancel() {
        try {
            List<InputEventAPI> events = new ArrayList<>();
            events.add(InputEventReflector.GetInstance().createMouseDownEvent(mouseX, mouseY, 1));
            events.add(InputEventReflector.GetInstance().createMouseUpEvent(mouseX, mouseY, 1));
            // send the left-click up event after the right click - this ensures the UI doesn't get confused seeing multiple left-click events in a row
            events.add(InputEventReflector.GetInstance().createMouseUpEvent(mouseX, mouseY, 0));
            tradeUiReflector.getParent().processInput(events);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Failed send right-click mouse event!", ex);
        }
    }

    void updateMousePos() {
        InputEventReflector.GetInstance().GetShim().overrideMousePos(mouseX, mouseY);
    }

    public boolean tryGetComponents() {
        try {
            var children = UIPanelReflector.getChildItems((UIPanelAPI) tradeUiReflector.getTradePanel());
            for(var child : children) {
                if(CargoTransferHandlerAPI.class.isAssignableFrom(child.getClass())) {
                    cargoTransferHandler = (CargoTransferHandlerAPI) child;
                    for(Object field : ClassReflector.GetInstance().getDeclaredFields(cargoTransferHandler.getClass())) {
                        Class<?> fieldCls = FieldReflector.GetInstance().GetVariableType(field);
                        if(UIPanelAPI.class.isAssignableFrom(fieldCls) &&
                                !CargoItemStack.class.isAssignableFrom(fieldCls) &&
                                !CargoStackView.class.isAssignableFrom(fieldCls) &&
                                !CargoData.class.isAssignableFrom(fieldCls)) {
                            scrollbarField = field;
                            scrollbar = (UIPanelAPI) FieldReflector.GetInstance().GetVariable(scrollbarField, cargoTransferHandler);
                            if(scrollbar != null) {
                                mouseX = (int) scrollbar.getPosition().getX();
                                mouseY = (int) scrollbar.getPosition().getCenterY();
                                getScrollbarValue = MethodHandles.lookup().findVirtual(scrollbar.getClass(), "getValue", MethodType.methodType(int.class));

                                updateMousePos();
                            }
                            return true;
                        }
                    }
                }
            }
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get data grid view for the upper cargo view of the trade UI!", ex);
        }
        return false;
    }

    @Override
    public void preInput(float advance) {
        if(scrollbar == null && !tryGetComponents()) {
            return;
        } else {
            try {
                // sometimes when trade UI calls us, the trade UI is not actually transferring cargo so we need to pass control back to TradeScreen
                if (scrollbar == null || FieldReflector.GetInstance().GetVariable(scrollbarField, cargoTransferHandler) == null) {
                    // scrollbar UI has been nullified without our intervention, assume the transfer is cancelled
                    InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, TradeScreen.ID, new Object[]{ tradeUiReflector });
                }
            } catch (Throwable ex) {
                Global.getLogger(getClass()).warn("Couldn't get the scrollbar from the cargo transfer handler, can't tell if it is open!");
                InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, TradeScreen.ID, new Object[]{ tradeUiReflector });
            }
        }
        if(controller.getButtonEvent(HandlerController.Buttons.LeftStickLeft) == 1 && controller.isLeftStickLeft()) {
            // the 10 is hard-coded in the scrollbar source code
            mouseX -= 10;
            mouseX = Math.max((int) scrollbar.getPosition().getX(), mouseX);
            updateMousePos();
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickRight) == 1 && controller.isLeftStickRight()) {
            mouseX += 10;
            mouseX = Math.min((int)(scrollbar.getPosition().getX() + scrollbar.getPosition().getWidth()), mouseX);
            updateMousePos();
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickUp) == 1 && controller.isLeftStickUp()) {
            mouseX = (int)scrollbar.getPosition().getX();
            updateMousePos();
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickDown) == 1 && controller.isLeftStickDown()) {
            mouseX = (int)(scrollbar.getPosition().getX() + scrollbar.getPosition().getWidth());
            updateMousePos();
        } else if(controller.getButtonEvent(HandlerController.Buttons.B) == 1 && controller.isButtonBPressed()) {
            cancel();
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, TradeScreen.ID, new Object[]{ tradeUiReflector });
        } else if(controller.getButtonEvent(HandlerController.Buttons.A) == 1 && controller.isButtonAPressed()) {
            confirm();
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, TradeScreen.ID, new Object[]{ tradeUiReflector });
        } else if(controller.getButtonEvent(HandlerController.Buttons.Y) == 1 && controller.isButtonYPressed()) {
            confirm();
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, TradeScreen.ID, new Object[]{ tradeUiReflector });
        }
    }

    @Override
    public String getId() {
        return ID;
    }
}
