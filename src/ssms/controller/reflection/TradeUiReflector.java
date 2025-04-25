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
    MethodHandle confirmTransaction;
    MethodHandle cancelTransaction;
    private TradeUiReflector(Object tradeObj) throws Throwable {
        this.tradeObj = tradeObj;

        confirmTransaction = MethodHandles.lookup().findVirtual(tradeObj.getClass(), "confirmTransaction", MethodType.methodType(void.class));

        cancelTransaction = MethodHandles.lookup().findVirtual(tradeObj.getClass(), "cancelTransaction", MethodType.methodType(void.class));
    }

    public static TradeUiReflector TryGet(CoreUIAPI coreUIAPI) {
        try {
            var children = UIPanelReflector.getChildItems((UIPanelAPI) coreUIAPI);
            if(!children.isEmpty()) {
                var tradeParents = UIPanelReflector.getChildItems((UIPanelAPI) children.get(children.size() - 1));
                if(!tradeParents.isEmpty()) {
                    var tradePanel = UIPanelReflector.getChildItems((UIPanelAPI) tradeParents.get(0));
                    if(tradePanel != null) {
                        return new TradeUiReflector(tradePanel.get(0));
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
}
