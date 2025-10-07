package ssms.controller.titlescreen;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import lunalib.lunaUI.panel.LunaBaseCustomPanelPlugin;
import ssms.controller.*;
import ssms.controller.enums.AxisMapping;
import ssms.controller.enums.ButtonMapping;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.reflection.TitleScreenStateReflector;

import java.awt.*;
import java.util.List;

public class AutoMapperUI extends InputScreenBase {
    public static final String ID = "AutoMapper";
    LogicalButtons[] buttons = LogicalButtons.values();
    int calibrationIndex = -1;

    ControllerMapping tempMapping = null;
    boolean parsedBtn = false;
    AutoMapperPanel autoMapperPanel;
    TitleScreenStateReflector titleScreenStateReflector;
    ControllerMapper controllerMapper;

    static class AutoMapperPanel extends LunaBaseCustomPanelPlugin {
        UIPanelAPI parentPanel;
        CustomPanelAPI subpanel;
        TooltipMakerAPI imgElem;
        public AutoMapperPanel(UIPanelAPI parentPanel) {
            this.parentPanel = parentPanel;
            subpanel = Global.getSettings().createCustom(500, 30, this);
            var txtElem = subpanel.createUIElement(450, 20, false);
            var lbl = txtElem.addPara("Press this button, or hold any button to skip: ", Color.white, 0.f);
            var pos = lbl.autoSizeToWidth(lbl.computeTextWidth(lbl.getText()));
            txtElem.getPosition().setSize(pos.getWidth(), txtElem.getPosition().getHeight());
            subpanel.getPosition().setSize(pos.getWidth() + 25.f + 8, subpanel.getPosition().getHeight());
            initFromScript(subpanel);
            txtElem.getPosition().inLMid(0.f);
            lbl.getPosition().inLMid(5.f);
            lbl.setAlignment(Alignment.LMID);
            subpanel.addUIElement(txtElem);
        }

        public void hide() {
            parentPanel.removeComponent(subpanel);
        }

        public void setImage(String spritePath) {
            if(imgElem != null) {
                subpanel.removeComponent(imgElem);
            }
            if(spritePath != null) {
                imgElem = subpanel.createUIElement(25, 25, false);
                imgElem.getPosition().inRMid(5.f);
                imgElem.addImage(spritePath, 25.f, 25.f, 0.f);
                subpanel.addUIElement(imgElem);
            }
        }

        @Override
        public void init() {
            subpanel.getPosition().inMid();
            this.parentPanel.addComponent(subpanel);
        }
    }

    @Override
    public void deactivate() {
        if(autoMapperPanel != null) {
            autoMapperPanel.hide();
            autoMapperPanel = null;
        }
    }

    @Override
    public void activate(Object... args) {
        titleScreenStateReflector = new TitleScreenStateReflector();
        tempMapping = new ControllerMapping();
        calibrationIndex = 0;
        parsedBtn = false;
        SSMSControllerModPluginEx.controller.poll();
        controllerMapper = new ControllerMapper(SSMSControllerModPluginEx.controller.controller);
        if(Global.getCurrentState() == GameState.TITLE) {
            var panel = titleScreenStateReflector.getScreenPanel();
            autoMapperPanel = new AutoMapperPanel(panel);

            var indicatorEnum = Indicators.fromButton(buttons[calibrationIndex]);
            if(indicatorEnum != null) {
                var spritePath = SSMSControllerModPluginEx.defaultIndicators.get(indicatorEnum);
                if (spritePath != null) {
                    autoMapperPanel.setImage(spritePath);
                }
            }
        }
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return List.of();
    }


    void moveNextButton() {
        controllerMapper.resetControllerState();
        timeSinceButtonPressed = 0.f;
        calibrationIndex++;
        if(calibrationIndex < buttons.length) {
            switch (buttons[calibrationIndex]) {
                case LeftStickRight, LeftStickDown, RightStickRight, RightStickDown, DpadRight, DpadDown -> calibrationIndex++;
                default -> {
                    break;
                }
            }
        }
        if(calibrationIndex >= buttons.length) {
            if(tempMapping != null) {
                SSMSControllerModPluginEx.controller = new HandlerController(SSMSControllerModPluginEx.controller.controller, tempMapping);
            }
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, TitleScreenUI.ID, new Object[]{});
        } else {
            var indicatorEnum = Indicators.fromButton(buttons[calibrationIndex]);
            if (indicatorEnum != null) {
                var spritePath = SSMSControllerModPluginEx.defaultIndicators.get(indicatorEnum);
                if (spritePath != null) {
                    autoMapperPanel.setImage(spritePath);
                } else {
                    autoMapperPanel.setImage(null);
                }
            } else {
                autoMapperPanel.setImage(null);
            }
        }
        parsedBtn = false;
    }

    float timeSinceButtonPressed = 0;
    @Override
    public void preInput(float advance) {
        if(calibrationIndex >= buttons.length) {
            Global.getLogger(getClass()).warn("we reached preInput with a calibration index greater than the max. how?!");
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, TitleScreenUI.ID, new Object[]{});
        }
        if(!controllerMapper.isControllerInRestingState()) {
            parsedBtn = true;
            timeSinceButtonPressed += advance;
            if(timeSinceButtonPressed >= 2) {
                moveNextButton();
            }
        } else if(parsedBtn) {
            LogicalButtons btn = buttons[calibrationIndex];
            controllerMapper.setMappingIndices(btn, tempMapping);
            moveNextButton();
        }
    }

    @Override
    public String getId() {
        return ID;
    }
}
