package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.ui.P;
import org.lwjgl.input.Keyboard;
import ssms.controller.InputScreenBase;
import ssms.controller.InputScreenManager;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.CodexUI;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.BorderedPanelReflector;
import ssms.controller.reflection.InteractionDialogReflector;
import ssms.controller.reflection.ScrollPanelReflector;
import ssms.controller.reflection.UIPanelReflector;

import java.util.ArrayList;
import java.util.List;

public class FleetMarketTabUI extends InputScreenBase {
    public static final String ID = "FleetMarketTab";
    FleetMarketTabReflector fleetMarketTabReflector;
    List<DirectionalUINavigator.NavigationObject> directionalObjects;
    DirectionalUINavigator directionalUINavigator;
    InteractionDialogReflector interactionDialogReflector;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object ... args) {
        if(args.length > 0) {
            fleetMarketTabReflector = (FleetMarketTabReflector) args[0];
        }
        directionalUINavigator = new DirectionalUINavigator(new ArrayList<>());
        List<ScrollPanelReflector> scrollers = new ArrayList<>();
        directionalObjects = new ArrayList<>();
        getPanelNavigatables(fleetMarketTabReflector, directionalObjects, scrollers);
        directionalUINavigator.setNavigationObjects(directionalObjects);
        for(var scroller : scrollers) {
            directionalUINavigator.addScrollPanel(scroller);
        }
        interactionDialogReflector = InteractionDialogReflector.getCurrentInstance();
        indicators = null;
    }

    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = new ArrayList<>();

            addDirectionalUINavigator(directionalUINavigator);
            addButtonPressHandler("Close Dialog", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
            addButtonPressHandler("Open Codex", LogicalButtons.Y, new KeySender(Keyboard.KEY_F2));
            addButtonPressHandler("More Info", LogicalButtons.X, new KeySender(Keyboard.KEY_F1));
            addButtonPressHandler("Select character tab", LogicalButtons.BumperLeft, new KeySender(Keyboard.KEY_C, 'c'));
            addButtonPressHandler("Select refit tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_R, 'r'));
        }
        return indicators;
    }

    public void preInput(float advance) {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.FLEET) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        }
        if(isCodexOpen()) {
            InputScreenManager.getInstance().transitionDelayed(CodexUI.ID, getId());
        }
        if(interactionDialogReflector != null && !interactionDialogReflector.isCoreUiOpen()) {
            InputScreenManager.getInstance().transitionDelayed(DialogUI.ID);
        }


        List<ScrollPanelReflector> scrollers = new ArrayList<>();
        List<DirectionalUINavigator.NavigationObject> directionalObjectsTmp = new ArrayList<>();
        getPanelNavigatables(fleetMarketTabReflector, directionalObjectsTmp, scrollers);
        if(directionalObjectsTmp.size() != directionalObjects.size()) {
            directionalObjects = directionalObjectsTmp;
            directionalUINavigator.clearScrollPanels();
            for(var scroller : scrollers) {
                directionalUINavigator.addScrollPanel(scroller);
            }
            directionalUINavigator.setNavigationObjects(directionalObjects);
        }

        if(directionalUINavigator != null) {
            directionalUINavigator.advance(advance);
        }
    }

    public static class FleetMarketTabReflector extends UIPanelReflector {
        static Class<?> fleetMarketParentCls = null;

        FleetMarketTabReflector(UIPanelAPI panel) {
            super(panel);
        }

        public static FleetMarketTabReflector TryGet(CoreUIAPI coreUIAPI, BorderedPanelReflector borderedPanelReflector) {
            var parentPanel = borderedPanelReflector.getInnerPanel();
            if (fleetMarketParentCls == null) {
                fleetMarketParentCls = parentPanel.getPanel().getClass();
                return new FleetMarketTabReflector(parentPanel.getPanel());
            } else if(fleetMarketParentCls.isAssignableFrom(parentPanel.getPanel().getClass())) {
                return new FleetMarketTabReflector(parentPanel.getPanel());
            }
            return null;
        }
    }
}