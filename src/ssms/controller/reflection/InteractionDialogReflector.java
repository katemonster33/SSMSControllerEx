package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;

import java.util.Objects;

public class InteractionDialogReflector {
    static InteractionDialogReflector instance;
    static MethodReflector getCoreUI;
    static ClassReflector interactionDialogCls;
    public InteractionDialogReflector(InteractionDialogAPI interactionDialogAPI) {
        interactionDialogCls = new ClassReflector(interactionDialogAPI.getClass());
        getCoreUI = interactionDialogCls.getDeclaredMethod("getCoreUI");
    }

    public CoreUIAPI getCoreUI(InteractionDialogAPI interactionDialogAPI) {
        return (CoreUIAPI) getCoreUI.invoke(interactionDialogAPI);
    }
}
