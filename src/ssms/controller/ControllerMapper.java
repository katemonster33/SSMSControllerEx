package ssms.controller;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import ssms.controller.enums.AxisMapping;
import ssms.controller.enums.ButtonMapping;
import ssms.controller.enums.LogicalButtons;

import java.util.Arrays;

public class ControllerMapper {
    boolean[] buttonsChanged;
    boolean[] axesChanged;
    boolean[] btnRestingState;
    boolean povXChanged, povYChanged;

    float[] axisRestingState;
    float povXRestingState, povYRestingState;

    Controller controller;

    int btnCount = -1;

    public ControllerMapper(Controller controller) {
        this.controller = controller;
        btnCount = controller.getAxisCount() * 2 + controller.getButtonCount();
        btnRestingState = new boolean[controller.getButtonCount()];
        buttonsChanged = new boolean[controller.getButtonCount()];
        for(int i = 0; i < btnRestingState.length; i++) {
            btnRestingState[i] = controller.isButtonPressed(i);
        }
        axisRestingState = new float[controller.getAxisCount()];
        axesChanged = new boolean[controller.getAxisCount()];
        for(int i = 0; i < axisRestingState.length; i++) {
            axisRestingState[i] = controller.getAxisValue(i);
        }
        povXChanged = povYChanged = false;
        povXRestingState = controller.getPovX();
        povYRestingState = controller.getPovY();
    }


    public boolean isControllerInRestingState() {
        boolean retval = false;
        for(int i = 0; i < controller.getButtonCount(); i++) {
            if(controller.isButtonPressed(i) != btnRestingState[i]) {
                retval = buttonsChanged[i] = true;
            }
        }
        for(int i = 0; i < controller.getAxisCount(); i++) {
            if(Math.abs(controller.getAxisValue(i) - axisRestingState[i]) > 0.2) {
                retval = axesChanged[i] = true;
            }
        }
        if(SSMSControllerModPluginEx.controller.controller.getPovX() != povXRestingState) {
            retval = povXChanged = true;
        }
        if(SSMSControllerModPluginEx.controller.controller.getPovY() != povYRestingState) {
            retval = povYChanged = true;
        }
        return !retval;
    }

    public
    void resetControllerState() {
        Arrays.fill(buttonsChanged, false);
        Arrays.fill(axesChanged, false);
        povXChanged = false;
        povYChanged = false;
    }

    public void setMappingIndices(LogicalButtons btn, ControllerMapping tempMapping) {
        var btnMapping = ButtonMapping.fromButton(btn);
        var axisMapping = AxisMapping.fromButton(btn);
        if(btnMapping != null) {
            for(int i = 0; i < controller.getButtonCount(); i++) {
                if(buttonsChanged[i]) {
                    tempMapping.mapButton(new ControllerMapping.ButtonData(btnMapping, i));
                    break;
                }
            }
        }
        if(axisMapping != null) {
            for (int i = 0; i < SSMSControllerModPluginEx.controller.controller.getAxisCount(); i++) {
                if (axesChanged[i]) {
                    tempMapping.mapAxis(new ControllerMapping.AxisData(axisMapping, null, i));
                    break;
                }
            }
        }
        if(povXChanged) {
            tempMapping.mapPov(AxisMapping.DPadX);
        }
        if(povYChanged) {
            tempMapping.mapPov(AxisMapping.DPadY);
        }
    }
}
