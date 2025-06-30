package ssms.controller.titlescreen;

import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.ScrollPanelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.ui.P;
import org.lwjgl.input.Keyboard;
import ssms.controller.InputScreenBase;
import ssms.controller.InputScreenManager;
import ssms.controller.InputShim;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.MessageBoxReflector;
import ssms.controller.reflection.ScrollPanelReflector;
import ssms.controller.reflection.TitleScreenStateReflector;
import ssms.controller.reflection.UIPanelReflector;

import java.util.ArrayList;
import java.util.List;

public class LoadGameUI extends InputScreenBase {
    public static String ID = "LoadGame";
    TitleScreenStateReflector titleScreenStateReflector;
    MessageBoxReflector loadGameDialog;
    DirectionalUINavigator directionalUINavigator;
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object... args) {
        titleScreenStateReflector = (TitleScreenStateReflector) args[0];

        var panels = new UIPanelReflector(titleScreenStateReflector.getScreenPanel()).getChildPanels();
        if(!panels.isEmpty()) {
            if((loadGameDialog = MessageBoxReflector.TryGet(panels.get(panels.size() - 1))) == null) {
                throw new RuntimeException("Couldn't reflect the dialog box type from the LoadGameDialog!");
            }
            List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
            for(var childItem : loadGameDialog.getInnerPanel().getChildItems()) {
                if(childItem instanceof ButtonAPI btn) {
                    directionalObjects.add(new Pair<>(btn, null));
                } else if(childItem instanceof ScrollPanelAPI scrollPanelAPI) {
                    ScrollPanelReflector scrollPanelReflector = new ScrollPanelReflector(scrollPanelAPI);
                    for(var scrollableObj : scrollPanelReflector.getChildPanels()) {
                        directionalObjects.add(new Pair<>(scrollableObj, scrollPanelReflector));
                    }
                }
            }
            directionalUINavigator = new DirectionalUINavigator(directionalObjects){
                @Override
                public void onSelect(Pair<UIComponentAPI, Object> selectedObj) {
                    super.onSelect(selectedObj);
                    if(selectedObj.two instanceof ScrollPanelReflector scrollPanelReflector) {
                        scrollPanelReflector.ensureVisible(selectedObj.one);
                    }
                }
            };
        }

    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = new ArrayList<>();
            addDigitalJoystickHandler("Navigate", Joystick.DPad, directionalUINavigator);
            addButtonPressHandler("Select", LogicalButtons.A, (float advance) -> {
                if(directionalUINavigator.getSelected() != null) {
                    var pos = directionalUINavigator.getSelected().one.getPosition();
                    InputShim.mouseDownUp((int) pos.getCenterX(), (int) pos.getCenterY(), InputEventMouseButton.LEFT);
                }
            });
            addButtonPressHandler("Close", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        }
        return indicators;
    }

    @Override
    public void preInput(float advance) {
        if(titleScreenStateReflector.getDialogType() == null) {
            InputScreenManager.getInstance().transitionToScreen(TitleScreenUI.ID, titleScreenStateReflector);
        }
    }
}
