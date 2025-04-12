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
package ssms.controller.titlescreen;

import com.fs.starfarer.title.TitleScreenState;
import com.fs.state.AppDriver;

import ssms.controller.inputScreens.InputScope;
import ssms.controller.reflection.ClassReflector;
import ssms.controller.reflection.MethodReflector;
import ssms.controller.reflection.UIPanelReflector;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

import org.apache.log4j.Level;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

/**
 * The no scope. With an empty input screen as a fallback for the InputScreenManager.
 * 
 * @author Malte Schulze
 */
public class TitleScreenScope implements InputScope {
    public static final String ID = "TitleScreen";
    public static final String DEFAULT_SCREEN = "TitleScreen";
    @Override
    public void activate(Object ...args) {
    }

    @Override
    public void deactivate() {
        
    }

    @Override
    public String getId() { return ID; }

    @Override
    public String getDefaultScreen() { return DEFAULT_SCREEN; }


}
