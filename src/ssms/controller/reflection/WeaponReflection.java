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
                Object obj = MethodReflector.GetInstance().invoke(method)
            }
            //((com.fs.starfarer.combat.systems.R)weapon).getAimTracker().new(targetLocation);
            ((com.fs.starfarer.combat.systems.OOOo)weapon).getAimTracker().Ó00000(targetLocation);
            //((com.fs.starfarer.combat.systems.R)weapon).getAimTracker().o00000(targetLocation);
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Failed to aim weapon! " + ex.getMessage());
        }
    }
}
