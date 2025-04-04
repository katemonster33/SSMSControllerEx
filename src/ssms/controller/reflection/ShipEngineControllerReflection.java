package ssms.controller.reflection;


import com.fs.starfarer.api.Global;

import ssms.controller.SSMSControllerModPluginEx;

public class ShipEngineControllerReflection {
    static protected Object mGetEffectiveStrafeAcceleration;
    static protected boolean initialized = false;

    static public float getEffectiveStrafeAcceleration(Object original) {
        if ( !initialized ) {
            try {
                Class<?> c = original.getClass(); //Class.forName("com.fs.starfarer.combat.entities.ship.H");
                mGetEffectiveStrafeAcceleration = ClassReflector.GetInstance().getDeclaredMethod(c, "getEffectiveStrafeAcceleration");
            } catch (Throwable ex) {
                Global.getLogger(SSMSControllerModPluginEx.class).log(org.apache.log4j.Level.ERROR, "Failed to find methods for " + original.getClass().getSimpleName() + "!", ex);
            }
        }
        try {
            return (float) MethodReflector.GetInstance().invoke(mGetEffectiveStrafeAcceleration, original);
        } catch (Throwable ex) {

        }
        return 0f;
    }
}
