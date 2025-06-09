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

import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.ui.trade.CargoDataGridView;
import net.java.games.input.Component;
import net.java.games.input.ControllerEnvironment;
import ssms.controller.campaign.*;
import ssms.controller.combat.*;
import ssms.controller.generic.MessageBoxScreen;
import ssms.controller.reflection.*;
import ssms.controller.titlescreen.AutoMapperUI;
import ssms.controller.titlescreen.TitleScreenUI;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

import java.io.IOException;
import java.util.*;

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
    static public HashMap<String, ControllerMapping> controllerMappings;
    static public HashMap<String, ControllerMapping> controllerMappingsByGuid;
    static public EnumMap<Indicators,String> defaultIndicators;
    
    @Override
    public void onApplicationLoad() throws Exception {

        JSONObject obj = Global.getSettings().loadJSON("data/config/settings.json", "SSMSControllerEx");
        if(obj == null) {
            Global.getLogger(getClass()).error("no settings!!!");
            return;
        }
        Global.getLogger(InputShim.class).setLevel(Level.ERROR);
        controllerMappings = configureControllerMappings(obj.getJSONObject("controllerMappings"));
        var indicatorsByController = configureSettingsApplicationController(obj.getJSONObject("graphics"));
        for(var mapping : controllerMappings.values()) {
            if(!mapping.indicatorProfile.isEmpty()) {
                mapping.indicators = indicatorsByController.get(mapping.indicatorProfile);
            }
        }
        var testPnl = Global.getSettings().createCustom(1.f, 1.f, null);
        UIPanelReflector.initialize((Class<? extends UIPanelAPI>) testPnl.getClass().getSuperclass());
        var testElem = testPnl.createUIElement(1.f, 1.f, false);
        var testBtn = testElem.addButton("TEST", null, 1.f, 1.f, 0.f);
        ButtonReflector.init(testBtn.getClass());

        try {
            InputShim.install();
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Couldn't install input shim!", ex);
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
        
        InputScreenManager man = InputScreenManager.getInstance();
        
        man.registerScope(new InputScopeBase());
        man.registerScope(new BattleScope());
        man.registerScope(new CampaignScope());
        
        man.registerScreen(new InputScreenBase());
        man.registerScreen(new BattleSteeringScreen());
        man.registerScreen(new BattleTargetingScreen());
        man.registerScreen(new BattleMenuScreen());
        man.registerScreen(new TitleScreenUI());
        man.registerScreen(new AutoMapperUI());
        man.registerScreen(new MainCampaignUI());
        man.registerScreen(new DialogUI());
        man.registerScreen(new MessageBoxScreen());
        man.registerScreen(new BattleDeploymentScreen());
        man.registerScreen(new WarroomScreen());
        man.registerScreen(new TradeScreen());
        man.registerScreen(new CargoStackPickerScreen());
        man.registerScreen(new CharacterTabUI());
        man.registerScreen(new FleetTabUI());
        man.registerScreen(new MapTabUI());
        man.registerScreen(new IntelTabUI());
        man.registerScreen(new IntelPlanetTabUi());
        man.registerScreen(new IntelFactionTabUi());
        man.registerScreen(new IntelPlanetStarSystemUI());
        man.registerScreen(new RefitTabUI());
        man.registerScreen(new CommandTabUI());
    }

     enum ButtonMapping
     {
         A,
         B,
         X,
         Y,
         Select,
         Start,
         L1,
         R1,
         L2,
         R2,
         L3,
         R3,
         Guide,
         DPadLeft,
         DPadRight,
         DPadUp,
         DPadDown,
         Invalid
     };
    
     enum AxisMapping
     {
         LeftStickX,
         LeftStickY,
         RightStickX,
         RightStickY,
         LeftTrigger,
         RightTrigger,
         DPadX,
         DPadY,
         Invalid
     };

     enum POVMapping
     {
         DPadLeft,
         DPadRight,
         DPadUp,
         DPadDown,
         Invalid
     };

     // TODO : This is the preferred approach to map controller buttons. But without a reliable way to get USB device info (vendor, product, version) and turn it into the GUID we need to lookup the device in our table, this can't work.
     static HashMap<String, ControllerMapping> configureControllerMappingEx() {
         HashMap<String, ControllerMapping> output = new HashMap<>();
         var axisMappingsIndexed = AxisMapping.values();
         try {
             var controllerMapJson = Global.getSettings().loadJSON("data/config/controllerMappings.json");
             String platform = System.getProperty("os.name");
             if(platform.contains("Windows")) platform = "Windows";
             else if(platform.contains("OS X")) platform = "Mac OS X";
             else if(platform.contains("Linux")) platform = "Linux";
             var platformMappings = controllerMapJson.getJSONObject(platform);
             for(int i = 0; i < platformMappings.length(); i++) {
                 var controllerName = platformMappings.names().getString(i);
                 var controller = platformMappings.getJSONObject(controllerName);
                 var buttons = controller.getJSONArray("buttons");
                 ControllerMapping newMapping = new ControllerMapping();
                 for(int btnIdx = 0; btnIdx < buttons.length(); btnIdx++) {
                     if(!buttons.isNull(btnIdx)) {
                         ButtonMapping btnId = ButtonMapping.values()[buttons.getInt(btnIdx)];
                         switch(btnId)
                         {
                             case A:                   newMapping.btnA = btnIdx; break;
                             case B:                   newMapping.btnB = btnIdx; break;
                             case X:                   newMapping.btnX = btnIdx; break;
                             case Y:                   newMapping.btnY = btnIdx; break;
                             case L1:          newMapping.btnBumperLeft = btnIdx; break;
                             case R1:         newMapping.btnBumperRight = btnIdx; break;
                             case Select :             newMapping.btnSelect = btnIdx; break;
                             case Start :              newMapping.btnStart = btnIdx; break;
                             case L3:     newMapping.btnLeftStick = btnIdx; break;
                             case R3:    newMapping.btnRightStick = btnIdx; break;
                             case L2:         newMapping.btnLeftTrigger = btnIdx; break;
                             case R2:        newMapping.btnRightTrigger = btnIdx; break;

                             //TODO : add support for D-Pads reported as buttons??? maybe??? do I care?? probably not
                             case DPadDown:
                             case DPadLeft:
                             case DPadRight:
                             case DPadUp:
                                 break;

                             //TODO: same as above???
                             case Guide:
                                 break;

                             case Invalid:
                             default:
                                 break;
                         }
                     }
                 }
                 var axes = controller.getJSONArray("axes");
                 EnumMap<AxisMapping, Integer> axisMappings = new EnumMap<>(AxisMapping.class);
                 for(int axisIdx = 0; axisIdx < axes.length(); axisIdx++) {
                     if(!axes.isNull(axisIdx)) {
                         axisMappings.put(axisMappingsIndexed[axes.getInt(axisIdx)], axisIdx);
                     }
                 }
                 mapAxisByAxisIndex(axisMappings, newMapping, Axes.LeftStick, AxisMapping.LeftStickX, AxisMapping.LeftStickY);
                 var povs = controller.getJSONArray("povs");
                 for(int povIdx = 0; povIdx < povs.length(); povIdx++) {
                     if(!povs.isNull(povIdx)) {
                         POVMapping povId = POVMapping.values()[povs.getInt(povIdx)];
                         switch(povId) {
                             case DPadDown, DPadUp -> newMapping.axisIndexDpadY = 255;
                             case DPadLeft, DPadRight -> newMapping.axisIndexDpadX = 255;
                         }
                     }
                 }
                 // TODO: make indicators based off controller name somehow??
                 newMapping.indicatorProfile = "xbox360";
                 output.put(controllerName, newMapping);
             }
         } catch(IOException | JSONException ex) {
             Global.getLogger(SSMSControllerModPluginEx.class).log(Level.FATAL, "Couldn't read controller button mappings!");
         }
         return output;
     }

    private static void mapAxisByAxisIndex(EnumMap<AxisMapping, Integer> axisMappings, ControllerMapping newMapping, Axes axis, AxisMapping xAxisMapping, AxisMapping yAxisMapping) {
        if(axisMappings.containsKey(xAxisMapping) && axisMappings.containsKey(yAxisMapping)) {
            newMapping.mapAxis(new ControllerMapping.AxisData(axis, null, (int) axisMappings.get(xAxisMapping), null, (int) axisMappings.get(yAxisMapping)));
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {

        Global.getSector().addTransientScript(new CampaignControllerListener());
        Global.getSector().getListenerManager().addListener(new CampaignRenderer());
    }

    void mapAxisFromJson(ControllerMapping controllerMapping, JSONObject jsonObject, Axes axes, String xAxisJsonName, String yAxisJsonName ) throws JSONException {
        if(jsonObject.has(xAxisJsonName) && jsonObject.has(yAxisJsonName))  {
            controllerMapping.mapAxis(new ControllerMapping.AxisData(axes,
                    Enum.valueOf(AxisId.class, jsonObject.getString(xAxisJsonName)),
                    -1,
                    Enum.valueOf(AxisId.class, jsonObject.getString(yAxisJsonName)),
                    -1));
        }
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
                    if(deviceMappings.has("indicatorProfile")) newMapping.indicatorProfile = deviceMappings.getString("indicatorProfile");
                    JSONArray deviceButtons = deviceMappings.getJSONArray("buttons");

                    if(deviceButtons != null) {
                        for(int btnIdx = 0; btnIdx < deviceButtons.length(); btnIdx++) {
                            String btnName = deviceButtons.getString(btnIdx);
                            try {
                                var btn = Enum.valueOf(Buttons.class, btnName);
                                newMapping.mapButton(new ControllerMapping.ButtonData(btn, btnIdx));
                            } catch(IllegalArgumentException e) {
                                Global.getLogger(SSMSControllerModPluginEx.class).warn("Couldn't match button enum from string: " + btnName);
                            }
                        }
                    }
                    JSONObject deviceAxes = deviceMappings.getJSONObject("axes");

                    if(deviceAxes != null) {
                        mapAxisFromJson(newMapping, deviceAxes, Axes.LeftStick, "LeftStickX", "LeftStickY");
                        mapAxisFromJson(newMapping, deviceAxes, Axes.RightStick, "RightStickX", "RightStickY");
                        mapAxisFromJson(newMapping, deviceAxes, Axes.Trigger, "LeftTrigger", "RightTrigger");
                        mapAxisFromJson(newMapping, deviceAxes, Axes.DPad, "DPadX", "DPadY");
                    }
                    output.put(deviceName, newMapping);
                }
            }
        } catch(JSONException je) {
            Global.getLogger(getClass()).log(Level.ERROR, "Failed to load controller mappings - Mod cannot function! " + je.getMessage());
        }
        return output;
    }

    private HashMap<String, EnumMap<Indicators, String>> configureSettingsApplicationController(JSONObject graphicsObject) {
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
                    String filePath = obj.getString(indicatorName);
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
            StringBuilder axesInfo = new StringBuilder(" with axes: ");
            for ( int j = 0; j < con.getAxisCount(); j++ ) {
                if(j != 0) axesInfo.append(", ");
                axesInfo.append(con.getAxisName(j));
            }
            logger.info(axesInfo);
            StringBuilder buttonsInfo = new StringBuilder(" with buttons: ");
            for ( int j = 0; j < con.getButtonCount(); j++ ) {
                if(j != 0) buttonsInfo.append(", ");
                buttonsInfo.append(con.getButtonName(j));
            }
            logger.info(buttonsInfo);
            StringBuilder rumblersInfo = new StringBuilder(" with rumblers: ");
            for ( int j = 0; j < con.getRumblerCount(); j++ ) {
                if(j != 0) rumblersInfo.append(", ");
                rumblersInfo.append(con.getRumblerName(j));
            }
            logger.info(rumblersInfo);
        }
        if ( controllerMappings != null || controllerMappingsByGuid != null ) {
            for ( int i = 0; i < Controllers.getControllerCount(); i++ ) {
                Controller con = Controllers.getController(i);
                if(con.getButtonCount() == 0) {
                    continue;
                }
                ControllerMapping conMap = null;
                String guid = getControllerGuid(con);
                if (guid != null) {
                    if(controllerMappingsByGuid == null) {
                        controllerMappingsByGuid = configureControllerMappingEx();
                    }
                    conMap = controllerMappingsByGuid.get(guid);
                    if(conMap != null && System.getProperty("os.name").contains("Windows")) {
                        convertAxisInstanceToIdx(conMap, con);
                    }
                }
                if(conMap == null) {
                    //String conName = con.getName(); //new StringBuilder(con.getName()).append("(").append(con.getAxisCount()).append(",").append(con.getButtonCount()).append(")").toString();
                    conMap = controllerMappings.get(con.getName());
                }
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

    static int fixAxisIndex(int axisIdx, List<Pair<Integer, Integer>> realIndices) {
        if(axisIdx >= 0 && realIndices.size() > axisIdx && axisIdx != 0xFF) return realIndices.get(axisIdx).two;
        return axisIdx;
    }

    static void convertAxisInstanceToIdx(ControllerMapping conMap, Controller con) {
        if(con.getAxisCount() == 0) return;
        try {
            List<Pair<Integer, Integer>> instances = new ArrayList<>();
            var axes = (List<?>) FieldReflector.GetInstance().GetVariableByName("axes", con);
            for(int axisIdx = 0; axisIdx < axes.size(); axisIdx++) {
                var axis = axes.get(axisIdx);
                var obj = FieldReflector.GetInstance().GetVariableByName("object", axis);
                instances.add(new Pair<>((int) FieldReflector.GetInstance().GetVariableByName("instance", obj), axisIdx));
            }
            instances.sort(Comparator.comparingInt(o -> o.one));
            for(var axisMapping : conMap.getMappedAxes()) {
                var newMapping = fixAxisIndices(axisMapping, instances);
                conMap.mapAxis(newMapping);
            }
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).error("Couldn't reflect instance ID of controller axes!");
        }
    }

    static ControllerMapping.AxisData fixAxisIndices(ControllerMapping.AxisData axisMapping, List<Pair<Integer, Integer>> instances) {
         return new ControllerMapping.AxisData(axisMapping.getAxis(),
                 axisMapping.getXAxisId(),
                 fixAxisIndex(axisMapping.getXAxisIndex(), instances),
                 axisMapping.getYAxisId(),
                 fixAxisIndex(axisMapping.getYAxisIndex(), instances));
    }

    //implementation of SDL_Swap16LE
    static String asLittleEndian(int i) {
         return String.format("%02x%02x", i & 0xFF, (i >> 8) & 0xFF);
    }

    // a rough approximation of SDL's SDL_CreateJoystickGUID to use their controller mapping JSON
    static String makeControllerGuid(int vendor, int product, int version) {
        String guid = "0300" + // first 2 bytes is always 0300 for USB
            "0000" + // CRC of the vendor name and device name?? seems to be always zero
            asLittleEndian(vendor) +
            "0000" + // always zero
            asLittleEndian(product) +
            "0000" + // always zero
            asLittleEndian(version) +
            "00" + // driver signature, always zero??
            "00"; // driver data, always zero??
        return guid;
    }

    static String getControllerGuid(Controller con) {
        String platform = System.getProperty("os.name");
        try {
            if(platform.contains("Windows")) {
                var privateDev = FieldReflector.GetInstance().GetVariableByName("target", con);
                var device = FieldReflector.GetInstance().GetVariableByName("device", privateDev);
                var address = (long) FieldReflector.GetInstance().GetVariableByName("address", device);
                var vidpid = DirectInputDeviceEx.GetVidPid(address);
                if(vidpid != 0) {
                    return makeControllerGuid((int)(vidpid & 0xFFFF), (int)((vidpid >> 16) & 0xFFFF), 0);
                }
            } else if(platform.contains("Mac OS X")) {
                return null;
            } else if(platform.contains("Linux")) {
                var target = FieldReflector.GetInstance().GetVariableByName("target", con);
                var eventController = FieldReflector.GetInstance().GetVariableByName("eventController", target);
                var device = FieldReflector.GetInstance().GetVariableByName("device", eventController);
                var inputDeviceId = FieldReflector.GetInstance().GetVariableByName("input_id", device);
                int vendor = (int) FieldReflector.GetInstance().GetVariableByName("vendor", inputDeviceId);
                int product = (int) FieldReflector.GetInstance().GetVariableByName("product", inputDeviceId);
                int version = (int) FieldReflector.GetInstance().GetVariableByName("version", inputDeviceId);
                return makeControllerGuid(vendor, product, version);
            }
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.FATAL, "Failed to reflect controller GUID! ", ex);
        }
        return null;
    }
}
