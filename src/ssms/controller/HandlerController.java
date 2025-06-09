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

import java.util.*;
import java.util.function.Function;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.util.input.ControllerAdapter;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.enums.AxisId;
import ssms.controller.enums.AxisMapping;
import ssms.controller.enums.ButtonMapping;
import ssms.controller.enums.LogicalButtons;

/**
 * Provides abstracted access to a game controller based on a {@link ssms.controller.ControllerMapping ControllerMapping}. 
 * 
 * @author Malte Schulze
 */
public class HandlerController {
    final public Controller controller;
    final public ControllerMapping mapping;
    public float axisBtnConversionDeadzone, joystickDeadzone;
    AxisMapping[] axisMappingByIndex;
    EnumMap<AxisMapping, Integer> axisIndexByMapping;
    ButtonMapping[] btnMappingByIndex;
    EnumMap<ButtonMapping, Integer> btnIndexByMapping;
    List<Pair<LogicalButtons, Boolean>> buttonEvents = new ArrayList<>();
    List<Pair<AxisMapping, Float>> axisEvents = new ArrayList<>();
    float[] lastFrameAxisValues;
    float lastFramePovX, lastFramePovY;

    public HandlerController() {
        this(new ControllerAdapter(), null);
    }

    public HandlerController(Controller controller, ControllerMapping mapping) {
        this.controller = controller;
        this.mapping = mapping;
        Map<String,Integer> axisIndices = new HashMap<>();
        for ( int i = 0; i < controller.getAxisCount(); i++ ) {
            axisIndices.put(controller.getAxisName(i), i);
        }

        if ( mapping != null ) {
            axisMappingByIndex = new AxisMapping[controller.getAxisCount()];
            for(var axisData : mapping.getMappedAxes()) {
                var index = getAxisIndex(axisData.getAxisId(), axisData.getAxisIndex(), axisIndices, controller.getAxisCount());
                axisMappingByIndex[index] = axisData.getAxisMapping();
                axisIndexByMapping.put(axisData.getAxisMapping(), axisData.getAxisIndex());
            }
            for(var btnData : mapping.getMappedButtons()) {
                var index = getIndexCoercingNull(btnData.getButtonIndex(), controller.getButtonCount());
                btnMappingByIndex[index] = btnData.getButtonMapping();
                btnIndexByMapping.put(btnData.getButtonMapping(), index);
            }
            this.axisBtnConversionDeadzone = mapping.axisBtnConversionDeadzone;
            this.joystickDeadzone = mapping.joystickDeadzone * mapping.joystickDeadzone;
        } else {
            this.axisBtnConversionDeadzone = 0.85f;
            this.joystickDeadzone = 0.0625f;
        }
        lastFrameAxisValues = new float[controller.getAxisCount()];
        for(int axisIdx = 0; axisIdx < controller.getAxisCount(); axisIdx++) {
            lastFrameAxisValues[axisIdx] = controller.getAxisValue(axisIdx);
        }
        lastFramePovX = controller.getPovX();
        lastFramePovY = controller.getPovY();

        for(int index = 0; index < controller.getAxisCount(); index++) {
            controller.setDeadZone(index, 0f);
        }
    }

    String[] getAxisNames(AxisId axisId)
    {
       return switch (axisId) {
           case XAxis -> new String[]{"X Axis", "x"};
           case YAxis -> new String[]{"Y Axis", "y"};
           case ZAxis -> new String[]{"Z Axis", "z"};
           case ZRotation -> new String[]{"Z Rotation", "rz"};
           case XRotation -> new String[]{"X Rotation", "rx"};
           case YRotation -> new String[]{"Y Rotation", "ry"};
           default -> new String[]{ };
       };
    }

   int getAxisIndex(AxisId axisId, Integer axisIndex, Map<String,Integer> axisIndices, int axisCount)
   {
       if(axisId == null) {
           if(axisIndex != null && axisIndex >= 0) {
               return axisIndex;
           }
           return -1;
       }
       return switch(axisId) {
           case POVX, POVY -> 0xFF;
           case None -> -1;
           default -> {
               for (String axisName : getAxisNames(axisId)) {
                   Integer index = axisIndices.get(axisName);
                   if (index != null) {
                       yield getIndexCoercingNull(index, axisCount);
                   }
               }
               yield -1;
           }
       };
   }
    
    protected final int getIndexCoercingNull(Integer value, int below) {
        if ( value == null ) return -1;
        if ( value >= below ) return -1;
        return value;
    }

    boolean isLowerAxisBtnLimitReached(float val) {
        return val <= -axisBtnConversionDeadzone;
    }

    boolean isUpperAxisBtnLimitReached(float val) {
        return val < axisBtnConversionDeadzone;
    }

