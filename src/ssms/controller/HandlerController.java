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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.input.Controller;
import org.lwjgl.util.input.ControllerAdapter;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

/**
 * Provides abstracted access to a game controller based on a {@link ssms.controller.ControllerMapping ControllerMapping}. 
 * 
 * @author Malte Schulze
 */
public class HandlerController {
    final public Controller controller;
    final public ControllerMapping mapping;
    public float axisBtnConversionDeadzone, joystickDeadzone;
    protected Vector2f leftStick = new Vector2f();
    protected Vector2f rightStick = new Vector2f();
    protected Vector2f dpad = new Vector2f();
    protected boolean[] btnStates;
    protected int[] btnEvents;
    public enum Buttons {
        A,B,X,Y,BumperLeft,BumperRight,Start,Select,LeftStickButton,RightStickButton,RightStickUp,RightStickDown,RightStickLeft,RightStickRight,
        LeftStickUp,LeftStickDown,LeftStickLeft,LeftStickRight,LeftTrigger,RightTrigger,DpadLeft,DpadRight,DpadUp,DpadDown
    }
    
    protected int axisLeftStickX, axisLeftStickY, axisRightStickX, axisRightStickY, axisTrigger, axisRTrigger, axisDpadX, axisDpadY,
            btnA, btnB, btnX, btnY, btnBumperLeft, btnBumperRight, btnStart, btnSelect, btnLeftStick, btnRightStick;

    public HandlerController() {
        this(new ControllerAdapter(), null);
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
           default -> null;
       };
   }

   int getAxisIndex(AxisId axisId, Integer axisIndex, Map<String,Integer> axisIndices, int axisCount)
   {
       if(axisId == null)
       {
           if(axisIndex != null && axisIndex >= 0) {
               return axisIndex;
           }
           return -1;
       }
       switch(axisId)
       {
           default:
               String[] axisNames = getAxisNames(axisId);
               if(axisNames != null) {
                   for(String axisName : axisNames) {
                       Integer index = axisIndices.get(axisName);
                       if(index != null) {
                           return getIndexCoercingNull(index, axisCount);
                       }
                   }
               }
               return -1;

           case POVX:
           case POVY:
               return 0xFF;

           case None:
               return -1;
       }
   }
