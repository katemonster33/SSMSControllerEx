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

import lunalib.lunaSettings.LunaSettings;
import ssms.controller.inputScreens.InputScope_360;
import ssms.controller.inputScreens.InputScope_Battle;
import ssms.controller.inputScreens.InputScreenManager;
import ssms.controller.inputScreens.InputScreen_BattleMenu;
import ssms.controller.inputScreens.InputScreen_BattleSteering;
import ssms.controller.inputScreens.InputScreen_BattleTargeting;
import ssms.controller.inputScreens.InputScreen_Bluescreen;
import ssms.controller.steering.SteeringController_FreeFlight;
import ssms.controller.steering.SteeringController_OrbitTarget;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import com.fs.starfarer.api.graphics.SpriteAPI;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

/**
 * Sets up configuration and application scoped persistency for the mod.
 * 
 * @author Malte Schulze
 */
public final class SSMSControllerModPluginEx extends BaseModPlugin {
    public static final String modId = "SSMSControllerEx";
    static public HandlerController controller = new HandlerController();
    static public List<Class<?>> registeredSteeringController = new ArrayList<>();
    static public HashMap<String, ControllerMapping> controllerMappings;
    static public EnumMap<Indicators,SpriteAPI> defaultIndicators;
    
    @Override
    public void onApplicationLoad() throws Exception {
        LunaSettings.getString(modId, "controllerMapping");
        // ControllerMapping xbox360 = new ControllerMapping();
        // xbox360.btnA = 0;
        // xbox360.btnB = 1;
        // xbox360.btnX = 2;
        // xbox360.btnY = 3;
        // xbox360.btnBumperLeft = 4;
        // xbox360.btnBumperRight = 5;
        // xbox360.btnSelect = 6;
        // xbox360.btnStart = 7;
        // xbox360.btnLeftStick = 8;
        // xbox360.btnRightStick = 9;
        // xbox360.axisLeftStickX = "X Axis";
        // xbox360.axisLeftStickY = "Y Axis";
        // xbox360.axisRightStickX = "X Rotation";
        // xbox360.axisRightStickY = "Y Rotation";
        // xbox360.axisTrigger = "Z Axis";
        // xbox360.axisBtnConversionDeadzone = 0.85f;
        // xbox360.joystickDeadzone = 0.25f;
        // xbox360.deviceName = "Controller (XBOX 360 For Windows)(5,10)";
        // xbox360.indicators = new EnumMap<>(Indicators.class);
        JSONObject obj = Global.getSettings().loadJSON("data/config/settings.json", "SSMSControllerEx");
        if(obj == null) {
            Global.getLogger(this.getClass()).log(Level.DEBUG, "no settings!!!");
            return;
        }
        controllerMappings = configureControllerMappings(obj.getJSONObject("controllerMappings"));
        var indicatorsByController = configureSettingsApplicationController(obj.getJSONObject("graphics"));
        var xbox360Indicators = indicatorsByController.get("xbox360");
        var xbox360 = controllerMappings.get("Controller (XBOX 360 For Windows)(5,10)");
        if(xbox360Indicators != null && xbox360 != null) {
            xbox360.indicators = xbox360Indicators;
        } 
        defaultIndicators = xbox360.indicators;
        
        // if ( controllerMappings == null ) controllerMappings = new ArrayList<>();
        // controllerMappings.add(xbox360);
        reconnectController();
        
        
        registeredSteeringController.add(SteeringController_FreeFlight.class);
        registeredSteeringController.add(SteeringController_OrbitTarget.class);
        
        InputScreenManager man = InputScreenManager.getInstance();
        
        man.registerScope(InputScope_360.class);
        man.registerScope(InputScope_Battle.class);
        
        man.registerScreen(InputScreen_Bluescreen.class);
        man.registerScreen(InputScreen_BattleSteering.class);
        man.registerScreen(InputScreen_BattleTargeting.class);
        man.registerScreen(InputScreen_BattleMenu.class);
        
        man.updateIndicators();
    }

