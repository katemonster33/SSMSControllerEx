package ssms.controller.generic;

import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.codex2.CodexDialog;
import org.lwjgl.input.Keyboard;
import ssms.controller.InputScreenBase;
import ssms.controller.InputScreenManager;
import ssms.controller.InputShim;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;

import java.util.ArrayList;
import java.util.List;

public class CodexUI extends InputScreenBase {
    public static final String ID = "Codex";
    CodexDialog activeCode = null;
    String screenToReturnTo = InputScreenBase.ID;
    List<DirectionalUINavigator.NavigationObject> tabNavItems = new ArrayList<>();
    DirectionalUINavigator directionalUINavigator;
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = new ArrayList<>();
            addDirectionalUINavigator(directionalUINavigator);
            addButtonPressHandler("Select", LogicalButtons.A, (float advance) -> {
                if(directionalUINavigator.getSelected() != null) {
                    var sel = directionalUINavigator.getSelected();
                    InputShim.mouseDownUp((int) sel.getCenterX(), (int) sel.getCenterY(), InputEventMouseButton.LEFT);
                }
            });
            addButtonPressHandler("Close", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        }
        return indicators;
    }

    @Override
    public void activate(Object... args) {
        activeCode = (CodexDialog) args[0];
        screenToReturnTo = (String)args[1];
        directionalUINavigator = new DirectionalUINavigator(new ArrayList<>());
        indicators = null;
    }

    @Override
    public void preInput(float advance) {
        if(!isCodexOpen()) {
            InputScreenManager.getInstance().transitionToScreen(screenToReturnTo);
        }
        if(directionalUINavigator != null) {
            directionalUINavigator.advance(advance);
        }
    }
}
