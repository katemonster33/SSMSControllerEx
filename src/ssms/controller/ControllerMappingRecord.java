package ssms.controller;

import ssms.controller.enums.*;

import com.fs.starfarer.api.Global;

import java.util.*;
import java.util.regex.*;

public class ControllerMappingRecord {
    public static class ButtonConfigRecord {
        private ButtonMapping _buttonMapping;
        private int _number;

        public ButtonConfigRecord(ButtonMapping buttonMapping, int number){
            _buttonMapping = buttonMapping;
            _number = number;
        }

        public ButtonMapping getButtonMapping(){
            return _buttonMapping;
        }

        public int getNumber(){
            return _number;
        }
    }

    public static class AxisConfigRecord {
        private AxisMapping _axisMapping;
        private int _number;

        public AxisConfigRecord(AxisMapping axisMapping, int number){
            _axisMapping = axisMapping;
            _number = number;
        }

        public AxisMapping getAxisMapping(){
            return _axisMapping;
        }

        public int getNumber(){
            return _number;
        }
    }

    public static class POVConfigRecord {
        private POVMapping _povMapping;
        private int _number;

        public POVConfigRecord(POVMapping povMapping, int number){
            _povMapping = povMapping;
            _number = number;
        }

        public POVMapping getPOVMapping(){
            return _povMapping;
        }

        public int getNumber(){
            return _number;
        }
    }

    private String _guid;
    private String _platform;
    private String _name;
    private ButtonConfigRecord[] _buttons;
    private AxisConfigRecord[] _axes;
    private POVConfigRecord[] _povs;

    private ControllerMappingRecord(String guid, String name, String platform, ButtonConfigRecord[] buttons, AxisConfigRecord[] axes, POVConfigRecord[] povs){
        _guid = guid;
        _name = name;
        _platform = platform;
        _buttons = buttons;
        _axes = axes;
        _povs = povs;
    }

    public static ControllerMappingRecord parse(String dbRecord) throws Exception {
        String[] segments = dbRecord.split(","); //o - GUID, 1 - name
        List<ButtonConfigRecord> buttons = new ArrayList<ButtonConfigRecord>();
        List<AxisConfigRecord> axes = new ArrayList<AxisConfigRecord>();
        List<POVConfigRecord> povs = new ArrayList<POVConfigRecord>();
        String platform = null;
        for(int i = 2; i < segments.length; i++) {
            if(segments[i].equals(""))
                continue;

            String[] keyAndValue = segments[i].split(":");
            if(keyAndValue.length != 2){
                Global.getLogger(SSMSControllerModPluginEx.class).warn("Cannot parse controller config segment: \"" + segments[i] + "\"");
                continue;
            }

            if(keyAndValue[0].equals("platform")){
                platform = keyAndValue[1];
            }

            ButtonMapping button = null;
            AxisMapping axis = null;
            POVMapping pov = null;

            switch(keyAndValue[0]) {
                case "a" -> button = ButtonMapping.A;
                case "b" -> button = ButtonMapping.B;
                case "x" -> button = ButtonMapping.X;
                case "y" -> button = ButtonMapping.Y;
                case "back" -> button = ButtonMapping.Select;
                case "start" -> button = ButtonMapping.Start;
                case "leftshoulder" -> button = ButtonMapping.L1;
                case "rightshoulder" -> button = ButtonMapping.R1;
                case "lefttrigger" -> { button = ButtonMapping.L2; axis = AxisMapping.LeftTrigger; break; }
                case "righttrigger" -> { button = ButtonMapping.R2; axis = AxisMapping.RightTrigger; break; }
                case "leftstick" -> button = ButtonMapping.L3;
                case "rightstick" -> button = ButtonMapping.R3;
                case "guide" -> button = ButtonMapping.Guide;
                case "leftx" -> axis = AxisMapping.LeftStickX;
                case "lefty" -> axis = AxisMapping.LeftStickY;
                case "rightx" -> axis = AxisMapping.RightStickX;
                case "righty" -> axis = AxisMapping.RightStickY;
                case "dpleft" -> { button = ButtonMapping.DPadLeft; axis = AxisMapping.DPadX; pov = POVMapping.DPadLeft; break; }
                case "dpright" -> { button = ButtonMapping.DPadRight; axis = AxisMapping.DPadX; pov = POVMapping.DPadRight; break; }
                case "dpup" -> { button = ButtonMapping.DPadUp; axis = AxisMapping.DPadY; pov = POVMapping.DPadUp; break; }
                case "dpdown" -> { button = ButtonMapping.DPadDown; axis = AxisMapping.DPadY; pov = POVMapping.DPadDown; break; }
                case "platform" -> platform = keyAndValue[1];
            }

            Integer buttonNumber = button != null ? tryReadButtonNumber(keyAndValue[1]) : null;
            Integer axisNumber = axis != null ? tryReadAxisNumber(keyAndValue[1]) : null;
            Integer povNumber = pov != null ? tryReadPOVNumber(keyAndValue[1]) : null;
            if(button != null && buttonNumber != null){
                buttons.add(new ButtonConfigRecord(button, buttonNumber));
            }else if(axis != null && axisNumber != null){
                axes.add(new AxisConfigRecord(axis, axisNumber));
            }else if(pov != null && povNumber != null){
                povs.add(new POVConfigRecord(pov, povNumber));
            }else if (button != null || axis != null || pov != null){
                Global.getLogger(SSMSControllerModPluginEx.class).warn("Unable to map \"" + keyAndValue[0] + "\" to \"" + keyAndValue[1] + "\" for Controller \"" + segments[1] + "\" with GUID \"" + segments[0] + "\"");
            }
        }
        if(platform == null){
            Global.getLogger(SSMSControllerModPluginEx.class).error("No platform found for Controller: \"" + dbRecord + "\"");
            throw new Exception("No platform found for Controller: \"" + dbRecord + "\"");
        }
        ButtonConfigRecord[] buttonsArr = new ButtonConfigRecord[buttons.size()];
        buttons.toArray(buttonsArr);
        AxisConfigRecord[] axesArr = new AxisConfigRecord[axes.size()];
        axes.toArray(axesArr);
        POVConfigRecord[] povsArr = new POVConfigRecord[povs.size()];
        povs.toArray(povsArr);
        return new ControllerMappingRecord(segments[0], segments[1], platform, buttonsArr, axesArr, povsArr);
    }

