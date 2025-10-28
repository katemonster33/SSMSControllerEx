package ssms.controller.generic;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.codex2.CodexDialog;
import org.lwjgl.input.Keyboard;
import ssms.controller.InputScopeBase;
import ssms.controller.InputScreenBase;
import ssms.controller.InputScreenManager;
import ssms.controller.InputShim;
import ssms.controller.combat.BattleScope;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.*;

import java.util.ArrayList;
import java.util.List;

public class CodexUI extends InputScreenBase {
    public static final String ID = "Codex";
    CodexDialog activeCode = null;
    UIPanelReflector codexInnerPanel = null;
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
            List<ScrollPanelReflector> scrollers = new ArrayList<>();
            getPanelNavigatables(codexInnerPanel, tabNavItems, scrollers);
            directionalUINavigator.clearScrollPanels();
            for(var scroller : scrollers) {
                directionalUINavigator.addScrollPanel(scroller);
            }
            directionalUINavigator.setNavigationObjects(tabNavItems);
            addDirectionalUINavigator(directionalUINavigator);
            addButtonPressHandler("Close", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        }
        return indicators;
    }

    @Override
    public void activate(Object... args) {
        activeCode = tryGetCodexDialog();
        var getInnerPanel = new ClassReflector(activeCode.getClass()).findDeclaredMethod("getInnerPanel");
        codexInnerPanel = new UIPanelReflector((UIPanelAPI) getInnerPanel.invoke(activeCode));
        screenToReturnTo = (String)args[0];
        directionalUINavigator = new DirectionalUINavigator(new ArrayList<>()) {
            @Override
            public void onSelect(NavigationObject navigationObject) {
                super.onSelect(navigationObject);
                if(navigationObject.tag instanceof ScrollPanelReflector scrollPanelReflector) {
                    scrollPanelReflector.ensureVisible(navigationObject.component);
                }
            }
        };
        indicators = null;
    }

    @Override
    public UIPanelAPI getPanelForIndicators() {
        if(activeCode != null) {
            return new UIPanelReflector((UIPanelAPI) activeCode).getParent();
        } else {
            return super.getPanelForIndicators();
        }
    }

    @Override
    public void preInput(float advance) {
        if(!isCodexOpen()) {
            if(Global.getCurrentState() == GameState.COMBAT && InputScreenManager.getInstance().getScreen(screenToReturnTo).getScopes()[0] == BattleScope.ID) {
                InputScreenManager.getInstance().transitionToScope(BattleScope.ID, Global.getCombatEngine());
            } else {
                InputScreenManager.getInstance().transitionToScreen(screenToReturnTo);
            }
        }
        List<DirectionalUINavigator.NavigationObject> tmpItems = new ArrayList<>();
        getPanelNavigatables(codexInnerPanel, tmpItems, new ArrayList<>());
        if(tmpItems.size() != tabNavItems.size()) {
            tabNavItems = tmpItems;
            directionalUINavigator.setNavigationObjects(tabNavItems);
        }
        if(directionalUINavigator != null) {
            directionalUINavigator.advance(advance);
        }
    }
}
