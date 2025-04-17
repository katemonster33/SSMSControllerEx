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

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.combat.CombatState;

import ssms.controller.campaign.CampaignControllerListener;
import ssms.controller.combat.BattleScope;

import java.util.List;

import org.apache.log4j.Level;
import ssms.controller.titlescreen.AutoMapperUI;
import ssms.controller.titlescreen.TitleScreenUI;
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
    boolean initDone = false;
    public EveryFrameCombatPlugin_Controller() {
        //Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "created every frame");
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        //Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "initialized every frame");
        this.engine = engine;
        CampaignControllerListener.pluginActive = false;
        nextLog = 0;
        skipFrame = true;
        if(Global.getCurrentState() == GameState.TITLE) {
            if(SSMSControllerModPluginEx.controller != null) {
                if(SSMSControllerModPluginEx.controller.mapping == null) {
                    if(!InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, AutoMapperUI.ID, new Object[]{})) {
                        Global.getLogger(SSMSControllerModPluginEx.class).fatal("Failed to transition to AutoMapper UI!");
                    } else {
                        skipFrame = false;
                        initDone = true;
                    }
                } else {
                    if(!InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, TitleScreenUI.ID, new Object[]{})) {
                        Global.getLogger(SSMSControllerModPluginEx.class).fatal("Failed to transition to title screen UI!");
                    } else {
                        skipFrame = false;
                        initDone = true;
                    }
                }
            } else Global.getLogger(getClass()).warn("No controllers loaded!");
        } else if ( engine != null && engine.getContext() != null && (engine.isSimulation() || (engine.getCombatUI() != null && CombatState.class.isAssignableFrom(engine.getCombatUI().getClass())))
        && SSMSControllerModPluginEx.controller != null && SSMSControllerModPluginEx.controller.mapping != null ) {
            if ( !InputScreenManager.getInstance().transitionToScope(BattleScope.ID, engine) ) {
                Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Failed to transition into battle scope!");
                InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID);
            } else {
                initDone = true;
                skipFrame = false;
            }
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
        if ( skipFrame ) return;
        if(!initDone) {
            init(Global.getCombatEngine());
            skipFrame = false;
            return;
        }
        InputScreenManager man = InputScreenManager.getInstance();
        HandlerController handler = SSMSControllerModPluginEx.controller;
        handler.poll();
        if(BattleScope.class.isAssignableFrom(man.getCurrentScope().getClass())) {
            BattleScope battleScope = (BattleScope) man.getCurrentScope();

            if (!battleScope.engine.getCombatUI().isShowingCommandUI()) {
                if (wasShowingWarroom) {
                    battleScope.adjustZoom();
                }

                ShipAPI ps = getControlledShip();
                if (ps != null && battleScope.engine.isEntityInPlay(ps)) {
                    if (!battleScope.psCache.isForShip(ps)) {
                        battleScope.psCache.setShip(ps, handler, battleScope.engine);
                    }
                }
            }

            wasShowingWarroom = battleScope.engine.getCombatUI().isShowingCommandUI();
        }
        
        //TODO inputs for the warroom
        //TODO menu entries for switching ships(camera jumps to the targeted eligeble ship like targeting next and previous then selecting to pick a ship)
        //TODO menu entry for ending combat/simulation
            
        man.startFrame();
        man.preInput(amount);

    }
    // UIPanelAPI getScreenPanel()
    // {
    //     TitleScreenState titlescreen  = (TitleScreenState)AppDriver.getInstance().getCurrentState();

    //     var methodClass = Class.forName("java.lang.reflect.Method", false, ClassLoader.getSystemClassLoader());
    //     var getNameMethod = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String.class));
    //     var invokeMethod = MethodHandles.lookup().findVirtual(methodClass, "invoke", MethodType.methodType(Object.class, Object.class, Object[].class));
        
    //     Method foundMethod = null;
    //     var methods = titlescreen.getClass().getMethods();
    //     for(int index = 0; index < methods.length; index++)
    //     {
    //         if (getNameMethod.invoke(methods[index]) == "getScreenPanel")
    //         {
    //             foundMethod = methods[index];
    //         }
    //     }

    //     return (UIPanelAPI)invokeMethod.invoke(foundMethod, titlescreen);
    // }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if ( skipFrame ) {
            /*Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, 
                "advance");*/
            return;
        }
        switch(Global.getCurrentState())
        {
            case COMBAT:
                break;
            case TITLE:

                // TitleScreenState titleScreen = (TitleScreenState) AppDriver.getInstance().getCurrentState();
                // Object panel = titleScreen.getScreenPanel();
                // ClassReflector.GetInstance().getDeclaredMethod(panel.getClass(), "" null, null)
            case CAMPAIGN:
                break;
        }
        super.advance(amount, events);
        InputScreenManager.getInstance().postIntput(amount);
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        if ( skipFrame ) {
            /*Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, 
                "renderInWorldCoords");*/
            return;
        }
        super.renderInWorldCoords(viewport);
        InputScreenManager.getInstance().renderInWorld(viewport);
    }
    
    @Override
    public void renderInUICoords(ViewportAPI viewport) {
        if ( skipFrame ) {
            /*Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, 
                "renderInUICoords");*/
            return;
        }
        super.renderInUICoords(viewport);
        InputScreenManager.getInstance().renderUI(viewport);
        InputScreenManager.getInstance().stopFrame();
    }
}
