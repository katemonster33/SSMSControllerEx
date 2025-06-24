package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.util.Objects;

public class InteractionDialogReflector extends UIPanelReflector {
    static InteractionDialogReflector instance;
    static MethodReflector getCoreUI;
    static ClassReflector interactionDialogCls;
    public InteractionDialogReflector(InteractionDialogAPI interactionDialogAPI) {
        super((UIPanelAPI) interactionDialogAPI);
        interactionDialogCls = new ClassReflector(interactionDialogAPI.getClass());
        getCoreUI = interactionDialogCls.getDeclaredMethod("getCoreUI");
    }

    public CoreUIAPI getCoreUI(InteractionDialogAPI interactionDialogAPI) {
        return (CoreUIAPI) getCoreUI.invoke(interactionDialogAPI);
    }
}
