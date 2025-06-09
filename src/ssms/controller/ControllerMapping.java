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

import ssms.controller.enums.AxisId;
import ssms.controller.enums.AxisMapping;
import ssms.controller.enums.ButtonMapping;
import ssms.controller.enums.Indicators;

import java.util.*;

/**
 * Stores information on how to interpret game controller signals.
 * 
 * @author Malte Schulze
 */
public class ControllerMapping {
    public String deviceName;
    public float axisBtnConversionDeadzone = 0.85f, joystickDeadzone = 0.25f;
    public Map<String,Object> customProperties = new HashMap<>();
    public EnumMap<Indicators, String> indicators = new EnumMap<>(Indicators.class);
    EnumMap<AxisMapping, AxisData> axisDatas = new EnumMap<>(AxisMapping.class);
    EnumMap<ButtonMapping, ButtonData> buttonDatas= new EnumMap<>(ButtonMapping.class);

    public String indicatorProfile;

    public void mapAxis(AxisData axisData) {
        axisDatas.put(axisData.getAxisMapping(), axisData);
    }

    public void mapButton(ButtonData buttonData) {
        buttonDatas.put(buttonData.getButtonMapping(), buttonData);
    }

    public void mapPov(AxisMapping axisMapping) {
        axisDatas.put(axisMapping, new AxisData(axisMapping, null, 0xFF));
    }

    public List<AxisData> getMappedAxes() {
        return new ArrayList<>(axisDatas.values());
    }

    public List<ButtonData> getMappedButtons() {
        return new ArrayList<>(buttonDatas.values());
    }

    public static class AxisData {
        AxisMapping axisMapping;
        AxisId axisId;
        int axisIndex;
        public AxisData(AxisMapping axisMapping, AxisId axisId, int axisIndex) {
            this.axisMapping = axisMapping;
            this.axisId = axisId;
            this.axisIndex = axisIndex;
        }

        public AxisMapping getAxisMapping() {
            return axisMapping;
        }

        public AxisId getAxisId() {
            return axisId;
        }

        public int getAxisIndex() {
            return axisIndex;
        }
    }

    public static class ButtonData {
        ButtonMapping buttonMapping;
        int buttonIndex;
        public ButtonData(ButtonMapping buttonMapping, int buttonIndex) {
            this.buttonMapping = buttonMapping;
            this.buttonIndex = buttonIndex;
        }

        public ButtonMapping getButtonMapping() {
            return buttonMapping;
        }

        public int getButtonIndex() {
            return buttonIndex;
        }
    }
}
