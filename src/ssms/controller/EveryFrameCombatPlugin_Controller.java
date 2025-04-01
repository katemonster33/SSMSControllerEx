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

import com.fs.starfarer.api.Global;
//import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.combat.CombatState;
import com.fs.state.AppDriver;

import lunalib.backend.ui.components.base.LunaUIButton;
import lunalib.lunaUI.LunaUIUtils;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;

import java.awt.*;
//import org.apache.log4j.Level;

/**
 * Handles all controls for combat via game controller.
 * 
 * @author Malte Schulze
 */
public class EveryFrameCombatPlugin_Controller extends BaseEveryFrameCombatPlugin {
    protected CombatEngineAPI engine;
    protected float nextLog;
    public static Robot T1000 = null;
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
        try {
            T1000 = new Robot();
        } catch(AWTException awte) {
            Global.getLogger(getClass()).log(Level.ERROR, "Failed to initialize the robot, mod cannot function!");
        }
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
        if(SSMSControllerModPluginEx.controller != null) {
            SSMSControllerModPluginEx.controller.poll();
            switch(Global.getCurrentState())
            {
                case TITLE:
                    if(SSMSControllerModPluginEx.controller.isButtonAPressed()) {
                        T1000.mouseMove(200, 100);
                    } else if(SSMSControllerModPluginEx.controller.isButtonBPressed()) {
                        T1000.mouseMove(250, 80);
                    } else if(SSMSControllerModPluginEx.controller.isButtonYPressed()) {
                        T1000.mouseMove(300, 80);
                    } else if(SSMSControllerModPluginEx.controller.isButtonXPressed()) {
                        T1000.mouseMove(350, 100);
                    }
                    break;
                case CAMPAIGN:
                    //Global.getSector().
                    break;
                case COMBAT:
                    break;

            }
        }
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
