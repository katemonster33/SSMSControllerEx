package ssms.controller.titlescreen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import kotlin.Pair;
import kotlin.Unit;
import lunalib.backend.ui.components.base.LunaUIBaseElement;
import lunalib.backend.ui.components.base.LunaUIButton;
import lunalib.backend.ui.components.base.LunaUIPlaceholder;
import lunalib.lunaUI.panel.LunaBaseCustomPanelPlugin;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import ssms.controller.HandlerController;
import ssms.controller.InputScreenBase;
import ssms.controller.InputScreenManager;
import ssms.controller.SSMSControllerModPluginEx;
import ssms.controller.enums.AxisMapping;
import ssms.controller.enums.ButtonMapping;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.reflection.ComboBoxReflector;
import ssms.controller.reflection.TitleScreenStateReflector;
import java.util.List;

import java.util.ArrayList;

public class ControllerSettingsUI extends InputScreenBase {
    public static String ID = "ControllerSettings";
    ControllerSettingsPanel controllerSettingsPanel = null;
    public static class ControllerSettingsPanel extends LunaBaseCustomPanelPlugin {
        ButtonMapping[] indicatorsLst;
        List<Pair<ButtonMapping, LogicalButtons>> btnList;
        AxisMapping[] axisMapLst;
        UIPanelAPI parentPanel;
        CustomPanelAPI subpanel;
        CustomPanelAPI customIndicatorsPanel;
        ComboBoxReflector cmbReflector;
        public ControllerSettingsPanel(UIPanelAPI parentPanel) {
            this.parentPanel = parentPanel;
            subpanel =  Global.getSettings().createCustom(500, 500, this);

            subpanel.getPosition().inMid();

            initFromScript(subpanel);
            parentPanel.addComponent(subpanel);
        }

        @Override
        public void init() {
            refreshList();
            var btnElem = subpanel.createUIElement(100, 35.f, false);

            btnElem.getPosition().inBL(0.f, 0.f);
            getPanel().addUIElement(btnElem);

            LunaUIButton btn = new LunaUIButton(false, false, 85.f, 30.f, "Rescan", "0", subpanel, btnElem);
            if(btn.getButtonText() != null) {
                btn.getButtonText().setText("Rescan");
                btn.getButtonText().getPosition().inTL(btn.getButtonText().getPosition().getWidth() / 2 - btn.getButtonText().computeTextWidth(btn.getButtonText().getText()) / 2, btn.getButtonText().getPosition().getHeight() - btn.getButtonText().computeTextHeight(btn.getButtonText().getText()) / 2);
            }
            btn.onClick((LunaUIBaseElement elem, InputEventAPI evt) -> {
                refreshList();
                return Unit.INSTANCE;
            });
        }

