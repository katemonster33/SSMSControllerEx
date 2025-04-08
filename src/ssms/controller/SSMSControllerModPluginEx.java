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
import net.java.games.input.ControllerEnvironment;
import ssms.controller.inputScreens.InputScope_360;
import ssms.controller.inputScreens.InputScope_Battle;
import ssms.controller.inputScreens.InputScope_TitleScreen;
import ssms.controller.inputScreens.InputScreenManager;
import ssms.controller.inputScreens.InputScreen_BattleMenu;
import ssms.controller.inputScreens.InputScreen_BattleSteering;
import ssms.controller.inputScreens.InputScreen_BattleTargeting;
import ssms.controller.inputScreens.InputScreen_Bluescreen;
import ssms.controller.inputScreens.InputScreen_TitleScreen;
import ssms.controller.reflection.ClassReflector;
import ssms.controller.reflection.FieldReflector;
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

        JSONObject obj = Global.getSettings().loadJSON("data/config/settings.json", "SSMSControllerEx");
        if(obj == null) {
            Global.getLogger(this.getClass()).log(Level.DEBUG, "no settings!!!");
            return;
        }

        controllerMappings = configureControllerMappings(obj.getJSONObject("controllerMappings"));
        //controllerMappings = configureControllerMappingEx();
        var indicatorsByController = configureSettingsApplicationController(obj.getJSONObject("graphics"));
        for(var mapping : controllerMappings.values()) {
            if(!mapping.indicatorProfile.isEmpty()) {
                mapping.indicators = indicatorsByController.get(mapping.indicatorProfile);
            }
        }
        // var csvObj = Global.getSettings().loadCSV("data/config/gamecontrollerdb.txt");
        // for(int i =0; i < csvObj.length(); i++) {
        //     String guid = csvObj.getJSONObject(i).getString("guid");
        //     if(!guid.startsWith("#")) {
        //         Global.getLogger(getClass()).log(Level.INFO, "Found controller!");
        //     }
        // }
        defaultIndicators = indicatorsByController.get("xbox360");
        
        // if ( controllerMappings == null ) controllerMappings = new ArrayList<>();
        // controllerMappings.add(xbox360);
        reconnectController();
        
        
        registeredSteeringController.add(SteeringController_FreeFlight.class);
        registeredSteeringController.add(SteeringController_OrbitTarget.class);
        
        InputScreenManager man = InputScreenManager.getInstance();
        
        man.registerScope(new InputScope_360());
        man.registerScope(new InputScope_Battle());
        man.registerScope(new InputScope_TitleScreen());
        
        man.registerScreen(new InputScreen_Bluescreen());
        man.registerScreen(new InputScreen_BattleSteering());
        man.registerScreen(new InputScreen_BattleTargeting());
        man.registerScreen(new InputScreen_BattleMenu());
        man.registerScreen(new InputScreen_TitleScreen());
        
        man.updateIndicators();
    }

    // enum ButtonMapping
    // {
    //     A,
    //     B,
    //     X,
    //     Y,
    //     Select,
    //     Start,
    //     L1,
    //     R1,
    //     L2,
    //     R2,
    //     L3,
    //     R3,
    //     Guide,
    //     DPadLeft,
    //     DPadRight,
    //     DPadUp,
    //     DPadDown,
    //     Invalid
    // };
    
    // enum AxisMapping
    // {
    //     LeftStickX,
    //     LeftStickY,
    //     RightStickX,
    //     RightStickY,
    //     LeftTrigger,
    //     RightTrigger,
    //     DPadX,
    //     DPadY,
    //     Invalid
    // };

    // enum POVMapping
    // {
    //     DPadLeft,
    //     DPadRight,
    //     DPadUp,
    //     DPadDown,
    //     Invalid
    // };

    // TODO : This is the preferred approach to map controller buttons. But without a reliable way to get USB device info (vendor, product, version) and turn it into the GUID we need to lookup the device in our table, this can't work.
    // protected HashMap<String, ControllerMapping> configureControllerMappingEx() {
    //     HashMap<String, ControllerMapping> output = new HashMap<>();
    //     try {
    //         var controllerMapJson = Global.getSettings().loadJSON("data/config/controllerMappings.json");
    //         String platform = System.getProperty("os.name");
    //         if(platform.contains("Windows")) platform = "Windows";
    //         else if(platform.contains("OS X")) platform = "Mac OS X";
    //         else if(platform.contains("Linux")) platform = "Linux";
    //         var platformMappings = controllerMapJson.getJSONObject(platform);
    //         for(int i = 0; i < platformMappings.length(); i++) {
    //             var controllerName = platformMappings.names().getString(i);
    //             var controller = platformMappings.getJSONObject(controllerName);
    //             var buttons = controller.getJSONArray("buttons");
    //             ControllerMapping newMapping = new ControllerMapping();
    //             for(int btnIdx = 0; btnIdx < buttons.length(); btnIdx++) {
    //                 if(!buttons.isNull(btnIdx)) {
    //                     ButtonMapping btnId = ButtonMapping.values()[buttons.getInt(btnIdx)];
    //                     switch(btnId)
    //                     {
    //                         case A:                   newMapping.btnA = btnIdx; break;
    //                         case B:                   newMapping.btnB = btnIdx; break;
    //                         case X:                   newMapping.btnX = btnIdx; break;
    //                         case Y:                   newMapping.btnY = btnIdx; break;
    //                         case L1:          newMapping.btnBumperLeft = btnIdx; break;
    //                         case R1:         newMapping.btnBumperRight = btnIdx; break;
    //                         case Select :             newMapping.btnSelect = btnIdx; break;
    //                         case Start :              newMapping.btnStart = btnIdx; break;
    //                         case L3:     newMapping.btnLeftStick = btnIdx; break;
    //                         case R3:    newMapping.btnRightStick = btnIdx; break;
    //                         case L2:         newMapping.btnLeftTrigger = btnIdx; break;
    //                         case R2:        newMapping.btnRightTrigger = btnIdx; break;

    //                         //TODO : add support for D-Pads reported as buttons??? maybe??? do I care?? probably not
    //                         case DPadDown:
    //                         case DPadLeft:
    //                         case DPadRight:
    //                         case DPadUp:
    //                             break;

    //                         //TODO: same as above???
    //                         case Guide:
    //                             break;

    //                         case Invalid:
    //                         default:
    //                             break;
    //                     }
    //                 }
    //             }
    //             var axes = controller.getJSONArray("axes");
    //             for(int axisIdx = 0; axisIdx < axes.length(); axisIdx++) {
    //                 if(!axes.isNull(axisIdx)) {
    //                     AxisMapping axisId = AxisMapping.values()[axes.getInt(axisIdx)];
    //                     switch(axisId)
    //                     {
    //                         case LeftStickX: newMapping.axisIdLX = axisIdx; break;
    //                         case LeftStickY: newMapping.axisIdLY = axisIdx; break;
    //                         case RightStickX: newMapping.axisIdRX = axisIdx; break;
    //                         case RightStickY: newMapping.axisIdRY = axisIdx; break;
    //                         case LeftTrigger: newMapping.axisIdLT = axisIdx; break;
    //                         case RightTrigger: newMapping.axisIdRT = axisIdx; break;
    //                         case DPadX: newMapping.axisIdDpadX = axisIdx; break;
    //                         case DPadY: newMapping.axisIdDpadY = axisIdx; break;

    //                         case Invalid:
    //                         default:
    //                             break;
    //                     }
    //                 }
    //             }
    //             var povs = controller.getJSONArray("povs");
    //             for(int povIdx = 0; povIdx < povs.length(); povIdx++) {
    //                 if(!povs.isNull(povIdx)) {
    //                     POVMapping povId = POVMapping.values()[povs.getInt(povIdx)];
    //                     switch(povId)
    //                     {
    //                         case DPadDown: newMapping.axisIdDpadY = 255; break;
    //                         case DPadLeft: newMapping.axisIdDpadX = 255; break;
    //                         case DPadRight: newMapping.axisIdDpadX = 255; break;
    //                         case DPadUp: newMapping.axisIdDpadY = 255; break;

    //                         default: break;
    //                     }
    //                 }
    //             }
    //             // TODO: make indicators based off controller name somehow??
    //             newMapping.indicatorProfile = "xbox360";
    //             output.put(controllerName, newMapping);
    //         }
    //     } catch(IOException | JSONException ex) {
    //         Global.getLogger(getClass()).log(Level.FATAL, "Couldn't read controller button mappings!");
    //     }
    //     return output;
    // }

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
                    if(deviceMappings.has("indicatorProfile")) newMapping.indicatorProfile = deviceMappings.getString("indicatorProfile");
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
        //ArrayList<Controller> controllers = new ArrayList<>();
        		try {
			ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();

			net.java.games.input.Controller[] found = env.getControllers();
			ArrayList<net.java.games.input.Controller> lollers = new ArrayList<net.java.games.input.Controller>();
			for ( net.java.games.input.Controller c : found ) {
				if ( (!c.getType().equals(net.java.games.input.Controller.Type.KEYBOARD)) &&
				     (!c.getType().equals(net.java.games.input.Controller.Type.MOUSE)) ) {
					lollers.add(c);
				}
			}

			for ( net.java.games.input.Controller c : lollers ) {
                logger.log(Level.INFO, c.toString());
                //controllers.add(new JInputController)
				//createController(c);
			}

		} catch (Throwable e) {
            logger.log(Level.INFO, "derp");
		}
        if ( Controllers.isCreated() ) Controllers.destroy();
        Controllers.create();
        Controllers.poll();
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
                String guid = getControllerGuid(con);
                //String conName = con.getName(); //new StringBuilder(con.getName()).append("(").append(con.getAxisCount()).append(",").append(con.getButtonCount()).append(")").toString();
                ControllerMapping conMap = controllerMappings.get(guid);
                if ( conMap != null ) {
                    con.poll();
                    controller = new HandlerController(con, conMap);
                    Global.getLogger(SSMSControllerModPluginEx.class).log(Level.INFO, "Identified controller [" + con.getName() + "], mappings associated successfully!");
                    break;
                }
            }
        }
        if(controller == null) {
            for ( int i = 0; i < Controllers.getControllerCount(); i++ ) {
                Controller con = Controllers.getController(i);
                if (con.getButtonCount() > 6 && con.getAxisCount() >= 2) {
                    controller = new HandlerController(con, null);
                    Global.getLogger(SSMSControllerModPluginEx.class).log(Level.INFO, "Identified controller [" + con.getName() + "], no mappings loaded!!");
                    break;
                }
            }
        }
        if ( controller == null ) {
            controller = new HandlerController();
            controller.poll();
        } 
    }

    static String getControllerGuid(Controller con) {
        String platform = System.getProperty("os.name");
        try {
            if(platform.contains("Windows")) {
                var privateDevField = ClassReflector.GetInstance().getDeclaredField(con.getClass(), "target");
                var privateDev = FieldReflector.GetInstance().GetVariable(privateDevField, con);
                var field = ClassReflector.GetInstance().getDeclaredField(privateDev.getClass(), "device");
                var device = FieldReflector.GetInstance().GetVariable(field, privateDev);
                var guidField = ClassReflector.GetInstance().getDeclaredField(device.getClass(), "guid");
                return (String) FieldReflector.GetInstance().GetVariable(guidField, device);
            } else if(platform.contains("Mac OS X")) {

            } else if(platform.contains("Linux")) {

            }
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.FATAL, "Failed to reflect controller GUID! " + ex);
        }
        return null;
    }
}
