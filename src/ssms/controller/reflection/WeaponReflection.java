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
            var weaponClass = weapon.getClass();
            Object getAimTracker = null;
            for(Object method : ClassReflector.GetInstance().getDeclaredMethods(weaponClass)) {
                if(MethodReflector.GetInstance().getName(method) == "getAimTracker") {
                    getAimTracker = method;
                    break;
                }
            }
            if(getAimTracker != null) {
                Object setAimTargetMethod = null;
                Object aimTracker = MethodReflector.GetInstance().invoke(getAimTracker, weaponClass);
                var methods = ClassReflector.GetInstance().getDeclaredMethods(aimTracker.getClass());
                for(Object m : methods) {
                    var paramTypes = MethodReflector.GetInstance().getParameterTypes(m);
                    var retType = MethodReflector.GetInstance().getReturnType(m);
                    if(paramTypes.length == 1 && paramTypes[0] == Vector2f.class && retType == void.class)
                    {
                        setAimTargetMethod = m;
                        break;
                    }
                }
                if(setAimTargetMethod != null) {
                    MethodReflector.GetInstance().invoke(aimTracker, targetLocation);
                } else {
                    Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Could not find aim tracker to aim weapon! :(");
                }
            }
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Failed to aim weapon! " + ex.getMessage());
        }
    }
}
