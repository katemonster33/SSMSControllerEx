package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.CampaignState;
import com.fs.state.AppDriver;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class CampaignStateReflector {
    Object getWidgetPanel = null;
    Object getScreenHandle = null;
    Object cs;
    MethodHandle getZoomFactor;
    static CampaignStateReflector instance;
    private CampaignStateReflector() {
        cs = AppDriver.getInstance().getState(CampaignState.STATE_ID);

        try {
            getScreenHandle = ClassReflector.GetInstance().findDeclaredMethod(CampaignState.class, "getScreenPanel");

            getZoomFactor = MethodHandles.lookup().findVirtual(CampaignState.class, "getZoomFactor", MethodType.methodType(float.class));
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Couldn't reflect into CampaignState!");
        }
    }

    public static CampaignStateReflector GetInstance() {
        if(instance == null) {
            instance = new CampaignStateReflector();
        }
        return instance;
    }

    public float getZoomFactor() {
        try {
            return (float)getZoomFactor.invoke(cs);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't call CampaignState.getZoomFactor!", ex);
            return 1.f;
        }
    }

    public UIPanelAPI getScreenPanel() {
        try {
            return (UIPanelAPI) MethodReflector.GetInstance().invoke(getScreenHandle, cs);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't call CampaignState.getScreenPanel!", ex);
            return null;
        }
    }
}
