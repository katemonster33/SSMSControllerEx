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

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.impl.combat.MineStrikeStats;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.combat.entities.Ship;
import com.fs.starfarer.prototype.Utils;
import com.fs.state.AppDriver;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.reflection.ClassReflector;
import ssms.controller.reflection.CombatStateReflector;
import ssms.controller.reflection.WeaponReflection;
import ssms.controller.steering.SteeringController;
import ssms.controller.steering.SteeringController_FreeFlight;
import ssms.controller.steering.SteeringController_OrbitTarget;

/**
 *
 * @author Malte Schulze
 */
public class InputScreen_BattleSteering implements InputScreen {
    public static final String ID = "BattleSteering";
    public static final String SCOPES = "Battle";
    protected HandlerController handler;
    protected InputScope_Battle scope;
    protected CombatEngineAPI engine;
    protected CombatStateReflector csr;
    protected InputScope_Battle.PlayerShipCache psCache;
    protected boolean isAlternateSteering = false;
    private boolean adjustOmniShieldFacing = false;
    private Vector2f v1 = new Vector2f();
    protected List<Pair<Indicators, String>> screenIndicators, indicators;
    protected SteeringController lastSteeringController;

    public InputScreen_BattleSteering() {
        screenIndicators = new ArrayList<>();
        screenIndicators.add(new Pair<>(Indicators.LeftStickButton, "Switch Mode"));
        screenIndicators.add(new Pair<>(Indicators.A, "Fire"));
        screenIndicators.add(new Pair<>(Indicators.B, "Shield"));
        screenIndicators.add(new Pair<>(Indicators.X, "System"));
        screenIndicators.add(new Pair<>(Indicators.Y, "Vent"));
        screenIndicators.add(new Pair<>(Indicators.Start, "Menu"));
        screenIndicators.add(new Pair<>(Indicators.Select, "Targeting"));
        screenIndicators.add(new Pair<>(Indicators.RightStickUp, "Toggle Fighters"));
        screenIndicators.add(new Pair<>(Indicators.RightStickDown, "Toggle Autofire"));
        screenIndicators.add(new Pair<>(Indicators.RightStickLeft, "Prev Wpn Grp"));
        screenIndicators.add(new Pair<>(Indicators.RightStickRight, "Next Wpn Grp"));
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }
    
    private void updateIndicators(SteeringController currentSteeringController) {
        if ( lastSteeringController == currentSteeringController ) return;
        if ( indicators == null ) indicators = new ArrayList<>(screenIndicators);
        else {
            indicators.clear();
            indicators.addAll(screenIndicators);
        }
        if ( currentSteeringController != null )
            indicators.addAll(currentSteeringController.getIndicators());
        lastSteeringController = currentSteeringController;
    }

    @Override
    public void deactivate() {
        handler = null;
        scope = null;
        csr = null;
        engine = null;
        psCache = null;
    }

    @Override
    public void activate(Object... args) {
        handler = SSMSControllerModPluginEx.controller;
        scope = (InputScope_Battle)InputScreenManager.getInstance().getCurrentScope();
        csr = CombatStateReflector.GetInstance();
        engine = scope.engine;
        psCache = scope.psCache;
    }
    
    protected boolean processShipInputs(ShipAPI ps) {
        return scope.isControllerSteeringEnabled() && engine.isEntityInPlay(ps) && !ps.isHulk() && !ps.controlsLocked();
    }
    
