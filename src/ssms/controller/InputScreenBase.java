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

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import ssms.controller.enums.AxisMapping;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.AbstractButtonInputHandler;
import ssms.controller.reflection.CampaignStateReflector;
import ssms.controller.reflection.CombatStateReflector;
import ssms.controller.reflection.TitleScreenStateReflector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author Malte Schulze
 */
public class InputScreenBase {

    public static final String ID = "NoScreen";
    public static final String SCOPES = InputScopeBase.ID;
    protected List<Pair<Indicators, String>> indicators;
    protected HashMap<LogicalButtons, Consumer<Boolean>> buttonHandlers;
    protected HashMap<AxisMapping, Object> axisHandlers;
    protected Object leftJoystickHandler;
    protected Object rightJoystickHandler;
    protected HandlerController controller;

    public InputScreenBase() {
        buttonHandlers = new HashMap<>();
        indicators = new ArrayList<>();
        controller = SSMSControllerModPluginEx.controller;
    }

    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }

    public void deactivate() {
    }

    public void activate(Object ...args) throws Throwable {
    }

    public void renderInWorld(ViewportAPI viewport) {
    }

    public void renderUI(ViewportAPI viewport) {
    }

    public void preInput(float advance) {
        for(var btnHandler : buttonHandlers.values()) {
            //btnHandler.pro(advance);
        }
    }

    public final void processButtonEvents(List<Pair<LogicalButtons, Boolean>> buttonEvents) {
        for(var btnEvent : buttonEvents) {

        }
    }

    public void postInput(float advance) {
    }

    public String getId() { return ID; }

    public String[] getScopes() { return new String[]{ SCOPES }; }

    protected void addHandler(String msg, LogicalButtons logicalButtons, Consumer<Float> btnPressHandler) {
        var indicator = Indicators.fromButton(logicalButtons);
        if(indicator == null) {
            Global.getLogger(getClass()).warn("given button doesn't translate to indicator! " + handler.getButtons());
            return;
        }
        buttonHandlers.put(logicalButtons, btnPressHandler);
        indicators.add(new Pair<>(indicator, msg));
    }

    protected void clearHandlers() {
        buttonHandlers.clear();
    }

    public UIPanelAPI getPanelForIndicators() {
        return switch (Global.getCurrentState()) {
            case TITLE -> TitleScreenStateReflector.GetInstance().getScreenPanel();
            case CAMPAIGN -> CampaignStateReflector.GetInstance().getScreenPanel();
            case COMBAT -> CombatStateReflector.GetInstance().getWidgetPanel();
        };
    }

    public Alignment getIndicatorsAlignment() {
        return switch (Global.getCurrentState()) {
            case TITLE -> Alignment.BL;
            case CAMPAIGN -> Alignment.RMID;
            case COMBAT -> Alignment.BR;
        };
    }
}