// int getAxisIndex(int axisId, int axisCount)
// {
//     if(axisId == 255) return axisId;
//     else return getIndexCoercingNull(axisId, axisCount);
// }

    public HandlerController(Controller controller, ControllerMapping mapping) {
        this.controller = controller;
        this.mapping = mapping;
        Map<String,Integer> axisIndices = new HashMap<>();
        for ( int i = 0; i < controller.getAxisCount(); i++ ) {
            axisIndices.put(controller.getAxisName(i), i);
        }
        
        if ( mapping != null ) {
            axisLeftStickX = getAxisIndex(mapping.axisIdLX, mapping.axisIndexLX, axisIndices, controller.getAxisCount());
            axisLeftStickY = getAxisIndex(mapping.axisIdLY, mapping.axisIndexLY, axisIndices, controller.getAxisCount());
            axisRightStickX = getAxisIndex(mapping.axisIdRX, mapping.axisIndexRX, axisIndices, controller.getAxisCount());
            axisRightStickY = getAxisIndex(mapping.axisIdRY, mapping.axisIndexRY, axisIndices, controller.getAxisCount());
            axisDpadX = getAxisIndex(mapping.axisIdDpadX, mapping.axisIndexDpadX, axisIndices, controller.getAxisCount());
            axisDpadY = getAxisIndex(mapping.axisIdDpadY, mapping.axisIndexDpadY, axisIndices, controller.getAxisCount());
            axisTrigger = getAxisIndex(mapping.axisIdLT, mapping.axisIndexLT, axisIndices, controller.getAxisCount());
            axisRTrigger = getAxisIndex(mapping.axisIdRT, mapping.axisIndexRT, axisIndices, controller.getAxisCount());
            
            btnA = getIndexCoercingNull(mapping.btnA,controller.getButtonCount());
            btnB = getIndexCoercingNull(mapping.btnB,controller.getButtonCount());
            btnX = getIndexCoercingNull(mapping.btnX,controller.getButtonCount());
            btnY = getIndexCoercingNull(mapping.btnY,controller.getButtonCount());
            btnBumperLeft = getIndexCoercingNull(mapping.btnBumperLeft,controller.getButtonCount());
            btnBumperRight = getIndexCoercingNull(mapping.btnBumperRight,controller.getButtonCount());
            btnStart = getIndexCoercingNull(mapping.btnStart,controller.getButtonCount());
            btnSelect = getIndexCoercingNull(mapping.btnSelect,controller.getButtonCount());
            btnLeftStick = getIndexCoercingNull(mapping.btnLeftStick,controller.getButtonCount());
            btnRightStick = getIndexCoercingNull(mapping.btnRightStick,controller.getButtonCount());
            
            this.axisBtnConversionDeadzone = mapping.axisBtnConversionDeadzone;
            this.joystickDeadzone = mapping.joystickDeadzone * mapping.joystickDeadzone;
        } else {
            axisLeftStickX = axisLeftStickY = axisRightStickX = axisRightStickY = axisTrigger = axisRTrigger = axisDpadX = axisDpadY =
                btnA = btnB = btnX = btnY = btnBumperLeft = btnBumperRight = btnStart = btnSelect = btnLeftStick = btnRightStick = -1;

            this.axisBtnConversionDeadzone = 0.85f;
            this.joystickDeadzone = 0.0625f;
        }

        for(int index = 0; index < controller.getAxisCount(); index++) {
            controller.setDeadZone(index, 0f);
        }
        
        // if ( axisLeftStickX >= 0 ) {
        //     controller.setDeadZone(axisLeftStickX, 0f);
        // }
        // if ( axisLeftStickY >= 0 ) {
        //     controller.setDeadZone(axisLeftStickY, 0f);
        // }
        // if ( axisRightStickX >= 0 ) {
        //     controller.setDeadZone(axisLeftStickX, 0f);
        // }
        // if ( axisRightStickY >= 0 ) {
        //     controller.setDeadZone(axisLeftStickY, 0f);
        // }
        // if ( axisTrigger >= 0 ) {
        //     controller.setDeadZone(axisTrigger, 0f);
        // }
        
        btnStates = new boolean[controller.getButtonCount()+controller.getAxisCount()*2];
        Arrays.fill(btnStates, false);
        btnEvents = new int[btnStates.length];
        Arrays.fill(btnEvents, 0);
    }
    
    protected final int getIndexCoercingNull(Integer value, int below) {
        if ( value == null ) return -1;
        if ( value >= below ) return -1;
        return value;
    }
    
    public void poll() {
        this.controller.poll();
        
        for ( int i = 0; i < controller.getButtonCount(); i++ ) {
            boolean pressed = this.controller.isButtonPressed(i);
            if ( pressed ) {
                if ( !btnStates[i] ) {
                    btnStates[i] = true;
                    btnEvents[i] = 1;
                } else if ( btnEvents[i] != 0 ) {
                    btnEvents[i] = 0;
                }
            } else {
                if ( btnStates[i] ) {
                    btnStates[i] = false;
                    btnEvents[i] = -1;
                } else if ( btnEvents[i] != 0 ) {
                    btnEvents[i] = 0;
                }
            }
        }
        
        for ( int j = 0, i = controller.getButtonCount(); j < controller.getAxisCount(); j++, i++ ) {
            boolean pressed = this.controller.getAxisValue(j) <= -axisBtnConversionDeadzone;
            if ( pressed ) {
                if ( !btnStates[i] ) {
                    btnStates[i] = true;
                    btnEvents[i] = 1;
                } else if ( btnEvents[i] != 0 ) {
                    btnEvents[i] = 0;
                }
            } else {
                if ( btnStates[i] ) {
                    btnStates[i] = false;
                    btnEvents[i] = -1;
                } else if ( btnEvents[i] != 0 ) {
                    btnEvents[i] = 0;
                }
            }
            pressed = this.controller.getAxisValue(j) >= axisBtnConversionDeadzone;
            i++;
            if ( pressed ) {
                if ( !btnStates[i] ) {
                    btnStates[i] = true;
                    btnEvents[i] = 1;
                } else if ( btnEvents[i] != 0 ) {
                    btnEvents[i] = 0;
                }
            } else {
                if ( btnStates[i] ) {
                    btnStates[i] = false;
                    btnEvents[i] = -1;
                } else if ( btnEvents[i] != 0 ) {
                    btnEvents[i] = 0;
                }
            }
        }
    }
    
    public int getButtonEvent(Buttons btn) {
        return switch (btn) {
            case A -> getBtnEvent(btnA);
            case B -> getBtnEvent(btnB);
            case X -> getBtnEvent(btnX);
            case Y -> getBtnEvent(btnY);
            case Start -> getBtnEvent(btnStart);
            case Select -> getBtnEvent(btnSelect);
            case BumperLeft -> getBtnEvent(btnBumperLeft);
            case BumperRight -> getBtnEvent(btnBumperRight);
            case LeftStickButton -> getBtnEvent(btnLeftStick);
            case RightStickButton -> getBtnEvent(btnRightStick);
            case RightStickDown -> getBtnEvent(axisRightStickY, controller.getButtonCount() + 1);
            case RightStickUp -> getBtnEvent(axisRightStickY, controller.getButtonCount());
            case RightStickLeft -> getBtnEvent(axisRightStickX, controller.getButtonCount());
            case RightStickRight -> getBtnEvent(axisRightStickX, controller.getButtonCount() + 1);
            case LeftStickDown -> getBtnEvent(axisLeftStickY, controller.getButtonCount() + 1);
            case LeftStickUp -> getBtnEvent(axisLeftStickY, controller.getButtonCount());
            case LeftStickLeft -> getBtnEvent(axisLeftStickX, controller.getButtonCount());
            case LeftStickRight -> getBtnEvent(axisLeftStickX, controller.getButtonCount() + 1);
            case LeftTrigger -> axisRTrigger >= 0 ? getBtnEvent(axisTrigger, controller.getButtonCount() + 1) : getBtnEvent(axisTrigger, controller.getButtonCount());
            case RightTrigger -> axisRTrigger >= 0 ? getBtnEvent(axisRTrigger, controller.getButtonCount() + 1) : getBtnEvent(axisTrigger, controller.getButtonCount() + 1);
            case DpadUp -> getBtnEvent(axisDpadY, controller.getButtonCount());
            case DpadDown -> getBtnEvent(axisDpadY, controller.getButtonCount() + 1);
            case DpadLeft -> getBtnEvent(axisDpadX, controller.getButtonCount());
            case DpadRight -> getBtnEvent(axisDpadX, controller.getButtonCount() + 1);
        };
    }
    
    public int getBtnEvent(int i) {
        if ( i < 0 ) return 0;
        return btnEvents[i];
    }

    public boolean getBtnState(int i) {
        if ( i < 0 ) return false;
        return btnStates[i];
    }
    
    private int getBtnEvent(int i, int j) {
        if ( i < 0 ) return 0;
        return btnEvents[i*2+j];
    }
    
    public ReadableVector2f getLeftStick() {
        //TODO we could clamp the steering to something like 120 distinct values so that the directional input is more stable.
        //custom deadzone that takes into account the length of the vector to determine if it should be zero. That way we can steer with full precision in 360° 
        //but ignore a poorly resting joystick.
        leftStick.x = axisLeftStickX >= 0 ? controller.getAxisValue(axisLeftStickX) : 0f;
        leftStick.y = axisLeftStickY >= 0 ? -controller.getAxisValue(axisLeftStickY) : 0f;
        if ( leftStick.lengthSquared() < joystickDeadzone ) {
            leftStick.x = 0;
            leftStick.y = 0;
        }
        return leftStick;
    }
    
    public boolean isLeftStickLeft() {
        return axisLeftStickX >= 0 ? controller.getAxisValue(axisLeftStickX) <= -axisBtnConversionDeadzone : false;
    }
    
    public boolean isLeftStickRight() {
        return axisLeftStickX >= 0 ? controller.getAxisValue(axisLeftStickX) >= axisBtnConversionDeadzone : false;
    }
    
    public boolean isLeftStickUp() {
        return axisLeftStickY >= 0 ? controller.getAxisValue(axisLeftStickY) <= -axisBtnConversionDeadzone : false;
    }
    
    public boolean isLeftStickDown() {
        return axisLeftStickY >= 0 ? controller.getAxisValue(axisLeftStickY) >= axisBtnConversionDeadzone : false;
    }
    
    public ReadableVector2f getRightStick() {
        rightStick.x = axisRightStickX >= 0 ? controller.getAxisValue(axisRightStickX) : 0f;
        rightStick.y = axisRightStickY >= 0 ? -controller.getAxisValue(axisRightStickY) : 0f;
        if ( rightStick.lengthSquared() < joystickDeadzone ) {
            rightStick.x = 0;
            rightStick.y = 0;
        }
        return rightStick;
    }
    
    public boolean isRightStickLeft() {
        return axisRightStickX >= 0 ? controller.getAxisValue(axisRightStickX) <= -axisBtnConversionDeadzone : false;
    }
    
    public boolean isRightStickRight() {
        return axisRightStickX >= 0 ? controller.getAxisValue(axisRightStickX) >= axisBtnConversionDeadzone : false;
    }
    
    public boolean isRightStickUp() {
        return axisRightStickY >= 0 ? controller.getAxisValue(axisRightStickY) >= axisBtnConversionDeadzone : false;
    }
    
    public boolean isRightStickDown() {
        return axisRightStickY >= 0 ? controller.getAxisValue(axisRightStickY) <= -axisBtnConversionDeadzone : false;
    }

    
    public ReadableVector2f getDpad() {
        //TODO we could clamp the steering to something like 120 distinct values so that the directional input is more stable.
        //custom deadzone that takes into account the length of the vector to determine if it should be zero. That way we can steer with full precision in 360° 
        //but ignore a poorly resting joystick.
        if(axisDpadX >= 0)
        {
            if(axisDpadX == 0xFF) dpad.x = controller.getPovX();
            else dpad.x = controller.getAxisValue(axisDpadX);
        }
        if(axisDpadY >= 0)
        {
            if(axisDpadY == 0xFF) dpad.y = controller.getPovY();
            else dpad.y = controller.getAxisValue(axisDpadY);
        }
        dpad.x = axisLeftStickX >= 0 ? controller.getAxisValue(axisLeftStickX) : 0f;
        dpad.y = axisLeftStickY >= 0 ? -controller.getAxisValue(axisLeftStickY) : 0f;
        return leftStick;
    }
    
    public boolean isDpadLeft() {
        var dpadVal = getDpad();
        return dpadVal.getX() < 0;
    }
    
    public boolean isDpadRight() {
        var dpadVal = getDpad();
        return dpadVal.getX() > 0;
    }
    
    public boolean isDpadkUp() {
        var dpadVal = getDpad();
        return dpadVal.getY() > 0;
    }
    
    public boolean isDpadDown() {
        var dpadVal = getDpad();
        return dpadVal.getY() < 0;
    }
    
    public float getTrigger() {
        return axisTrigger >= 0 ? controller.getAxisValue(axisTrigger) : 0f;
    }
    
    public boolean isTriggerLeft() {
        return axisTrigger >= 0 ? controller.getAxisValue(axisTrigger) >= axisBtnConversionDeadzone : false;
    }
    
    public boolean isTriggerRight() {
        if(axisRTrigger >= 0) {
            return controller.getAxisValue(axisRTrigger) >= axisBtnConversionDeadzone;
        } else {
            return axisTrigger >= 0 ? controller.getAxisValue(axisTrigger) <= -axisBtnConversionDeadzone : false;
        }
    }
    
    public boolean isButtonAPressed() {
        return btnA >= 0 ? controller.isButtonPressed(btnA) : false;
    }

    public boolean isButtonBPressed() {
        return btnB >= 0 ? controller.isButtonPressed(btnB) : false;
    }
    
    public boolean isButtonXPressed() {
        return btnX >= 0 ? controller.isButtonPressed(btnX) : false;
    }
    
    public boolean isButtonYPressed() {
        return btnY >= 0 ? controller.isButtonPressed(btnY) : false;
    }
    
    public boolean isButtonBumperLeftPressed() {
        return btnBumperLeft >= 0 ? controller.isButtonPressed(btnBumperLeft) : false;
    }
    
    public boolean isButtonBumperRightPressed() {
        return btnBumperRight >= 0 ? controller.isButtonPressed(btnBumperRight) : false;
    }
    
    public boolean isButtonStartPressed() {
        return btnStart >= 0 ? controller.isButtonPressed(btnStart) : false;
    }
    
    public boolean isButtonSelectPressed() {
        return btnSelect >= 0 ? controller.isButtonPressed(btnSelect) : false;
    }
    
    public boolean isButtonLeftStickPressed() {
        return btnLeftStick >= 0 ? controller.isButtonPressed(btnLeftStick) : false;
    }
    
    public boolean isButtonRightStickPressed() {
        return btnRightStick >= 0 ? controller.isButtonPressed(btnRightStick) : false;
    }
}
