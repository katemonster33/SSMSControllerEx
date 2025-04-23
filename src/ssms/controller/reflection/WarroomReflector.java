package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.coreui.P;

public class WarroomReflector {
    static Class<?> warroomClass;
    Object warroomObject;

    Object processInputImpl;

    public WarroomReflector(Object warroomObj) {
        this.warroomObject = warroomObj;
        try {
            this.processInputImpl = ClassReflector.GetInstance().findDeclaredMethod(warroomObj.getClass(), "processInputImpl");

            var processInputTypes = MethodReflector.GetInstance().getParameterTypes(processInputImpl);
            InputEventReflector.initializeFromListType(processInputTypes[0]);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Could not reflect into input event list type!");
        }
    }


    public void processInputImpl(Object inputsList) {
        try {
            MethodReflector.GetInstance().invoke(processInputImpl, warroomObject, inputsList);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Failed to send fake inputs to warroom screen!", ex);
        }
    }

}
