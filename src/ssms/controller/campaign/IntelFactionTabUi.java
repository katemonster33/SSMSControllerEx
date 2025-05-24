package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CampaignEngine;
import com.fs.starfarer.campaign.comms.IntelTabData;
import com.fs.starfarer.campaign.ui.intel.FactionIntelPanel;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.reflection.ClassReflector;
import ssms.controller.reflection.IntelTabReflector;
import ssms.controller.reflection.MethodReflector;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class IntelFactionTabUi extends InputScreenBase {
    public static final String ID = "IntelFactionTab";
    List<Pair<Indicators, String>> indicators;
    HandlerController controller;
    IntelTabReflector intelTabReflector;
    IntelFactionTabReflector intelFactionTabReflector;
    IntelTabData intelTabData;
    List<UIComponentAPI> factionButtons;
    int selectedIndex = -1;
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String[] getScopes() {
        return new String[] { CampaignScope.ID };
    }

    @Override
    public void activate(Object ... args) throws Throwable {
        intelTabReflector = (IntelTabReflector) args[0];
        controller = SSMSControllerModPluginEx.controller;
        intelFactionTabReflector = new IntelFactionTabReflector(intelTabReflector.getFactionIntelPanel());
        factionButtons = intelFactionTabReflector.getFactionButtons();
        intelTabData = CampaignEngine.getInstance().getUIData().getIntelData();
        selectedIndex = -1;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if (indicators == null) {
            indicators = new ArrayList<>();

            indicators.add(new Pair<>(Indicators.LeftStick, "Navigate"));
            indicators.add(new Pair<>(Indicators.A, "Select"));
            indicators.add(new Pair<>(Indicators.B, "Close"));
            indicators.add(new Pair<>(Indicators.LeftTrigger, "Select planet tab"));
        }
        return indicators;
    }

    void navigateButton() {
        if(factionButtons == null || factionButtons.isEmpty()) {
            selectedIndex = -1;
            return;
        }
        else if(selectedIndex < 0) selectedIndex = 0;
        else if(selectedIndex >= factionButtons.size()) selectedIndex = factionButtons.size() - 1;

        var pos = factionButtons.get(selectedIndex).getPosition();
        InputShim.mouseMove((int) pos.getCenterX(), (int) pos.getCenterY());
    }

    @Override
    public void preInput(float amount) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.INTEL) InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        else if(intelTabData.getSelectedTabIndex() == 0) InputScreenManager.getInstance().transitionDelayed(IntelTabUI.ID, intelTabReflector);
        else if(intelTabData.getSelectedTabIndex() == 1) InputScreenManager.getInstance().transitionDelayed(IntelPlanetTabUi.ID, intelTabReflector);

        if(controller.getButtonEvent(HandlerController.Buttons.LeftStickUp) == 1) {
            selectedIndex--;
            navigateButton();
            if(selectedIndex != -1) intelFactionTabReflector.ensureVisible(factionButtons.get(selectedIndex));
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickDown) == 1) {
            selectedIndex++;
            navigateButton();
            if(selectedIndex != -1) intelFactionTabReflector.ensureVisible(factionButtons.get(selectedIndex));
        } else if(controller.getButtonEvent(HandlerController.Buttons.A) == 1 && selectedIndex != -1 && selectedIndex < factionButtons.size()) {
            var pos = factionButtons.get(selectedIndex).getPosition();
            InputShim.mouseDownUp((int) pos.getCenterX(), (int) pos.getCenterY(), InputEventMouseButton.LEFT);
        } else if(controller.getButtonEvent(HandlerController.Buttons.LeftTrigger) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_2, '2');
        }
    }

    public static class IntelFactionTabReflector
    {
        FactionIntelPanel factionIntelPanel;
        Object getList;
        MethodHandle getItems;
        Object ensureVisible;
        public IntelFactionTabReflector(FactionIntelPanel factionIntelPanel) throws Throwable
        {
            this.factionIntelPanel = factionIntelPanel;

            getList = ClassReflector.GetInstance().getDeclaredMethod(FactionIntelPanel.class, "getList");

            Class<?> listCls = MethodReflector.GetInstance().getReturnType(getList);
            getItems = MethodHandles.lookup().findVirtual(listCls, "getItems", MethodType.methodType(List.class));
            ensureVisible = ClassReflector.GetInstance().findDeclaredMethod(listCls, "ensureVisible");
        }

        public List<UIComponentAPI> getFactionButtons() {
            List<UIComponentAPI> factionButtons = new ArrayList<>();
            try {
                var lst = MethodReflector.GetInstance().invoke(getList, factionIntelPanel);

                var items = (List<?>) getItems.invoke(lst);
                for(var item : items) {
                    if(UIComponentAPI.class.isAssignableFrom(item.getClass())) {
                        factionButtons.add((UIComponentAPI) item);
                    }
                }
            } catch(Throwable ex) {
                Global.getLogger(getClass()).error("Couldn't get faction buttons!", ex);
            }
            return factionButtons;
        }

        public void ensureVisible(UIComponentAPI factionButton) {
            try {
                var lst = MethodReflector.GetInstance().invoke(getList, factionIntelPanel);

                MethodReflector.GetInstance().invoke(ensureVisible, lst, factionButton);
            } catch(Throwable ex) {
                Global.getLogger(getClass()).error("Couldn't ensure faction is visible!", ex);
            }
        }
    }
}
