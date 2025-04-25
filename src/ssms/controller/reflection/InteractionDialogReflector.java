package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;

import java.util.Objects;

public class InteractionDialogReflector {
    static InteractionDialogReflector instance;
    Object getCoreUI;
    Object getPlugin;
    private InteractionDialogReflector() throws Throwable{

        var interactionDialogAPI = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        Object getCargoPicker = null;
        var methods = ClassReflector.GetInstance().getDeclaredMethods(interactionDialogAPI.getClass());
        for(var meth : methods) {
            if(Objects.equals(MethodReflector.GetInstance().getName(meth), "getCargoPicker")) {
                getCargoPicker = meth;
                break;
            }
        }

        getCoreUI = ClassReflector.GetInstance().findDeclaredMethod(interactionDialogAPI.getClass(), "getCoreUI");
        getPlugin = ClassReflector.GetInstance().findDeclaredMethod(interactionDialogAPI.getClass(), "getPlugin");
    }

    public CoreUIAPI getCoreUI(InteractionDialogAPI interactionDialogAPI) {
        try {
            return (CoreUIAPI) MethodReflector.GetInstance().invoke(getCoreUI, interactionDialogAPI);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get CoreUI from InteractionDialogAPI!", ex);
            return null;
        }
    }

    public InteractionDialogPlugin getPlugin(InteractionDialogAPI interactionDialogAPI) {
        try {
            return (InteractionDialogPlugin) MethodReflector.GetInstance().invoke(getPlugin, interactionDialogAPI);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't fetch active plugin of interaction dialog!", ex);
            return null;
        }
    }

    public static InteractionDialogReflector GetInstance() {
        try {
            if (instance == null) {
                instance = new InteractionDialogReflector();
            }
        } catch(Throwable ex) {
            Global.getLogger(InteractionDialogReflector.class).fatal("Couldn't reflect into InteractionDialog!", ex);
        }
        return instance;
    }
}
