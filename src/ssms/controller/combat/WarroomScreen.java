package ssms.controller.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Pair;
import ssms.controller.*;
import ssms.controller.reflection.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class WarroomScreen extends InputScreenBase {
    public static final String ID = "Warroom";
    CombatStateReflector csr;
    HandlerController controller;
    BattleScope scope;
    WarroomReflector warroomReflector;
    List<Pair<Indicators, String>> indicators;

    public WarroomScreen() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.Start, "Pause"));
        indicators.add(new Pair<>(Indicators.Select, "Show video feed"));
    }
    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }

    @Override
    public void activate(Object ... args) {
        csr = CombatStateReflector.GetInstance();
        warroomReflector = new WarroomReflector(csr.getWarroom());
        controller = SSMSControllerModPluginEx.controller;
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
            return;
        }

        if(controller.getButtonEvent(HandlerController.Buttons.Start) == 1) {
            Global.getCombatEngine().setPaused(!Global.getCombatEngine().isPaused());
        } else if(controller.getButtonEvent(HandlerController.Buttons.Select) == 1) {
            csr.HideWarroom();
            csr.SetVideoFeedToPlayerShip();
            //Global.getCombatEngine().sho
        }

    }
}
