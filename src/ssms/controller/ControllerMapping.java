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

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Pair;

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
    EnumMap<Axes, AxisData> axisDatas = new EnumMap<>(Axes.class);
    EnumMap<Buttons, ButtonData> buttonDatas= new EnumMap<>(Buttons.class);

    public String indicatorProfile;

    public void mapAxis(AxisData axisData) {
        axisDatas.put(axisData.getAxis(), axisData);
    }

    public void mapButton(ButtonData buttonData) {
        buttonDatas.put(buttonData.getButtons(), buttonData);
    }

    public void mapPov(Axes axis) {
        axisDatas.put(axis, new AxisData(axis, null, 0xFF, null, 0xFF));
    }

    public List<AxisData> getMappedAxes() {
        return new ArrayList<>(axisDatas.values());
    }

    public List<ButtonData> getMappedButtons() {
        return new ArrayList<>(buttonDatas.values());
    }

    public static class AxisData {
        Axes axis;
        AxisId xAxisId;
        int xAxisIndex;
        AxisId yAxisId;
        int yAxisIndex;
        public AxisData(Axes axis, AxisId xAxisId, int xAxisIndex, AxisId yAxisId, int yAxisIndex) {
            this.axis = axis;
            this.xAxisId = xAxisId;
            this.xAxisIndex = xAxisIndex;
            this.yAxisId = yAxisId;
            this.yAxisIndex = yAxisIndex;
        }

        public Axes getAxis() {
            return axis;
        }

        public AxisId getXAxisId() {
            return xAxisId;
        }

        public int getXAxisIndex() {
            return xAxisIndex;
        }

        public AxisId getYAxisId() {
            return yAxisId;
        }

        public int getYAxisIndex() {
            return yAxisIndex;
        }
    }

    public static class ButtonData {
        Buttons buttons;
        int buttonIndex;
        public ButtonData(Buttons buttons, int buttonIndex) {
            this.buttons = buttons;
            this.buttonIndex = buttonIndex;
        }

        public Buttons getButtons() {
            return buttons;
        }

        public int getButtonIndex() {
            return buttonIndex;
        }
    }
}
