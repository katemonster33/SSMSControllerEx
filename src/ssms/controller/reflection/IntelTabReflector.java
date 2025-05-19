package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.comms.v2.EventsPanel;
import com.fs.starfarer.campaign.ui.intel.FactionIntelPanel;

public class IntelTabReflector {
    UIPanelAPI intelUiParent;
    EventsPanel eventsPanel;
    UIPanelAPI planetTabData;
    FactionIntelPanel factionIntelPanel;
    private IntelTabReflector(UIPanelAPI intelUiParent, EventsPanel eventsPanel, UIPanelAPI planetTabData, FactionIntelPanel factionIntelPanel) {
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
            var children = UIPanelReflector.getChildItems(intelTabUi);
            if(children.size() == 6) {
                EventsPanel eventsPanel = null;
                FactionIntelPanel factionIntelPanel = null;
                UIPanelAPI planetTabData = null;
                for(int i = 3; i < children.size(); i++) {
                    var child = children.get(i);
                    if(child instanceof EventsPanel tmp) {
                        eventsPanel = tmp;
                    } else if(child instanceof  FactionIntelPanel tmp) {
                        factionIntelPanel = tmp;
                    } else if(planetTabData == null && UIPanelAPI.class.isAssignableFrom(child.getClass())) {
                        planetTabData = (UIPanelAPI) child;
                    }
                }
                if(eventsPanel != null && factionIntelPanel != null && planetTabData != null) {
                    return new IntelTabReflector(intelTabUi, eventsPanel, planetTabData, factionIntelPanel);
                }
            }
        } catch(Throwable ex) {
            Global.getLogger(IntelTabReflector.class).error("Couldn't reflect into IntelTab UI!", ex);
        }
        return null;
    }
}
