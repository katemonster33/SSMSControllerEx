package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;

import java.util.Objects;

public class InteractionDialogReflector {
    static InteractionDialogReflector instance;
    static Object getCoreUI;
    private InteractionDialogReflector() throws Throwable{

        var interactionDialogAPI = Global.getSector().getCampaignUI().getCurrentInteractionDialog();

        getCoreUI = ClassReflector.GetInstance().findDeclaredMethod(interactionDialogAPI.getClass(), "getCoreUI");
    }

    public CoreUIAPI getCoreUI(InteractionDialogAPI interactionDialogAPI) {
        try {
            return (CoreUIAPI) MethodReflector.GetInstance().invoke(getCoreUI, interactionDialogAPI);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get CoreUI from InteractionDialogAPI!", ex);
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
