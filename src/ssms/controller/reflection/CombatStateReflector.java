package ssms.controller.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.log4j.Level;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipSystemSpecAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.combat.CombatState;
import com.fs.starfarer.coreui.refit.returnsuper;
import com.fs.state.AppDriver;

import ssms.controller.SSMSControllerModPluginEx;

public class CombatStateReflector {
    
    Object mCombatStateSetVideoFeedSource = null;
    Object getZoomFactorMethod = null;
    MethodHandle showWarRoom = null;
    MethodHandle isAutoPilotOn = null;
    MethodHandle setAutoPilot = null;
    ZoomTrackerReflector zoomTracker;
    public Object cs;
    static CombatStateReflector instance;
    private CombatStateReflector()
    {
        cs = AppDriver.getInstance().getState("CombatState");

        try {
            showWarRoom = MethodHandles.lookup().findVirtual(cs.getClass(), "showWarroom", MethodType.methodType(void.class));
            
            setAutoPilot = MethodHandles.lookup().findVirtual(cs.getClass(), "setAutopilot", MethodType.methodType(void.class, boolean.class));
            
            isAutoPilotOn = MethodHandles.lookup().findVirtual(cs.getClass(), "isAutopilotOn", MethodType.methodType(boolean.class));

        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.FATAL, "Couldn't find essential methods of CombatState class!");
        }
    }

    public static CombatStateReflector GetInstance() 
    {
        if(instance == null) instance = new CombatStateReflector();

        return instance;
    }

    public void setAutoOmniShield() {
        try {
            Object field = ClassReflector.GetInstance().getDeclaredField(cs.getClass(), "AUTO_OMNI_SHIELDS");

            FieldReflector.GetInstance().SetVariable(field, cs, true);
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Couldn't set auto-omni shielding! " + ex.getMessage());
        }
    }

    protected boolean InitZoomTracker() {
        if (zoomTracker == null) {
            try {
                zoomTracker = new ZoomTrackerReflector();
            } catch (Throwable ex) {
                Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Failed to reflect zoom tracker, ensure SSMSUnlock is installed!", ex);
                zoomTracker = null;
                return false;
            }
        }
        return true;
    }
    
    protected boolean InitCombatState() {
        if ( mCombatStateSetVideoFeedSource == null ) {
            try {
                for(var m : ClassReflector.GetInstance().getDeclaredMethods(cs.getClass())) {
                    if ( MethodReflector.GetInstance().getName(m).equals("setVideoFeedSource") ) {
                        mCombatStateSetVideoFeedSource = m;
                        break;
                    }
                }
                getZoomFactorMethod = ClassReflector.GetInstance().getDeclaredMethod(cs.getClass(), "getZoomFactor");
                if ( mCombatStateSetVideoFeedSource == null ) return false;
            } catch (Throwable ex) {
                Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Failed to reflect combat state, ensure SSMSUnlock is installed!", ex);
                mCombatStateSetVideoFeedSource = null;
                return false;
            }
        }
        return true;
    }

    public void ShowWarRoom(){
        try {
            showWarRoom.invoke(cs);
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Couldnt show the war room!");
        }
    }

    public void ToggleAutoPilot() {
        try {
            boolean autoPilotState = (boolean)isAutoPilotOn.invoke(cs);
            setAutoPilot.invoke(cs, !autoPilotState);
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Failed to set auto-pilot state!");
        }
    }

    public float getZoomFactor() {
        try {
            return (float)MethodReflector.GetInstance().invoke(getZoomFactorMethod, cs);
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Couldn't fetch zoom factor! " + ex.getMessage());
        }
        return 1.0f;
    }

    public void setZoomFactor(float desiredZoomFactor)
    {
        if ( !InitCombatState() ) return;
        if ( !InitZoomTracker() ) return;

        if ( getZoomFactor() == desiredZoomFactor ) {
            return;
        }
        
        try {
            zoomTracker.SetZoom(cs, desiredZoomFactor);
        } catch (Throwable t) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Failed to adjust zoom tracker, ensure SSMSUnlock is installed!", t);
        }
    }

    public ShipwideAIFlags playerShipShieldAIFlags() {
        try {
            Object f = ClassReflector.GetInstance().getDeclaredField(cs.getClass(), "playerShipShieldAIFlags");
            
            return (ShipwideAIFlags) FieldReflector.GetInstance().GetVariable(f, cs);
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Couldn't fetch player ship shield AI flags!");
        }
        return null;
    }
    
    public void HideHud()
    {
        try {
            Object f = ClassReflector.GetInstance().getDeclaredField(CombatState.class, "hideHud");
            FieldReflector.GetInstance().SetVariable(f, cs, true);
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Failed to hide HUD! " + ex.getMessage());
        }
    }
    
    public void SetVideoFeedToPlayerShip() {
        cs.setVideoFeedSource(null);
        var methods = ClassReflector.GetInstance().getDeclaredMethods(cs.getViewMouseOffset().getClass());
        ArrayList<Object> matchingMethods = new ArrayList<>();
        for(int index = 0; index < methods.length; index++) {
            var fieldTypes = MethodReflector.GetInstance().getParameterTypes(methods[index]);
            if(fieldTypes.length == 2 && fieldTypes[0] == float.class && fieldTypes[1] == float.class) {
                matchingMethods.add(methods[index]);
            }
        }
        if(matchingMethods.size() == 2) {
            
            cs.getViewMouseOffset().\u00D300000(0.0F, 0.0F);
            cs.getViewMouseOffset().o00000(0.0F, 0.0F);
        }
    }
}