    boolean generateAxisButtonEvent(LogicalButtons btn, float curVal, float lastFrameVal, Function<Float, Boolean> isLimitReached) {
        boolean lastFrameLimitReached = isLimitReached.apply(lastFrameVal), limitReached = isLimitReached.apply(curVal);
        if(lastFrameLimitReached != limitReached) {
            buttonEvents.add(new Pair<>(btn, limitReached));
            return true;
        }
        return false;
    }

    void generateJoystickButtonEvents(LogicalButtons lowerLimitBtn, LogicalButtons upperLimitBtn, float curVal, float lastFrameVal) {
        if (!generateAxisButtonEvent(lowerLimitBtn, curVal, lastFrameVal, this::isUpperAxisBtnLimitReached)) {
            generateAxisButtonEvent(upperLimitBtn, curVal, lastFrameVal, this::isLowerAxisBtnLimitReached);
        }
    }

    void generateAxisButtonEvents(AxisMapping axisMapping, float curVal, float lastFrameVal) {
        switch(axisMapping) {
            case LeftStickX -> generateJoystickButtonEvents(LogicalButtons.LeftStickLeft, LogicalButtons.LeftStickRight, curVal, lastFrameVal);
            case LeftStickY -> generateJoystickButtonEvents(LogicalButtons.LeftStickUp, LogicalButtons.LeftStickDown, curVal, lastFrameVal);
            case RightStickX -> generateJoystickButtonEvents(LogicalButtons.RightStickLeft, LogicalButtons.RightStickRight, curVal, lastFrameVal);
            case RightStickY -> generateJoystickButtonEvents(LogicalButtons.RightStickUp, LogicalButtons.RightStickDown, curVal, lastFrameVal);
            case DPadX -> generateJoystickButtonEvents(LogicalButtons.DpadLeft, LogicalButtons.DpadRight, curVal, lastFrameVal);
            case DPadY -> generateJoystickButtonEvents(LogicalButtons.DpadUp, LogicalButtons.DpadDown, curVal, lastFrameVal);
            case LeftTrigger -> generateAxisButtonEvent(LogicalButtons.LeftTrigger, curVal, lastFrameVal, this::isUpperAxisBtnLimitReached);
            case RightTrigger -> {
                if(axisIndexByMapping.containsKey(AxisMapping.LeftTrigger)) {
                    generateAxisButtonEvent(LogicalButtons.LeftTrigger, curVal, lastFrameVal, this::isLowerAxisBtnLimitReached);
                } else {
                    generateAxisButtonEvent(LogicalButtons.RightTrigger, curVal, lastFrameVal, this::isUpperAxisBtnLimitReached);
                }
            }
        }
    }

    // the actual polling happens outside our control, Starsector itself calls Controllers.poll() every frame, this method just assembles the LWJGL controller events into mapped events
    public void poll() {
        while (Controllers.next()) {
            if(Controllers.getEventSource() == controller) {
                if(Controllers.isEventButton()) {
                    int btnIdx = Controllers.getEventControlIndex();
                    if(btnMappingByIndex != null && btnMappingByIndex.length > btnIdx) {
                        var btn = LogicalButtons.fromMapping(btnMappingByIndex[btnIdx]);
                        if(btn != null) {
                            buttonEvents.add(new Pair<>(btn, Controllers.getEventButtonState()));
                        }
                    }
                } else if(Controllers.isEventAxis()) {
                    int axisIdx = Controllers.getEventControlIndex();
                    if(axisMappingByIndex != null && axisMappingByIndex.length > axisIdx) {
                        var axisMapping = axisMappingByIndex[axisIdx];
                        float axisVal = controller.getAxisValue(axisIdx);
                        if(axisVal >= -joystickDeadzone && axisVal <= joystickDeadzone) {
                            axisVal = 0.f;
                        }
                        // Controller.poll compares the joystick value to last frame already,
                        //  we do it again here to not have multiple events of 0.0f because of our manual deadzone code
                        if(axisVal != lastFrameAxisValues[axisIdx]) {
                            axisEvents.add(new Pair<>(axisMapping, axisVal));

                            generateAxisButtonEvents(axisMapping, axisVal, lastFrameAxisValues[axisIdx]);
                        }
                    }
                } else if(Controllers.isEventPovX() && controller.getPovX() != lastFramePovX) {
                    generateAxisButtonEvents(AxisMapping.DPadX, controller.getPovX(), lastFramePovX);
                } else if(Controllers.isEventPovY() && controller.getPovY() != lastFramePovY) {
                    generateAxisButtonEvents(AxisMapping.DPadX, controller.getPovX(), lastFramePovX);
                }
            }
        }

        for(int axisIdx = 0; axisIdx < controller.getAxisCount(); axisIdx++) {
            lastFrameAxisValues[axisIdx] = controller.getAxisValue(axisIdx);
        }
    }

    int getButtonEvent(LogicalButtons btn, List<Pair<LogicalButtons, Boolean>> eventsLst) {
        for(var btnEvent : eventsLst) {
            if(btnEvent.one == btn) {
                return btnEvent.two ? 1 : -1;
            }
        }
        return 0;
    }

