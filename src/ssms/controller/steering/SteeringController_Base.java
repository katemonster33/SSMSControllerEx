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

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;

/**
 * Provides common functionality to steering controllers.
 * 
 * @author Malte Schulze
 */
public abstract class SteeringController_Base implements SteeringController {
    protected boolean allowAcceleration, allowTurning, allowStrafe;
    
    protected void calculateAllowances(ShipAPI ps) {
        ShipSystemAPI system = ps.getSystem();
        if ( system != null && system.isOn() ) {
            allowTurning = system.getSpecAPI().isTurningAllowed();
            allowAcceleration = system.getSpecAPI().isAccelerateAllowed() && !system.getSpecAPI().isAlwaysAccelerate();
            allowStrafe = system.getSpecAPI().isStrafeAllowed();
        } else {
            if ( !allowTurning ) allowTurning = true;
            if ( !allowAcceleration ) allowAcceleration = true;
            if ( !allowStrafe ) allowStrafe = true;
        }
    }
    
    protected void turnToAngle(ShipAPI ps, float desiredFacing, float timePassed) {
        float facing = ps.getFacing();
        if ( desiredFacing < 0f ) desiredFacing += 360f;

        boolean turnLeft;
        if ( facing < desiredFacing ) turnLeft = desiredFacing - facing < 180f;
        else turnLeft = facing - desiredFacing > 180f;

        float f2 = ps.getAngularVelocity();
        if (Math.abs(f2) > ps.getMaxTurnRate() || Math.abs(f2)*0.8f > Math.abs(desiredFacing-facing)) {
            //current turn rate is above the max turn rate or it needs to be dampend to avoid overshooting the turn, 
            //the turning rate gets reduced by twice the current acceleration to at most zero
            float f = Math.signum(f2) * timePassed * ps.getTurnAcceleration() * 2.0f;
            if (Math.abs(f2) < Math.abs(f)) {
                f = f2;
            }
            f2 -= f;
        } else {
            float f3 = turnLeft ? Math.min(Math.abs(desiredFacing-facing),timePassed * ps.getTurnAcceleration())
                : Math.max(-Math.abs(desiredFacing-facing),-timePassed * ps.getTurnAcceleration());
            
            float f4 = Math.abs(f2 + f3);
            if (f4 > ps.getMaxTurnRate()) {
                //combinedTurning is capped at max turn rate
                f2 = turnLeft ? ps.getMaxTurnRate(): -ps.getMaxTurnRate();
            } else if (f4 <= ps.getMaxTurnRate()) {
                //combinedTurning is below max turn rate and can be used
                f2 += f3;
            }
        }
        ps.setAngularVelocity(f2);
    }
}
