package ssms.controller.enums;

public enum AxisMapping {
    LeftStickX,
    LeftStickY,
    RightStickX,
    RightStickY,
    LeftTrigger,
    RightTrigger,
    DPadX,
    DPadY,
    Invalid;

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
}
