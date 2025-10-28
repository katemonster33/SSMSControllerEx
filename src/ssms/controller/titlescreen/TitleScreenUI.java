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

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import lunalib.lunaUI.panel.LunaBaseCustomPanelPlugin;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.awt.Color;
import ssms.controller.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.CodexUI;
import ssms.controller.generic.LoadGameUI;
import ssms.controller.generic.MessageBoxScreen;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.MessageBoxReflector;
import ssms.controller.reflection.TitleScreenStateReflector;
import ssms.controller.reflection.UIPanelReflector;

/**
 *
 * @author Malte Schulze
 */
public class TitleScreenUI extends InputScreenBase {

    public static final String ID = "TitleScreen";
    DirectionalUINavigator directionalUINavigator;
    List<ButtonAPI> mainMenuButtons = null;
    Object dialogType = null;
    TitleScreenStateReflector titleScreenStateReflector;
    float buttonWidth = 130f;
    float buttonHeight = 50f;
    LazyFont.DrawableString settingsButtonText;
    @Override
    public void activate(Object ...args) {
        titleScreenStateReflector = new TitleScreenStateReflector();
        UIPanelAPI panel = titleScreenStateReflector.getScreenPanel();
        UIPanelReflector.initialize(panel.getClass());
        mainMenuButtons = null;
        directionalUINavigator = new DirectionalUINavigator(new ArrayList<>());
        indicators = null;
        try {
            LazyFont font = LazyFont.loadFont(Fonts.DEFAULT_SMALL);

            settingsButtonText = font.createText("Controller Settings", Misc.getBasePlayerColor(), 15f);
        } catch(FontException fe) {
            Global.getLogger(getClass()).error("Failed to load default font", fe);
        }
    }

    @Override
    public void deactivate() {

    }

