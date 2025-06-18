package ssms.controller.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.inputhelper.MapInputHandler;
import ssms.controller.reflection.*;

import java.util.ArrayList;
import java.util.List;

public class WarroomScreen extends InputScreenBase {
    public static final String ID = "Warroom";
    CombatStateReflector csr;
    WarroomReflector warroomReflector;
    DirectionalUINavigator directionalUINavigator;
    MapInputHandler mapInputHandler;
    ViewportAPI viewportAPI;

    @Override
    public void activate(Object ... args) {
        csr = CombatStateReflector.GetInstance();
        warroomReflector = new WarroomReflector(csr.getWarroom());
        viewportAPI = Global.getCombatEngine().getViewport();

        indicators = new ArrayList<>();
        addButtonPressHandler("Pause", LogicalButtons.Start, (float advance) -> Global.getCombatEngine().setPaused(!Global.getCombatEngine().isPaused()));
        addButtonPressHandler("Show video feed", LogicalButtons.Select, (float advance) -> {
            csr.HideWarroom();
            csr.SetVideoFeedToPlayerShip();
        });
        mapInputHandler = addMapHandler(csr.getWarroom());
        addButtonPressHandler("Target point/object", LogicalButtons.X, (float advance) -> {
            var desiredMousePos = mapInputHandler.getDesiredMousePos();
            if(desiredMousePos != null) {
                InputShim.mouseDownUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
            }
        });
        addButtonPressHandler("Return to deployment screen", LogicalButtons.Y, new KeySender(Keyboard.KEY_G, 'G'));
        var buttons = warroomReflector.getButtons();
        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for(var btn : buttons) {
            directionalObjects.add(new Pair<>(btn, null));
        }
        directionalUINavigator = new DirectionalUINavigator(directionalObjects);
        addDigitalJoystickHandler("Select buttons", Joystick.DPad, directionalUINavigator);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String[] getScopes() {
        return new String[]{ BattleScope.ID };
    }

    @Override
    public void preInput(float advance) {
        if(!Global.getCombatEngine().getCombatUI().isShowingCommandUI()) {
            InputScreenManager.getInstance().transitionDelayed(BattleSteeringScreen.ID);
        } else if(Global.getCombatEngine().getCombatUI().isShowingDeploymentDialog()) {
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, BattleDeploymentScreen.ID, new Object[]{ csr });
        }
        var buttons = warroomReflector.getButtons();
        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for(var btn : buttons) {
            directionalObjects.add(new Pair<>(btn, null));
        }
        directionalUINavigator.setNavigationObjects(directionalObjects);
        mapInputHandler.advance(advance);
    }
}
