package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.fleet.CargoData;
import com.fs.starfarer.campaign.ui.trade.CargoItemStack;
import com.fs.starfarer.campaign.ui.trade.CargoStackView;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class CargoTransferHandlerReflector {
    CargoTransferHandlerAPI cargoTransferHandler;
    MethodHandle getPickedUpStack;
    MethodReflector getOrigStackSource;
    FieldReflector scrollbarField;
    public CargoTransferHandlerReflector(CargoTransferHandlerAPI cargoTransferHandler) throws  Throwable {

        this.cargoTransferHandler = cargoTransferHandler;
        ClassReflector cthApiReflector = new ClassReflector(cargoTransferHandler.getClass());
        for(FieldReflector field : cthApiReflector.getDeclaredFields()) {
            Class<?> fieldCls = field.getType();
            // we're iterating through obfuscating fields finding the first one that isn't cargo-related - this is the scrollbar
            if(UIPanelAPI.class.isAssignableFrom(fieldCls) &&
                    !CargoItemStack.class.isAssignableFrom(fieldCls) &&
                    !CargoStackView.class.isAssignableFrom(fieldCls) &&
                    !CargoData.class.isAssignableFrom(fieldCls)) {
                scrollbarField = field;
                break;
            }
        }

        getPickedUpStack = MethodHandles.lookup().findVirtual(cargoTransferHandler.getClass(), "getPickedUpStack", MethodType.methodType(CargoItemStack.class));
        getOrigStackSource = cthApiReflector.findDeclaredMethod("getOrigStackSource");
    }

    public CargoItemStack getPickedUpStack() {
        try {
            return (CargoItemStack) getPickedUpStack.invoke(cargoTransferHandler);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't invoke getPickedUpStack from cargo transfer handler!", ex);
            return null;
        }
    }

    public UIPanelAPI getOrigStackSource() {
        return (UIPanelAPI) getOrigStackSource.invoke(cargoTransferHandler);
    }

    public CargoTransferHandlerAPI getCargoTransferHandler() {
        return cargoTransferHandler;
    }

    public UIPanelAPI getScrollbar() {
        return (UIPanelAPI) scrollbarField.get(cargoTransferHandler);
    }
}
