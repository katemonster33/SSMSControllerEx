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
    static Class<?> tradePanelCls;
    private TradeUiReflector(Object tradeObj, UIPanelAPI parentObj, CoreUIAPI coreUIAPI) {
        this.tradeObj = tradeObj;
        this.parent = parentObj;
        this.coreUIAPI = coreUIAPI;
    }

    public CoreUIAPI getCoreUIAPI() {
        return coreUIAPI;
    }

    public static TradeUiReflector TryGet(CoreUIAPI coreUIAPI, BorderedPanelReflector borderedPanelReflector) {
        var parentPanel = borderedPanelReflector.getPanel();
        if (tradeParentCls == null || tradePanelCls == null) {
            try {
                var shouldShowLogisticsOnSwitch = ClassReflector.GetInstance().findDeclaredMethod(parentPanel.getClass(), "shouldShowLogisticsOnSwitch");
                if (shouldShowLogisticsOnSwitch != null) {
                    var tradePanel = UIPanelReflector.getChildItems(parentPanel);
                    if (tradePanel != null && !tradePanel.isEmpty() && tradePanel.size() <= 2) {
                        var tradeObj = tradePanel.get(0);
                        confirmTransaction = MethodHandles.lookup().findVirtual(tradeObj.getClass(), "confirmTransaction", MethodType.methodType(void.class));

                        cancelTransaction = MethodHandles.lookup().findVirtual(tradeObj.getClass(), "cancelTransaction", MethodType.methodType(void.class));

                        getEntityCargoView = ClassReflector.GetInstance().findDeclaredMethod(tradeObj.getClass(), "getEntityCargoDisplay");

                        getPlayerCargoView = ClassReflector.GetInstance().findDeclaredMethod(tradeObj.getClass(), "getPlayerCargoDisplay");

                        getTransferHandler = ClassReflector.GetInstance().findDeclaredMethod(tradeObj.getClass(), "getTransferHandler");

                        tradeParentCls = parentPanel.getClass();
                        tradePanelCls = tradeObj.getClass();
                        return new TradeUiReflector(tradeObj, parentPanel, coreUIAPI);
                    }
                }
            } catch (Throwable ex) {
                Global.getLogger(TradeUiReflector.class).warn("Couldn't reflect trade UI type!", ex);
            }
        } else if (tradeParentCls.isAssignableFrom(parentPanel.getClass())) {
            var tradePanel = UIPanelReflector.getChildItems(parentPanel);
            if (!tradePanel.isEmpty() && tradePanelCls.isAssignableFrom(tradePanel.get(0).getClass())) {
                return new TradeUiReflector(tradePanel.get(0), parentPanel, coreUIAPI);
            }
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
