package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
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
    CoreUIAPI coreUIAPI;
    static MethodHandle confirmTransaction;
    static MethodHandle cancelTransaction;
    static Object getEntityCargoView;
    static Object getPlayerCargoView;
    static Object getTransferHandler;
    static Class<?> tradeParentCls;
    private TradeUiReflector(Object tradeObj, UIPanelAPI parentObj, CoreUIAPI coreUIAPI) throws Throwable {
        this.tradeObj = tradeObj;
        this.parent = parentObj;
        this.coreUIAPI = coreUIAPI;
        if(getTransferHandler == null) {
            confirmTransaction = MethodHandles.lookup().findVirtual(tradeObj.getClass(), "confirmTransaction", MethodType.methodType(void.class));

            cancelTransaction = MethodHandles.lookup().findVirtual(tradeObj.getClass(), "cancelTransaction", MethodType.methodType(void.class));

            getEntityCargoView = ClassReflector.GetInstance().findDeclaredMethod(tradeObj.getClass(), "getEntityCargoDisplay");

            getPlayerCargoView = ClassReflector.GetInstance().findDeclaredMethod(tradeObj.getClass(), "getPlayerCargoDisplay");

            getTransferHandler = ClassReflector.GetInstance().findDeclaredMethod(tradeObj.getClass(), "getTransferHandler");
        }
    }

    public CoreUIAPI getCoreUIAPI() {
        return coreUIAPI;
    }

    public static TradeUiReflector TryGet(CoreUIAPI coreUIAPI) {
        try {
            var children = UIPanelReflector.getChildItems((UIPanelAPI) coreUIAPI);
            for(var coreuiChild : children) {
                if(UIPanelAPI.class.isAssignableFrom(coreuiChild.getClass())) {
                    var tradeParents = UIPanelReflector.getChildItems((UIPanelAPI) coreuiChild);
                    if (tradeParents.size() == 1) {
                        if(tradeParentCls == null) {
                            if(UIPanelAPI.class.isAssignableFrom(tradeParents.get(0).getClass())) {
                                var tradeParent = (UIPanelAPI) tradeParents.get(0);
                                var shouldShowLogisticsOnSwitch = ClassReflector.GetInstance().findDeclaredMethod(tradeParent.getClass(), "shouldShowLogisticsOnSwitch");
                                if (shouldShowLogisticsOnSwitch != null) {
                                    var tradePanel = UIPanelReflector.getChildItems(tradeParent);
                                    if (tradePanel != null && !tradePanel.isEmpty() && tradePanel.size() <= 2) {
                                        return new TradeUiReflector(tradePanel.get(0), tradeParent, coreUIAPI);
                                    }
                                }
                            }
                        } else if(tradeParentCls.isAssignableFrom(tradeParents.get(0).getClass())) {
                            var tradePanel = UIPanelReflector.getChildItems((UIPanelAPI) tradeParents);
                            if (!tradePanel.isEmpty()) {
                                return new TradeUiReflector(tradePanel.get(0), (UIPanelAPI) tradeParents.get(0), coreUIAPI);
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
            var cargoViewParent = MethodReflector.GetInstance().invoke(getEntityCargoView, tradeObj);
            MethodHandle getDataGrid = MethodHandles.lookup().findVirtual(cargoViewParent.getClass(), "getCargoDataView", MethodType.methodType(CargoDataGridView.class));
            return new CargoDataGridViewReflector((CargoDataGridView) getDataGrid.invoke(cargoViewParent), (UIPanelAPI) cargoViewParent);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get data grid view for the upper cargo view of the trade UI!", ex);
        }
        return null;
    }

    public CargoDataGridViewReflector getPlayerCargoView() {
        try {
            var cargoViewParent = MethodReflector.GetInstance().invoke(getPlayerCargoView, tradeObj);
            MethodHandle getDataGrid = MethodHandles.lookup().findVirtual(cargoViewParent.getClass(), "getCargoDataView", MethodType.methodType(CargoDataGridView.class));
            return new CargoDataGridViewReflector((CargoDataGridView) getDataGrid.invoke(cargoViewParent), (UIPanelAPI) cargoViewParent);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get data grid view for the upper cargo view of the trade UI!", ex);
        }
        return null;
    }

    public CargoTransferHandlerReflector getCargoTransferHandler() {
        try {
            return new CargoTransferHandlerReflector((CargoTransferHandlerAPI) MethodReflector.GetInstance().invoke(getTransferHandler, tradeObj));
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
