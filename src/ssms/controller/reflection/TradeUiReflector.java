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

public class TradeUiReflector extends UIPanelReflector {
    CoreUIAPI coreUIAPI;
    static MethodHandle confirmTransaction;
    static MethodHandle cancelTransaction;
    static MethodReflector getEntityCargoView;
    static MethodReflector getPlayerCargoView;
    static MethodReflector getTransferHandler;
    static Class<?> tradeParentCls;
    static Class<?> tradePanelCls;
    private TradeUiReflector(UIPanelAPI tradeObj, CoreUIAPI coreUIAPI) {
        super(tradeObj);
        this.coreUIAPI = coreUIAPI;
    }

    public CoreUIAPI getCoreUIAPI() {
        return coreUIAPI;
    }

    public static TradeUiReflector TryGet(CoreUIAPI coreUIAPI, BorderedPanelReflector borderedPanelReflector) {
        var parentPanel = borderedPanelReflector.getInnerPanel();
        if (tradeParentCls == null || tradePanelCls == null) {
            try {
                var shouldShowLogisticsOnSwitch = new ClassReflector(parentPanel.getPanel().getClass()).findDeclaredMethod("shouldShowLogisticsOnSwitch");
                if (shouldShowLogisticsOnSwitch != null) {
                    var childItems = parentPanel.getChildItems();
                    if (childItems != null && !childItems.isEmpty() && childItems.size() <= 2 && childItems.get(0) instanceof UIPanelAPI tradePanel) {
                        confirmTransaction = MethodHandles.lookup().findVirtual(tradePanel.getClass(), "confirmTransaction", MethodType.methodType(void.class));

                        cancelTransaction = MethodHandles.lookup().findVirtual(tradePanel.getClass(), "cancelTransaction", MethodType.methodType(void.class));

                        ClassReflector tradeObjCls = new ClassReflector(tradePanel.getClass());

                        getEntityCargoView = tradeObjCls.findDeclaredMethod("getEntityCargoDisplay");

                        getPlayerCargoView = tradeObjCls.findDeclaredMethod("getPlayerCargoDisplay");

                        getTransferHandler = tradeObjCls.findDeclaredMethod("getTransferHandler");

                        tradeParentCls = parentPanel.getPanel().getClass();
                        tradePanelCls = tradePanel.getClass();
                        return new TradeUiReflector(tradePanel, coreUIAPI);
                    }
                }
            } catch (Throwable ex) {
                Global.getLogger(TradeUiReflector.class).warn("Couldn't reflect trade UI type!", ex);
            }
        } else if (tradeParentCls.isAssignableFrom(parentPanel.getPanel().getClass())) {
            var childItems = parentPanel.getChildItems();
            if (!childItems.isEmpty() && tradePanelCls.isAssignableFrom(childItems.get(0).getClass())) {
                return new TradeUiReflector((UIPanelAPI) childItems.get(0), coreUIAPI);
            }
        }
        return null;
    }

    public CargoDataGridViewReflector getOtherCargoView() {
        try {
            var cargoViewParent = getEntityCargoView.invoke(panel);
            MethodHandle getDataGrid = MethodHandles.lookup().findVirtual(cargoViewParent.getClass(), "getCargoDataView", MethodType.methodType(CargoDataGridView.class));
            return new CargoDataGridViewReflector((CargoDataGridView) getDataGrid.invoke(cargoViewParent), (UIPanelAPI) cargoViewParent);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get data grid view for the upper cargo view of the trade UI!", ex);
        }
        return null;
    }

    public CargoDataGridViewReflector getPlayerCargoView() {
        try {
            var cargoViewParent = getPlayerCargoView.invoke(panel);
            MethodHandle getDataGrid = MethodHandles.lookup().findVirtual(cargoViewParent.getClass(), "getCargoDataView", MethodType.methodType(CargoDataGridView.class));
            return new CargoDataGridViewReflector((CargoDataGridView) getDataGrid.invoke(cargoViewParent), (UIPanelAPI) cargoViewParent);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get data grid view for the upper cargo view of the trade UI!", ex);
        }
        return null;
    }

    public CargoTransferHandlerReflector getCargoTransferHandler() {
        try {
            return new CargoTransferHandlerReflector((CargoTransferHandlerAPI) getTransferHandler.invoke(panel));
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get data grid view for the upper cargo view of the trade UI!", ex);
        }
        return null;
    }
}