        public void refreshList() {
            if(cmbReflector != null) {
                getPanel().removeComponent(cmbReflector.getPanel());
            }
            if(customIndicatorsPanel != null) {
                getPanel().removeComponent(customIndicatorsPanel);
            }
            try {
                SSMSControllerModPluginEx.reconnectController();
            } catch(Exception ex) {
                Global.getLogger(getClass()).warn("Couldn't refresh controllers!", ex);
            }
            ArrayList<String> test = new ArrayList<>();

            for (int i = 0; i < Controllers.getControllerCount(); i++ ) {
                Controller con = Controllers.getController(i);
                test.add("Controller " + i + ": " + con.getName());
            }
            cmbReflector = new ComboBoxReflector(400.f, "Controller list... (" + Controllers.getControllerCount() + ")", test, "2");
            cmbReflector.getPanel().getPosition().inTR(4.f, 4.f);
            getPanel().addComponent(cmbReflector.getPanel());

            customIndicatorsPanel = getPanel().createCustomPanel(getPanel().getPosition().getWidth(), getPanel().getPosition().getHeight() - cmbReflector.getPanel().getPosition().getHeight() - 45, this);
            customIndicatorsPanel.getPosition().inTL(0.f, cmbReflector.getPanel().getPosition().getHeight() + 8.f);
            getPanel().addComponent(customIndicatorsPanel);
            indicatorsLst = ButtonMapping.values();
            btnList = new ArrayList<>();
            for(var btnMap : indicatorsLst) {
                var logicalBtn = LogicalButtons.fromMapping(btnMap);
                if(logicalBtn != null) {
                    btnList.add(new Pair<>(btnMap, logicalBtn));
                }
            }
            axisMapLst = AxisMapping.values();

            var indicatorImages = customIndicatorsPanel.createUIElement(customIndicatorsPanel.getPosition().getWidth(), customIndicatorsPanel.getPosition().getHeight(), true);
            indicatorImages.getPosition().inTL(0.f, 0.f);
            var cardPanel = new LunaUIPlaceholder(true, getPanel().getPosition().getWidth() , 25 * (indicatorsLst.length + axisMapLst.length), "empty", "none", getPanel(), indicatorImages);
            assert cardPanel.getLunaElement() != null;
            cardPanel.getPosition().inTL(0.f, 0.f);
            var elem1 = cardPanel.getLunaElement().createUIElement(getPanel().getPosition().getWidth() , 25 * (indicatorsLst.length + axisMapLst.length), false);
            var elem2 = cardPanel.getLunaElement().createUIElement(getPanel().getPosition().getWidth() , 25 * (indicatorsLst.length + axisMapLst.length), false);
            for(var btnMapPair : btnList) {
                elem1.addI
                elem1.addImage(SSMSControllerModPluginEx.defaultIndicators.get(Indicators.fromButton(btnMapPair.getSecond())), 25, 0.f);
                String mappingTxt = getMappingTxt(btnMapPair);
                elem2.addPara(mappingTxt, 10.f);
            }
            for(var axisPair : axisMapLst) {
                elem1.addImage(SSMSControllerModPluginEx.defaultIndicators.get(Indicators.fromButton(axisPair.getLowerLimitButton())), 25, 0.f);
                elem2.addPara(getMappingTxt(axisPair), 10.f);
            }
            elem1.getPosition().inTL(0.f, 0.f);
            elem2.getPosition().inTL(50.f, -5.f);
            cardPanel.getLunaElement().addUIElement(elem1);
            cardPanel.getLunaElement().addUIElement(elem2);
            customIndicatorsPanel.addUIElement(indicatorImages);
        }

        @NotNull
        private static String getMappingTxt(Pair<ButtonMapping, LogicalButtons> btnMapPair) {
            String mappingTxt = SSMSControllerModPluginEx.controller.controller != null ? "Controller " + SSMSControllerModPluginEx.controller.controller.getIndex() : "No Controller";
            var mapping = SSMSControllerModPluginEx.controller.mapping;
            boolean mapFound = false;
            for(var mappedBtn : mapping.getMappedButtons()) {
                if(mappedBtn.getButtonMapping() == btnMapPair.component1()) {
                    mapFound = true;
                    mappingTxt += ", button " + mappedBtn.getButtonIndex();
                    break;
                }
            }
            if(!mapFound && SSMSControllerModPluginEx.controller.controller != null) {
                mappingTxt += ", not mapped!!";
            }
            return mappingTxt;
        }

        @NotNull
        private static String getMappingTxt(AxisMapping axisMapping) {
            String mappingTxt = SSMSControllerModPluginEx.controller.controller != null ? "Controller " + SSMSControllerModPluginEx.controller.controller.getIndex() : "No Controller";
            var mapping = SSMSControllerModPluginEx.controller.mapping;
            boolean mapFound = false;
            for(var mappedBtn : mapping.getMappedAxes()) {
                if(mappedBtn.getAxisMapping() == axisMapping) {
                    mapFound = true;
                    mappingTxt += ", axis " + mappedBtn.getAxisIndex();
                    break;
                }
            }
            if(!mapFound && SSMSControllerModPluginEx.controller.controller != null) {
                mappingTxt += ", not mapped!!";
            }
            return mappingTxt;
        }

        public void cleanup() {
            parentPanel.removeComponent(getPanel());
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object ... args) {
        controllerSettingsPanel = new ControllerSettingsPanel(new TitleScreenStateReflector().getScreenPanel());
    }

    @Override
    public void processInputEvents(float advance, List<InputEventAPI> events) {
        for(InputEventAPI evt : events) {
            if(evt.isKeyDownEvent() && evt.getEventValue() == Keyboard.KEY_ESCAPE) {
                evt.consume();
                InputScreenManager.getInstance().transitionToScreen(TitleScreenUI.ID);
            }
        }
    }

    @Override
    public void deactivate() {
        controllerSettingsPanel.cleanup();
    }

}
