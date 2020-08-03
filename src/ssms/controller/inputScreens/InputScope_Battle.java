/*
 * Copyright (C) 2020 Malte Schulze.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library;  If not, see 
 * <https://www.gnu.org/licenses/>.
 */
package ssms.controller.inputScreens;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.combat.CombatState;
import com.fs.starfarer.util.oOOO;
import com.fs.state.AppDriver;
import java.lang.reflect.Field;
import org.apache.log4j.Level;
import ssms.controller.EveryFrameCombatPlugin_Controller;
import ssms.controller.HandlerController;
import ssms.controller.SSMSControllerModPlugin;
import ssms.controller.steering.SteeringController;
import ssms.controller.steering.SteeringController_FreeFlight;

/**
 *
 * @author Malte Schulze
 */
@InputScopeOption_ID("Battle")
@InputScopeOption_DefaultScreen("BattleSteering")
public class InputScope_Battle implements InputScope {
    public CombatEngineAPI engine;
    public CombatState cs;
    public PlayerShipCache psCache;
    private float desiredZoomFactor = 2f;
    private boolean controllerSteeringEnabled = true;
    private float offsetFacingAngle = 0f;
    
    /**
     * Preserves per ship settings during combat.
     */
    static public class PlayerShipCache {
        public ShipAPI ps;
        public boolean hasFighters;
        public SteeringController steeringController;

        public PlayerShipCache() {}
        
        public void setShip(ShipAPI ps, HandlerController gameController, CombatEngineAPI engine) {
            discard();
            this.ps = ps;
            this.hasFighters = ps.getLaunchBaysCopy().isEmpty();
            try {
                createSteeringController(SSMSControllerModPlugin.primarySteeringMode, ps, gameController, engine);
            } catch (InstantiationException | IllegalAccessException ex) {
                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Primary Steering Mode contains a controller without a puclic no argument constructor! Using fallback controller.", ex);
                if ( this.steeringController != null ) this.steeringController.discard();
                this.steeringController = new SteeringController_FreeFlight();
                this.steeringController.activate(ps, gameController, engine);
            }
        }
        
        public boolean isForShip(ShipAPI ship) {
            return ps != null && ps.equals(ship);
        }
        
        public void discard() {
            ps = null;
            hasFighters = false;
            if ( this.steeringController != null ) this.steeringController.discard();
            steeringController = null;
        }

        public void setSteeringController(Class steeringMode, HandlerController gameController, CombatEngineAPI engine) {
            try {
                createSteeringController(steeringMode, ps, gameController, engine);
            } catch (InstantiationException | IllegalAccessException ex) {
                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Steering Mode contains a controller without a puclic no argument constructor! Using fallback controller.", ex);
                if ( this.steeringController != null ) this.steeringController.discard();
                this.steeringController = new SteeringController_FreeFlight();
                this.steeringController.activate(ps, gameController, engine);
            }
        }
        
        private void createSteeringController(Class steeringMode, ShipAPI ship, HandlerController gameController, CombatEngineAPI engine) throws InstantiationException, IllegalAccessException {
            if ( this.steeringController != null ) {
                this.steeringController.discard();
                this.steeringController = null;
            }
            this.steeringController = (SteeringController) steeringMode.newInstance();
            if ( !this.steeringController.activate(ship, gameController, engine) ) throw new InstantiationException("Activation failed!");
        }
    }
    
    @Override
    public void activate(Object ...args) {
        engine = (CombatEngineAPI) args[0];
        cs = (CombatState) AppDriver.getInstance().getState(CombatState.STATE_ID);
        psCache = new PlayerShipCache();
    }

    @Override
    public void deactivate() {
        engine = null;
        cs = null;
        if ( psCache != null ) psCache.discard();
        psCache = null;
    }
    
    public boolean isValidTarget(ShipAPI ship) {
        return ship != null && !ship.isHulk() && ship.getOwner() != 100 && psCache.ps.getOwner() != ship.getOwner() && ship.isTargetable() && !ship.isDrone() && !ship.isFighter() && !ship.isShuttlePod();
    }
    
    public boolean isControllerSteeringEnabled() {
        return controllerSteeringEnabled;
    }
    
    public void setControllerSteeringEnabled(boolean enabled) {
        controllerSteeringEnabled = enabled;
    }
    
    public float getOffsetFacingAngle() {
        return offsetFacingAngle;
    }
    
    public void setOffsetFacingAngle(float angle) {
        this.offsetFacingAngle = angle;
    }
    
    public void timeDilation(boolean active, String id) {
        if ( engine == null ) return;
        id = "SSMSQoLTimeDilationController_"+id;
        MutableStat.StatMod timeDilationModifier = engine.getTimeMult().getMultStatMod(id);
        if ( active ) {
            if ( timeDilationModifier == null ) {
                engine.getTimeMult().modifyMult(id, 0f);
            }
        } else {
            if ( timeDilationModifier != null ) {
                engine.getTimeMult().unmodifyMult(id);
            }
        }
    }
    
    public void adjustZoom() {
        if ( cs.getZoomFactor() != desiredZoomFactor ) {
            try {
                Field f = CombatState.class.getDeclaredField("zoomTracker");
                if ( !f.isAccessible() ) f.setAccessible(true);
                oOOO zoomTracker = (oOOO) f.get(cs);
                f = oOOO.class.getDeclaredField("\u00D800000");
                if ( !f.isAccessible() ) f.setAccessible(true);
                f.set(zoomTracker, desiredZoomFactor);
                zoomTracker.Ó00000(desiredZoomFactor);
            } catch (Throwable t) {
                engine.getCombatUI().addMessage(0, "error: "+t.getMessage());
                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to adjust zoom tracker, ensure SSMSUnlock is installed!", t);
            }
        }
    }
    
    public void setZoom(float zoomFactor) {
        desiredZoomFactor = zoomFactor;
        adjustZoom();
    }
}
