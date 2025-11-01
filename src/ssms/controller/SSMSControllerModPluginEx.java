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

import com.fs.starfarer.api.util.Pair;
import org.jetbrains.annotations.NotNull;
import ssms.controller.campaign.*;
import ssms.controller.combat.*;
import ssms.controller.enums.*;
import ssms.controller.generic.CodexUI;
import ssms.controller.generic.MessageBoxScreen;
import ssms.controller.reflection.*;
import ssms.controller.titlescreen.*;
import ssms.controller.generic.LoadGameUI;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

import java.io.IOException;
import java.util.*;
import java.net.JarURLConnection;

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
    static HashMap<String, EnumMap<Indicators, String>> indicatorsByController;
    static public JSONObject oldMappingsJson;
    static public JSONObject newMappingsJson;
    static public EnumMap<Indicators,String> defaultIndicators;
    
    @Override
    public void onApplicationLoad() throws Exception {

        JSONObject obj = Global.getSettings().loadJSON("data/config/settings.json", "SSMSControllerEx");
        if(obj == null) {
            Global.getLogger(getClass()).error("no settings!!!");
            return;
        }
        //f.setAccessible(true);

        var classLoader = Thread.currentThread().getContextClassLoader();
        var packagePath = "com/fs/starfarer/ui";
        var resources = classLoader.getResources(packagePath);
        ClassReflector.suppressWarnings = true;
        while (resources.hasMoreElements()) {
            var resource = resources.nextElement();
            if (Objects.equals(resource.getProtocol(), "jar")) {
                var jarConnection = (JarURLConnection) resource.openConnection() ;
                var jarFile = jarConnection.getJarFile();
                var entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    var entryName = entry.getName();

                    if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                        var className = entryName.replace('/', '.').substring(0, entryName.length() - 6);

                        try {
                            var clazz = Class.forName(className);
                            if(ComboBoxReflector.tryInit(clazz)) {
                                break;
                            }
                        } catch (Exception ex) {
                            // Skip classes that can't be loaded
                        }
                    }
                }
            }
        }
        ClassReflector.suppressWarnings = false;
        Global.getLogger(InputShim.class).setLevel(Level.ERROR);
        oldMappingsJson = obj.getJSONObject("controllerMappings");
        indicatorsByController = configureSettingsApplicationController(obj.getJSONObject("graphics"));
        var testPnl = Global.getSettings().createCustom(1.f, 1.f, null);
        UIPanelReflector.initialize(testPnl.getClass().getSuperclass());
        UIComponentReflector.initialize(testPnl.getClass().getSuperclass().getSuperclass());
        var testElem = testPnl.createUIElement(1.f, 1.f, false);
        var testBtn = testElem.addButton("TEST", null, 1.f, 1.f, 0.f);
        ButtonReflector.init(testBtn.getClass());
        MessageBoxReflector.initialize();

        try {
            InputShim.install();
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Couldn't install input shim!", ex);
        }
        defaultIndicators = indicatorsByController.get("xbox360");

        reconnectController();
        
        InputScreenManager man = InputScreenManager.getInstance();
        
        man.registerScope(new InputScopeBase());
        man.registerScope(new BattleScope());
        
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
        man.registerScreen(new MissionScreenUI());
        man.registerScreen(new LoadGameUI());
        man.registerScreen(new NewGameUI());
        man.registerScreen(new ControllerSettingsUI());
        man.registerScreen(new CodexUI());
        man.registerScreen(new FleetMarketTabUI());
    }

     enum POVMapping
     {
         DPadLeft,
         DPadRight,
         DPadUp,
         DPadDown,
         Invalid
     };

     static JSONObject configureControllerMappingEx() {
         JSONObject output = null;
         try {
             var controllerMapJson = Global.getSettings().loadJSON("data/config/controllerMappings.json");
             String platform = System.getProperty("os.name");
             if(platform.contains("Windows")) platform = "Windows";
             else if(platform.contains("OS X")) platform = "Mac OS X";
             else if(platform.contains("Linux")) platform = "Linux";
             output = controllerMapJson.getJSONObject(platform);
         } catch(IOException | JSONException ex) {
             Global.getLogger(SSMSControllerModPluginEx.class).log(Level.FATAL, "Couldn't read controller button mappings!");
         }
         return output;
     }

    @NotNull
    private static ControllerMapping createControllerMappingFromJson(JSONObject controller) throws JSONException {
        var buttons = controller.getJSONArray("buttons");
        ControllerMapping newMapping = new ControllerMapping();
        for(int btnIdx = 0; btnIdx < buttons.length(); btnIdx++) {
            if(!buttons.isNull(btnIdx)) {
                ButtonMapping btnId = ButtonMapping.values()[buttons.getInt(btnIdx)];
                newMapping.mapButton(new ControllerMapping.ButtonData(btnId, btnIdx));
            }
        }
        var axisMappingsIndexed = AxisMapping.values();
        var axes = controller.getJSONArray("axes");
        for(int axisIdx = 0; axisIdx < axes.length(); axisIdx++) {
            if(!axes.isNull(axisIdx)) {
                newMapping.mapAxis(new ControllerMapping.AxisData(axisMappingsIndexed[axes.getInt(axisIdx)], null, axisIdx));
            }
        }
        var povs = controller.getJSONArray("povs");
        for(int povIdx = 0; povIdx < povs.length(); povIdx++) {
            if(!povs.isNull(povIdx)) {
                POVMapping povId = POVMapping.values()[povs.getInt(povIdx)];
                switch(povId) {
                    case DPadDown, DPadUp -> newMapping.mapPov(AxisMapping.DPadY);
                    case DPadLeft, DPadRight -> newMapping.mapPov(AxisMapping.DPadX);
                }
            }
        }
        // TODO: make indicators based off controller name somehow??
        newMapping.indicatorProfile = "xbox360";
        return newMapping;
    }

    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().addTransientScript(new CampaignControllerListener());
        Global.getSector().getListenerManager().addListener(new CampaignRenderer());
    }

    @NotNull
    private static ControllerMapping createMappingFromLegacyJson(String devName, JSONObject deviceMappings) throws JSONException {
        ControllerMapping newMapping = new ControllerMapping();
        newMapping.deviceName = devName;
        if(deviceMappings.has("indicatorProfile")) newMapping.indicatorProfile = deviceMappings.getString("indicatorProfile");
        JSONArray deviceButtons = deviceMappings.getJSONArray("buttons");

        if(deviceButtons != null) {
            for(int btnIdx = 0; btnIdx < deviceButtons.length(); btnIdx++) {
                String btnName = deviceButtons.getString(btnIdx);
                try {
                    var btn = Enum.valueOf(LogicalButtons.class, btnName);
                    newMapping.mapButton(new ControllerMapping.ButtonData(ButtonMapping.fromButton(btn), btnIdx));
                } catch(IllegalArgumentException e) {
                    Global.getLogger(SSMSControllerModPluginEx.class).warn("Couldn't match button enum from string: " + btnName);
                }
            }
        }
        JSONObject deviceAxes = deviceMappings.getJSONObject("axes");

        if(deviceAxes != null) {
            for(var axisMapping : AxisMapping.values()) {
                var mappingString = axisMapping.toString();
                if(deviceAxes.has(mappingString)) {
                    newMapping.mapAxis(new ControllerMapping.AxisData(axisMapping, Enum.valueOf(AxisId.class, deviceAxes.getString(mappingString)), -1));
                }
            }
        }
        return newMapping;
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

    static public void reconnectController() throws Exception{
         reconnectController(null);
    }
    
    static public void reconnectController(Controller controllerToConnect) throws Exception {
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
        if ( oldMappingsJson != null || newMappingsJson != null ) {
            for ( int i = 0; i < Controllers.getControllerCount(); i++ ) {
                Controller con = Controllers.getController(i);
                if(controllerToConnect != null && con.getIndex() != controllerToConnect.getIndex()) {
                    continue;
                }
                if(con.getButtonCount() == 0) {
                    continue;
                }
                ControllerMapping conMap = null;
                String guid = getControllerGuid(con);
                if (guid != null) {
                    if(newMappingsJson == null) {
                        newMappingsJson = configureControllerMappingEx();
                    }
                    if(newMappingsJson.has(guid)) {
                        var obj = newMappingsJson.getJSONObject(guid);
                        conMap = createControllerMappingFromJson(obj);
                        if(System.getProperty("os.name").contains("Windows")) {
                            convertAxisInstanceToIdx(conMap, con);
                        }
                        Global.getLogger(SSMSControllerModPluginEx.class).log(Level.INFO, "Successfully matched controller GUID in config [" + guid + "]");
                    }
                }
                if(conMap == null) {
                    //String conName = con.getName(); //new StringBuilder(con.getName()).append("(").append(con.getAxisCount()).append(",").append(con.getButtonCount()).append(")").toString();
                    if(oldMappingsJson.has(con.getName())) {
                        var obj = oldMappingsJson.getJSONObject(con.getName());
                        conMap = createMappingFromLegacyJson(con.getName(), obj);
                        conMap.indicators = indicatorsByController.get(conMap.indicatorProfile);
                        Global.getLogger(SSMSControllerModPluginEx.class).log(Level.INFO, "Successfully matched controller name in legacy config [" + con.getName() + "]");
                    }
                }
                if ( conMap != null ) {
                    con.poll();
                    controller = new HandlerController(con, conMap);
                    Global.getLogger(SSMSControllerModPluginEx.class).log(Level.INFO, "Identified controller [" + con.getName() + "], mappings associated successfully!");
                    break;
                } else if(controllerToConnect != null) {
                    con.poll();
                    controller = new HandlerController(con, null);
                    Global.getLogger(SSMSControllerModPluginEx.class).log(Level.INFO, "Identified controller [" + con.getName() + "], no mappings loaded!!");
                    break;
                }
            }
        }
        if(controller == null) {
            for (int i = 0; i < Controllers.getControllerCount(); i++) {
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
            var axes = (List<?>) new ClassReflector(con.getClass()).getDeclaredField("axes").get(con);
            for(int axisIdx = 0; axisIdx < axes.size(); axisIdx++) {
                var axis = axes.get(axisIdx);
                var obj = new ClassReflector(axis.getClass()).getDeclaredField("object").get(axis);
                instances.add(new Pair<>((int) new ClassReflector(obj.getClass()).getDeclaredField("instance").get(obj), axisIdx));
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
         return new ControllerMapping.AxisData(axisMapping.getAxisMapping(),
                 axisMapping.getAxisId(),
                 fixAxisIndex(axisMapping.getAxisIndex(), instances));
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
                var privateDev = new ClassReflector(con.getClass()).getDeclaredField("target").get(con);
                var device = new ClassReflector(privateDev.getClass()).getDeclaredField("device").get(privateDev);
                var address = (long) new ClassReflector(device.getClass()).getDeclaredField("address").get(device);
                var vidpid = DirectInputDeviceEx.GetVidPid(address);
                if(vidpid != 0) {
                    return makeControllerGuid((int)(vidpid & 0xFFFF), (int)((vidpid >> 16) & 0xFFFF), 0);
                }
            } else if(platform.contains("Mac OS X")) {
                return null;
            } else if(platform.contains("Linux")) {
                var target = new ClassReflector(con.getClass()).getDeclaredField("target").get(con);
                var eventController = new ClassReflector(target.getClass()).getDeclaredField("eventController").get(target);
                var device = new ClassReflector(eventController.getClass()).getDeclaredField("device").get(eventController);
                var inputDeviceId = new ClassReflector(device.getClass()).getDeclaredField("input_id").get(device);
                var inputDevReflector = new ClassReflector(inputDeviceId.getClass());
                int vendor = (int)inputDevReflector.getDeclaredField("vendor").get(inputDeviceId);
                int product = (int) inputDevReflector.getDeclaredField("product").get(inputDeviceId);
                int version = (int) inputDevReflector.getDeclaredField("version").get(inputDeviceId);
                return makeControllerGuid(vendor, product, version);
            }
        } catch(Throwable ex) {
            Global.getLogger(SSMSControllerModPluginEx.class).log(Level.FATAL, "Failed to reflect controller GUID! ", ex);
        }
        return null;
    }
}
