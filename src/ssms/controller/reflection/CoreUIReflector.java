package ssms.controller.reflection;

import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.CampaignState;

public class CoreUIReflector extends UIPanelReflector {
    protected static Class<?> coreUiType;
    static MethodReflector getLogistics;
    static MethodReflector getCourse;
    static MethodReflector getCurrentTab;
    static {
        ClassReflector campaignStateCls = new ClassReflector(CampaignState.class);

        coreUiType = campaignStateCls.getDeclaredField("core").getType();

        ClassReflector coreUiReflector = new ClassReflector(coreUiType);

        getLogistics = coreUiReflector.getDeclaredMethod("getLogistics");

        getCourse = coreUiReflector.getDeclaredMethod("getCourse");

        getCurrentTab = coreUiReflector.getDeclaredMethod("getCurrentTab");
    }
    CoreUIAPI coreUIAPI;
    public CoreUIReflector(CoreUIAPI coreUi) {
        super((UIPanelAPI) coreUi);
        coreUIAPI = coreUi;
    }

    public CoreUIAPI getCoreUIAPI() {
        return coreUIAPI;
    }

    public UIPanelAPI getLogistics() {
        return (UIPanelAPI) getLogistics.invoke(coreUIAPI);
    }

    public UIPanelAPI getCourse() {
        return (UIPanelAPI) getCourse.invoke(coreUIAPI);
    }

    public UIPanelAPI getCurrentTab() {
        return (UIPanelAPI) getCurrentTab.invoke(coreUIAPI);
    }
}
