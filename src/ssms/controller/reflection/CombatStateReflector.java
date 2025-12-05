package ssms.controller.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.combat.CombatState;
import org.apache.log4j.Level;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.state.AppDriver;

import ssms.controller.SSMSControllerModPluginEx;

public class CombatStateReflector {
    
    MethodReflector mCombatStateSetVideoFeedSource = null;
    MethodReflector getZoomFactorMethod = null;
    MethodHandle showWarRoom = null;
    MethodHandle hideWarroom = null;
    MethodHandle isAutoPilotOn = null;
    MethodHandle setAutoPilot = null;
    MethodReflector getWidgetPanel = null;
    FieldReflector zoomTrackerField;
    ZoomTrackerReflector zoomTracker;
    public Object cs;
    static CombatStateReflector instance;
    FieldReflector entityToFollowField;
    FieldReflector warroom;
    FieldReflector deploymentDialog;
    FieldReflector autoOmniShield = null;
    FieldReflector playerShipShieldAIFlags;
    private CombatStateReflector()
    {
        cs = AppDriver.getInstance().getState(CombatState.STATE_ID);

        try {
            showWarRoom = MethodHandles.lookup().findVirtual(cs.getClass(), "showWarroom", MethodType.methodType(void.class));
            
            setAutoPilot = MethodHandles.lookup().findVirtual(cs.getClass(), "setAutopilot", MethodType.methodType(void.class, boolean.class));
            
            isAutoPilotOn = MethodHandles.lookup().findVirtual(cs.getClass(), "isAutopilotOn", MethodType.methodType(boolean.class));
            ClassReflector csReflector = new ClassReflector(cs.getClass());

            getWidgetPanel = csReflector.findDeclaredMethod("getWidgetPanel");

            entityToFollowField = csReflector.getDeclaredField("entityToFollow");

            warroom = csReflector.getDeclaredField("warroom");

            hideWarroom = MethodHandles.lookup().findVirtual(cs.getClass(), "hideWarroom", MethodType.methodType(void.class));

            mCombatStateSetVideoFeedSource = csReflector.findDeclaredMethod("setVideoFeedSource");

            getZoomFactorMethod = csReflector.getDeclaredMethod("getZoomFactor");

            autoOmniShield = csReflector.getDeclaredField("AUTO_OMNI_SHIELDS");

            deploymentDialog = csReflector.getDeclaredField("fdd");

            playerShipShieldAIFlags = csReflector.getDeclaredField("playerShipShieldAIFlags");

            zoomTrackerField = csReflector.getDeclaredField("zoomTracker");
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.FATAL, "Couldn't find essential methods of CombatState class!");
        }
    }

    public static CombatStateReflector GetInstance() 
    {
        if(instance == null) instance = new CombatStateReflector();

        return instance;
    }

    public static  CombatStateReflector newInstance() {
        return instance = new CombatStateReflector();
    }

    public void clearEntityToFollow()
    {
        entityToFollowField.set(cs, null);
    }

    public UIPanelAPI getWarroom() {
        return (UIPanelAPI) warroom.get(cs);
    }

    public UIPanelAPI getDeploymentDialog() {
        return (UIPanelAPI) deploymentDialog.get(cs);
    }

    public UIPanelAPI getWidgetPanel()
    {
       return (UIPanelAPI)getWidgetPanel.invoke(cs);
    }

    public void setAutoOmniShield() {
        autoOmniShield.set(cs, true);
    }
    
    public void SetVideoFeedToShipTarget(ShipAPI shipTarget) {
        mCombatStateSetVideoFeedSource.invoke(cs, shipTarget);
    }

    protected boolean InitZoomTracker() {
        if (zoomTracker == null) {
            try {
                zoomTracker = new ZoomTrackerReflector(zoomTrackerField.get(cs));
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
        return (float)getZoomFactorMethod.invoke(cs);
    }

    public ZoomTrackerReflector getZoomTracker() {
        if (!InitZoomTracker()) return null;

        return zoomTracker;
    }

    public void setZoomFactor(float desiredZoomFactor)
    {
        if (!InitZoomTracker()) return;

        if ( getZoomFactor() == desiredZoomFactor ) {
            return;
        }
        
        try {
            zoomTracker.setZoom(desiredZoomFactor);
        } catch (Throwable t) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Failed to adjust zoom tracker, ensure SSMSUnlock is installed!", t);
        }
    }

    public ShipwideAIFlags playerShipShieldAIFlags() {
        return (ShipwideAIFlags) playerShipShieldAIFlags.get(cs);
    }
    
//    public void HideHud()
//    {
//        try {
//            Object f = ClassReflector.GetInstance().getDeclaredField(cs.getClass(), "hideHud");
//            FieldReflector.GetInstance().SetVariable(f, cs, true);
//        } catch(Throwable ex) {
//            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.WARN, "Failed to hide HUD! " + ex.getMessage());
//        }
//    }
    
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
