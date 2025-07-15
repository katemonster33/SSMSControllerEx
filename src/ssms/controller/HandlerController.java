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
import com.fs.starfarer.ui.P;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.util.input.ControllerAdapter;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.enums.*;

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
    EnumMap<AxisMapping, Integer> axisIndexByMapping = new EnumMap<>(AxisMapping.class);
    ButtonMapping[] btnMappingByIndex;
    EnumMap<ButtonMapping, Integer> btnIndexByMapping = new EnumMap<>(ButtonMapping.class);
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
                if(index != -1) {
                    if (index != 0xFF) {
                        axisMappingByIndex[index] = axisData.getAxisMapping();
                    }
                    axisIndexByMapping.put(axisData.getAxisMapping(), axisData.getAxisIndex());
                }
            }
            btnMappingByIndex = new ButtonMapping[controller.getButtonCount()];
            for(var btnData : mapping.getMappedButtons()) {
                var index = getIndexCoercingNull(btnData.getButtonIndex(), controller.getButtonCount());
                if(index != -1) {
                    btnMappingByIndex[index] = btnData.getButtonMapping();
                    if(btnMappingByIndex[index] == null) {
                        Global.getLogger(getClass()).warn("Accidentally put a null mapping in the btnMappingByIndex list!");
                    }
                    btnIndexByMapping.put(btnData.getButtonMapping(), index);
                }
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
        return val > axisBtnConversionDeadzone;
    }

    boolean generateAxisButtonEvent(LogicalButtons btn, float curVal, float lastFrameVal, Function<Float, Boolean> isLimitReached) {
        boolean lastFrameLimitReached = isLimitReached.apply(lastFrameVal), limitReached = isLimitReached.apply(curVal);
        if(lastFrameLimitReached != limitReached) {
            buttonEvents.add(new Pair<>(btn, limitReached));
            return true;
        }
        return false;
    }

    void generateAxisButtonEvents(AxisMapping axisMapping, float curVal, float lastFrameVal) {
        switch(axisMapping) {
            case LeftTrigger -> generateAxisButtonEvent(LogicalButtons.LeftTrigger, curVal, lastFrameVal, this::isUpperAxisBtnLimitReached);
            case RightTrigger -> {
                if(axisIndexByMapping.containsKey(AxisMapping.RightTrigger)) {
                    generateAxisButtonEvent(LogicalButtons.RightTrigger, curVal, lastFrameVal, this::isUpperAxisBtnLimitReached);
                } else {
                    generateAxisButtonEvent(LogicalButtons.RightTrigger, curVal, lastFrameVal, this::isLowerAxisBtnLimitReached);
                }
            }
            default -> {
                if (!generateAxisButtonEvent(axisMapping.getLowerLimitButton(), curVal, lastFrameVal, this::isLowerAxisBtnLimitReached)) {
                    generateAxisButtonEvent(axisMapping.getUpperLimitButton(), curVal, lastFrameVal, this::isUpperAxisBtnLimitReached);
                }
            }
        }
    }

    // the actual polling happens outside our control, Starsector itself calls Controllers.poll() every frame, this method just assembles the LWJGL controller events into mapped events
    public void poll() {
        buttonEvents.clear();
        axisEvents.clear();
        while (Controllers.next()) {
            if(Controllers.getEventSource() == controller) {
                if(Controllers.isEventButton()) {
                    int btnIdx = Controllers.getEventControlIndex();
                    if(btnMappingByIndex != null && btnMappingByIndex.length > btnIdx && btnMappingByIndex[btnIdx] != null) {
                        var btn = LogicalButtons.fromMapping(btnMappingByIndex[btnIdx]);
                        if(btn != null) {
                            buttonEvents.add(new Pair<>(btn, Controllers.getEventButtonState()));
                        }
                    }
                } else if(Controllers.isEventAxis()) {
                    int axisIdx = Controllers.getEventControlIndex();
                    if(axisMappingByIndex != null && axisMappingByIndex.length > axisIdx && axisMappingByIndex[axisIdx] != null) {
                        var axisMapping = axisMappingByIndex[axisIdx];
                        float axisVal = getAxisValue(axisMapping);
                        // Controller.poll compares the joystick value to last frame already,
                        //  we do it again here to not have multiple events of 0.0f because of our manual deadzone code
                        if(axisVal != lastFrameAxisValues[axisIdx]) {
                            axisEvents.add(new Pair<>(axisMapping, axisVal));

                            generateAxisButtonEvents(axisMapping, axisVal, lastFrameAxisValues[axisIdx]);
                            lastFrameAxisValues[axisIdx] = axisVal;
                        }
                    }
                } else if(Controllers.isEventPovX() && controller.getPovX() != lastFramePovX) {
                    generateAxisButtonEvents(AxisMapping.DPadX, controller.getPovX(), lastFramePovX);
                } else if(Controllers.isEventPovY() && controller.getPovY() != lastFramePovY) {
                    generateAxisButtonEvents(AxisMapping.DPadY, controller.getPovY(), lastFramePovY);
                }
            }
        }
        lastFramePovX = controller.getPovX();
        lastFramePovY = controller.getPovY();
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

    Vector2f getJoystickValues(AxisMapping xMapping, AxisMapping yMapping) {
        Vector2f joystick = new Vector2f();
        if(axisIndexByMapping.containsKey(xMapping)) {
            var xIndex = axisIndexByMapping.get(xMapping);
            if(xIndex == 0xFF) joystick.x = controller.getPovX();
            else joystick.x = controller.getAxisValue(xIndex);
        }
        if(axisIndexByMapping.containsKey(yMapping)) {
            var yIndex = axisIndexByMapping.get(yMapping);
            if(yIndex == 0xFF) joystick.y = controller.getPovY();
            else joystick.y = controller.getAxisValue(yIndex);
        }
        if ( joystick.lengthSquared() < joystickDeadzone ) {
            joystick.x = 0;
            joystick.y = 0;
        }
        return joystick;
    }

    public Vector2f getJoystick(Joystick joystick) {
        return getJoystickValues(joystick.getXAxisMapping(), joystick.getYAxisMapping());
    }

    boolean isTriggerRight() {
        if(axisIndexByMapping.containsKey(AxisMapping.RightTrigger)) {
            return controller.getAxisValue(axisIndexByMapping.get(AxisMapping.RightTrigger)) >= axisBtnConversionDeadzone;
        } else if(axisIndexByMapping.containsKey(AxisMapping.LeftTrigger)){
            return controller.getAxisValue(axisIndexByMapping.get(AxisMapping.LeftTrigger)) <= -axisBtnConversionDeadzone;
        }
        return false;
    }

    float getAxisValue(AxisMapping axisMapping) {
        float val = 0.f;
        try {
            Integer axisIdxRaw = axisIndexByMapping.get(axisMapping);
            if(axisIdxRaw == null) {
                return -1.f;
            }
            int index = axisIdxRaw;
            val = controller.getAxisValue(index);
            if(val >= -joystickDeadzone && val <= joystickDeadzone) {
                val = 0.f;
            }
        } catch(IllegalArgumentException ex) {
            Global.getLogger(getClass()).info("Axis not mapped :" + axisMapping.toString());
        }
        return val;
    }

    public List<Pair<LogicalButtons, Boolean>> getButtonEvents() {
        return new ArrayList<>(buttonEvents);
    }

    public List<Pair<AxisMapping, Float>> getAxisEvents() {
        return new ArrayList<>(axisEvents);
    }

    public boolean isButtonPressed(LogicalButtons btn) {
        var btnMapping = ButtonMapping.fromButton(btn);
        if(btnMapping != null && btnIndexByMapping.containsKey(btnMapping)) {
            int btnIdx = btnIndexByMapping.get(btnMapping);
            return controller.isButtonPressed(btnIdx);
        } else if (btn == LogicalButtons.LeftTrigger) {
            return getAxisValue(AxisMapping.LeftTrigger) <= -axisBtnConversionDeadzone;
        } else if (btn == LogicalButtons.RightTrigger) {
            return isTriggerRight();
        } else {
            var joystick = Joystick.fromButton(btn);
            var axisMapping = AxisMapping.fromButton(btn);
            if (joystick != null && axisMapping != null) {
                if (btn == axisMapping.getLowerLimitButton()) {
                    return getAxisValue(axisMapping) <= -axisBtnConversionDeadzone;
                } else if (btn == axisMapping.getUpperLimitButton()) {
                    return getAxisValue(axisMapping) >= axisBtnConversionDeadzone;
                } else Global.getLogger(getClass()).warn("Called isButtonPressed with a joystick-button that couldn't be resolved!");
            }
        }
        return false;
    }
}
