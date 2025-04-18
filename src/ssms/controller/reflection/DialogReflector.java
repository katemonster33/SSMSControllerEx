package ssms.controller.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class DialogReflector {
    static MethodHandle dismiss;
    static MethodHandle actionPerformed;
    static Class<?> dialogClass;
    static DialogReflector instance;

    public static DialogReflector TryGet(Object obj) {
        if(dialogClass != null && dialogClass == obj.getClass() || dialogClass == obj.getClass().getSuperclass()) {
            return instance;
        } else {
            var lookup = MethodHandles.lookup();
            var setOptionConfirm = lookup.findVirtual(obj.getClass(), "setOptionOnKeyboardConfirm", MethodType.methodType(void.class, int.class));
            var setOptionCancel = lookup.findVirtual(obj.getClass(), "setOptionOnKeyboardCancel", MethodType.methodType(void.class, int.class));
            if(setOptionConfirm != null && setOptionCancel != null) { // we likely found our dialog class, move forward
                dismiss = lookup.findVirtual(obj.getClass(), "dismiss", MethodType.methodType(void.class, int.class));
                actionPerformed = lookup.findVirtual(obj.getClass(), "actionPerformed", MethodType.methodType(void.class, Object.class, Object.class));
            }
        }
    }
}
