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

import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.combat.entities.Ship;

import ssms.controller.ControllerMapping;
import ssms.controller.HandlerController;
import ssms.controller.Indicators;
import ssms.controller.SSMSControllerModPluginEx;
import ssms.qol.ui.AlignmentHorizontal;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lazywizard.lazylib.ui.LazyFont;
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
    enum State
    {
        Normal,
        CalibratingInputs
    };
    int calibrationIndex = -1;
    int btnCount = -1;
    boolean[] parsedIndices;
    ControllerMapping tempMapping = null;
    State currState = State.Normal;

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
        //InputScreenManager.getInstance().horizontalAlignment = AlignmentHorizontal.left;
        controller = SSMSControllerModPluginEx.controller.controller;
        
        scope = (InputScope_TitleScreen)InputScreenManager.getInstance().getCurrentScope();
    }
    
    @Override
    public void renderInWorld(ViewportAPI viewport) {
    }

    Indicators getIndicatorForButton(HandlerController.Buttons btn)
    {
        switch(btn)
        {
            case A: return Indicators.A;
            case B: return Indicators.B;
            case X: return Indicators.X;
            case Y: return Indicators.Y;
            case Select: return Indicators.Select;
            case Start: return Indicators.Start;
            case LeftStickButton: return Indicators.LeftStickButton;
            case RightStickButton: return Indicators.RightStickButton;
            case LeftStickLeft: return Indicators.LeftStickLeft;
            case LeftStickDown: return Indicators.LeftStickDown;
            case LeftStickRight: return Indicators.LeftStickRight;
            case LeftStickUp: return Indicators.LeftStickUp;
            case LeftTrigger: return Indicators.LeftTrigger;
            case RightStickDown: return Indicators.RightStickDown;
            case RightStickLeft: return Indicators.RightStickLeft;
            case RightStickRight: return Indicators.RightStickRight;
            case RightStickUp: return Indicators.RightStickUp;
            case RightTrigger: return Indicators.RightTrigger;
            case BumperLeft: return Indicators.BumperLeft;
            case BumperRight: return Indicators.BumperRight;
            
            case DpadDown:
            case DpadLeft:
            case DpadRight:
            case DpadUp:
            default:
                return null;
        }
    }

    @Override
    public void renderUI(ViewportAPI viewport) {
        if(currState == State.CalibratingInputs && calibrationIndex != -1 && calibrationIndex < buttons.length) {
            var indicatorEnum = getIndicatorForButton(buttons[calibrationIndex]);
            if(indicatorEnum != null) {
                var sprite = InputScreenManager.getInstance().indicatorSprites.get(indicatorEnum);
                if(sprite != null) {
                    var pos = viewport.getCenter();
                    pos.x = viewport.convertWorldXtoScreenX(pos.x);
                    pos.y = viewport.convertWorldYtoScreenY(pos.y);
                    var text = InputScreenManager.getInstance().defaultFont.createText("Press this button, or hold any button to skip: ", Color.white);
                    float totalWidth = text.getWidth() + 8 + sprite.getWidth();
                    pos.x -= (totalWidth / 2);
                    sprite.render(pos.x, pos.y + (sprite.getHeight() / 2));
                    text.draw(pos.x + sprite.getWidth() + 8, pos.y + (text.getHeight() / 2));
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
            }
            boolean parsedBtn = false;
            for(int i = 0; i < btnCount; i++) {
                if(SSMSControllerModPluginEx.controller.getBtnEvent(i) == 1 && !parsedIndices[i]) {
                    parsedBtn = true;
                    HandlerController.Buttons btn = buttons[calibrationIndex];
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
                        case LeftStickLeft: tempMapping.axisIndexLX = i; break;
                        case LeftStickRight: tempMapping.axisIndexLX = i - 1; break;
                        case LeftStickUp: tempMapping.axisIndexLY = i; break;
                        case LeftStickDown: tempMapping.axisIndexLY = i - 1; break;
                        case RightStickLeft: tempMapping.axisIndexRX = i; break;
                        case RightStickRight: tempMapping.axisIndexRX = i - 1; break;
                        case RightStickUp: tempMapping.axisIndexRY = i; break;
                        case RightStickDown: tempMapping.axisIndexRY = i - 1; break;
                        case LeftStickButton: tempMapping.btnLeftStick = i; break;
                        case RightStickButton: tempMapping.btnLeftStick = i; break;
                        case LeftTrigger: 
                            if(i >= controller.getButtonCount()) tempMapping.axisIndexLT = i; 
                            else tempMapping.btnLeftTrigger = i;
                            break;
                        case RightTrigger:
                            if(i >= controller.getButtonCount()) tempMapping.axisIndexRT = i; 
                            else tempMapping.btnRightTrigger = i;
                            break;
                        case DpadLeft: if(i >= controller.getButtonCount()) tempMapping.axisIndexDpadX = i; break;
                        case DpadRight: if(i >= controller.getButtonCount()) tempMapping.axisIndexDpadX = i - 1; break;
                        case DpadUp: if(i >= controller.getButtonCount()) tempMapping.axisIndexDpadX = i; break;
                        case DpadDown: if(i >= controller.getButtonCount()) tempMapping.axisIndexDpadX = i - 1; break;
                    }
                    break;
                }
            }
            if(parsedBtn) {
                calibrationIndex++;
                if(calibrationIndex >= HandlerController.Buttons.values().length) {
                    SSMSControllerModPluginEx.controller = new HandlerController(SSMSControllerModPluginEx.controller.controller, tempMapping);
                    tempMapping = null;
                    calibrationIndex = -1;
                    controller = SSMSControllerModPluginEx.controller.controller;
                    btnCount = controller.getAxisCount() * 2 + controller.getButtonCount();
                    parsedIndices = new boolean[btnCount];
                }
                currState = State.Normal;
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