    public int getButtonEvent(LogicalButtons btn) {
        return getButtonEvent(btn, buttonEvents);
    }

    ReadableVector2f getJoystickValues(AxisMapping xMapping, AxisMapping yMapping) {
        Vector2f joystick = new Vector2f();
        if(axisIndexByMapping.containsKey(xMapping)) {
            joystick.x = controller.getAxisValue(axisIndexByMapping.get(xMapping));
        }
        if(axisIndexByMapping.containsKey(yMapping)) {
            joystick.y = controller.getAxisValue(axisIndexByMapping.get(yMapping));
        }
        if ( joystick.lengthSquared() < joystickDeadzone ) {
            joystick.x = 0;
            joystick.y = 0;
        }
        return joystick;
    }
    
    public ReadableVector2f getLeftStick() {
        //TODO we could clamp the steering to something like 120 distinct values so that the directional input is more stable.
        //custom deadzone that takes into account the length of the vector to determine if it should be zero. That way we can steer with full precision in 360° 
        //but ignore a poorly resting joystick.
        return getJoystickValues(AxisMapping.LeftStickX, AxisMapping.LeftStickY);
    }
    
    public ReadableVector2f getRightStick() {
        return getJoystickValues(AxisMapping.RightStickX, AxisMapping.RightStickY);
    }
    
    boolean isTriggerRight() {
        if(axisIndexByMapping.containsKey(AxisMapping.RightTrigger)) {
            return controller.getAxisValue(axisIndexByMapping.get(AxisMapping.RightTrigger)) >= axisBtnConversionDeadzone;
        } else if(axisIndexByMapping.containsKey(AxisMapping.LeftTrigger)){
            return controller.getAxisValue(axisIndexByMapping.get(AxisMapping.LeftTrigger)) <= -axisBtnConversionDeadzone;
        }
        return false;
    }

    float getDpadX() {
        if(axisIndexByMapping.containsKey(AxisMapping.DPadX)) {
            int axisIdx = axisIndexByMapping.get(AxisMapping.DPadX);
            if(axisIdx != 0xFF) {
                return controller.getAxisValue(axisIdx);
            }
        }
        return controller.getPovX();
    }

    float getDpadY() {
        if(axisIndexByMapping.containsKey(AxisMapping.DPadY)) {
            int axisIdx = axisIndexByMapping.get(AxisMapping.DPadY);
            if(axisIdx != 0xFF) {
                return controller.getAxisValue(axisIdx);
            }
        }
        return controller.getPovY();
    }

    float getAxisValue(AxisMapping axisMapping) {
        float val = 0.f;
        try {
            int index = axisIndexByMapping.get(axisMapping);
            val = controller.getAxisValue(index);
        } catch(IllegalArgumentException ex) {
            Global.getLogger(getClass()).info("Axis not mapped :" + axisMapping.toString());
        }
        return val;
    }

    boolean getNormalBtnState(ButtonMapping btn) {
        if(btnIndexByMapping.containsKey(btn)) {
            int btnIdx = btnIndexByMapping.get(btn);
            return controller.isButtonPressed(btnIdx);
        }
        return false;
    }

    public boolean isButtonPressed(LogicalButtons btn) {
        //var axisMapping =
        return switch(btn) {
            case LeftStickLeft -> getAxisValue(AxisMapping.LeftStickX) <= -axisBtnConversionDeadzone;
            case LeftStickRight -> getAxisValue(AxisMapping.LeftStickX) >= axisBtnConversionDeadzone;
            case LeftStickUp -> getAxisValue(AxisMapping.LeftStickY) <= -axisBtnConversionDeadzone;
            case LeftStickDown -> getAxisValue(AxisMapping.LeftStickY) >= axisBtnConversionDeadzone;
            case RightStickLeft -> getAxisValue(AxisMapping.RightStickX) <= -axisBtnConversionDeadzone;
            case RightStickRight -> getAxisValue(AxisMapping.RightStickX) >= axisBtnConversionDeadzone;
            case RightStickUp -> getAxisValue(AxisMapping.RightStickY) <= -axisBtnConversionDeadzone;
            case RightStickDown -> getAxisValue(AxisMapping.RightStickY) >= axisBtnConversionDeadzone;
            case LeftTrigger -> getAxisValue(AxisMapping.LeftTrigger) <= -axisBtnConversionDeadzone;
            case RightTrigger -> isTriggerRight();
            case DpadUp -> getDpadY() <= -axisBtnConversionDeadzone;
            case DpadDown -> getDpadY() >= axisBtnConversionDeadzone;
            case DpadLeft -> getDpadX() <= -axisBtnConversionDeadzone;
            case DpadRight -> getDpadX() >= axisBtnConversionDeadzone;
            default -> getNormalBtnState(ButtonMapping.fromButton(btn));
        };
    }
}
