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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores information on how to interpret game controller signals.
 * 
 * @author Malte Schulze
 */
public class ControllerMapping {
    public String deviceName;
    public float axisBtnConversionDeadzone = 0.85f, joystickDeadzone = 0.25f;
    public Map<String,Object> customProperties = new HashMap<>();
    public EnumMap<Indicators, SpriteAPI> indicators;
    
    public AxisId axisIdLX, axisIdLY, axisIdRX, axisIdRY, axisIdLT, axisIdRT, axisIdDpadX, axisIdDpadY;
    public Integer axisIndexLX, axisIndexLY, axisIndexRX, axisIndexRY, axisIndexLT, axisIndexRT, axisIndexDpadX, axisIndexDpadY;
    public Integer btnA, btnB, btnX, btnY, btnBumperLeft, btnBumperRight, btnStart, btnSelect, btnLeftStick, btnRightStick, btnLeftTrigger, btnRightTrigger;
}
