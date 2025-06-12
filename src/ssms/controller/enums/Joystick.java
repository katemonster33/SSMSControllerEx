package ssms.controller.enums;

public enum Joystick {
    Left,
    Right,
    DPad;

    public static Joystick fromButton(LogicalButtons logicalButtons) {
        return switch(logicalButtons) {
            case LeftStickLeft, LeftStickRight, LeftStickUp, LeftStickDown -> Left;
            case RightStickLeft, RightStickRight, RightStickUp, RightStickDown -> Right;
            case DpadLeft, DpadRight, DpadUp, DpadDown -> DPad;
            default -> null;
        };
    }

    public static Joystick fromAxisMapping(AxisMapping axisMapping) {
        return switch (axisMapping) {
            case LeftStickX, LeftStickY -> Left;
            case RightStickX, RightStickY -> Right;
            case DPadX, DPadY -> DPad;
            default -> null;
        };
    }

    public AxisMapping getXAxisMapping() {
        return switch (this) {
            case Left -> AxisMapping.LeftStickX;
            case Right -> AxisMapping.RightStickX;
            case DPad -> AxisMapping.DPadX;
        };
    }

    public AxisMapping getYAxisMapping() {
        return switch (this) {
            case Left -> AxisMapping.LeftStickY;
            case Right -> AxisMapping.RightStickY;
            case DPad -> AxisMapping.DPadY;
        };
    }
}
