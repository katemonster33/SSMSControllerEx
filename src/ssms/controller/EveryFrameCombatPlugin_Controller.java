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
package ssms.controller;

//import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.List;
//import org.apache.log4j.Level;

/**
 * Handles all controls for combat via game controller.
 * 
 * @author Malte Schulze
 */
public class EveryFrameCombatPlugin_Controller extends BaseEveryFrameCombatPlugin {
    protected CombatEngineAPI engine;
    protected float nextLog;
    protected boolean wasShowingWarroom = false, skipFrame = true;

    public EveryFrameCombatPlugin_Controller() {
        //Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "created every frame");
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        //Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "initialized every frame");
        this.engine = engine;
        nextLog = 0;
        skipFrame = true;
        
        if ( engine != null && engine.getContext() != null && (engine.isSimulation() || engine.getCombatUI() != null)
                && SSMSControllerModPluginEx.controller != null && SSMSControllerModPluginEx.controller.mapping != null ) {
            
        }
    }
    
    protected ShipAPI getControlledShip() {
        return engine.getPlayerShip();
    }
    
    protected boolean isControllerConfigured(HandlerController handler) {
        return handler != null && handler.mapping != null;
    }

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
        
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
    }
    
    @Override
    public void renderInUICoords(ViewportAPI viewport) {
        if ( skipFrame ) {
            /*Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, 
                "renderInUICoords");*/
            return;
        }
        super.renderInUICoords(viewport);
    }
}
