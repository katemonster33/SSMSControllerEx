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
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Pair;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.HandlerController;
import ssms.controller.enums.Indicators;
import ssms.controller.reflection.CombatStateReflector;
import ssms.controller.reflection.ShipEngineControllerReflection;

/**
 *
 * @author Malte Schulze
 */
public class SteeringController_OrbitTarget extends SteeringController_Base {
    protected ShipAPI ps;
    protected HandlerController handler;
    protected float desiredDistance;
    protected ReadableVector2f vDesiredHeadingLastValidInput;
    protected List<Pair<Indicators, String>> indicators;

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }

    @Override
    public boolean activate(ShipAPI playerShip, HandlerController controller, CombatEngineAPI engine) {
        if ( playerShip.getShipTarget() == null ) return false;
        this.ps = playerShip;
        this.handler = controller;
        desiredDistance = calculateDesiredDistanceForOrbitingTarget(ps);
        vDesiredHeadingLastValidInput = Vector2f.sub(ps.getLocation(), ps.getShipTarget().getLocation(), new Vector2f());
        
        if ( indicators == null ) {
            indicators = new ArrayList<>();
            indicators.add(new Pair<>(null, "Orbital Steering"));
            indicators.add(new Pair<>(Indicators.LeftStick, "Relative Pos"));
            indicators.add(new Pair<>(Indicators.LeftTrigger, "Further"));
            indicators.add(new Pair<>(Indicators.RightTrigger, "Closer"));
        }
        return true;
    }

    @Override
    public void discard() {
        ps = null;
        handler = null;
        vDesiredHeadingLastValidInput = null;
    }

    @Override
    public void onTargetSelected() {
        desiredDistance = calculateDesiredDistanceForOrbitingTarget(ps);
    }
    
    @Override
    public boolean isTargetValid() {
        return ps.getShipTarget() != null;
    }

    @Override
    public void steer(float timeAdvanced, float offsetFacingAngle) {
        calculateAllowances(ps);
        
        if ( handler.isButtonPressed(LogicalButtons.RightTrigger) ) {
            desiredDistance -= Math.max(ps.getEngineController().getMaxSpeedWithoutBoost(), 400f) * timeAdvanced;
            desiredDistance = Math.max(ps.getCollisionRadius() + ps.getShipTarget().getCollisionRadius(), desiredDistance);
        } else if ( handler.isButtonPressed(LogicalButtons.LeftTrigger) ) {
            desiredDistance += Math.max(ps.getEngineController().getMaxSpeedWithoutBoost(), 400f) * timeAdvanced;
        }

        ReadableVector2f vDesiredHeading = handler.getLeftStick();
        if ( vDesiredHeading.getX() == 0 || vDesiredHeading.getY() == 0 ) {
            vDesiredHeading = vDesiredHeadingLastValidInput;
        }

        if ( vDesiredHeading.getX() != 0 || vDesiredHeading.getY() != 0 ) {
            advance(timeAdvanced, vDesiredHeading, desiredDistance);

            //its possible that we should turn towards the target leading average of the current weapon group  instead of the ship location, 
            //so that rigid mounts are more likely to hit
            if ( allowTurning ) turnToAngle(ps, Util.getFacingFromHeading(Vector2f.sub(ps.getShipTarget().getLocation(), ps.getLocation(), new Vector2f()))+offsetFacingAngle,timeAdvanced);

            vDesiredHeadingLastValidInput = vDesiredHeading;
        }
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport, float offsetFacingAngle) {
        if ( !isTargetValid() ) return;
        
        ReadableVector2f heading = handler.getLeftStick();
        if ( heading.getX() == 0 && heading.getY() == 0 ) {
            heading = Util.getHeadingFromFacing(ps.getFacing());
        }
        float zoom = CombatStateReflector.GetInstance().getZoomFactor();
        
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        //HUD color for own ship
        final float cr = 155.0f/255.0f, cg = 255.0f/255.0f, cb = 0f/255.0f;
        GL11.glColor3f(cr, cg, cb);
        GL11.glLineWidth(1f);
        Vector2f vHeadingNormalised = new Vector2f(heading);
        vHeadingNormalised.normalise();

        float f = heading.lengthSquared();
        if ( f != 1f ) {
            f = (float) Math.sqrt(f);
        }
        Vector2f pentagonCenter = new Vector2f(heading);
        if ( f > 0f ) pentagonCenter.scale(desiredDistance/f);
        Vector2f.add(ps.getShipTarget().getLocation(), pentagonCenter, pentagonCenter);

        //adjusting the size of the marker based on ship size but also constraining it to avoid silly dimensions.
        float radius = ps.getCollisionRadius() * 0.1f;
        if ( radius < 5f ) radius = 5f;
        else if ( radius > 20f ) radius = 20f;
        radius *= zoom;
        float angleIncrement = (float) Math.toRadians(360.0f / 5f);

        //rotating the pentagon so that it points in the right direction and the missing slice is opposite to that point.
        float angle = (float) Math.toRadians(Util.getFacingFromHeading(Vector2f.sub(ps.getShipTarget().getLocation(), pentagonCenter, new Vector2f())) + offsetFacingAngle) + 3f * angleIncrement;

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(pentagonCenter.x, pentagonCenter.y);
        //alpha on the edges avoids sharp contours for large ships.
        GL11.glColor4f(cr, cg, cb, 0.2f);
        for (int k = 0; k < 5; ++k) {
            GL11.glVertex2f(pentagonCenter.x + radius * (float)Math.cos(angle), pentagonCenter.y + radius * (float)Math.sin(angle));
            angle += angleIncrement;
        }
        GL11.glEnd();
    }
    
    protected float calculateDesiredDistanceForOrbitingTarget(ShipAPI ps) {
        float minSafeDistance = DamagingExplosionSpec.getShipExplosionRadius(ps.getShipTarget()) + ps.getCollisionRadius();
        float currentDistance = Vector2f.sub(ps.getShipTarget().getLocation(), ps.getLocation(), new Vector2f()).lengthSquared();
        if ( minSafeDistance * minSafeDistance > currentDistance ) return minSafeDistance;
        else return (float) Math.sqrt(currentDistance);
    }
    
    public void advance(float time, ReadableVector2f vDesiredHeading, float desiredDistance) {
        if ( !allowAcceleration && !allowStrafe ) return;
            
        MutableShipStatsAPI stats = ps.getMutableStats();
        float maxSpeed = Math.max(1f, stats.getMaxSpeed().getModifiedValue());
        float effectiveAcceleration = Math.max(1f, stats.getAcceleration().getModifiedValue());
        float effectiveDeceleration = Math.max(1f, stats.getDeceleration().getModifiedValue());
        float effectiveStrafeAcceleration = ShipEngineControllerReflection.getEffectiveStrafeAcceleration(ps.getEngineController());
        
        Vector2f error = getAxisAcceleration(vDesiredHeading, desiredDistance, new Vector2f());
        if ( !allowAcceleration ) error.x = 0;
        if ( !allowStrafe ) error.y = 0;
        
        float targetAcceleration, targetStrafeAcceleration;
        if ( error.x > 0 ) {
            if ( error.x > effectiveAcceleration ) {
                targetAcceleration = Math.min(error.x, maxSpeed);
            } else {
                targetAcceleration = Math.min(error.x, effectiveAcceleration);
            }
        } else {
            if ( error.x < -effectiveDeceleration ) {
                targetAcceleration = Math.max(error.x, -maxSpeed);
            } else {
                targetAcceleration = Math.max(error.x, -effectiveDeceleration);
            }
        }
        if ( error.y > 0 ) {
            if ( error.y > effectiveStrafeAcceleration ) {
                targetStrafeAcceleration = Math.min(error.y, maxSpeed);
            } else {
                targetStrafeAcceleration = Math.min(error.y, effectiveStrafeAcceleration);
            }
        } else {
            if ( error.y < -effectiveStrafeAcceleration ) {
                targetStrafeAcceleration = Math.max(error.y, -maxSpeed);
            } else {
                targetStrafeAcceleration = Math.max(error.y, -effectiveStrafeAcceleration);
            }
        }
        
        Vector2f velocity = ps.getVelocity();
        Vector2f vShipForwardDirection = Util.getHeadingFromFacing(ps.getFacing());
        Vector2f vShipStrafingLeftDirection = Util.getHeadingFromFacing(ps.getFacing() + 90f);
        
        velocity.set(velocity.x + (vShipForwardDirection.x * targetAcceleration + vShipStrafingLeftDirection.x * targetStrafeAcceleration) * time, 
                velocity.y + (vShipForwardDirection.y * targetAcceleration + vShipStrafingLeftDirection.y * targetStrafeAcceleration) * time);
        
        //not necessary since the "drifting" code will clamp it like this.
        float velocityAcceleration = velocity.lengthSquared();
        if ( velocityAcceleration > maxSpeed * maxSpeed ) {
            velocityAcceleration = (float) Math.sqrt(velocityAcceleration);
            float f = Math.max(effectiveAcceleration, effectiveDeceleration) * 2.0F * time;
            if (velocityAcceleration - f < maxSpeed)
              f = velocityAcceleration - maxSpeed;
            velocity.set(velocity.x * (velocityAcceleration - f) / velocityAcceleration, velocity.y * (velocityAcceleration - f) / velocityAcceleration);
        }
    }
    
    protected Vector2f getAxisAcceleration(ReadableVector2f vDesiredHeading, float desiredDistance, Vector2f result) {
        float f = vDesiredHeading.lengthSquared();
        if ( f != 1f ) {
            f = (float) Math.sqrt(f);
        }
        
        Vector2f vDesiredLocation = new Vector2f(vDesiredHeading);
        if ( f > 0f ) vDesiredLocation.scale(desiredDistance/f);
        Vector2f.add(ps.getShipTarget().getLocation(), vDesiredLocation, vDesiredLocation);
        Vector2f.sub(vDesiredLocation, ps.getVelocity(), vDesiredLocation);
        Vector2f.add(ps.getShipTarget().getVelocity(), vDesiredLocation, vDesiredLocation);
        Vector2f vShipFacing = Util.getHeadingFromFacing(ps.getFacing());
        
        float accelerationToReachDesiredLocation, strafingToReachDesiredLocation;
        Vector2f v1 = new Vector2f();
        Vector2f vShipLocation = ps.getLocation();
        float distanceToDesiredLocation = Vector2f.sub(vDesiredLocation, vShipLocation, v1).length();
        if ( Math.abs(vShipFacing.x) > 1e-5 ) {
            accelerationToReachDesiredLocation = (float) (Math.cos(Vector2f.angle(vShipFacing, v1))) * distanceToDesiredLocation;
            strafingToReachDesiredLocation = (vDesiredLocation.y - vShipLocation.y - vShipFacing.y * accelerationToReachDesiredLocation) / vShipFacing.x;
        } else {
            strafingToReachDesiredLocation = (float) (Math.sin(Vector2f.angle(vShipFacing, v1))) * distanceToDesiredLocation;
            if ( vShipFacing.y < 0 ) strafingToReachDesiredLocation = -strafingToReachDesiredLocation;
            accelerationToReachDesiredLocation = (vDesiredLocation.y - vShipLocation.y) / vShipFacing.y;
        }
        
        result.set(accelerationToReachDesiredLocation, strafingToReachDesiredLocation);
        return result;
    }

    @Override
    public boolean getAllowsEveryTarget() {
        return false;
    }

    @Override
    public String getLabel() {
        return "[SSMS] Orbital";
    }
}
