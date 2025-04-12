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
package ssms.controller.inputScreens;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Pair;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import ssms.controller.ControllerMapping;
import ssms.controller.HandlerController;
import ssms.controller.Indicators;
import ssms.controller.SSMSControllerModPluginEx;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Controller;

/**
 *
 * @author Malte Schulze
 */
public class InputScreen_TitleScreen implements InputScreen {
    InputScope_TitleScreen scope;
    public static final String ID = "TitleScreen";
    public static final String SCOPES = "TitleScreen";
    protected List<Pair<Indicators, String>> indicators;
    HandlerController.Buttons[] buttons = HandlerController.Buttons.values();
    Controller controller = null;
    LazyFont defaultFont;
    float textLineHeight;
    enum State
    {
        Normal,
        CalibratingInputs
    };
    int calibrationIndex = -1;
    ArrayList<Integer> lastParsedIndices = new ArrayList<Integer>();
    int btnCount = -1;
    boolean[] parsedIndices;
    ControllerMapping tempMapping = null;
    State currState = State.Normal;
    boolean parsedBtn = false;
    public InputScreen_TitleScreen() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.LeftStickUp, "Up"));
        indicators.add(new Pair<>(Indicators.LeftStickDown, "Down"));
        indicators.add(new Pair<>(Indicators.LeftStickLeft, "Left"));
        indicators.add(new Pair<>(Indicators.LeftStickRight, "Right"));
        indicators.add(new Pair<>(Indicators.A, "Confirm"));
        indicators.add(new Pair<>(Indicators.B, "Cancel"));
        indicators.add(new Pair<>(Indicators.Select, "Reset keybindings"));
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }

    @Override
    public void deactivate() {
    }

    @Override
    public void activate(Object ...args) {
        controller = SSMSControllerModPluginEx.controller.controller;
        scope = (InputScope_TitleScreen)InputScreenManager.getInstance().getCurrentScope();
        try {
            defaultFont = LazyFont.loadFont("graphics/fonts/insignia21LTaa.fnt");
            textLineHeight = defaultFont.createText("A").getHeight();
        } catch(FontException ex) {
            Global.getLogger(getClass()).fatal("Failed to load insignia21LTaa.fnt! " + ex);
        }
    }
    
    @Override
    public void renderInWorld(ViewportAPI viewport) {
    }

    Indicators getIndicatorForButton(HandlerController.Buttons btn)
    {
        return switch (btn) {
            case A -> Indicators.A;
            case B -> Indicators.B;
            case X -> Indicators.X;
            case Y -> Indicators.Y;
            case Select -> Indicators.Select;
            case Start -> Indicators.Start;
            case LeftStickButton -> Indicators.LeftStickButton;
            case RightStickButton -> Indicators.RightStickButton;
            case LeftStickLeft -> Indicators.LeftStickLeft;
            case LeftStickDown -> Indicators.LeftStickDown;
            case LeftStickRight -> Indicators.LeftStickRight;
            case LeftStickUp -> Indicators.LeftStickUp;
            case LeftTrigger -> Indicators.LeftTrigger;
            case RightStickDown -> Indicators.RightStickDown;
            case RightStickLeft -> Indicators.RightStickLeft;
            case RightStickRight -> Indicators.RightStickRight;
            case RightStickUp -> Indicators.RightStickUp;
            case RightTrigger -> Indicators.RightTrigger;
            case BumperLeft -> Indicators.BumperLeft;
            case BumperRight -> Indicators.BumperRight;
            default -> null;
        };
    }

    @Override
    public void renderUI(ViewportAPI viewport) {
        if(currState == State.CalibratingInputs && calibrationIndex != -1 && calibrationIndex <= buttons.length) {
            var indicatorEnum = getIndicatorForButton(buttons[calibrationIndex]);
            if(indicatorEnum != null) {
                var spritePath =  SSMSControllerModPluginEx.defaultIndicators.get(indicatorEnum);
                if(spritePath != null) {
                    SpriteAPI sprite = Global.getSettings().getSprite(spritePath);
                    if(sprite != null) {
                        sprite.setWidth(50.f);
                        sprite.setHeight(50.f);
                        var pos = viewport.getCenter();
                        pos.x = viewport.convertWorldXtoScreenX(pos.x);
                        pos.y = viewport.convertWorldYtoScreenY(pos.y);
                        ///var text = InputScreenManager.getInstance().defaultFont.createText("Press this button, or hold any button to skip: ", Color.white);
                        float totalWidth = 200;//text.getWidth() + sprite.getWidth();
                        pos.x -= (totalWidth / 2);
                       // text.draw(pos.x, pos.y + (text.getHeight() / 2) + 50);
                        sprite.render(pos.x, pos.y + (sprite.getHeight() / 2));
                    }
                }
            }
        }
    }

    @Override
    public void preInput(float advance) {
        if(currState == State.Normal) {
            if(SSMSControllerModPluginEx.controller.mapping == null) {
                currState = State.CalibratingInputs;
            } else {
                var handler = SSMSControllerModPluginEx.controller;
                if ( handler.getButtonEvent(HandlerController.Buttons.LeftStickDown) == 1 ) {
                    scope.selectNextButton();
                } else if ( handler.getButtonEvent(HandlerController.Buttons.LeftStickUp) == 1 ) {
                    scope.selectPrevButton();
                } else if ( handler.getButtonEvent(HandlerController.Buttons.A) == 1 ) {
                    scope.clickButton();
                } else if ( handler.getButtonEvent(HandlerController.Buttons.Select) == 1 ) {
                    currState = State.CalibratingInputs;
                }
            }
        } else if(currState == State.CalibratingInputs) {
            if(calibrationIndex == -1 || tempMapping == null) {
                tempMapping = new ControllerMapping();
                calibrationIndex = 0;
                btnCount = controller.getAxisCount() * 2 + controller.getButtonCount();
                parsedIndices = new boolean[btnCount];
                lastParsedIndices.clear();
                parsedBtn = false;
            }
            HandlerController.Buttons btn = buttons[calibrationIndex];
            for(int i = 0; i < btnCount; i++) {
                if(SSMSControllerModPluginEx.controller.getBtnEvent(i) == 1 && SSMSControllerModPluginEx.controller.getBtnState(i) && !parsedIndices[i] && !lastParsedIndices.contains(i)) {
                    lastParsedIndices.add(i);
                    parsedBtn = true;
                    parsedIndices[i] = true;
                    switch(btn)
                    {
                        case A: tempMapping.btnA = i; break;
                        case B: tempMapping.btnB = i; break;
                        case X: tempMapping.btnX = i; break;
                        case Y: tempMapping.btnY = i; break;
                        case BumperLeft: tempMapping.btnBumperLeft = i; break;
                        case BumperRight: tempMapping.btnBumperRight = i; break;

                        case Select: tempMapping.btnSelect = i; break;
                        case Start: tempMapping.btnStart = i; break;
                        case LeftStickButton: tempMapping.btnLeftStick = i; break;
                        case RightStickButton: tempMapping.btnRightStick = i; break;
                        case LeftStickLeft: case LeftStickRight: tempMapping.axisIndexLX = (i - controller.getButtonCount()) / 2; break;
                        case LeftStickUp: case LeftStickDown: tempMapping.axisIndexLY = (i - controller.getButtonCount()) / 2; break;
                        case RightStickLeft: case RightStickRight: tempMapping.axisIndexRX = (i - controller.getButtonCount()) / 2; break;
                        case RightStickUp: case RightStickDown: tempMapping.axisIndexRY = (i - controller.getButtonCount()) / 2; break;
                        case LeftTrigger: 
                            if(i >= controller.getButtonCount()) tempMapping.axisIndexLT = tempMapping.axisIndexRT = (i - controller.getButtonCount()) / 2;
                            else tempMapping.btnLeftTrigger = i;
                            break;
                        case RightTrigger:
                            if(i >= controller.getButtonCount()) tempMapping.axisIndexLT = tempMapping.axisIndexRT = (i - controller.getButtonCount()) / 2;
                            else tempMapping.btnRightTrigger = i;
                            break;
                        case DpadLeft: if(i >= controller.getButtonCount()) tempMapping.axisIndexDpadX = i; break;
                        case DpadRight: if(i >= controller.getButtonCount()) tempMapping.axisIndexDpadX = i - 1; break;
                        case DpadUp: if(i >= controller.getButtonCount()) tempMapping.axisIndexDpadY = i; break;
                        case DpadDown: if(i >= controller.getButtonCount()) tempMapping.axisIndexDpadY = i - 1; break;
                    }
                    break;
                }
            }
            if(parsedBtn) {
                boolean allParsedIndicesOff = true;
                if(calibrationIndex != 18 || tempMapping.axisIndexRT == null) {
                    for (var lastIdx : lastParsedIndices) {
                        if (lastIdx != null && SSMSControllerModPluginEx.controller.getBtnState(lastIdx)) {
                            allParsedIndicesOff = false;
                            break;
                        }
                    }
                }
                if(allParsedIndicesOff) {
                    lastParsedIndices.clear();
                    calibrationIndex++;
                    parsedBtn = false;
                    if(calibrationIndex == 18 && tempMapping.axisIndexRT != null) {
                        calibrationIndex++;
                    }
                    if(calibrationIndex > 18) {
                        SSMSControllerModPluginEx.controller = new HandlerController(SSMSControllerModPluginEx.controller.controller, tempMapping);
                        tempMapping = null;
                        calibrationIndex = -1;
                        controller = SSMSControllerModPluginEx.controller.controller;
                        btnCount = controller.getAxisCount() * 2 + controller.getButtonCount();
                        parsedIndices = null;
                        currState = State.Normal;
                    }
                }
            }
            
        }
    }

    @Override
    public void postInput(float advance) {
    }

    @Override
    public String getId() { return ID; }

    public String[] getScopes() { return new String[]{ SCOPES }; }
}
