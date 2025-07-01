package ssms.controller.generic;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.save.LoadGameDialog;
import com.fs.starfarer.campaign.ui.UITable;
import org.lwjgl.input.Keyboard;
import ssms.controller.InputScreenBase;
import ssms.controller.InputScreenManager;
import ssms.controller.InputShim;
import ssms.controller.campaign.MainCampaignUI;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.*;
import ssms.controller.titlescreen.TitleScreenUI;

import java.util.ArrayList;
import java.util.List;

public class LoadGameUI extends InputScreenBase {
    public static String ID = "LoadGame";
    TitleScreenStateReflector titleScreenStateReflector;
    MessageBoxReflector loadGameDialog;
    DirectionalUINavigator directionalUINavigator;
    UITableReflector uiTableReflector;
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object... args) {
        var panels = new UIPanelReflector(getPanelForIndicators()).getChildPanels();
        LoadGameDialog tmpDlg = null;
        for(var pnl : panels) {
            if(pnl instanceof LoadGameDialog dlg) {
                tmpDlg = dlg;
            }
        }
        if(tmpDlg == null || !MessageBoxReflector.isMsgBox(tmpDlg)) {
            throw new RuntimeException("Couldn't reflect the dialog box type from the LoadGameDialog!");
        }
        loadGameDialog = new MessageBoxReflector(tmpDlg);
        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for(var childItem : loadGameDialog.getInnerPanel().getChildItems()) {
            if(childItem instanceof ButtonAPI btn) {
                directionalObjects.add(new Pair<>(btn, null));
            } else if(childItem instanceof UITable uiTable) {
                uiTableReflector = new UITableReflector(uiTable);
                for(var scrollableObj : uiTableReflector.getItems()) {
                    directionalObjects.add(new Pair<>(scrollableObj, uiTableReflector));
                }
            }
        }
        directionalUINavigator = new DirectionalUINavigator(directionalObjects){
            @Override
            public void onSelect(Pair<UIComponentAPI, Object> selectedObj) {
                super.onSelect(selectedObj);
                if(selectedObj.two instanceof UITableReflector) {
                    uiTableReflector.ensureVisible(selectedObj.one);
                }
            }
        };
        indicators = null;
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
        if (loadGameDialog.isBeingDismissed()) {
            if(Global.getCurrentState() == GameState.TITLE) {
                InputScreenManager.getInstance().transitionToScreen(TitleScreenUI.ID);
            } else if(Global.getCurrentState() == GameState.CAMPAIGN) {
                InputScreenManager.getInstance().transitionToScreen(MainCampaignUI.ID);
            }
        }
        //directionalUINavigator.
    }
}
