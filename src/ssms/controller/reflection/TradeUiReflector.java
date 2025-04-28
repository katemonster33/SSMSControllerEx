package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.ui.trade.CargoDataGridView;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class TradeUiReflector {
    Object tradeObj;
    UIPanelAPI parent;
    MethodHandle confirmTransaction;
    MethodHandle cancelTransaction;
    private TradeUiReflector(Object tradeObj, UIPanelAPI parentObj) throws Throwable {
        this.tradeObj = tradeObj;
        this.parent = parentObj;

        confirmTransaction = MethodHandles.lookup().findVirtual(tradeObj.getClass(), "confirmTransaction", MethodType.methodType(void.class));

        cancelTransaction = MethodHandles.lookup().findVirtual(tradeObj.getClass(), "cancelTransaction", MethodType.methodType(void.class));
    }

    public static TradeUiReflector TryGet(CoreUIAPI coreUIAPI) {
        try {
            var children = UIPanelReflector.getChildItems((UIPanelAPI) coreUIAPI);
            for(var coreuiChild : children) {
                if(UIPanelAPI.class.isAssignableFrom(coreuiChild.getClass())) {
                    var tradeParents = UIPanelReflector.getChildItems((UIPanelAPI) coreuiChild);
                    if (tradeParents.size() == 1 && UIPanelAPI.class.isAssignableFrom(tradeParents.get(0).getClass())) {
                        var tradeParent = (UIPanelAPI) tradeParents.get(0);
                        var shouldShowLogisticsOnSwitch = ClassReflector.GetInstance().findDeclaredMethod(tradeParent.getClass(), "shouldShowLogisticsOnSwitch");
                        if (shouldShowLogisticsOnSwitch != null) {
                            var tradePanel = UIPanelReflector.getChildItems(tradeParent);
                            if (tradePanel != null && !tradePanel.isEmpty() && tradePanel.size() <= 2) {
                                return new TradeUiReflector(tradePanel.get(0), tradeParent);
                            }
                        }
                    }
                }
            }
            Global.getLogger(TradeUiReflector.class).warn("Did not find any trade UI in the UI tree underneath InteractionDialog");
        } catch (Throwable ex) {
            Global.getLogger(TradeUiReflector.class).warn("Failed to reflect trade UI object", ex);
        }
        return null;
    }

    public CargoDataGridViewReflector getOtherCargoView() {
        try {
            var children = UIPanelReflector.getChildItems((UIPanelAPI) tradeObj);
            if(children.size() >= 6) {
                var cargoViewParent = children.get(4);
                MethodHandle getDataGrid = MethodHandles.lookup().findVirtual(cargoViewParent.getClass(), "getCargoDataView", MethodType.methodType(CargoDataGridView.class));
                return new CargoDataGridViewReflector((CargoDataGridView) getDataGrid.invoke(cargoViewParent));
            }
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get data grid view for the upper cargo view of the trade UI!", ex);
        }
        return null;
    }

    public CargoDataGridViewReflector getPlayerCargoView() {
        try {
            var children = UIPanelReflector.getChildItems((UIPanelAPI) tradeObj);
            if(children.size() >= 6) {
                var cargoViewParent = children.get(3);
                MethodHandle getDataGrid = MethodHandles.lookup().findVirtual(cargoViewParent.getClass(), "getCargoDataView", MethodType.methodType(CargoDataGridView.class));
                return new CargoDataGridViewReflector((CargoDataGridView) getDataGrid.invoke(cargoViewParent));
            }
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get data grid view for the upper cargo view of the trade UI!", ex);
        }
        return null;
    }

    public UIComponentAPI getTradePanel() {
        return (UIComponentAPI) tradeObj;
    }

    public UIPanelAPI getParent() {
        return parent;
    }
}
