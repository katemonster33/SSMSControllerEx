package ssms.controller.enums;

/// the name of this enum is a little confusing. Buttons here means any value on a controller toggleable in an on-off way
public enum LogicalButtons {
    A, B, X, Y, BumperLeft, BumperRight, Start, Select, LeftStickButton, RightStickButton, RightStickUp, RightStickDown, RightStickLeft, RightStickRight,
    LeftStickUp, LeftStickDown, LeftStickLeft, LeftStickRight, LeftTrigger, RightTrigger, DpadLeft, DpadRight, DpadUp, DpadDown;

    public static LogicalButtons fromIndicator(Indicators indicators) {
        return switch (indicators) {
            case A -> LogicalButtons.A;
            case B -> LogicalButtons.B;
            case X -> LogicalButtons.X;
            case Y -> LogicalButtons.Y;
            case LeftStickDown -> LogicalButtons.LeftStickDown;
            case LeftStickUp -> LogicalButtons.LeftStickUp;
            case LeftStickLeft -> LogicalButtons.LeftStickLeft;
            case LeftStickRight -> LogicalButtons.LeftStickRight;
            case RightStickDown -> LogicalButtons.RightStickDown;
            case RightStickUp -> LogicalButtons.RightStickUp;
            case RightStickLeft -> LogicalButtons.RightStickLeft;
            case RightStickRight -> LogicalButtons.RightStickRight;
            case Select -> LogicalButtons.Select;
            case Start -> LogicalButtons.Start;
            case DPadUp -> LogicalButtons.DpadUp;
            case DPadDown -> LogicalButtons.DpadDown;
            case DPadLeft -> LogicalButtons.DpadLeft;
            case DPadRight -> LogicalButtons.DpadRight;
            case BumperLeft -> LogicalButtons.BumperLeft;
            case BumperRight -> LogicalButtons.BumperRight;
            case LeftTrigger -> LogicalButtons.LeftTrigger;
            case RightTrigger -> LogicalButtons.RightTrigger;
            default -> null;
        };
    }

    public static LogicalButtons fromMapping(ButtonMapping buttonMapping) {
        return switch(buttonMapping) {
            case A -> A;
            case B -> B;
            case Y -> Y;
            case X -> X;
            case Select -> Select;
            case Start -> Start;
            case L1 -> BumperLeft;
            case L2 -> LeftTrigger;
            case L3 -> LeftStickButton;
            case R1 -> BumperRight;
            case R2 -> RightTrigger;
            case R3 -> RightStickButton;
            case DPadLeft -> DpadLeft;
            case DPadRight -> DpadRight;
            case DPadDown -> DpadDown;
            case DPadUp -> DpadUp;
            default -> null;
        };
    }
}
