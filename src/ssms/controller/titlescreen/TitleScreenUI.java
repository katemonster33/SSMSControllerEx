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
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;

import java.util.ArrayList;
import java.util.List;

import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.TitleScreenStateReflector;
import ssms.controller.reflection.UIPanelReflector;

/**
 *
 * @author Malte Schulze
 */
public class TitleScreenUI extends InputScreenBase {
    public static final String ID = "TitleScreen";
    DirectionalUINavigator directionalUINavigator;
    @Override
    public void activate(Object ...args) {
        UIPanelAPI panel = TitleScreenStateReflector.GetInstance().getScreenPanel();
        UIPanelReflector.initialize(panel.getClass());
        //var buttons = UIPanelReflector.getChildButtons(panel, true);
        var buttons = TitleScreenStateReflector.GetInstance().getMainMenuButtons();

        List<Pair<UIComponentAPI, Object>> directionalUiElements = new ArrayList<>();
        for(var btn : buttons) {
            directionalUiElements.add(new Pair<>(btn, null));
        }

        indicators = new ArrayList<>();

        directionalUINavigator = new DirectionalUINavigator(directionalUiElements);
        addDigitalJoystickHandler("Navigate Menu", Joystick.DPad, directionalUINavigator);
        addButtonPressHandler("Confirm", LogicalButtons.A, this::clickButton);
        addButtonPressHandler("Cancel", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        addButtonPressHandler("Reset keybindings", LogicalButtons.Select, (float advance) -> InputScreenManager.getInstance().transitionToScreen(AutoMapperUI.ID));
    }

    public void clickButton(float advance)
    {
        if(directionalUINavigator.getSelected() != null) {
            var btn = directionalUINavigator.getSelected().one;
            InputShim.mouseMove((int) btn.getPosition().getCenterX(), (int) btn.getPosition().getCenterY());
            InputShim.mouseDownUp((int) btn.getPosition().getCenterX(), (int) btn.getPosition().getCenterY(), InputEventMouseButton.LEFT);
        }
    }

    @Override
    public String getId() { return ID; }

    public String[] getScopes() { return new String[]{ InputScopeBase.ID }; }
}
