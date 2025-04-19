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

import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.util.Pair;

import java.util.List;

/**
 *
 * @author Malte Schulze
 */
public class InputScreenBase {

    public static final String ID = "NoScreen";
    public static final String SCOPES = InputScopeBase.ID;

    public List<Pair<Indicators, String>> getIndicators() {
        return null;
    }

    public void deactivate() {
    }

    public void activate(Object ...args) {
    }

    public void renderInWorld(ViewportAPI viewport) {
    }

    public void renderUI(ViewportAPI viewport) {
    }

    public void preInput(float advance) {
    }

    public void postInput(float advance) {
    }

    public String getId() { return ID; }

    public String[] getScopes() { return new String[]{ SCOPES }; }
}
