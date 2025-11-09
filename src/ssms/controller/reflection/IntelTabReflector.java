package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.comms.v2.EventsPanel;
import com.fs.starfarer.campaign.ui.intel.FactionIntelPanel;

public class IntelTabReflector extends UIPanelReflector {
    CoreUIAPI coreUIAPI;
    BorderedPanelReflector borderedPanelReflector;
    UIPanelAPI intelUiParent;
    static Class<?> intelTabCls;
    static MethodReflector getFactionPanel;
    static MethodReflector getEventsPanel;
    static MethodReflector getPlanetsPanel;

    private IntelTabReflector(CoreUIAPI coreUIAPI, BorderedPanelReflector borderedPanelReflector, UIPanelAPI intelUiParent) {
        super(intelUiParent);
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

    public static IntelTabReflector TryGet(CoreUIAPI coreUIAPI, UIPanelReflector intelPanelReflector) {
        if(intelTabCls == null) {
            var intelTabUi = intelPanelReflector.getPanel();
            ClassReflector intelTabClsReflector = new ClassReflector(intelTabUi.getClass());
            getFactionPanel = intelTabClsReflector.getDeclaredMethod("getFactionPanel");
            getEventsPanel = intelTabClsReflector.getDeclaredMethod("getEventsPanel");
            getPlanetsPanel = intelTabClsReflector.getDeclaredMethod("getPlanetsPanel");
            if(getFactionPanel != null && getEventsPanel != null && getPlanetsPanel != null) {
                intelTabCls = intelTabUi.getClass();
                return new IntelTabReflector(coreUIAPI, borderedPanelReflector, intelTabUi);
            }
        } else if(intelTabCls.isAssignableFrom(intelPanelReflector.getPanel().getClass())) {
            var borderedPanel = BorderedPanelReflector.TryGet(coreUIAPI, intelPanelReflector.getParent());
            return new IntelTabReflector(coreUIAPI, borderedPanel, intelPanelReflector.getPanel());
        }
        return null;
    }
}
