/*
 * Copyright (C) 2020 Malte Schulze.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library;  If not, see 
 * <https://www.gnu.org/licenses/>.
 */
package ssms.controller.titlescreen;

import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import ssms.controller.*;

import java.util.ArrayList;
import java.util.List;

import ssms.controller.enums.Indicators;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.reflection.TitleScreenStateReflector;
import ssms.controller.reflection.UIPanelReflector;

/**
 *
 * @author Malte Schulze
 */
public class TitleScreenUI extends InputScreenBase {
    public static final String ID = "TitleScreen";
    List<ButtonAPI> titleScreenButtons = null;
    UIPanelAPI mainMenuPanel = null;
    int selectedButton = -1;
    DirectionalUINavigator  directionalUINavigator;

    public TitleScreenUI() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.LeftStick, "Navigate Menu"));
        indicators.add(new Pair<>(Indicators.A, "Confirm"));
        indicators.add(new Pair<>(Indicators.B, "Cancel"));
        indicators.add(new Pair<>(Indicators.Select, "Reset keybindings"));
    }

    @Override
    public void activate(Object ...args) {
        UIPanelAPI panel = TitleScreenStateReflector.GetInstance().getScreenPanel();;
        UIPanelReflector.initialize(panel.getClass());
        var buttons = UIPanelReflector.getChildButtons(panel, true);
        List<Pair<UIComponentAPI, Object>> directionalUiElements = new ArrayList<>();
        for(var btn : buttons) {
            directionalUiElements.add(new Pair<>(btn, null));
        };
        directionalUINavigator = new DirectionalUINavigator(directionalUiElements) {
            @Override
            public void onConfirm(Pair<UIComponentAPI, Object> selectedPair) {
                var btn =  selectedPair.one;
                InputShim.mouseMove((int) btn.getPosition().getCenterX(), (int) btn.getPosition().getCenterY());
                InputShim.mouseDownUp((int) btn.getPosition().getCenterX(), (int) btn.getPosition().getCenterY(), InputEventMouseButton.LEFT);
            }
        };
    }

    public void selectNextButton()
    {
        if(titleScreenButtons != null && !titleScreenButtons.isEmpty()) {
            int oldSelectedButton = selectedButton;
            if(selectedButton == -1) {
                selectedButton = 0;
            } else if(selectedButton < (titleScreenButtons.size() - 1)) {
                selectedButton++;
            }
            if(!titleScreenButtons.get(selectedButton).isEnabled()) {
                selectedButton++;
            }
            if(selectedButton >= titleScreenButtons.size()) {
                selectedButton = 0;
            }
            if(selectedButton != oldSelectedButton && oldSelectedButton != -1) {
                titleScreenButtons.get(oldSelectedButton).unhighlight();
            }
            titleScreenButtons.get(selectedButton).highlight();
        }
    }

    public void selectPrevButton()
    {
        if(titleScreenButtons != null && !titleScreenButtons.isEmpty()) {
            int oldSelectedButton = selectedButton;
            if(selectedButton == -1) {
                selectedButton = 0;
            } else if(selectedButton > 0) {
                selectedButton--;
            }
            if(!titleScreenButtons.get(selectedButton).isEnabled()) {
                selectedButton--;
            }
            if(selectedButton < 0) {
                selectedButton = titleScreenButtons.size() - 1;
            }
            if(selectedButton != oldSelectedButton && oldSelectedButton != -1) {
                titleScreenButtons.get(oldSelectedButton).unhighlight();
            }
            titleScreenButtons.get(selectedButton).highlight();
        }
    }

    public void clickButton()
    {
        if(selectedButton != -1 && titleScreenButtons != null && selectedButton < titleScreenButtons.size()) {
            var btn =  titleScreenButtons.get(selectedButton);
            InputShim.mouseMove((int) btn.getPosition().getCenterX(), (int) btn.getPosition().getCenterY());
        }
    }

    @Override
    public void preInput(float advance) {
        directionalUINavigator.handleInput(advance);
//        if ( controller.getButtonEvent(Buttons.LeftStickDown) == 1 ) {
//            selectNextButton();
//        } else if ( controller.getButtonEvent(Buttons.LeftStickUp) == 1 ) {
//            selectPrevButton();
//        } else if ( controller.getButtonEvent(Buttons.A) == 1 ) {
//            clickButton();
//        } else if ( controller.getButtonEvent(Buttons.Select) == 1 ) {
//            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, AutoMapperUI.ID, new Object[]{});
//        } else if (controller.getButtonEvent(Buttons.DpadDown) == 1) {
//            selectNextButton();
//        } else if (controller.getButtonEvent(Buttons.DpadUp) == 1) {
//            selectPrevButton();
//        }
    }

    @Override
    public String getId() { return ID; }

    public String[] getScopes() { return new String[]{ InputScopeBase.ID }; }
}
