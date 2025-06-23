package ssms.controller.titlescreen;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.UIPanelAPI;
import ssms.controller.InputScreenBase;
import ssms.controller.reflection.ClassReflector;
import ssms.controller.reflection.FieldReflector;
import ssms.controller.reflection.MethodReflector;
import ssms.controller.reflection.TitleScreenStateReflector;

public class MissionScreenUI extends InputScreenBase {
    public  static String ID = "MissionScreen";
    MissionWidgetReflector missionWidgetReflector;
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object ... args) {
        missionWidgetReflector = new MissionWidgetReflector(TitleScreenStateReflector.GetInstance().getMissionWidget());
    }



    static class MissionWidgetReflector {
        UIPanelAPI missionWidget;

        public MissionWidgetReflector(UIPanelAPI missionWidget) {
            this.missionWidget = missionWidget;
        }


        public Fader getMissionWidgetFader() {
            try {
                var getFaderMethod = ClassReflector.GetInstance().findDeclaredMethod(missionWidget.getClass(), "getFader");

                return (Fader) MethodReflector.GetInstance().invoke(getFaderMethod, missionWidget);
            } catch(Throwable ex) {
                Global.getLogger(getClass()).error("Couldn't get mission widget fader!", ex);
            }
            return null;
        }
    }
}
