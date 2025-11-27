package ssms.controller.titlescreen;

import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import ssms.controller.InputScreenBase;
import ssms.controller.InputScreenManager;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.inputhelper.DirectionalUINavigator.NavigationObject;
import ssms.controller.reflection.*;

import java.util.ArrayList;
import java.util.List;

public class NewGameUI extends InputScreenBase {
    public static String ID = "NewGame";
    TitleScreenStateReflector titleScreenStateReflector;
    DirectionalUINavigator directionalUINavigator;
    UIPanelReflector newGameUiPanel;
    List<ButtonAPI> uiButtons;
    MethodReflector ensurePortraitVisible;
    MessageBoxReflector portraitPicker;
    UIPanelAPI portraitScroller;
    boolean portraitPickerOpen = false;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object... args) {
        titleScreenStateReflector = new TitleScreenStateReflector();
        var titleScreenPanels = new UIPanelReflector(titleScreenStateReflector.getScreenPanel()).getChildPanels();
        newGameUiPanel = new UIPanelReflector(titleScreenPanels.get(titleScreenPanels.size() - 1));
        uiButtons = newGameUiPanel.getChildButtons(true);
        directionalUINavigator = new DirectionalUINavigator(new ArrayList<>()) {
            @Override
            public void onSelect(NavigationObject selectedObj) {
                super.onSelect(selectedObj);
                if (portraitPickerOpen && selectedObj.tag instanceof ScrollPanelReflector) {
                    ensurePortraitVisible.invoke(portraitScroller, selectedObj.component);
                }
            }
        };
        refreshIndicators();
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = new ArrayList<>();

            List<NavigationObject> directionalObjects = new ArrayList<>();
            List<ScrollPanelReflector> scrollers = new ArrayList<ScrollPanelReflector>();
            getPanelNavigatables(newGameUiPanel, directionalObjects, scrollers, new ArrayList<>());
            directionalUINavigator.setNavigationObjects(directionalObjects);
            directionalUINavigator.clearScrollPanels();
            for(var scroller : scrollers) {
                directionalUINavigator.addScrollPanel(scroller);
            }
            addDirectionalUINavigator(directionalUINavigator);
            addButtonPressHandler("Close", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        }
        return indicators;
    }

    boolean isButtonsChanged(List<ButtonAPI> left, List<ButtonAPI> right) {
        if (left.size() != right.size()) {
            return true;
        }

        for (int index = 0; index < left.size(); index++) {
            PositionAPI leftPos = left.get(index).getPosition(), rightPos = right.get(index).getPosition();
            if (leftPos.getX() != rightPos.getX() ||
                    leftPos.getY() != rightPos.getY() ||
                    leftPos.getWidth() != rightPos.getWidth() ||
                    leftPos.getHeight() != rightPos.getHeight()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void preInput(float advance) {
        if(titleScreenStateReflector.getDialogType() == null) {
            InputScreenManager.getInstance().transitionToScreen(TitleScreenUI.ID);
        } else {
            boolean lastFramePickerOpen = portraitPickerOpen;
            portraitPickerOpen = false;
            for(var pnl : newGameUiPanel.getChildPanels()) {
                if(MessageBoxReflector.isMsgBox(pnl)) {
                    portraitPickerOpen = true;
                    if(portraitPicker == null) portraitPicker = new MessageBoxReflector(pnl);

                    portraitScroller = portraitPicker.getInnerPanel().getChildPanels().get(0);
                    break;
                }
            }
            if(!portraitPickerOpen) portraitPicker = null;
            if(portraitPicker != null && ensurePortraitVisible == null) {
                ensurePortraitVisible = new ClassReflector(portraitScroller.getClass()).findDeclaredMethod("ensureVisible");
            }
            if(lastFramePickerOpen != portraitPickerOpen) {
                refreshIndicators();
            }
        }

        List<ButtonAPI> btnsTmp = newGameUiPanel.getChildButtons(true);
        if(isButtonsChanged(uiButtons, btnsTmp)) {
            uiButtons = btnsTmp;
            directionalUINavigator.setNavigationObjects(uiButtons.stream().map(DirectionalUINavigator.NavigationObject::new).toList());
        }
        directionalUINavigator.advance(advance);
    }
}
