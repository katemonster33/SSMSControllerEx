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

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import ssms.controller.Indicators;

/**
 * Sets up configuration and application scoped persistency for the mod.
 * 
 * @author Malte Schulze
 */
public final class SSMSControllerModPlugin extends BaseModPlugin {
    static public HandlerController controller = new HandlerController();
    static public List<ControllerMapping> controllerMappings;
    static public EnumMap<Indicators,String> defaultIndicators;
    
    @Override
    public void onApplicationLoad() throws Exception {
        ControllerMapping xbox360 = new ControllerMapping();
        xbox360.btnA = 0;
        xbox360.btnB = 1;
        xbox360.btnX = 2;
        xbox360.btnY = 3;
        xbox360.btnBumperLeft = 4;
        xbox360.btnBumperRight = 5;
        xbox360.btnSelect = 6;
        xbox360.btnStart = 7;
        xbox360.btnLeftStick = 8;
        xbox360.btnRightStick = 9;
        xbox360.axisLeftStickX = "X Axis";
        xbox360.axisLeftStickY = "Y Axis";
        xbox360.axisRightStickX = "X Rotation";
        xbox360.axisRightStickY = "Y Rotation";
        xbox360.axisTrigger = "Z Axis";
        xbox360.axisBtnConversionDeadzone = 0.85f;
        xbox360.joystickDeadzone = 0.25f;
        xbox360.deviceName = "Controller (XBOX 360 For Windows)(5,10)";
        xbox360.indicators = new EnumMap<>(Indicators.class);
        JSONObject obj = Global.getSettings().loadJSON("data/config/settings.json");
        if(obj == null) {
            Global.getLogger(this.getClass()).log(Level.DEBUG, "no settings!!!");
            return;
        }

        var indicatorsByController = configureSettingsApplicationController(obj.getJSONObject("graphics"));
        var xbox360Indicators = indicatorsByController.get("xbox360");
        if(xbox360Indicators != null) {
            xbox360.indicators = xbox360Indicators;
        } 
        defaultIndicators = xbox360.indicators;
        
        if ( controllerMappings == null ) controllerMappings = new ArrayList<>();
        controllerMappings.add(xbox360);
        reconnectController();
        
    }
    
    protected HashMap<String, EnumMap<Indicators, String>> configureSettingsApplicationController(JSONObject graphicsObject) {
        HashMap<String, EnumMap<Indicators, String>> output = new HashMap<>();
        try {
            JSONArray names = graphicsObject.names();
            for (int i =0; i < names.length(); i++) {
                String controllerName = names.getString(i);
                if(controllerName == null) continue;
                JSONObject obj = graphicsObject.getJSONObject(controllerName);
                if(obj == null) continue;
                JSONArray indicatorNames = obj.names();
                EnumMap<Indicators, String> controllerIndicators = new EnumMap<>(Indicators.class);
                for(int nameIndex = 0; nameIndex < indicatorNames.length(); nameIndex++) {
                    String indicatorName = indicatorNames.getString(nameIndex);
                    String filePath = graphicsObject.getString(indicatorName);
                    if(filePath == null) continue;
                    else if(filePath.contains("graphics/indicators")) {
                        Indicators ind = Enum.valueOf(Indicators.class, indicatorName);
                        controllerIndicators.put(ind, filePath);
                    }
                }
                output.put(controllerName, controllerIndicators);
                //Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Found "+imagesFound+" .png images for indicators in repo \""+repo.Ó00000+"\".");
            }
        } catch(JSONException je) {
            Global.getLogger(getClass()).log(Level.ERROR, "Failed to parse indicators from settings.json: " + je.getMessage());
        }
        if(output.size() == 0) {
            Global.getLogger(getClass()).log(Level.ERROR, "Did not parse any indicators from settings.json!");
        }
        return output;
    }
    
    static public void reconnectController() throws Exception {
        //only works during application start lwjgl does not refresh the list if connection status changes.
        Logger logger = Global.getLogger(SSMSControllerModPlugin.class);
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
            Map<String,Controller> namedControllers = new HashMap<>();
            for ( int i = 0; i < Controllers.getControllerCount(); i++ ) {
                Controller con = Controllers.getController(i);
                namedControllers.put(new StringBuilder(con.getName()).append("(").append(con.getAxisCount()).append(",").append(con.getButtonCount()).append(")").toString(), con);
            }
            for ( ControllerMapping mapping : controllerMappings ) {
                Controller con = namedControllers.get(mapping.deviceName);
                if ( con != null ) {
                    con.poll();
                    controller = new HandlerController(con,mapping);
                    break;
                }
            }
        }
        if ( controller == null ) controller = new HandlerController();
        controller.poll();
    }
}
