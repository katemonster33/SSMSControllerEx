package ssms.controller.reflection;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.ui.UIPanelAPI;

public class CoursePanelReflector extends UIPanelReflector{
    static Class<?> coursePnlType;

    static MethodReflector getInner;

    static {
        ClassReflector coreUiReflector = new ClassReflector(CoreUIReflector.coreUiType);
        coursePnlType = coreUiReflector.getDeclaredMethod("getCourse").getReturnType();

        ClassReflector courseReflector = new ClassReflector(coursePnlType);

        getInner = courseReflector.getDeclaredMethod("getInner");
    }

    public CoursePanelReflector(UIPanelAPI uiPanelAPI) {
        super(uiPanelAPI);
    }

    public Fader getInner() {
        return (Fader) getInner.invoke(panel);
    }
}
