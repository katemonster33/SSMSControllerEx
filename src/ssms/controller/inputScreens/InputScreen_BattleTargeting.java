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

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FogOfWarAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.combat.entities.Ship;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.HandlerController;
import ssms.controller.Indicators;
import ssms.controller.SSMSControllerModPluginEx;
import ssms.controller.reflection.CombatStateReflector;

/**
 *
 * @author Malte Schulze
 */
@InputScreenOption_ID("BattleTargeting")
@InputScreenOption_Scopes("Battle")
public class InputScreen_BattleTargeting implements InputScreen {
    protected HandlerController handler;
    protected InputScope_Battle scope;
    protected Targeting targeting;
    protected ShipAPI ps;
    protected InputScope_Battle.PlayerShipCache psCache;
    protected List<Pair<Indicators, String>> indicators;

    public InputScreen_BattleTargeting() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.BumperRight, "Next"));
        indicators.add(new Pair<>(Indicators.BumperLeft, "Previous"));
        indicators.add(new Pair<>(Indicators.Select, "Select"));
        indicators.add(new Pair<>(Indicators.Start, "Clear"));
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }
    
    /**
     * Preserves the targeting selection state.
     */
    static protected class Targeting {
        List<ShipAPI> targets;
        int index;

        public Targeting(List<ShipAPI> targets) {
            this.targets = targets;
            this.index = -1;
        }
        
        public void discard() {
            targets.clear();
        }
        
        public boolean hasTargets() {
            return targets.size() > 0;
        }
        
        public ShipAPI next() {
            if ( ++index >= targets.size() ) {
                index = 0;
            }
            return targets.get(index);
        }
        
        public ShipAPI previous() {
            if ( --index < 0 ) {
                index = targets.size() - 1;
            }
            return targets.get(index);
        }
    }

    @Override
    public void deactivate() {
        if ( targeting != null ) targeting.discard();
        targeting = null;
        scope.timeDilation(false,"TARGETING");
        CombatStateReflector.GetInstance().SetVideoFeedToPlayerShip();
        
        handler = null;
        scope = null;
        ps = null;
        psCache = null;
    }

    @Override
    public void activate(Object... args) {
        handler = SSMSControllerModPluginEx.controller;
        scope = (InputScope_Battle)InputScreenManager.getInstance().getCurrentScope();
        CombatEngineAPI engine = scope.engine;
        psCache = scope.psCache;
        ps = psCache.ps;
        targeting = new Targeting(targetsByDistance(engine.getShips(), ps.getLocation(), ps.getOwner() == 100 ? engine.getFogOfWar(0) : engine.getFogOfWar(ps.getOwner())));
        if ( targeting.hasTargets() ) {
            ps.setShipTarget((Ship) targeting.next());
            psCache.steeringController.onTargetSelected();
            scope.timeDilation(true,"TARGETING");
        }
    }
    
    @Override
    public void renderInWorld(ViewportAPI viewport) {
    }

    @Override
    public void renderUI(ViewportAPI viewport) {
    }
    
    protected void closeTargeting() {
        InputScreenManager.getInstance().transitionDelayed("BattleSteering");
    }
    
    @Override
    public void preInput(float advance) {
        if ( !targeting.hasTargets() ) {
            closeTargeting();
            return;
        }
        if ( handler.getButtonEvent(HandlerController.Buttons.BumperRight) == 1 ) {
            if ( targeting.hasTargets() ) {
                ps.setShipTarget((Ship) targeting.next());
                psCache.steeringController.onTargetSelected();
            }
        } else if ( handler.getButtonEvent(HandlerController.Buttons.BumperLeft) == 1 ) {
            if ( targeting.hasTargets() ) {
                ps.setShipTarget((Ship) targeting.previous());
                psCache.steeringController.onTargetSelected();
            }
        } else if ( handler.getButtonEvent(HandlerController.Buttons.Start) == 1 ) {
            if ( targeting != null ) targeting.discard();
            targeting = null;
            ps.setShipTarget(null);
            closeTargeting();
        } else if ( handler.getButtonEvent(HandlerController.Buttons.Select) == 1 ) {
            if ( targeting != null ) targeting.discard();
            targeting = null;
            closeTargeting();
        }
        
        //center on target
        CombatStateReflector.GetInstance().SetVideoFeedToShipTarget(ps.getShipTarget());
    }

    @Override
    public void postInput(float advance) {
    }
    
    protected List<ShipAPI> targetsByDistance(List<ShipAPI> ships, Vector2f measureFromPoint, FogOfWarAPI fogOfWar) {
        List<Pair<Float,ShipAPI>> shipsByDistance = new ArrayList<>();
        for (ShipAPI ship : ships) {
            if (scope.isValidTarget(ship) && fogOfWar.isVisible(ship)) {
                shipsByDistance.add(new Pair<>(Vector2f.sub(ship.getLocation(), measureFromPoint, new Vector2f()).lengthSquared(),ship));
            }
        }
        Collections.sort(shipsByDistance, new Comparator<Pair<Float, ShipAPI>>() {
            @Override
            public int compare(Pair<Float, ShipAPI> o1, Pair<Float, ShipAPI> o2) {
                return o1.one.compareTo(o2.one);
            }
        });
        List<ShipAPI> orderedShips = new ArrayList<>(shipsByDistance.size());
        for ( Pair<Float,ShipAPI> p : shipsByDistance ) orderedShips.add(p.two);
        return orderedShips;
    }
}
