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

/**
 * The no scope. With an empty input screen as a fallback for the InputScreenManager.
 * 
 * @author Malte Schulze
 */
@InputScopeOption_ID("NoScope")
@InputScopeOption_DefaultScreen("NoScreen")
public class InputScope_360 implements InputScope {

    @Override
    public void activate(Object ...args) {
        
    }

    @Override
    public void deactivate() {
        
    }
    
}
