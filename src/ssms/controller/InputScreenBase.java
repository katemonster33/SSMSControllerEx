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
package ssms.controller;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.enums.AxisMapping;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.*;
import ssms.controller.reflection.CampaignStateReflector;
import ssms.controller.reflection.CombatStateReflector;
import ssms.controller.reflection.TitleScreenStateReflector;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 *
 * @author Malte Schulze
 */
public class InputScreenBase {

    DirectionalUINavigator directionalUINavigator;
    MapInputHandler mapInputHandler;
    public static final String ID = "NoScreen";
    public static final String SCOPES = InputScopeBase.ID;
    protected List<Pair<Indicators, String>> indicators;
    protected EnumMap<LogicalButtons, ButtonPressHandler> buttonHandlers;
    protected EnumMap<LogicalButtons, ButtonChangeHandler> buttonExHandlers;
    protected EnumMap<Joystick, DigitalJoystickHandler> digitalJoystickHandlers;
    protected EnumMap<Joystick, AnalogJoystickHandler> analogJoystickHandlers;
    protected HandlerController controller;
    protected EnumMap<Joystick, Vector2f> lastFrameJoystickVal;

    public InputScreenBase() {
        buttonHandlers = new EnumMap<>(LogicalButtons.class);
        buttonExHandlers = new EnumMap<>(LogicalButtons.class);
        indicators = new ArrayList<>();
        digitalJoystickHandlers = new EnumMap<>(Joystick.class);
        analogJoystickHandlers = new EnumMap<>(Joystick.class);
        lastFrameJoystickVal = new EnumMap<>(Joystick.class);
        controller = SSMSControllerModPluginEx.controller;
    }

    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }

    public void deactivate() {
        directionalUINavigator = null;
        mapInputHandler = null;
    }

    public void activate(Object ...args) throws Throwable {
    }

    public void renderInWorld(ViewportAPI viewport) {
    }

    public void renderUI(ViewportAPI viewport) {
        if(directionalUINavigator != null) {
            directionalUINavigator.render();
        }

        if(mapInputHandler != null) {
            mapInputHandler.render();
        }
    }

    public void preInput(float advance) {
    }

    public void processInputEvents(float advance, List<InputEventAPI> events) {
    }

    public final void processControllerEvents(float advance, List<Pair<LogicalButtons, Boolean>> buttonEvents, List<Pair<AxisMapping, Float>> axisEvents) {
        for (var btnEvent : buttonEvents) {
            if (!btnEvent.two) {
                var btnHandler = buttonHandlers.get(btnEvent.one);
                if (btnHandler != null) btnHandler.performAction(advance);
            }
            var btnExHandler = buttonExHandlers.get(btnEvent.one);
            if (btnExHandler != null) btnExHandler.performAction(advance, btnEvent.two);

            if (btnEvent.two) {
                Joystick joystick = Joystick.fromButton(btnEvent.one);
                var btnAxis = AxisMapping.fromButton(btnEvent.one);
                if (joystick != null && btnAxis != null) {
                    var handler = digitalJoystickHandlers.get(joystick);
                    if (handler != null) {
                        if (btnAxis == joystick.getXAxisMapping()) {
                            if (btnEvent.one == btnAxis.getLowerLimitButton()) handler.performLeftAction(advance);
                            else if (btnEvent.one == btnAxis.getUpperLimitButton()) handler.performRightAction(advance);
                        } else if (btnAxis == joystick.getYAxisMapping()) {
                            if (btnEvent.one == btnAxis.getLowerLimitButton()) handler.performUpAction(advance);
                            else if (btnEvent.one == btnAxis.getUpperLimitButton()) handler.performDownAction(advance);
                        }
                    }
                }
            }
        }
        EnumMap<Joystick, Boolean> joystickHandlersToFire = new EnumMap<>(Joystick.class);
        for (var axisEvent : axisEvents) {
            Joystick joystick = Joystick.fromAxisMapping(axisEvent.one);
            if (joystick != null) {
                joystickHandlersToFire.put(joystick, true);
            }
        }
        for (Joystick joystick : joystickHandlersToFire.keySet()) {
            var handler = analogJoystickHandlers.get(joystick);
            if (handler != null){
                var joystickVal = controller.getJoystick(joystick);
                var lastFrameJoystick = lastFrameJoystickVal.get(joystick);
                if(lastFrameJoystick == null || joystickVal.getX() != lastFrameJoystick.getX() || joystickVal.getY() != lastFrameJoystick.getY()) {
                    handler.performAction(advance, joystickVal);
                    lastFrameJoystickVal.put(joystick, joystickVal);
                }
            }
        }
        for(var handler : buttonExHandlers.values()) {
            if(handler instanceof ButtonPressOrHoldHandler buttonPressOrHoldHandler) {
                buttonPressOrHoldHandler.advance(advance);
            }
        }

        if(directionalUINavigator != null && directionalUINavigator.isContextChanged()) {
            refreshIndicators();
        }
    }

    public void postInput(float advance) {
    }

    public String getId() { return ID; }

    public String[] getScopes() { return new String[]{ SCOPES }; }

    protected void addButtonPressHandler(String msg, LogicalButtons logicalButtons, ButtonPressHandler btnPressHandler) {
        var indicator = Indicators.fromButton(logicalButtons);
        if(indicator == null) {
            Global.getLogger(getClass()).warn("given button doesn't translate to indicator! " + logicalButtons);
            return;
        }
        buttonHandlers.put(logicalButtons, btnPressHandler);
        indicators.add(new Pair<>(indicator, msg));
    }

    protected void addButtonChangeHandler(String msg, LogicalButtons logicalButtons, ButtonChangeHandler btnChangeHandler) {
        var indicator = Indicators.fromButton(logicalButtons);
        if(indicator == null) {
            Global.getLogger(getClass()).warn("given button doesn't translate to indicator! " + logicalButtons);
            return;
        }
        buttonExHandlers.put(logicalButtons, btnChangeHandler);
        indicators.add(new Pair<>(indicator, msg));
    }

    protected  void addButtonPressOrHoldHandler(String pressMsg, String holdMsg, LogicalButtons logicalButtons, ButtonPressOrHoldHandler btnPressHoldHandler) {
        var indicator = Indicators.fromButton(logicalButtons);
        if(indicator == null) {
            Global.getLogger(getClass()).warn("given button doesn't translate to indicator! " + logicalButtons);
            return;
        }
        buttonExHandlers.put(logicalButtons, btnPressHoldHandler);
        indicators.add(new Pair<>(indicator, "(Quick press) " + pressMsg));
        indicators.add(new Pair<>(indicator, "(Press & Hold) " + holdMsg));
    }

    protected void addDigitalJoystickHandler(String msg, Joystick joystick, DigitalJoystickHandler digitalJoystickHandler) {
        Indicators joystickIndicator = Indicators.fromJoystick(joystick);
        if(joystickIndicator != null) {
            indicators.add(new Pair<>(joystickIndicator, msg));
        }
        digitalJoystickHandlers.put(joystick, digitalJoystickHandler);
        if(digitalJoystickHandler instanceof DirectionalUINavigator directionalUINavigator1) {
            this.directionalUINavigator = directionalUINavigator1;
        }
    }

    protected void addDirectionalUINavigator(DirectionalUINavigator directionalUINavigator) {
        this.directionalUINavigator = directionalUINavigator;

        indicators.add(new Pair<>(Indicators.DPad, "Navigate"));
        digitalJoystickHandlers.put(Joystick.DPad, directionalUINavigator);

        indicators.add(new Pair<>(Indicators.LeftStick, "Move cursor"));

        switch(directionalUINavigator.getCurContext()) {
            case Map -> {
                indicators.add(new Pair<>(Indicators.RightStick, "Move map"));
                indicators.add(new Pair<>(Indicators.LeftTrigger, "Zoom out"));
                indicators.add(new Pair<>(Indicators.RightTrigger, "Zoom in"));
            }
        }

        addButtonChangeHandler("Select", LogicalButtons.A, directionalUINavigator::handleAButton);

        directionalUINavigator.setJoystickEnabled(true);
    }

    protected void addAnalogJoystickHandler(String msg, Joystick joystick, AnalogJoystickHandler analogJoystickHandler) {
        Indicators joystickIndicator = Indicators.fromJoystick(joystick);
        if(joystickIndicator != null) {
            indicators.add(new Pair<>(joystickIndicator, msg));
        }
        analogJoystickHandlers.put(joystick, analogJoystickHandler);
    }

    protected MapInputHandler addMapHandler(UIComponentAPI mapComponent) {
        mapInputHandler = new MapInputHandler(mapComponent);
        addAnalogJoystickHandler("Move cursor", Joystick.Left, mapInputHandler::handleLeftJoystick);
        addAnalogJoystickHandler("Move map", Joystick.Right, mapInputHandler::handleRightJoystick);
        addButtonChangeHandler("Select", LogicalButtons.A, mapInputHandler::handleAButton);
        indicators.add(new Pair<>(Indicators.LeftTrigger, "Zoom out"));
        indicators.add(new Pair<>(Indicators.RightTrigger, "Zoom in"));
        mapInputHandler.centerMousePos();
        return mapInputHandler;
    }

    public void refreshIndicators() {
        indicators = null;
        clearHandlers();
        InputScreenManager.getInstance().refreshIndicators();
    }

    protected void clearHandlers() {
        buttonHandlers.clear();
        buttonExHandlers.clear();
        digitalJoystickHandlers.clear();
        analogJoystickHandlers.clear();
        indicators = null;
    }

    public UIPanelAPI getPanelForIndicators() {
        return switch (Global.getCurrentState()) {
            case TITLE -> new TitleScreenStateReflector().getScreenPanel();
            case CAMPAIGN -> CampaignStateReflector.GetInstance().getScreenPanel();
            case COMBAT -> CombatStateReflector.GetInstance().getWidgetPanel();
        };
    }

    public Alignment getIndicatorsAlignment() {
        return switch (Global.getCurrentState()) {
            case TITLE -> Alignment.BL;
            case CAMPAIGN -> Alignment.RMID;
            case COMBAT -> Alignment.BR;
        };
    }
}
