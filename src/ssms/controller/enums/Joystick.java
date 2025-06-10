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

    public LogicalButtons[] getButtons() {
        return switch (this) {
            case Left ->
                    new LogicalButtons[]{LogicalButtons.LeftStickUp, LogicalButtons.LeftStickDown, LogicalButtons.LeftStickLeft, LogicalButtons.LeftStickRight};
            case Right ->
                    new LogicalButtons[]{LogicalButtons.RightStickUp, LogicalButtons.RightStickDown, LogicalButtons.RightStickLeft, LogicalButtons.RightStickRight};
            case DPad ->
                    new LogicalButtons[]{LogicalButtons.DpadUp, LogicalButtons.DpadDown, LogicalButtons.DpadLeft, LogicalButtons.DpadRight};
        };
    }
}
