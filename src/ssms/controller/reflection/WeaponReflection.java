package ssms.controller.reflection;

import org.apache.log4j.Level;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.WeaponAPI;

import ssms.controller.SSMSControllerModPluginEx;

public class WeaponReflection {

    public static void AimWeapon(WeaponAPI weapon, Vector2f targetLocation)
    {
        try {
            MethodReflector getAimTracker = new ClassReflector(weapon.getClass()).findDeclaredMethod("getAimTracker");
            if(getAimTracker != null) {
                MethodReflector setAimTargetMethod = null;
                Object aimTracker = getAimTracker.invoke(weapon);
                var methods = new ClassReflector(aimTracker.getClass()).getDeclaredMethods();
                for(MethodReflector m : methods) {
                    var paramTypes = m.getParameterTypes();
                    if(paramTypes.length == 1 && paramTypes[0] == Vector2f.class && m.getReturnType() == void.class)
                    {
                        setAimTargetMethod = m;
                        break;
                    }
                }
                if(setAimTargetMethod != null) {
                    setAimTargetMethod.invoke(aimTracker, targetLocation);
                } else {
                    Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Could not find aim tracker to aim weapon! :(");
                }
            }
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Failed to aim weapon! " + ex.getMessage());
        }
    }
}
