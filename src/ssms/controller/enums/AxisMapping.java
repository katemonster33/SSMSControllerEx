package ssms.controller.enums;

public enum AxisMapping {
    LeftStickX,
    LeftStickY,
    RightStickX,
    RightStickY,
    LeftTrigger,
    RightTrigger,
    DPadX,
    DPadY;

    public static AxisMapping fromButton(LogicalButtons logicalButtons) {
        return switch(logicalButtons) {
            case LeftStickLeft, LeftStickRight -> LeftStickX;
            case LeftStickUp, LeftStickDown -> LeftStickY;
            case RightStickLeft, RightStickRight -> RightStickX;
            case RightStickUp, RightStickDown -> RightStickY;
            case LeftTrigger -> LeftTrigger;
            case RightTrigger -> RightTrigger;
            case DpadLeft, DpadRight -> DPadX;
            case DpadUp, DpadDown -> DPadY;
            default -> null;
        };
    }

    public LogicalButtons getUpperLimitButton() {
        return switch(this) {
            case LeftStickX -> LogicalButtons.LeftStickRight;
            case LeftStickY -> LogicalButtons.LeftStickUp;
            case RightStickX -> LogicalButtons.RightStickRight;
            case RightStickY -> LogicalButtons.RightStickUp;
            case LeftTrigger -> LogicalButtons.LeftTrigger;
            case RightTrigger -> LogicalButtons.RightTrigger;
            case DPadX -> LogicalButtons.DpadRight;
            case DPadY -> LogicalButtons.DpadUp;
        };
    }

    public LogicalButtons getLowerLimitButton() {
        return switch(this) {
            case LeftStickX -> LogicalButtons.LeftStickLeft;
            case LeftStickY -> LogicalButtons.LeftStickDown;
            case RightStickX -> LogicalButtons.RightStickLeft;
            case RightStickY -> LogicalButtons.RightStickDown;
            case LeftTrigger -> LogicalButtons.LeftTrigger;
            case RightTrigger -> LogicalButtons.RightTrigger;
            case DPadX -> LogicalButtons.DpadLeft;
            case DPadY -> LogicalButtons.DpadDown;
        };
    }
}