    protected HashMap<String, ControllerMapping> configureControllerMappings(JSONObject controllerMappingsObj) {
        HashMap<String, ControllerMapping> output = new HashMap<>();
        try {
            for (int conIdx = 0; conIdx < controllerMappingsObj.length(); conIdx++) {
                String deviceName = controllerMappingsObj.names().getString(conIdx);
                JSONObject deviceMappings = controllerMappingsObj.getJSONObject(deviceName);
                if(deviceMappings == null) {
                    Global.getLogger(getClass()).log(Level.ERROR, "Failed to parse controller mappings for " + deviceName);
                } else {
                    ControllerMapping newMapping = new ControllerMapping();
                    newMapping.deviceName = deviceName;
                    JSONArray deviceButtons = deviceMappings.getJSONArray("buttons");

                    if(deviceButtons != null) {
                        for(int btnIdx = 0; btnIdx < deviceButtons.length(); btnIdx++) {
                            String btnName = deviceButtons.getString(btnIdx);

                            switch(btnName)
                            {
                                case "A":                   newMapping.btnA = btnIdx; break;
                                case "B":                   newMapping.btnB = btnIdx; break;
                                case "X":                   newMapping.btnX = btnIdx; break;
                                case "Y":                   newMapping.btnY = btnIdx; break;
                                case "BumperLeft":          newMapping.btnBumperLeft = btnIdx; break;
                                case "BumperRight":         newMapping.btnBumperRight = btnIdx; break;
                                case "Select" :             newMapping.btnSelect = btnIdx; break;
                                case "Start" :              newMapping.btnStart = btnIdx; break;
                                case "LeftStickButton":     newMapping.btnLeftStick = btnIdx; break;
                                case "RightStickButton":    newMapping.btnRightStick = btnIdx; break;
                                case "LeftTrigger":         newMapping.btnLeftTrigger = btnIdx; break;
                                case "RightTrigger":        newMapping.btnRightTrigger = btnIdx; break;
                            }
                        }
                    }
                    JSONObject deviceAxes = deviceMappings.getJSONObject("axes");

                    if(deviceAxes != null) {
                        if(deviceAxes.has("LeftStickX")) newMapping.axisIdLX = Enum.valueOf(AxisId.class, deviceAxes.getString("LeftStickX"));
                        if(deviceAxes.has("LeftStickY")) newMapping.axisIdLY = Enum.valueOf(AxisId.class, deviceAxes.getString("LeftStickY"));
                        if(deviceAxes.has("RightStickX")) newMapping.axisIdRX = Enum.valueOf(AxisId.class, deviceAxes.getString("RightStickX"));
                        if(deviceAxes.has("RightStickY")) newMapping.axisIdRY = Enum.valueOf(AxisId.class, deviceAxes.getString("RightStickY"));
                        if(deviceAxes.has("LeftTrigger")) newMapping.axisIdLT = Enum.valueOf(AxisId.class, deviceAxes.getString("LeftTrigger"));
                        if(deviceAxes.has("RightTrigger")) newMapping.axisIdRT = Enum.valueOf(AxisId.class, deviceAxes.getString("RightTrigger"));
                        //newMapping.axisRightStickY = deviceAxes.getString("RightStickY");
                        if(deviceAxes.has("DPadX")) newMapping.axisIdDpadX = Enum.valueOf(AxisId.class, deviceAxes.getString("DPadX"));
                        if(deviceAxes.has("DPadY")) newMapping.axisIdDpadY = Enum.valueOf(AxisId.class, deviceAxes.getString("DPadY"));
                    }
                    output.put(deviceName, newMapping);
                }
            }
        } catch(JSONException je) {
            Global.getLogger(getClass()).log(Level.ERROR, "Failed to load controller mappings - Mod cannot function! " + je.getMessage());
        }
        return output;
    }

