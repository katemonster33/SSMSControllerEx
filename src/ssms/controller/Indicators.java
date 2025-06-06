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

/**
 *
 * @author Malte Schulze
 */
public enum Indicators {
    A,B,X,Y,BumperLeft,BumperRight,Start,Select,LeftStickButton,RightStickButton,RightStickUp,RightStickDown,RightStickLeft,RightStickRight,
    LeftStickUp,LeftStickDown,LeftStickLeft,LeftStickRight,LeftStick,RightStick, LeftTrigger, RightTrigger, DPadUp, DPadDown, DPadLeft, DPadRight, DPad;

    public static Indicators fromButton(Buttons btn)
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
            case DpadUp -> Indicators.DPadUp;
            case DpadDown -> Indicators.DPadDown;
            case DpadLeft -> Indicators.DPadLeft;
            case DpadRight -> Indicators.DPadRight;
            default -> null;
        };
    }
}