    @Override
    public void preInput(float amount) {
        ShipAPI ps = psCache.ps;
        if ( processShipInputs(ps) ) {
            //autopilot flag is inverted!
            if ( engine.isUIAutopilotOn() && !engine.isPaused() && amount > 0f ) {
                if ( handler.getButtonEvent(HandlerController.Buttons.Select) == 1 ) {
                    InputScreenManager.getInstance().transitionDelayed("BattleTargeting");
                }
                if ( handler.getButtonEvent(HandlerController.Buttons.Start) == 1 ) {
                    InputScreenManager.getInstance().transitionDelayed("BattleMenu");
                }
                if ( handler.getButtonEvent(HandlerController.Buttons.LeftStickButton) == 1 ) {
                    isAlternateSteering = !isAlternateSteering;
                    if ( isAlternateSteering ) {
                        psCache.setSteeringController(SteeringController_OrbitTarget.class, handler, engine);
                    } else {
                        psCache.setSteeringController(SteeringController_FreeFlight.class, handler, engine);
                    }
                }

                if ( isAlternateSteering && ( !scope.isValidTarget(ps.getShipTarget()) || !psCache.steeringController.isTargetValid() ) ) {
                    isAlternateSteering = false;
                    psCache.setSteeringController(SteeringController_FreeFlight.class, handler, engine);
                }
                updateIndicators(psCache.steeringController);
                psCache.steeringController.steer(amount, scope.getOffsetFacingAngle());

                Vector2f targetLocation;
                if ( ps.getSelectedGroupAPI() != null ) {
                    List<WeaponAPI> weapons = ps.getSelectedGroupAPI().getWeaponsCopy();
                    for ( WeaponAPI weapon : weapons ) {
                        if ( weapon.isDisabled() ) continue;
                        if ( ps.getShipTarget() != null ) {
                            targetLocation = targetLeading(weapon.getLocation(),ps.getShipTarget().getLocation(),ps.getShipTarget().getVelocity(),weapon.getProjectileSpeed(),v1);
                        } else {
                            targetLocation = targetFrontal(ps.getLocation(),weapon.getRange(),ps.getFacing(),v1);
                        }

                        if ( targetLocation == null ) targetLocation = targetFrontal(ps.getLocation(),weapon.getRange(),ps.getFacing(),v1);

                        WeaponReflection.AimWeapon(weapon, targetLocation);
                    }
                    if ( handler.isButtonAPressed() ) ps.giveCommand(ShipCommand.FIRE, v1, -1);
                }

                //start venting
                if ( handler.getButtonEvent(HandlerController.Buttons.Y) == 1 ) {
                    ps.giveCommand(ShipCommand.VENT_FLUX, null, -1);
                }

                //TODO maybe adjust shield facing in the after input processed method if it got turned on this frame
                //shield/cloak on/off
                if ( handler.getButtonEvent(HandlerController.Buttons.B) == 1 ) {
                    if ( ps.getShield() != null ) {
                        if ( ps.getShield().getType() == ShieldAPI.ShieldType.OMNI) {
                            CombatStateReflector.GetInstance().setAutoOmniShield();
                            //we only want auto shields if they are turned on otherwise the AI decides when to turn on the shields as well
                            try {
                                var flags = CombatStateReflector.GetInstance().playerShipShieldAIFlags();
                                if ( ps.getShield() != null && ps.getShield().isOff() ) {
                                    flags.unsetFlag(ShipwideAIFlags.AIFlags.DO_NOT_USE_SHIELDS);
                                    flags.setFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON, Float.MAX_VALUE);
                                    adjustOmniShieldFacing = true;
                                } else {
                                    flags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_USE_SHIELDS, Float.MAX_VALUE);
                                    flags.unsetFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON);
                                }
                            } catch ( Throwable t ) {
                                Global.getLogger(SSMSControllerModPluginEx.class).log(Level.ERROR, "Failed to get field playerShipShieldAIFlags on CombatState!", t);
                                ps.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, -1);
                            }
                        } else {
                            ps.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, -1);
                        }
                    } else if ( ps.getPhaseCloak() != null ) {
                        
                        ps.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, -1);
                    }
                }

                //activate system
                if ( handler.getButtonEvent(HandlerController.Buttons.X) == 1 ) {
                    if ( ps.getShipTarget() != null ) {
                        //due to a bug in vanilla coding the getAI method must return not null in order for the minestrike to use the override
                        //replacing the script with a corrected version that skips the AI check
                        //TODO make the script replacement work again!
                        // Object script = UtilObfuscation.TryGetScript(ps.getSystem());
                        // if ( script != null ) {
                        //     if ( MineStrikeStats.class == script.getClass() ) {
                        //         UtilObfuscation.SetScript(ps., new MineStrikeStatsFixed());
                        //     }
                        // }
                        ps.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS, 1, ps.getShipTarget().getLocation());
                        ps.giveCommand(ShipCommand.USE_SYSTEM, null, -1);
                    } else ps.giveCommand(ShipCommand.USE_SYSTEM, null, -1);
                }

                //second joystick cycles fighter modes and weapon groups if not held down. up fighter mode, left right weapon groups, down autofire
                //toggle fighter mode
                if ( psCache.hasFighters && handler.getButtonEvent(HandlerController.Buttons.RightStickUp) == 1 ) {
                    ps.setPullBackFighters(true);
                    //ps.giveCommand(ShipCommand.PULL_BACK_FIGHTERS, null, -1);
                }
                //toggle autofire
                if ( handler.getButtonEvent(HandlerController.Buttons.RightStickDown) == 1 ) {
                    ps.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, null, ps.getWeaponGroupsCopy().indexOf(ps.getSelectedGroupAPI()));
                }
                //select weapon group
                if ( handler.getButtonEvent(HandlerController.Buttons.RightStickRight) == 1 ) {
                    List<WeaponGroupAPI> wgs = ps.getWeaponGroupsCopy();
                    int indx = wgs.indexOf(ps.getSelectedGroupAPI()) + 1;
                    if ( indx >= wgs.size() ) {
                        indx = 0;
                    }
                    ps.giveCommand(ShipCommand.SELECT_GROUP, null, indx);
                }
                if ( handler.getButtonEvent(HandlerController.Buttons.RightStickLeft) == 1 ) {
                    List<WeaponGroupAPI> wgs = ps.getWeaponGroupsCopy();
                    int indx = wgs.indexOf(ps.getSelectedGroupAPI()) - 1;
                    if ( indx < 0 ) {
                        indx = wgs.size() > 0 ? wgs.size() - 1 : 0;
                    }
                    ps.giveCommand(ShipCommand.SELECT_GROUP, null, indx);
                }
                //TODO replaced with adjustOmniShieldFacing, requires testing
                //wasShieldOn = ps.getShield() != null && ps.getShield().isOn();
            }// else wasShieldOn = false;
        }// else wasShieldOn = false;
        
        //center on player ship
        CombatStateReflector.GetInstance().SetVideoFeedToPlayerShip();
    }
    
    @Override
    public void postInput(float amount) {
        //If an omni shield was raised during the last frame we change its facing based on the selected broadside if no target is selected and otherwise facing the target
        if ( adjustOmniShieldFacing/*!wasShieldOn*/ ) {
            ShipAPI ps = psCache.ps;
            if ( ps != null ) {
                ShieldAPI s = ps.getShield();
                if ( s != null && s.isOn() && ((Ship)ps).getShield().isOmni() ) {
                    if ( ps.getShipTarget() == null ) {
                        ps.getShield().forceFacing(ps.getFacing() + scope.getOffsetFacingAngle());
                    } else {
                        Vector2f v = Vector2f.sub(ps.getShipTarget().getLocation(), ps.getLocation(), new Vector2f());
                        ps.getShield().forceFacing(Util_Steering.getFacingFromHeading(v));
                    }
                }
            }
            adjustOmniShieldFacing = false;
        }
    }
    
    @Override
    public void renderInWorld(ViewportAPI viewport) {
        //remember ... autopilot flag is inverted
        if ( engine.isUIAutopilotOn() && psCache.steeringController != null ) {
            psCache.steeringController.renderInWorldCoords(viewport, scope.getOffsetFacingAngle());
        }
    }

    @Override
    public void renderUI(ViewportAPI viewport) {
        
    }
    
    protected Vector2f targetFrontal(Vector2f vPosition, float range, float facing, Vector2f result) {
        float rad = (float) Math.toRadians(facing);
        result.set(vPosition.x + range * (float)Math.cos(rad), vPosition.y + range * (float)Math.sin(rad));
        return result;
    }
    
    protected Vector2f targetLeading(Vector2f vShooterPos, Vector2f vTargetPos, Vector2f vTargetVelocity, float projectileSpeed, Vector2f result) {
        if ( projectileSpeed == 0f ) {
            return result.set(vTargetPos);
        }
        if ( projectileSpeed >= 1e10f ) projectileSpeed = 1e10f;
        float a, b, c, t1, t2;
        a = Vector2f.dot(vTargetVelocity, vTargetVelocity) - projectileSpeed * projectileSpeed;
        Vector2f.sub(vTargetPos, vShooterPos, result);
        b = 2f * Vector2f.dot(result, vTargetVelocity);
        c = Vector2f.dot(result, result);
        float sqrt = (float)Math.sqrt(b*b-4*a*c);
        t1 = (-b + sqrt)/(2*a);
        t2 = (-b - sqrt)/(2*a);
        
        if (!Float.isNaN(t1) && t1 > 0 ) {
            result.set(vTargetPos.x+t1*vTargetVelocity.x,vTargetPos.y+t1*vTargetVelocity.y);
            return result;
        } else if (!Float.isNaN(t2) && t2 > 0 ) {
            result.set(vTargetPos.x+t2*vTargetVelocity.x,vTargetPos.y+t2*vTargetVelocity.y);
            return result;
        }
        return null;
    }

    @Override
    public String getId() { return ID; }

    @Override
    public String[] getScopes() { return new String[]{ SCOPES }; }
}
