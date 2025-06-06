package ssms.controller;

public enum Buttons {
    A, B, X, Y, BumperLeft, BumperRight, Start, Select, LeftStickButton, RightStickButton, RightStickUp, RightStickDown, RightStickLeft, RightStickRight,
    LeftStickUp, LeftStickDown, LeftStickLeft, LeftStickRight, LeftTrigger, RightTrigger, DpadLeft, DpadRight, DpadUp, DpadDown;

    public static Buttons fromIndicator(Indicators indicators) {
        return switch (indicators) {
            case A -> Buttons.A;
            case B -> Buttons.B;
            case X -> Buttons.X;
            case Y -> Buttons.Y;
            case LeftStickDown -> Buttons.LeftStickDown;
            case LeftStickUp -> Buttons.LeftStickUp;
            case LeftStickLeft -> Buttons.LeftStickLeft;
            case LeftStickRight -> Buttons.LeftStickRight;
            case RightStickDown -> Buttons.RightStickDown;
            case RightStickUp -> Buttons.RightStickUp;
            case RightStickLeft -> Buttons.RightStickLeft;
            case RightStickRight -> Buttons.RightStickRight;
            case Select -> Buttons.Select;
            case Start -> Buttons.Start;
            case DPadUp -> Buttons.DpadUp;
            case DPadDown -> Buttons.DpadDown;
            case DPadLeft -> Buttons.DpadLeft;
            case DPadRight -> Buttons.DpadRight;
            case BumperLeft -> Buttons.BumperLeft;
            case BumperRight -> Buttons.BumperRight;
            case LeftTrigger -> Buttons.LeftTrigger;
            case RightTrigger -> Buttons.RightTrigger;
            default -> null;
        };
    }
}
