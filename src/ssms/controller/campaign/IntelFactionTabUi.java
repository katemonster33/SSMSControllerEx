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
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
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
    IntelTabReflector intelTabReflector;
    IntelFactionTabReflector intelFactionTabReflector;
    IntelTabData intelTabData;
    DirectionalUINavigator directionalUINavigator;
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object ... args) throws Throwable {
        intelTabReflector = (IntelTabReflector) args[0];
        intelFactionTabReflector = new IntelFactionTabReflector(intelTabReflector.getFactionIntelPanel());
        intelTabData = CampaignEngine.getInstance().getUIData().getIntelData();
        indicators = new ArrayList<>();
        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for(var btn : intelFactionTabReflector.getFactionButtons()) {
            directionalObjects.add(new Pair<>(btn, null));
        }
        directionalUINavigator = new DirectionalUINavigator(directionalObjects);
        addDigitalJoystickHandler("Navigate", Joystick.DPad, directionalUINavigator);

        addButtonPressHandler("Select planet tab", LogicalButtons.LeftTrigger, new KeySender(Keyboard.KEY_2, '2'));
        addButtonPressHandler("Select map tab", LogicalButtons.BumperLeft, new KeySender(Keyboard.KEY_TAB));
        addButtonPressHandler("Select command tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_D, 'd'));

        addButtonPressHandler("Select", LogicalButtons.A, (float advance) -> {
            if(directionalUINavigator.getSelected() != null) {
                var pos = directionalUINavigator.getSelected().one.getPosition();
                InputShim.mouseDownUp((int) pos.getCenterX(), (int) pos.getCenterY(), InputEventMouseButton.LEFT);
            }
        });
        addButtonPressHandler("Close", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
    }

    @Override
    public void preInput(float amount) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.INTEL) InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        else if(intelTabData.getSelectedTabIndex() == 0) InputScreenManager.getInstance().transitionDelayed(IntelTabUI.ID, intelTabReflector);
        else if(intelTabData.getSelectedTabIndex() == 1) InputScreenManager.getInstance().transitionDelayed(IntelPlanetTabUi.ID, intelTabReflector);
    }

    public static class IntelFactionTabReflector
    {
        FactionIntelPanel factionIntelPanel;
        MethodReflector getList;
        MethodHandle getItems;
        MethodReflector ensureVisible;
        public IntelFactionTabReflector(FactionIntelPanel factionIntelPanel) throws Throwable
        {
            this.factionIntelPanel = factionIntelPanel;

            getList = new ClassReflector(FactionIntelPanel.class).getDeclaredMethod("getList");

            Class<?> listCls = getList.getReturnType();
            getItems = MethodHandles.lookup().findVirtual(listCls, "getItems", MethodType.methodType(List.class));
            ensureVisible = new ClassReflector(listCls).findDeclaredMethod("ensureVisible");
        }

        public List<UIComponentAPI> getFactionButtons() {
            List<UIComponentAPI> factionButtons = new ArrayList<>();
            var lst = getList.invoke(factionIntelPanel);
            try {
                var items = (List<?>) getItems.invoke(lst);
                for (var item : items) {
                    if (item instanceof UIComponentAPI uiComponentAPI) {
                        factionButtons.add(uiComponentAPI);
                    }
                }
            } catch(Throwable ex) {
                Global.getLogger(getClass()).error("Couldn't invoke getItems method!", ex);
            }
            return factionButtons;
        }

        public void ensureVisible(UIComponentAPI factionButton) {
            var lst = getList.invoke(factionIntelPanel);

            ensureVisible.invoke(lst, factionButton);
        }
    }
}
