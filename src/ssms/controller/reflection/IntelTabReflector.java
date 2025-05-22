package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.comms.v2.EventsPanel;
import com.fs.starfarer.campaign.ui.UITable;
import com.fs.starfarer.campaign.ui.intel.FactionIntelPanel;
import com.fs.starfarer.campaign.ui.intel.PlanetListV2;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class IntelTabReflector {
    CoreUIAPI coreUIAPI;
    UIPanelAPI intelUiParent;
    EventsPanel eventsPanel;
    UIPanelAPI planetTabData;
    FactionIntelPanel factionIntelPanel;


    private IntelTabReflector(CoreUIAPI coreUIAPI, UIPanelAPI intelUiParent, EventsPanel eventsPanel, UIPanelAPI planetTabData, FactionIntelPanel factionIntelPanel) {
        this.coreUIAPI = coreUIAPI;
        this.intelUiParent = intelUiParent;
        this.eventsPanel = eventsPanel;
        this.planetTabData = planetTabData;
        this.factionIntelPanel = factionIntelPanel;
    }

    public EventsPanel getEventsPanel() {
        return eventsPanel;
    }

    public FactionIntelPanel getFactionIntelPanel() {
        return factionIntelPanel;
    }

    public UIPanelAPI getPlanetTabData() {
        return planetTabData;
    }

    public static IntelTabReflector TryGet(CoreUIAPI coreUIAPI, BorderedPanelReflector borderedPanelReflector) {
        try {
            var intelTabUi = borderedPanelReflector.getPanel();

            var getFactionPanel = ClassReflector.GetInstance().getDeclaredMethod(intelTabUi.getClass(), "getFactionPanel");
            FactionIntelPanel factionIntelPanel = (FactionIntelPanel) MethodReflector.GetInstance().invoke(getFactionPanel, intelTabUi);

            var getEventsPanel = ClassReflector.GetInstance().getDeclaredMethod(intelTabUi.getClass(), "getEventsPanel");
            EventsPanel eventsPanel = (EventsPanel) MethodReflector.GetInstance().invoke(getEventsPanel, intelTabUi);

            var getPlanetsPanel = ClassReflector.GetInstance().getDeclaredMethod(intelTabUi.getClass(), "getPlanetsPanel");
            UIPanelAPI planetPanel = (UIPanelAPI) MethodReflector.GetInstance().invoke(getPlanetsPanel, intelTabUi);

            return new IntelTabReflector(coreUIAPI, intelTabUi, eventsPanel, planetPanel, factionIntelPanel);
        } catch(Throwable ex) {
            Global.getLogger(IntelTabReflector.class).error("Couldn't reflect into IntelTab UI!", ex);
        }
        return null;
    }
}