    private static Pattern buttonNumberPattern = Pattern.compile("^b[0-9]+$");
    private static Integer tryReadButtonNumber(String buttonNumber) throws Exception {
        Matcher matcher = buttonNumberPattern.matcher(buttonNumber);
        if(!matcher.find()){
            return null;
        }
        String strippedButtonNumber = buttonNumber.replace("b", "");
        return Integer.parseInt(strippedButtonNumber);
    }

    private static Pattern axisNumberPattern = Pattern.compile("^[+-]?a[0-9]+~?$");
    private static Integer tryReadAxisNumber(String axisNumber) throws Exception {
        Matcher matcher = axisNumberPattern.matcher(axisNumber);
        if(!matcher.find()){
            return null;
        }
        String strippedAxisNumber = axisNumber.replace("a", "");
        strippedAxisNumber = strippedAxisNumber.replace("-", "");
        strippedAxisNumber = strippedAxisNumber.replace("+", "");
        strippedAxisNumber = strippedAxisNumber.replace("~", "");
        return Integer.parseInt(strippedAxisNumber);
    }

    private static Pattern povNumberPattern = Pattern.compile("^h0.[1248]$");
    private static Integer tryReadPOVNumber(String povNumber) throws Exception {
        Matcher matcher = povNumberPattern.matcher(povNumber);
        if(!matcher.find()){
            return null;
        }
        String strippedPOVNumber = povNumber.replace("h0.", "");
        int num = Integer.parseInt(strippedPOVNumber);
        switch(num){
            case 1: return 0;
            case 2: return 1;
            case 4: return 2;
            case 8: return 3;
            default: return null;
        }
    }

    public String getGUID(){
        return _guid;
    }

    public String getName(){
        return _name;
    }

    public String getPlatform(){
        return _platform;
    }

    public ButtonConfigRecord[] getButtons(){
        return _buttons;
    }

    public AxisConfigRecord[] getAxes(){
        return _axes;
    }

    public POVConfigRecord[] getPOVs(){
        return _povs;
    }
}
