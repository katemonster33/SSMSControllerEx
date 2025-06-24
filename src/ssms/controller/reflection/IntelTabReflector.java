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
    static MethodReflector getFactionPanel;
    static MethodReflector getEventsPanel;
    static MethodReflector getPlanetsPanel;

    private IntelTabReflector(CoreUIAPI coreUIAPI, BorderedPanelReflector borderedPanelReflector, UIPanelAPI intelUiParent) {
        this.coreUIAPI = coreUIAPI;
        this.borderedPanelReflector = borderedPanelReflector;
        this.intelUiParent = intelUiParent;
    }

    public EventsPanel getEventsPanel() {
        return (EventsPanel) getEventsPanel.invoke(intelUiParent);
    }

    public FactionIntelPanel getFactionIntelPanel() {
        return (FactionIntelPanel) getFactionPanel.invoke(intelUiParent);
    }

    public UIPanelReflector getPlanetTabData() {
        return new UIPanelReflector((UIPanelAPI) getPlanetsPanel.invoke(intelUiParent));
    }

    public BorderedPanelReflector getBorderedPanel() {
        return borderedPanelReflector;
    }

    public static IntelTabReflector TryGet(CoreUIAPI coreUIAPI, BorderedPanelReflector borderedPanelReflector) {
        if(intelTabCls == null) {
            try {
                var intelTabUi = borderedPanelReflector.getInnerPanel().getPanel();
                ClassReflector intelTabClsReflector = new ClassReflector(intelTabUi.getClass());
                getFactionPanel = intelTabClsReflector.getDeclaredMethod("getFactionPanel");
                getEventsPanel = intelTabClsReflector.getDeclaredMethod("getEventsPanel");
                getPlanetsPanel = intelTabClsReflector.getDeclaredMethod("getPlanetsPanel");
                intelTabCls = intelTabUi.getClass();
                return new IntelTabReflector(coreUIAPI, borderedPanelReflector, intelTabUi);
            } catch (Throwable ex) {
                Global.getLogger(IntelTabReflector.class).error("Couldn't reflect into IntelTab UI!", ex);
            }
        } else if(intelTabCls.isAssignableFrom(borderedPanelReflector.getInnerPanel().getClass())) {
            return new IntelTabReflector(coreUIAPI, borderedPanelReflector, borderedPanelReflector.getInnerPanel().getPanel());
        }
        return null;
    }
}
