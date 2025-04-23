package ssms.controller.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.combat.CombatState;
import org.apache.log4j.Level;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.state.AppDriver;

import ssms.controller.SSMSControllerModPluginEx;

public class CombatStateReflector {
    
    Object mCombatStateSetVideoFeedSource = null;
    Object getZoomFactorMethod = null;
    MethodHandle showWarRoom = null;
    MethodHandle hideWarroom = null;
    MethodHandle isAutoPilotOn = null;
    MethodHandle setAutoPilot = null;
    Object getWidgetPanel = null;
    ZoomTrackerReflector zoomTracker;
    public Object cs;
    static CombatStateReflector instance;
    Object entityToFollowField;
    Object warroom;
    Object autoOmniShield = null;
    private CombatStateReflector()
    {
        cs = AppDriver.getInstance().getState(CombatState.STATE_ID);

        try {
            showWarRoom = MethodHandles.lookup().findVirtual(cs.getClass(), "showWarroom", MethodType.methodType(void.class));
            
            setAutoPilot = MethodHandles.lookup().findVirtual(cs.getClass(), "setAutopilot", MethodType.methodType(void.class, boolean.class));
            
            isAutoPilotOn = MethodHandles.lookup().findVirtual(cs.getClass(), "isAutopilotOn", MethodType.methodType(boolean.class));

            getWidgetPanel = ClassReflector.GetInstance().findDeclaredMethod(cs.getClass(), "getWidgetPanel");

            entityToFollowField = ClassReflector.GetInstance().getDeclaredField(cs.getClass(), "entityToFollow");

            warroom = ClassReflector.GetInstance().getDeclaredField(cs.getClass(), "warroom");

            hideWarroom = MethodHandles.lookup().findVirtual(cs.getClass(), "hideWarroom", MethodType.methodType(void.class));

            mCombatStateSetVideoFeedSource = ClassReflector.GetInstance().findDeclaredMethod(cs.getClass(), "setVideoFeedSource");

            getZoomFactorMethod = ClassReflector.GetInstance().getDeclaredMethod(cs.getClass(), "getZoomFactor");

            autoOmniShield = ClassReflector.GetInstance().getDeclaredField(cs.getClass(), "AUTO_OMNI_SHIELDS");
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.FATAL, "Couldn't find essential methods of CombatState class!");
        }
    }

    public static CombatStateReflector GetInstance() 
    {
        if(instance == null) instance = new CombatStateReflector();

        return instance;
    }

    public void clearEntityToFollow()
    {
        try {
            FieldReflector.GetInstance().SetVariable(entityToFollowField, cs, null);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't set the video feed to be the player ship!", ex);
        }
    }

    public UIPanelAPI getWarroom() {
        try {
            return (UIPanelAPI) FieldReflector.GetInstance().GetVariable(warroom, cs);
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Couldn't get warroom! " + ex.getMessage());
        }
        return null;
    }

    public UIPanelAPI getWidgetPanel()
    {
        try {
           return (UIPanelAPI)MethodReflector.GetInstance().invoke(getWidgetPanel, cs);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Could not fetch the widget panel of CombatState!", ex);
        }
        return null;
    }

    public void setAutoOmniShield() {
        try {
            FieldReflector.GetInstance().SetVariable(autoOmniShield, cs, true);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't set auto-omni shielding!", ex);
        }
    }
    
    public void SetVideoFeedToShipTarget(ShipAPI shipTarget) {
        try {
            MethodReflector.GetInstance().invoke(mCombatStateSetVideoFeedSource, cs, shipTarget);
        } catch (Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't set video feed source!! ", ex);
        }
    }

    protected boolean InitZoomTracker() {
        if (zoomTracker == null) {
            try {
                zoomTracker = new ZoomTrackerReflector(cs);
            } catch (Throwable ex) {
                Global.getLogger(getClass()).warn("Failed to reflect zoom tracker!", ex);
                zoomTracker = null;
                return false;
            }
        }
        return true;
    }

    public void ShowWarRoom(){
        try {
            showWarRoom.invoke(cs);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldnt show the war room!", ex);
        }
    }

    public void HideWarroom()
    {
        try {
            hideWarroom.invoke(cs);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Failed to hide warroom!", ex);
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
            Object f = ClassReflector.GetInstance().getDeclaredField(cs.getClass(), "hideHud");
            FieldReflector.GetInstance().SetVariable(f, cs, true);
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Failed to hide HUD! " + ex.getMessage());
        }
    }
    
    public void SetVideoFeedToPlayerShip() {
        //TODO IMPLEMENT THIS
        SetVideoFeedToShipTarget(Global.getCombatEngine().getPlayerShip());
//        try {
//            MethodReflector.GetInstance().invoke(mCombatStateSetVideoFeedSource, null);
//        } catch(Throwable ex) {
//            Global.getLogger(getClass()).warn("Couldn't set video feed!");
//        }
        //cs.setVideoFeedSource(null);
        // var methods = ClassReflector.GetInstance().getDeclaredMethods(cs.getViewMouseOffset().getClass());
        // ArrayList<Object> matchingMethods = new ArrayList<>();
        // for(int index = 0; index < methods.length; index++) {
        //     var fieldTypes = MethodReflector.GetInstance().getParameterTypes(methods[index]);
        //     if(fieldTypes.length == 2 && fieldTypes[0] == float.class && fieldTypes[1] == float.class) {
        //         matchingMethods.add(methods[index]);
        //     }
        // }
        // if(matchingMethods.size() == 2) {
            
        //     cs.getViewMouseOffset().\u00D300000(0.0F, 0.0F);
        //     cs.getViewMouseOffset().o00000(0.0F, 0.0F);
        // }
    }
}
