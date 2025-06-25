package ssms.controller.reflection;


import com.fs.starfarer.api.Global;

import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import ssms.controller.SSMSControllerModPluginEx;

public class ShipEngineControllerReflection {
    static protected MethodReflector mGetEffectiveStrafeAcceleration;
    static protected boolean initialized = false;

    static public float getEffectiveStrafeAcceleration(ShipEngineControllerAPI original) {
        if ( !initialized ) {
            Class<?> c = original.getClass(); //Class.forName("com.fs.starfarer.combat.entities.ship.H");
            mGetEffectiveStrafeAcceleration = new ClassReflector(c).getDeclaredMethod("getEffectiveStrafeAcceleration");
            if(mGetEffectiveStrafeAcceleration == null) {
                Global.getLogger(SSMSControllerModPluginEx.class).error("Failed to find methods for " + original.getClass().getSimpleName() + "!");
            }
        }
        return (float) mGetEffectiveStrafeAcceleration.invoke(original);
    }
}
