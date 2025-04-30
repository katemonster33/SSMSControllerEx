package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.fleet.CargoData;
import com.fs.starfarer.campaign.ui.trade.CargoItemStack;
import com.fs.starfarer.campaign.ui.trade.CargoStackView;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class CargoTransferHandlerReflector {
    CargoTransferHandlerAPI cargoTransferHandler;
    Object scrollbarField;
    public CargoTransferHandlerReflector(CargoTransferHandlerAPI cargoTransferHandler) throws  Throwable {

        this.cargoTransferHandler = cargoTransferHandler;
        for(Object field : ClassReflector.GetInstance().getDeclaredFields(cargoTransferHandler.getClass())) {
            Class<?> fieldCls = FieldReflector.GetInstance().GetVariableType(field);
            // we're iterating through obfuscating fields finding the first one that isn't cargo-related - this is the scrollbar
            if(UIPanelAPI.class.isAssignableFrom(fieldCls) &&
                    !CargoItemStack.class.isAssignableFrom(fieldCls) &&
                    !CargoStackView.class.isAssignableFrom(fieldCls) &&
                    !CargoData.class.isAssignableFrom(fieldCls)) {
                scrollbarField = field;
                break;
            }
        }
    }

    public CargoTransferHandlerAPI getCargoTransferHandler() {
        return cargoTransferHandler;
    }

    public UIPanelAPI getScrollbar() {
        UIPanelAPI scrollbar = null;
        try {
            scrollbar = (UIPanelAPI) FieldReflector.GetInstance().GetVariable(scrollbarField, cargoTransferHandler);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get CargoTransferHandler's scrollbar element!", ex);
        }
        return scrollbar;
    }
}
