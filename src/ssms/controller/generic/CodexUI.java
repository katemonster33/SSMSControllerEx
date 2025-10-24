package ssms.controller.generic;

import com.fs.starfarer.codex2.CodexDialog;
import ssms.controller.InputScreenBase;
import ssms.controller.InputScreenManager;
import ssms.controller.inputhelper.DirectionalUINavigator;

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
    public void activate(Object... args) {
        activeCode = (CodexDialog) args[0];
        screenToReturnTo = (String)args[1];
    }

    @Override
    public void preInput(float advance) {
        var curCodex = tryGetCodexDialog();
        if(curCodex == null) {
            InputScreenManager.getInstance().transitionToScreen(screenToReturnTo);
        }
    }
}
