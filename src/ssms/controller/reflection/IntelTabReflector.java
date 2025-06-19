package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.comms.v2.EventsPanel;
import com.fs.starfarer.campaign.ui.intel.FactionIntelPanel;

public class IntelTabReflector {
    CoreUIAPI coreUIAPI;
    BorderedPanelReflector borderedPanelReflector;
    UIPanelAPI intelUiParent;
    static Class<?> intelTabCls;
    static Object getFactionPanel;
    static Object getEventsPanel;
    static Object getPlanetsPanel;

    private IntelTabReflector(CoreUIAPI coreUIAPI, BorderedPanelReflector borderedPanelReflector, UIPanelAPI intelUiParent) {
        this.coreUIAPI = coreUIAPI;
        this.borderedPanelReflector = borderedPanelReflector;
        this.intelUiParent = intelUiParent;
    }

    public EventsPanel getEventsPanel() {
        try {
            return (EventsPanel) MethodReflector.GetInstance().invoke(getEventsPanel, intelUiParent);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't reflect IntelTab UI getEventsPanel");
            return null;
        }
    }

    public FactionIntelPanel getFactionIntelPanel() {
        try {
            return (FactionIntelPanel) MethodReflector.GetInstance().invoke(getFactionPanel, intelUiParent);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't reflect IntelTab UI getFactionIntelPanel");
            return null;
        }
    }

    public UIPanelAPI getPlanetTabData() {
        try {
            return (UIPanelAPI) MethodReflector.GetInstance().invoke(getPlanetsPanel, intelUiParent);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't reflect IntelTab UI getPlanetsPanel");
            return null;
        }
    }

    public BorderedPanelReflector getBorderedPanel() {
        return borderedPanelReflector;
    }

    public static IntelTabReflector TryGet(CoreUIAPI coreUIAPI, BorderedPanelReflector borderedPanelReflector) {
        if(intelTabCls == null) {
            try {
                var intelTabUi = borderedPanelReflector.getInnerPanel();

                getFactionPanel = ClassReflector.GetInstance().getDeclaredMethod(intelTabUi.getClass(), "getFactionPanel");
                getEventsPanel = ClassReflector.GetInstance().getDeclaredMethod(intelTabUi.getClass(), "getEventsPanel");
                getPlanetsPanel = ClassReflector.GetInstance().getDeclaredMethod(intelTabUi.getClass(), "getPlanetsPanel");
                intelTabCls = intelTabUi.getClass();
                return new IntelTabReflector(coreUIAPI, borderedPanelReflector, intelTabUi);
            } catch (Throwable ex) {
                Global.getLogger(IntelTabReflector.class).error("Couldn't reflect into IntelTab UI!", ex);
            }
        } else if(intelTabCls.isAssignableFrom(borderedPanelReflector.getInnerPanel().getClass())) {
            return new IntelTabReflector(coreUIAPI, borderedPanelReflector, borderedPanelReflector.getInnerPanel());
        }
        return null;
    }
}
