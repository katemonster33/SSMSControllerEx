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
package ssms.controller.steering;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.util.Pair;
import java.util.ArrayList;
import java.util.List;

import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.InputShim;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.HandlerController;
import ssms.controller.enums.Indicators;
import ssms.controller.reflection.CombatStateReflector;

/**
 *
 * @author Malte Schulze
 */
public class SteeringController_FreeFlight extends SteeringController_Base {
    protected ShipAPI ps;
    protected HandlerController handler;
    protected List<Pair<Indicators, String>> indicators;


    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }

    @Override
    public boolean activate(ShipAPI playerShip, HandlerController controller, CombatEngineAPI engine) {
        this.ps = playerShip;
        this.handler = controller;
        
        if ( indicators == null ) {
            indicators = new ArrayList<>();
            indicators.add(new Pair<>(null, "Directional Steering"));
            indicators.add(new Pair<>(Indicators.LeftStick, "Steer"));
            indicators.add(new Pair<>(Indicators.RightStick, "Move"));
        }
        
        return true;
    }
    
    @Override
    public void discard() {
        ps = null;
        handler = null;
    }

    @Override
    public void onTargetSelected() {
        
    }

    @Override
    public boolean isTargetValid() {
        return true;
    }

    @Override
    public void steer(float timeAdvanced, float offsetFacingAngle) {
        calculateAllowances(ps);
        //turning the ship based on joystick and accelerating with the triggers
        if (allowAcceleration) {
            if (handler.isButtonPressed(LogicalButtons.RightStickUp)) {
                ps.giveCommand(ShipCommand.ACCELERATE, null, -1);
            } else if (handler.isButtonPressed(LogicalButtons.RightStickDown)) {
                ps.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, -1);
            } else if (ps.getAcceleration() < 0.1f) {
                //if the player leaves the throttle idle close to zero we assume a full stop is desired
                ps.giveCommand(ShipCommand.DECELERATE, null, -1);
            }
        }
        if (allowStrafe) {
            if (handler.isButtonPressed(LogicalButtons.RightStickLeft)) {
                ps.giveCommand(ShipCommand.STRAFE_LEFT, null, -1);
            } else if (handler.isButtonPressed(LogicalButtons.RightStickRight)) {
                ps.giveCommand(ShipCommand.STRAFE_RIGHT, null, -1);
            }
        }
        if ( allowTurning ) {
            var vDesiredHeading = handler.getJoystick(Joystick.Left);
            if ( vDesiredHeading.getX() != 0 || vDesiredHeading.getY() != 0 ) {
                vDesiredHeading.setY(-vDesiredHeading.getY());
                float desiredFacing = Util.getFacingFromHeading((Vector2f)vDesiredHeading);
                turnToAngle(ps,desiredFacing,timeAdvanced);
            }
        }
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport, float offsetFacingAngle) {
        Vector2f shipLocation = ps.getLocation();
        var heading = handler.getJoystick(Joystick.Left);
        heading.setY(-heading.getY());
        if ( heading.getX() == 0 && heading.getY() == 0 ) {
            heading = Util.getHeadingFromFacing(ps.getFacing());
        }
        if (CombatStateReflector.GetInstance().getWidgetPanel() == null ) return;
        float zoom = CombatStateReflector.GetInstance().getZoomFactor();
        
        //a pentagon that points in the direction the ship ship wants to head into, useful since the ship turns slowly 
        //and this way the user immediately has feedback on where he is steering.
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        //HUD color for own ship
        final float cr = 155.0f/255.0f, cg = 255.0f/255.0f, cb = 0f/255.0f;
        GL11.glColor3f(cr, cg, cb);
        GL11.glLineWidth(1f);
        Vector2f vHeadingNormalised = new Vector2f(heading);
        vHeadingNormalised.normalise();

        //adjusting the size of the marker based on ship size but also constraining it to avoid silly dimensions.
        float radius = ps.getCollisionRadius() * 0.1f;
        if ( radius < 5f ) radius = 5f;
        else if ( radius > 20f ) radius = 20f;
        radius *= zoom;
        Vector2f pentagonCenter = new Vector2f(shipLocation.x + vHeadingNormalised.x * ps.getCollisionRadius() * (1f + 2f * radius / ps.getCollisionRadius()), shipLocation.y + vHeadingNormalised.y * ps.getCollisionRadius() * (1f + 2f * radius / ps.getCollisionRadius()));
        float angleIncrement = (float) Math.toRadians(360.0f / 5f);
        //rotating the pentagon so that it points in the right direction and the missing slice is opposite to that point.
        float angle = (float) Math.toRadians(Util.getFacingFromHeading(new Vector2f(heading))) + 3f * angleIncrement;

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(pentagonCenter.x, pentagonCenter.y);
        //alpha on the edges avoids sharp contours for large ships.
        GL11.glColor4f(cr, cg, cb, 0.2f);
        for (int k = 0; k < 5; ++k) {
            GL11.glVertex2f(pentagonCenter.x + radius * (float)Math.cos(angle), pentagonCenter.y + radius * (float)Math.sin(angle));
            angle += angleIncrement;
        }
        GL11.glEnd();

        // var rightJoystick = handler.getJoystick(Joystick.Right);
        // if ((heading.getX() != 0 || heading.getY() != 0) && rightJoystick.x == 0.f && rightJoystick.y == 0.f) {
        //     float mouseX = viewport.convertWorldXtoScreenX(pentagonCenter.x);
        //     float mouseY = viewport.convertWorldYtoScreenY(pentagonCenter.y);
        //     InputShim.mouseMove((int) mouseX, (int) mouseY);
        // }
    }

    @Override
    public boolean getAllowsEveryTarget() {
        return true;
    }

    @Override
    public String getLabel() {
        return "[SSMS] Directional";
    }
}