package ssms.controller.titlescreen;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.UIPanelAPI;
import ssms.controller.InputScreenBase;
import ssms.controller.InputScreenManager;
import ssms.controller.reflection.*;

public class MissionScreenUI extends InputScreenBase {
    public  static String ID = "MissionScreen";
    MissionWidgetReflector missionWidgetReflector;
    TitleScreenStateReflector titleScreenStateReflector;
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object ... args) {
        titleScreenStateReflector = new TitleScreenStateReflector();
        missionWidgetReflector = new MissionWidgetReflector(titleScreenStateReflector.getMissionWidget());
    }

    @Override
    public void preInput(float advance) {
        if(missionWidgetReflector.getFader().isFadedOut()) {
            InputScreenManager.getInstance().transitionToScreen(TitleScreenUI.ID);
        }
    }

    static class MissionWidgetReflector extends UIPanelReflector {
        public MissionWidgetReflector(UIPanelAPI missionWidget) {
            super(missionWidget);
        }
    }
}