    protected HashMap<String, EnumMap<Indicators, SpriteAPI>> configureSettingsApplicationController(JSONObject graphicsObject) {
        HashMap<String, EnumMap<Indicators, SpriteAPI>> output = new HashMap<>();
        try {
            JSONArray names = graphicsObject.names();
            for (int i =0; i < names.length(); i++) {
                String controllerName = names.getString(i);
                if(controllerName == null) continue;
                JSONObject obj = graphicsObject.getJSONObject(controllerName);
                if(obj == null) continue;
                JSONArray indicatorNames = obj.names();
                EnumMap<Indicators, SpriteAPI> controllerIndicators = new EnumMap<>(Indicators.class);
                for(int nameIndex = 0; nameIndex < indicatorNames.length(); nameIndex++) {
                    String indicatorName = indicatorNames.getString(nameIndex);
                    String filePath = obj.getString(indicatorName);
                    if(filePath == null) continue;
                    SpriteAPI sprite = Global.getSettings().getSprite(filePath);
                    if(sprite == null) continue;
                    else if(filePath.contains("graphics/indicators")) {
                        Indicators ind = Enum.valueOf(Indicators.class, indicatorName);
                        controllerIndicators.put(ind, sprite);
                    }
                }
                output.put(controllerName, controllerIndicators);
                //Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Found "+imagesFound+" .png images for indicators in repo \""+repo.Ó00000+"\".");
            }
        } catch(JSONException je) {
            Global.getLogger(getClass()).log(Level.ERROR, "Failed to parse indicators from settings.json: " + je.getMessage());
        }
        if(output.isEmpty()) {
            Global.getLogger(getClass()).log(Level.ERROR, "Did not parse any indicators from settings.json!");
        }
        return output;
    }
    
    static public void reconnectController() throws Exception {
        //only works during application start lwjgl does not refresh the list if connection status changes.
        Logger logger = Global.getLogger(SSMSControllerModPluginEx.class);
        logger.setLevel(Level.INFO);
        
        if ( Controllers.isCreated() ) Controllers.destroy();
        Controllers.create();
        controller = null;
        
        //"Controller (XBOX 360 For Windows)"
        /*
        buttons:
        0 - A
        1 - B
        2 - X
        3 - Y
        4 - left bumper
        5 - right bumper
        6 - back
        7 - start
        8 - left stick
        9 - right stick
        axis:
        X Axis: left stick, left -1 to right 1
        Y Axis: left stick, top -1 to bottom 1
        X Rotation: right stick, left -1 to right 1
        Y Rotation: right stick, top -1 to bottom 1
        Z Axis: sum of triggers, left trigger adds up to 1 and right trigger removes up to 1
        */
        for ( int i = 0; i < Controllers.getControllerCount(); i++ ) {
            Controller con = Controllers.getController(i);
            logger.info("Found controller: "+con.getName());
            for ( int j = 0; j < con.getAxisCount(); j++ ) {
                logger.info("with axis: "+con.getAxisName(j));
            }
            for ( int j = 0; j < con.getButtonCount(); j++ ) {
                logger.info("with button: "+con.getButtonName(j));
            }
            for ( int j = 0; j < con.getRumblerCount(); j++ ) {
                logger.info("with rumbler: "+con.getRumblerName(j));
            }
        }
        if ( controllerMappings != null ) {
            for ( int i = 0; i < Controllers.getControllerCount(); i++ ) {
                Controller con = Controllers.getController(i);
                String conName = new StringBuilder(con.getName()).append("(").append(con.getAxisCount()).append(",").append(con.getButtonCount()).append(")").toString();
                ControllerMapping conMap = controllerMappings.get(conName);
                if ( conMap != null ) {
                    con.poll();
                    controller = new HandlerController(con, conMap);
                    Global.getLogger(SSMSControllerModPluginEx.class).log(Level.INFO, "Identified controller [" + con.getName() + "], mappings associated successfully!");
                    break;
                }
            }
        }
        if ( controller == null ) {
            controller = new HandlerController();
            controller.poll();
        } 
    }
}
