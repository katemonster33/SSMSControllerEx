package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;

import java.util.Objects;

public class InteractionDialogReflector {
    static InteractionDialogReflector instance;
    InteractionDialogAPI interactionDialogAPI;
    Object getPlugin;
    private InteractionDialogReflector() throws Throwable{
        interactionDialogAPI = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        Object getCargoPicker = null;
        var methods = ClassReflector.GetInstance().getDeclaredMethods(interactionDialogAPI.getClass());
        for(var meth : methods) {
            if(Objects.equals(MethodReflector.GetInstance().getName(meth), "getCargoPicker")) {
                getCargoPicker = meth;
                break;
            }
        }

        getPlugin = ClassReflector.GetInstance().findDeclaredMethod(interactionDialogAPI.getClass(), "getPlugin");
    }

    public InteractionDialogPlugin getPlugin() {
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