    void drawButton(float x, float y, float width, float height, Color back, Color border)
    {
        //background
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glDisable(GL11.GL_BLEND);

        GL11.glColor4f(back.getRed() / 255f,
                back.getGreen() / 255f,
                back.getBlue() / 255f,
                back.getAlpha() / 255f);

        GL11.glRectf(x, y , x + width, y + height);

        GL11.glEnd();
        GL11.glPopMatrix();

        //border
        GL11.glPushMatrix();

        GL11.glTranslatef(0f, 0f, 0f);
        GL11.glRotatef(0f, 0f, 0f, 1f);

        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glDisable(GL11.GL_BLEND);

        GL11.glColor4f(border.getRed() / 255f,
                border.getGreen() / 255f,
                border.getBlue() / 255f,
                border.getAlpha() / 255f );

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBegin(GL11.GL_LINE_STRIP);

        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + height);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x, y);

        GL11.glEnd();
        GL11.glPopMatrix();
    }

    float cooldown = 0f;
    @Override
    public void renderUI(ViewportAPI viewport) {
        super.renderUI(viewport);

        var x = 5f;
        var y = Global.getSettings().getScreenHeight() * 0.975f - buttonHeight * 3 - 10f;
        var back = Misc.getDarkPlayerColor().darker();
        var border = Misc.getDarkPlayerColor();

        var scale = Global.getSettings().getScreenScaleMult();

        var mouseX = Mouse.getX() / scale;
        var mouseY = Mouse.getY() / scale;

        cooldown--;
        cooldown = MathUtils.clamp(cooldown, -1f, 1000f);

        if (mouseX >= x && mouseX <= (x + buttonWidth) && mouseY >= y && mouseY <= (y + buttonHeight)) {
            border = Misc.getDarkPlayerColor().brighter();

            if (cooldown < 1 && Mouse.isButtonDown(0)) {
                cooldown = 20f;
                Global.getSoundPlayer().playUISound("ui_button_pressed", 1f, 1f);
                InputScreenManager.getInstance().transitionToScreen(ControllerSettingsUI.ID);

//                try {
//                    settingsPlugin = LunaSettingsUIMainPanel(false)
//                    settingsPanel = Global.getSettings().createCustom(Global.getSettings().screenWidth * 0.8f, Global.getSettings().screenHeight * 0.8f, settingsPlugin)
//                    settingsPlugin!!.initFromScript(settingsPanel!!)
//                    settingsPanel!!.position.inTL(Global.getSettings().screenWidth * 0.1f, Global.getSettings().screenHeight * 0.1f)
//
//                    var titlescreen: TitleScreenState = AppDriver.getInstance().currentState as TitleScreenState
//                    getScreenPanel().addComponent(settingsPanel)
//
//                    (settingsPlugin as LunaSettingsUIMainPanel).handler = this
//
//
//                    LunaSettingsUIMainPanel.panelOpen = true
//
//
//                } catch (e: Throwable) {
//                throw Exception("Error occured while creating panel" + e.printStackTrace())
//            }
            }
        }
        drawButton(x, y, buttonWidth, buttonHeight, back, border);
        if (settingsButtonText != null) {
            settingsButtonText.draw((x + buttonWidth / 2) - settingsButtonText.getWidth() / 2f, (y + buttonHeight / 2) + settingsButtonText.getHeight() / 2f);
        }
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = new ArrayList<>();
            if(titleScreenStateReflector.getDialogType() != null) {
                addButtonPressHandler("Cancel", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
            } else {
                addDirectionalUINavigator(directionalUINavigator);
                addButtonPressHandler("Cancel", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
                addButtonPressHandler("Reset keybindings", LogicalButtons.Select, (float advance) -> InputScreenManager.getInstance().transitionToScreen(AutoMapperUI.ID));
            }
        }
        return indicators;
    }

    @Override
    public String getId() { return ID; }

    @Override
    public void preInput(float advance) {
        var curMainMenuButtons = titleScreenStateReflector.getMainMenuButtons();
        Object curDialogType = titleScreenStateReflector.getDialogType();
        if (mainMenuButtons == null || curMainMenuButtons.size() != mainMenuButtons.size()) {

            List<Pair<UIComponentAPI, Object>> directionalUiElements = new ArrayList<>();
            if (curDialogType == null) {
                directionalUINavigator.setNavigationObjects(curMainMenuButtons.stream().map(DirectionalUINavigator.NavigationObject::new).toList());
            } else {
                var btns = new UIPanelReflector(titleScreenStateReflector.getScreenPanel()).getChildButtons(true);
                directionalUINavigator.setNavigationObjects(btns.stream().map(DirectionalUINavigator.NavigationObject::new).toList());
            }
        } else if(curDialogType != null) {
            if(Objects.equals(curDialogType.toString(), "LOAD_GAME")) {
                InputScreenManager.getInstance().transitionToScreen(LoadGameUI.ID, titleScreenStateReflector);
            } else if(Objects.equals(curDialogType.toString(), "NEW_GAME")) {
                    InputScreenManager.getInstance().transitionToScreen(NewGameUI.ID, titleScreenStateReflector);
            } else if(Objects.equals(curDialogType.toString(), "QUIT")) {
                for(var child : new UIPanelReflector(getPanelForIndicators()).getChildPanels()) {
                    if(MessageBoxReflector.isMsgBox(child)) {
                        InputScreenManager.getInstance().transitionToScreen(MessageBoxScreen.ID, new MessageBoxReflector(child), getId());
                        return;
                    }
                }
            } else if(Objects.equals(curDialogType.toString(), "CODEX")) {
                InputScreenManager.getInstance().transitionDelayed(CodexUI.ID, getId());
            }
        } else {
            var missionWidget = new UIPanelReflector(titleScreenStateReflector.getMissionWidget());
            if (missionWidget.getFader().isFadedIn()) {
                InputScreenManager.getInstance().transitionToScreen(MissionScreenUI.ID);
            }
        }
        if(isCodexOpen()) {
            InputScreenManager.getInstance().transitionDelayed(CodexUI.ID, getId());
        }
        if(curDialogType != dialogType) {
            refreshIndicators();
        }
        mainMenuButtons = curMainMenuButtons;
        dialogType = curDialogType;
        directionalUINavigator.advance(advance);
    }
}
