package ssms.controller.titlescreen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import kotlin.Pair;
import kotlin.Unit;
import lunalib.backend.ui.components.base.LunaUIBaseElement;
import lunalib.backend.ui.components.base.LunaUIButton;
import lunalib.backend.ui.components.base.LunaUIPlaceholder;
import lunalib.lunaUI.panel.LunaBaseCustomPanelPlugin;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
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
    ControllerMapper controllerMapper;
    LogicalButtons buttonToMap = null;
    boolean parsedBtn = false;
    public class ControllerSettingsPanel extends LunaBaseCustomPanelPlugin {
        LogicalButtons[] buttons = new LogicalButtons[]{
                LogicalButtons.A, LogicalButtons.B, LogicalButtons.X, LogicalButtons.Y, LogicalButtons.BumperLeft, LogicalButtons.BumperRight,
                LogicalButtons.Start, LogicalButtons.Select, LogicalButtons.LeftStickButton, LogicalButtons.RightStickButton,
                LogicalButtons.RightStickUp, LogicalButtons.RightStickLeft, LogicalButtons.LeftStickUp, LogicalButtons.LeftStickLeft,
                LogicalButtons.LeftTrigger, LogicalButtons.RightTrigger,
                LogicalButtons.DpadLeft, LogicalButtons.DpadRight, LogicalButtons.DpadUp, LogicalButtons.DpadDown
        };
        ArrayList<String> controllerNames = new ArrayList<>();

        UIPanelAPI parentPanel;
        CustomPanelAPI subpanel;
        CustomPanelAPI customIndicatorsPanel;
        ComboBoxReflector cmbReflector;
        public ControllerSettingsPanel(UIPanelAPI parentPanel) {
            this.parentPanel = parentPanel;
            subpanel = Global.getSettings().createCustom(700, 500, this);

            subpanel.getPosition().inMid();

            initFromScript(subpanel);
            parentPanel.addComponent(subpanel);
        }

        @Override
        public void init() {

            var btnElem2 = subpanel.createUIElement(200, 35.f, false);
            btnElem2.getPosition().inTL(0.f, 0.f);
            getPanel().addUIElement(btnElem2);
            LunaUIButton btn2 = new LunaUIButton(false, false, 185.f, 23.f, "Rescan", "0", subpanel, btnElem2);
            if(btn2.getButtonText() != null) {
                btn2.getButtonText().setText("Select controller");
                btn2.getButtonText().getPosition().inTL(btn2.getButtonText().getPosition().getWidth() / 2 - btn2.getButtonText().computeTextWidth(btn2.getButtonText().getText()) / 2, btn2.getButtonText().getPosition().getHeight() - btn2.getButtonText().computeTextHeight(btn2.getButtonText().getText()) / 2 - 2);
            }
            btn2.getPosition().inTL(3.f, 3.f);
            btn2.onClick((LunaUIBaseElement elem, InputEventAPI evt) -> {
                try {
                    int indexOfController = controllerNames.indexOf(cmbReflector.getSelected());
                    SSMSControllerModPluginEx.reconnectController(Controllers.getController(indexOfController));
                } catch(Exception ex) {
                    Global.getLogger(getClass()).warn("Couldn't refresh controllers!", ex);
                } finally {
                    refreshList();
                }
                return Unit.INSTANCE;
            });
            var btnElem = subpanel.createUIElement(100, 35.f, false);
            btnElem.getPosition().inBL(0.f, 0.f);
            getPanel().addUIElement(btnElem);

            LunaUIButton btn = new LunaUIButton(false, false, 85.f, 30.f, "Rescan", "0", subpanel, btnElem);
            if(btn.getButtonText() != null) {
                btn.getButtonText().setText("Rescan");
                btn.getButtonText().getPosition().inTL(btn.getButtonText().getPosition().getWidth() / 2 - btn.getButtonText().computeTextWidth(btn.getButtonText().getText()) / 2, btn.getButtonText().getPosition().getHeight() - btn.getButtonText().computeTextHeight(btn.getButtonText().getText()) / 2);
            }
            btn.onClick((LunaUIBaseElement elem, InputEventAPI evt) -> {
                try {
                    SSMSControllerModPluginEx.reconnectController();
                } catch(Exception ex) {
                    Global.getLogger(getClass()).warn("Couldn't refresh controllers!", ex);
                }
                refreshList();
                return Unit.INSTANCE;
            });

            refreshList();
        }

        public void refreshList() {
            if(cmbReflector != null) {
                getPanel().removeComponent(cmbReflector.getPanel());
            }
            if(customIndicatorsPanel != null) {
                getPanel().removeComponent(customIndicatorsPanel);
            }

            controllerNames.clear();
            int controllerActiveIndex = -1;
            for (int i = 0; i < Controllers.getControllerCount(); i++ ) {
                Controller con = Controllers.getController(i);
                controllerNames.add(i + ": " + con.getName() + " [" + con.getButtonCount() + " buttons, " + con.getAxisCount() + " axes]");
                if(SSMSControllerModPluginEx.controller.controller == con) {
                    controllerActiveIndex = i;
                }
            }
            String cmbText = "No controllers detected";
            if(Controllers.getControllerCount() > 0) {
                cmbText = controllerActiveIndex != -1 ? controllerNames.get(controllerActiveIndex) : "No controller mapped...";
                if(controllerActiveIndex != -1) {
                    controllerMapper = new ControllerMapper(Controllers.getController(controllerActiveIndex));
                }
            }
            cmbReflector = new ComboBoxReflector(500.f, cmbText, controllerNames, "2");
            cmbReflector.getPanel().getPosition().inTR(4.f, 4.f);
            getPanel().addComponent(cmbReflector.getPanel());


            customIndicatorsPanel = getPanel().createCustomPanel(getPanel().getPosition().getWidth(), getPanel().getPosition().getHeight() - cmbReflector.getPanel().getPosition().getHeight() - 45, this);
            customIndicatorsPanel.getPosition().inTL(0.f, cmbReflector.getPanel().getPosition().getHeight() + 8.f);
            getPanel().addComponent(customIndicatorsPanel);

            var indicatorImages = customIndicatorsPanel.createUIElement(customIndicatorsPanel.getPosition().getWidth(), customIndicatorsPanel.getPosition().getHeight(), true);
            indicatorImages.getPosition().inTL(0.f, 0.f);
            var cardPanel = new LunaUIPlaceholder(true, getPanel().getPosition().getWidth() , 25 * buttons.length, "empty", "none", getPanel(), indicatorImages);
            assert cardPanel.getLunaElement() != null;
            cardPanel.getPosition().inTL(0.f, 0.f);
            var elem1 = cardPanel.getLunaElement().createUIElement(50, 25 * buttons.length, false);
            var elem2 = cardPanel.getLunaElement().createUIElement(300, 25 * buttons.length, false);
            var elemBtns = cardPanel.getLunaElement().createUIElement(100, 25 * buttons.length, false);
            for(var logicalBtn : buttons) {
                elem1.addImage(SSMSControllerModPluginEx.defaultIndicators.get(Indicators.fromButton(logicalBtn)), 25, 0.f);
                String mappingTxt = getMappingTxt(logicalBtn);
                elem2.addPara(mappingTxt, 10.f);

                LunaUIButton btn = new LunaUIButton(false, false, 95.f, 25.f, logicalBtn, "", subpanel, elemBtns);

                if(btn.getButtonText() != null) {
                    btn.getButtonText().setText("Remap");
                    btn.getButtonText().getPosition().inTL(btn.getButtonText().getPosition().getWidth() / 2 - btn.getButtonText().computeTextWidth(btn.getButtonText().getText()) / 2, btn.getButtonText().getPosition().getHeight() - btn.getButtonText().computeTextHeight(btn.getButtonText().getText()) / 2);
                }
                btn.onClick((LunaUIBaseElement elem, InputEventAPI evt) -> {
                    buttonToMap = (LogicalButtons) elem.getKey();
                    refreshList();
                    return Unit.INSTANCE;
                });
            }

            elem1.getPosition().inTL(0.f, 0.f);
            elem2.getPosition().inTL(50.f, -5.f);
            elemBtns.getPosition().inTR(0.0f, 0.0f);
            cardPanel.getLunaElement().addUIElement(elem1);
            cardPanel.getLunaElement().addUIElement(elem2);
            cardPanel.getLunaElement().addUIElement(elemBtns);
            customIndicatorsPanel.addUIElement(indicatorImages);
        }

        @NotNull
        private String getMappingTxt(LogicalButtons button) {
            if(buttonToMap == button) {
                return "Press any button...";
            }
            String mappingTxt = SSMSControllerModPluginEx.controller.controller != null ? "Controller " + SSMSControllerModPluginEx.controller.controller.getIndex() : "No Controller";
            var mapping = SSMSControllerModPluginEx.controller.mapping;
            boolean mapFound = false;
            if(mapping != null) {
                var btnMap = ButtonMapping.fromButton(button);
                var axisMap = AxisMapping.fromButton(button);
                if(btnMap != null) {
                    for (var mappedBtn : mapping.getMappedButtons()) {
                        if (mappedBtn.getButtonMapping() == ButtonMapping.fromButton(button)) {
                            mapFound = true;
                            mappingTxt += ", button " + mappedBtn.getButtonIndex();
                            break;
                        }
                    }
                }
                if(axisMap != null && !mapFound) {
                    for (var mappedBtn : mapping.getMappedAxes()) {
                        if (mappedBtn.getAxisMapping() == axisMap) {
                            mapFound = true;
                            mappingTxt += ", axis " + mappedBtn.getAxisIndex();
                            break;
                        }
                    }
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
    public void preInput(float advance) {

        if(controllerMapper != null && buttonToMap != null) {
            if (!controllerMapper.isControllerInRestingState()) {
                parsedBtn = true;
            } else if (parsedBtn) {
                ControllerMapping mapping = SSMSControllerModPluginEx.controller.mapping == null ? new ControllerMapping() : SSMSControllerModPluginEx.controller.mapping;
                controllerMapper.setMappingIndices(buttonToMap, mapping);
                SSMSControllerModPluginEx.controller = new HandlerController(SSMSControllerModPluginEx.controller.controller, mapping);
                parsedBtn = false;
                buttonToMap = null;
                controllerSettingsPanel.refreshList();
            }
        }
    }

    @Override
    public void deactivate() {
        controllerSettingsPanel.cleanup();
    }

}
