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
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import ssms.controller.enums.AxisMapping;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.AxisHandler;
import ssms.controller.inputhelper.ButtonHandler;
import ssms.controller.inputhelper.AnalogJoystickHandler;
import ssms.controller.inputhelper.JoystickHandler;
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

    public static final String ID = "NoScreen";
    public static final String SCOPES = InputScopeBase.ID;
    protected List<Pair<Indicators, String>> indicators;
    protected EnumMap<LogicalButtons, ButtonHandler> buttonHandlers;
    protected EnumMap<AxisMapping, AxisHandler> axisHandlers;
    protected EnumMap<Joystick, JoystickHandler> joystickHandlers;
    protected HandlerController controller;

    public InputScreenBase() {
        buttonHandlers = new EnumMap<>(LogicalButtons.class);
        indicators = new ArrayList<>();
        axisHandlers = new EnumMap<>(AxisMapping.class);
        controller = SSMSControllerModPluginEx.controller;
    }

    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }

    public void deactivate() {
    }

    public void activate(Object ...args) throws Throwable {
    }

    public void renderInWorld(ViewportAPI viewport) {
    }

    public void renderUI(ViewportAPI viewport) {
    }

    public void preInput(float advance) {
    }

    public final void processControllerEvents(float advance, List<Pair<LogicalButtons, Boolean>> buttonEvents, List<Pair<AxisMapping, Float>> axisEvents) {
        for(var btnEvent : buttonEvents) {
            if(buttonHandlers.containsKey(btnEvent.one) && !btnEvent.two) {
                buttonHandlers.get(btnEvent.one).performAction(advance);
            }
        }
        boolean fireLeftStickEvent = false, fireRightStickEvent = false, fireDpadEvent = false;
        for(var axisEvent : axisEvents) {
            if(axisHandlers.containsKey(axisEvent.one)) {
                axisHandlers.get(axisEvent.one).performAction(advance, axisEvent.two);
            }
            switch(axisEvent.one) {
                case LeftStickX, LeftStickY -> fireLeftStickEvent = true;
                case RightStickX, RightStickY -> fireRightStickEvent = true;
                case DPadX, DPadY -> fireDpadEvent = true;
            }
        }
        if(leftAnalogJoystickHandler != null && fireLeftStickEvent) {
            leftAnalogJoystickHandler.performAction(advance, controller.getLeftStick());
        }
        if(rightAnalogJoystickHandler != null && fireRightStickEvent) {
            rightAnalogJoystickHandler.performAction(advance, controller.getRightStick());
        }
        if(dpadHandler != null && fireDpadEvent) {
            dpadHandler.performAction(advance, controller.getDpad());
        }
    }

    public void postInput(float advance) {
    }

    public String getId() { return ID; }

    public String[] getScopes() { return new String[]{ SCOPES }; }

    protected void addButtonPressHandler(String msg, LogicalButtons logicalButtons, ButtonHandler btnPressHandler) {
        var indicator = Indicators.fromButton(logicalButtons);
        if(indicator == null) {
            Global.getLogger(getClass()).warn("given button doesn't translate to indicator! " + logicalButtons);
            return;
        }
        buttonHandlers.put(logicalButtons, btnPressHandler);
        indicators.add(new Pair<>(indicator, msg));
    }

    protected void addAxisHandler(String msg, AxisMapping axisMapping, AxisHandler axisHandler) {

        var indicator = Indicators.DPad; //Indicators.fromAxisMapping(logicalButtons);
//        if(indicator == null) {
//            Global.getLogger(getClass()).warn("given button doesn't translate to indicator! " + logicalButtons);
//            return;
//        }
        axisHandlers.put(axisMapping, axisHandler);
        indicators.add(new Pair<>(indicator, msg));
    }

    protected void addJoystickHandler(String msg, Joystick joystick, JoystickHandler joystickHandler) {

    }

    protected void clearHandlers() {
        buttonHandlers.clear();
        axisHandlers.clear();
        joystickHandlers.clear();
    }

    public UIPanelAPI getPanelForIndicators() {
        return switch (Global.getCurrentState()) {
            case TITLE -> TitleScreenStateReflector.GetInstance().getScreenPanel();
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
