package ssms.controller.titlescreen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.ui.P;
import kotlin.Unit;
import lunalib.backend.ui.components.base.LunaUIBaseElement;
import lunalib.backend.ui.components.base.LunaUIButton;
import lunalib.lunaUI.panel.LunaBaseCustomPanelPlugin;
import org.apache.log4j.Level;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import ssms.controller.HandlerController;
import ssms.controller.InputScreenBase;
import ssms.controller.InputScreenManager;
import ssms.controller.SSMSControllerModPluginEx;
import ssms.controller.reflection.ComboBoxReflector;
import ssms.controller.reflection.TitleScreenStateReflector;
import java.util.List;

import java.util.ArrayList;

public class ControllerSettingsUI extends InputScreenBase {
    public static String ID = "ControllerSettings";
    ControllerSettingsPanel controllerSettingsPanel = null;
    public static class ControllerSettingsPanel extends LunaBaseCustomPanelPlugin {

        UIPanelAPI parentPanel;
        CustomPanelAPI subpanel;
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
            var btnElem = subpanel.createUIElement(100, 35.f, true);
            btnElem.getPosition().inBL(0.f, 0.f);
            subpanel.addUIElement(btnElem);
            LunaUIButton btn = new LunaUIButton(false, false, 85.f, 30.f, "Rescan", "0", subpanel, btnElem);
            btn.getButtonText().setText("Rescan");
            btn.getButtonText().getPosition().inTL(btn.getButtonText().getPosition().getWidth() / 2 - btn.getButtonText().computeTextWidth(btn.getButtonText().getText()) / 2,btn.getButtonText().getPosition().getHeight() - btn.getButtonText().computeTextHeight(btn.getButtonText().getText()) / 2);
            btn.onClick((LunaUIBaseElement elem, InputEventAPI evt) -> {
                refreshList();
                return Unit.INSTANCE;
            });
        }

        public void refreshList() {
            if(cmbReflector != null) {
                getPanel().removeComponent(cmbReflector.getPanel());
            }
            try {
                SSMSControllerModPluginEx.reconnectController();
            } catch(Exception ex) {
                Global.getLogger(getClass()).warn("Couldn't refresh controllers!", ex);
            }
            ArrayList<String> test = new ArrayList<>();

            for (int i = 0; i < Controllers.getControllerCount(); i++ ) {
                Controller con = Controllers.getController(i);
                test.add(con.getName());
            }
            cmbReflector = new ComboBoxReflector(400.f, "2", test, "2");
            getPanel().addComponent(cmbReflector.getPanel());
            cmbReflector.getPanel().getPosition().inTR(4.f, 4.f);
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
