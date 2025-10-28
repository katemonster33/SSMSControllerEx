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
package ssms.controller.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;

import org.apache.log4j.Level;
import ssms.controller.HandlerController;
import ssms.controller.InputScopeBase;
import ssms.controller.SSMSControllerModPluginEx;
import ssms.controller.reflection.CombatStateReflector;
import ssms.controller.steering.SteeringController;
import ssms.controller.steering.SteeringController_FreeFlight;

/**
 *
 * @author Malte Schulze
 */
public class BattleScope extends InputScopeBase {
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
                createSteeringController(new SteeringController_FreeFlight(), ps, gameController, engine);
            } catch (Throwable ex) {
                if ( !"Activation failed!".equals(ex.getMessage()) )
                    Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Primary Steering Mode contains a controller without a public, no-argument constructor! Using fallback controller.", ex);
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

        public void setSteeringController(SteeringController steeringController, HandlerController gameController, CombatEngineAPI engine) {
            try {
                createSteeringController(steeringController, ps, gameController, engine);
            } catch (Throwable ex) {
                if ( !"Activation failed!".equals(ex.getMessage()) )
                    Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Steering Mode contains a controller without a public, no-argument constructor! Using fallback controller.", ex);
                if ( this.steeringController != null ) this.steeringController.discard();
                this.steeringController = new SteeringController_FreeFlight();
                this.steeringController.activate(ps, gameController, engine);
            }
        }
        
        private void createSteeringController(SteeringController steeringMode, ShipAPI ship, HandlerController gameController, CombatEngineAPI engine) throws Throwable {
            if ( this.steeringController != null ) {
                this.steeringController.discard();
                this.steeringController = null;
            }
            this.steeringController = steeringMode;
            if ( !this.steeringController.activate(ship, gameController, engine) ) throw new InstantiationException("Activation failed!");
        }
    }
    
    @Override
    public void activate(Object ...args) {
        if(args.length > 0) {
            engine = (CombatEngineAPI) args[0];
            psCache = new PlayerShipCache();
        } else {
            engine = Global.getCombatEngine();
        }
        csr = CombatStateReflector.GetInstance();
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
        id = "SSMSQoLTimeDilationController_" + id;
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
        csr.clearEntityToFollow();
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
