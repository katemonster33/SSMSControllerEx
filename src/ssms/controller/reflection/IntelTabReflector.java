package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
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

    static MethodHandle getPlanetListV2;
    static MethodHandle getPlanetListTableRows;
    static MethodHandle selectPlanetTableRow;
    static Object getList;
    static Object ensurePlanetVisible;
    static Class<?> tableItemCls;
    static Class<?> tableListCls;

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

    public PlanetListV2 getPlanetList() {
        try {
            return (PlanetListV2) getPlanetListV2.invoke(planetTabData);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't get planet list!", ex);
            return null;
        }
    }

    public void ensurePlanetVisible(Object planetObj) {
        try {
            var buttonField = ClassReflector.GetInstance().getDeclaredField(planetObj.getClass().getSuperclass().getSuperclass(), "button");

            var btn = (ButtonAPI) FieldReflector.GetInstance().GetVariable(buttonField, planetObj);

            ensurePlanetVisible(btn);
            var lst = MethodReflector.GetInstance().invoke(getList, getPlanetList().getTable());
            MethodReflector.GetInstance().invoke(ensurePlanetVisible, lst, btn);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't ensure that the desired planet is visible in the scroller!");
        }
    }

    public void selectPlanetTableRow(Object row, Object arg, boolean b) {
        try {
            selectPlanetTableRow.invoke(getPlanetList().getTable(), row, arg, b);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Failed to select row of planet list table!");
        }
    }

    public List<UIPanelAPI> getPlanetsTableRows() {
        var planetListUi = getPlanetList();
        List<UIPanelAPI> planets = new ArrayList<>();
        if(planetListUi != null)
        {
            try {
                var table = planetListUi.getTable();

                var planetsListRaw = (List<?>) getPlanetListTableRows.invoke(table);
                for(Object planet : planetsListRaw) {
                    if(UIPanelAPI.class.isAssignableFrom(planet.getClass())) {
                        planets.add((UIPanelAPI) planet);
                    }
                }
            } catch(Throwable ex) {
                Global.getLogger(getClass()).error("Couldn't fetch current list of planets!", ex);
            }
        }
        return planets;
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

            getPlanetListV2 = MethodHandles.lookup().findVirtual(planetPanel.getClass(), "getPlanetList2", MethodType.methodType(PlanetListV2.class));

            getPlanetListTableRows = MethodHandles.lookup().findVirtual(UITable.class, "getRows", MethodType.methodType(List.class));

            var getSelected = ClassReflector.GetInstance().getDeclaredMethod(UITable.class, "getSelected");
            tableItemCls = MethodReflector.GetInstance().getReturnType(getSelected);
            selectPlanetTableRow = MethodHandles.lookup().findVirtual(UITable.class, "select", MethodType.methodType(void.class, tableItemCls, Object.class, boolean.class));

            getList = ClassReflector.GetInstance().getDeclaredMethod(UITable.class, "getList");

            tableListCls = MethodReflector.GetInstance().getReturnType(getList);
            ensurePlanetVisible = ClassReflector.GetInstance().findDeclaredMethod(tableListCls, "ensureVisible");

            return new IntelTabReflector(coreUIAPI, intelTabUi, eventsPanel, planetPanel, factionIntelPanel);
        } catch(Throwable ex) {
            Global.getLogger(IntelTabReflector.class).error("Couldn't reflect into IntelTab UI!", ex);
        }
        return null;
    }
}
