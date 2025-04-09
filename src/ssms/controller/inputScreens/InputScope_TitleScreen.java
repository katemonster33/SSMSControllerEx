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

import com.fs.starfarer.title.TitleScreenState;
import com.fs.state.AppDriver;

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
public class InputScope_TitleScreen implements InputScope {
    public static final String ID = "TitleScreen";
    public static final String DEFAULT_SCREEN = "TitleScreen";
    TitleScreenState titleScreen = null;
    List<ButtonAPI> titleScreenButtons = null;
    UIPanelAPI mainMenuPanel = null;
    MethodHandle doButtonClick = null;
    int selectedButton = -1;
    @Override
    public void activate(Object ...args) {
        TitleScreenState titlescreen  = (TitleScreenState)AppDriver.getInstance().getCurrentState();
        UIPanelAPI panel = titlescreen.getScreenPanel();
        UIPanelReflector.initialize(panel.getClass());
        var widgets = UIPanelReflector.getChildItems(panel);
        if(!widgets.isEmpty() && UIPanelAPI.class.isAssignableFrom(widgets.get(0).getClass())) {
            var mainMenu = (UIPanelAPI)widgets.get(0);
            try {
                var getMainMenu = ClassReflector.GetInstance().findDeclaredMethod(mainMenu.getClass(), "getMainMenu");
                mainMenuPanel = (UIPanelAPI) MethodReflector.GetInstance().invoke(getMainMenu, mainMenu);
                doButtonClick = MethodHandles.lookup().findVirtual(mainMenuPanel.getClass(), "actionPerformed", MethodType.methodType(void.class, Object.class, Object.class));
                var mainMenuWidgets = UIPanelReflector.getChildItems(mainMenuPanel);
                if(!mainMenuWidgets.isEmpty())
                {
                    titleScreenButtons = UIPanelReflector.getChildButtons((UIPanelAPI)mainMenuWidgets.get(0));
                }
            } catch(Throwable ex) {
                Global.getLogger(getClass()).log(Level.FATAL, "Couldn't get the main menu buttons!");
            }
        }
    }

    @Override
    public void deactivate() {
        
    }

    @Override
    public String getId() { return ID; }

    @Override
    public String getDefaultScreen() { return DEFAULT_SCREEN; }

    public void selectNextButton()
    {
        if(titleScreenButtons != null && !titleScreenButtons.isEmpty()) {
            int oldSelectedButton = selectedButton;
            if(selectedButton == -1) {
                selectedButton = 0;
            } else if(selectedButton < (titleScreenButtons.size() - 1)) {
                selectedButton++;
            }
            if(!titleScreenButtons.get(selectedButton).isEnabled()) {
                selectedButton++;
            }
            if(selectedButton >= titleScreenButtons.size()) {
                selectedButton = 0;
            }
            if(selectedButton != oldSelectedButton && oldSelectedButton != -1) {
                titleScreenButtons.get(oldSelectedButton).unhighlight();
            }
            titleScreenButtons.get(selectedButton).highlight();
        }
    }

    public void selectPrevButton()
    {
        if(titleScreenButtons != null && !titleScreenButtons.isEmpty()) {
            int oldSelectedButton = selectedButton;
            if(selectedButton == -1) {
                selectedButton = 0;
            } else if(selectedButton > 0) {
                selectedButton--;
            }
            if(!titleScreenButtons.get(selectedButton).isEnabled()) {
                selectedButton--;
            }
            if(selectedButton < 0) {
                selectedButton = titleScreenButtons.size() - 1;
            }
            if(selectedButton != oldSelectedButton && oldSelectedButton != -1) {
                titleScreenButtons.get(oldSelectedButton).unhighlight();
            }
            titleScreenButtons.get(selectedButton).highlight();
        }
    }

    public void clickButton()
    {
        if(selectedButton != -1 && titleScreenButtons != null && selectedButton < titleScreenButtons.size()) {
            try {
                doButtonClick.invoke(mainMenuPanel, null, titleScreenButtons.get(selectedButton));
            } catch(Throwable ex) {
                Global.getLogger(getClass()).log(Level.ERROR, "couldn't fire button event!");
            }
            //titleScreenButtons.get(selectedButton).
        }
    }
}
