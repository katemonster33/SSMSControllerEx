package ssms.controller.reflection;

import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.CampaignState;
import com.fs.starfarer.campaign.fleet.CampaignFleet;

public class CoreUIReflector extends UIPanelReflector {
    protected static Class<?> coreUiType;
    static MethodReflector getLogistics;
    static MethodReflector getCourse;
    static MethodReflector getCurrentTab;
    static FieldReflector abilityBarField;
    static {
        ClassReflector campaignStateCls = new ClassReflector(CampaignState.class);

        coreUiType = campaignStateCls.getDeclaredField("core").getType();

        ClassReflector coreUiReflector = new ClassReflector(coreUiType);

        getLogistics = coreUiReflector.getDeclaredMethod("getLogistics");

        getCourse = coreUiReflector.getDeclaredMethod("getCourse");

        getCurrentTab = coreUiReflector.getDeclaredMethod("getCurrentTab");

        int numMatches = 0;
        for(var field : coreUiReflector.getDeclaredFields()) {
            if(UIPanelAPI.class.isAssignableFrom(field.getType()) && new ClassReflector(field.getType()).getDeclaredConstructor(CampaignFleet.class) != null && (++numMatches) == 2) {
                abilityBarField = field;
                break;
            }
        }
    }
    CoreUIAPI coreUIAPI;
    public CoreUIReflector(CoreUIAPI coreUi) {
        super((UIPanelAPI) coreUi);
        coreUIAPI = coreUi;
    }

    public UIPanelAPI getAbilityBar() {
        return (UIPanelAPI) abilityBarField.get(coreUIAPI);
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
