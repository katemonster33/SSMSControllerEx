package ssms.controller.enums;

public enum ButtonMapping {
    A,
    B,
    X,
    Y,
    Select,
    Start,
    L1,
    R1,
    L2,
    R2,
    L3,
    R3,
    Guide,
    DPadLeft,
    DPadRight,
    DPadUp,
    DPadDown;

    public static ButtonMapping fromButton(LogicalButtons logicalButtons) {
        return switch(logicalButtons) {
            case A -> A;
            case B -> B;
            case X -> X;
            case Y -> Y;
            case LeftStickButton -> L3;
            case RightStickButton -> R3;
            case Select -> Select;
            case Start -> Start;
            case BumperLeft -> L1;
            case BumperRight -> R1;
            case LeftTrigger -> L2;
            case RightTrigger -> R2;
            default -> null;
        };
    }
}
