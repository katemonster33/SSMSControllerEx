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
package ssms.controller.inputScreens;

import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.util.Pair;
import ssms.controller.Indicators;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Malte Schulze
 */
public class InputScreen_Bluescreen implements InputScreen {
    public static final String ID = "NoScreen";
    public static final String SCOPES = "";

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return null;
    }

    @Override
    public void deactivate() {
    }

    @Override
    public void activate(Object ...args) {
    }
    
    @Override
    public void renderInWorld(ViewportAPI viewport) {
    }

    @Override
    public void renderUI(ViewportAPI viewport) {
    }

    @Override
    public void preInput(float advance) {
    }

    @Override
    public void postInput(float advance) {
    }

    @Override
    public String getId() { return ID; }

    @Override
    public String[] getScopes() { return new String[]{ SCOPES }; }
}
