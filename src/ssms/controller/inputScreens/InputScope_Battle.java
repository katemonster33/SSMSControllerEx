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

import org.apache.log4j.Level;
import ssms.controller.HandlerController;
import ssms.controller.SSMSControllerModPluginEx;
import ssms.controller.reflection.CombatStateReflector;
import ssms.controller.reflection.FieldReflector;
import ssms.controller.steering.SteeringController;
import ssms.controller.steering.SteeringController_FreeFlight;

/**
 *
 * @author Malte Schulze
 */
public class InputScope_Battle implements InputScope {
    public static final String ID = "Battle";
    public static final String DEFAULT_SCREEN = "BattleSteering";
    public CombatEngineAPI engine;
    public CombatStateReflector csr;
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
                createSteeringController(SteeringController_FreeFlight.class, ps, gameController, engine);
            } catch (Throwable ex) {
                if ( !"Activation failed!".equals(ex.getMessage()) )
                    Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Primary Steering Mode contains a controller without a puclic no argument constructor! Using fallback controller.", ex);
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

        public void setSteeringController(Class<?> steeringMode, HandlerController gameController, CombatEngineAPI engine) {
            try {
                createSteeringController(steeringMode, ps, gameController, engine);
            } catch (Throwable ex) {
                if ( !"Activation failed!".equals(ex.getMessage()) )
                    Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Steering Mode contains a controller without a puclic no argument constructor! Using fallback controller.", ex);
                if ( this.steeringController != null ) this.steeringController.discard();
                this.steeringController = new SteeringController_FreeFlight();
                this.steeringController.activate(ps, gameController, engine);
            }
        }
        
        private void createSteeringController(Class<?> steeringMode, ShipAPI ship, HandlerController gameController, CombatEngineAPI engine) throws Throwable {
            if ( this.steeringController != null ) {
                this.steeringController.discard();
                this.steeringController = null;
            }
            this.steeringController = (SteeringController) steeringMode.getDeclaredConstructor().newInstance();
            if ( !this.steeringController.activate(ship, gameController, engine) ) throw new InstantiationException("Activation failed!");
        }
    }
    
    @Override
    public void activate(Object ...args) {
        engine = (CombatEngineAPI) args[0];
        csr = CombatStateReflector.GetInstance();
        psCache = new PlayerShipCache();
    }

    @Override
    public void deactivate() {
        engine = null;
        csr = null;
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
        try {
        FieldReflector.GetInstance().SetVariable("entityToFollow", csr.cs, null);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).log(Level.WARN, "Couldn't set the video feed to be the player ship!");
        }
        csr.setZoomFactor(desiredZoomFactor);
    }
    
    public void setZoom(float zoomFactor) {
        desiredZoomFactor = zoomFactor;
        adjustZoom();
    }

    @Override
    public String getId() { return ID; }

    @Override
    public String getDefaultScreen() { return DEFAULT_SCREEN; }
}
