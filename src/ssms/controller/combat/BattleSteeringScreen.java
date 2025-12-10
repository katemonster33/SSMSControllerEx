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
package ssms.controller.combat;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.combat.entities.Ship;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Level;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.campaign.MainCampaignUI;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.CodexUI;
import ssms.controller.reflection.CombatStateReflector;
import ssms.controller.reflection.MethodReflector;
import ssms.controller.reflection.UIPanelReflector;
import ssms.controller.reflection.WeaponReflection;
import ssms.controller.steering.SteeringController;
import ssms.controller.steering.SteeringController_FreeFlight;
import ssms.controller.steering.SteeringController_OrbitTarget;
import ssms.controller.steering.Util;

/**
 *
 * @author Malte Schulze
 */
public class BattleSteeringScreen extends InputScreenBase {
    public static final String ID = "BattleSteering";
    public static final String SCOPES = "Battle";
    protected BattleScope scope;
    protected CombatEngineAPI engine;
    protected CombatStateReflector csr;
    protected BattleScope.PlayerShipCache psCache;
    protected boolean isAlternateSteering = false;
    private boolean adjustOmniShieldFacing = false;
    private Vector2f v1 = new Vector2f();
    protected List<Pair<Indicators, String>> screenIndicators;
    protected SteeringController lastSteeringController;
    boolean cameraPanningMode = false;
    boolean rightStickActive = false;
    ViewportAPI combatViewport = null;
    UIPanelReflector combatWidgetPanel;
    UIPanelReflector combatView;

    public BattleSteeringScreen() {
        screenIndicators = new ArrayList<>();
        screenIndicators.add(new Pair<>(Indicators.LeftStickButton, "Switch Mode"));
        screenIndicators.add(new Pair<>(Indicators.RightTrigger, "Fire"));
        screenIndicators.add(new Pair<>(Indicators.LeftTrigger, "Shield"));
        screenIndicators.add(new Pair<>(Indicators.BumperLeft, "Prev Wpn Grp"));
        screenIndicators.add(new Pair<>(Indicators.BumperRight, "Next Wpn Grp"));
        screenIndicators.add(new Pair<>(Indicators.A, "System"));
        screenIndicators.add(new Pair<>(Indicators.X, "Vent"));
        screenIndicators.add(new Pair<>(Indicators.Y, "Toggle Fighters"));
        screenIndicators.add(new Pair<>(Indicators.B, "End Combat / Settings"));
        screenIndicators.add(new Pair<>(Indicators.Start, "Menu"));
        screenIndicators.add(new Pair<>(Indicators.Select, "Targeting"));
        screenIndicators.add(new Pair<>(Indicators.DPadLeft, "Toggle Autofire"));
        screenIndicators.add(new Pair<>(Indicators.DPadDown, "Zoom Out"));
        screenIndicators.add(new Pair<>(Indicators.DPadUp, "Zoom In"));
    }
    
    private void updateIndicators(SteeringController currentSteeringController, boolean forceUpdate) {
        if ( lastSteeringController == currentSteeringController && !forceUpdate ) return;
        if ( indicators == null ) indicators = new ArrayList<>(screenIndicators);
        else {
            indicators.clear();
            indicators.addAll(screenIndicators);
        }
        if(cameraPanningMode) {
            indicators.add(new Pair<>(Indicators.RightStickButton, "Disable Camera Panning"));
        } else {
            indicators.add(new Pair<>(Indicators.RightStickButton, "Enable Camera Panning"));
        }
        if ( currentSteeringController != null )
            indicators.addAll(currentSteeringController.getIndicators());
        lastSteeringController = currentSteeringController;
        InputScreenManager.getInstance().refreshIndicators();
    }

    @Override
    public void deactivate() {
        scope = null;
        csr = null;
        engine = null;
        psCache = null;
    }

    @Override
    public void activate(Object... args) {
        scope = (BattleScope)InputScreenManager.getInstance().getCurrentScope();
        csr = CombatStateReflector.newInstance();
        engine = scope.engine;
        psCache = scope.psCache;
        lastSteeringController = null;
        cameraPanningMode = false;
        rightStickActive = false;
        updateIndicators(psCache.steeringController, false);
        combatViewport = Global.getCombatEngine().getViewport();
        combatWidgetPanel = null;
        combatView = null;

        // if we don't do this here, when the user zooms the view, the view spazzes out because it tries to go where the (real) mouse cursor is. we don't want this.
        InputShim.mouseMove((int)(Display.getWidth() / 2.f), (int)(Display.getHeight() / 2.f));
    }
    
    protected boolean processShipInputs(ShipAPI ps) {
        return scope.isControllerSteeringEnabled() && engine.isEntityInPlay(ps) && !ps.isHulk() && !ps.controlsLocked();
    }
    
    @Override
    public void preInput(float amount) {
        if(Global.getCurrentState() == GameState.CAMPAIGN) {
            InputScreenManager.getInstance().transitionToScreen(MainCampaignUI.ID);
            return;
        }
        if(Global.getCombatEngine().getCombatUI() == null) {
            return;
        }
        if(Global.getCombatEngine().getCombatUI().isShowingDeploymentDialog()) {
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, BattleDeploymentScreen.ID, new Object[]{ csr });
            return;
        } else if(Global.getCombatEngine().getCombatUI().isShowingCommandUI()) {
            InputScreenManager.getInstance().transitionDelayed(WarroomScreen.ID);
        } 
        if(combatView == null) {
            combatWidgetPanel = new UIPanelReflector(csr.getWidgetPanel());
            combatView = new UIPanelReflector(combatWidgetPanel.getChildPanels().get(0));
        } else if(isMessageBoxShown(combatView)) {
            return;
        }
        if ( controller.getButtonEvent(LogicalButtons.Select) == 1 ) {
            InputScreenManager.getInstance().transitionDelayed(BattleTargetingScreen.ID);
        }
        if ( controller.getButtonEvent(LogicalButtons.Start) == 1 ) {
            InputScreenManager.getInstance().transitionDelayed(BattleMenuScreen.ID);
        }
        if(isCodexOpen()) {
            InputScreenManager.getInstance().transitionToScreen(CodexUI.ID, getId());
            return;
        }
        ShipAPI ps = psCache.ps;
        if(!engine.isUIAutopilotOn()) {
            Vector2f leftStick = controller.getJoystick(Joystick.Left), rightStick = controller.getJoystick(Joystick.Right);
            if(leftStick.x != 0.f || leftStick.y != 0.f || rightStick.x != 0.f || rightStick.y != 0.f) {
                CombatStateReflector.GetInstance().turnOffAutopilot();
            }
        }
        if ( processShipInputs(ps) ) {
            //autopilot flag is inverted!
            if ( engine.isUIAutopilotOn() && !engine.isPaused() && amount > 0f ) {
                if ( controller.getButtonEvent(LogicalButtons.LeftStickButton) == 1 ) {
                    isAlternateSteering = !isAlternateSteering;
                    if ( isAlternateSteering ) {
                        psCache.setSteeringController(new SteeringController_OrbitTarget(), controller, engine);
                    } else {
                        psCache.setSteeringController(SSMSControllerModPluginEx.createDefaultSteering(), controller, engine);
                    }
                }

                if ( isAlternateSteering && ( !scope.isValidTarget(ps.getShipTarget()) || !psCache.steeringController.isTargetValid() ) ) {
                    isAlternateSteering = false;
                    psCache.setSteeringController(SSMSControllerModPluginEx.createDefaultSteering(), controller, engine);
                }
                updateIndicators(psCache.steeringController, false);
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
                    if ( controller.isButtonPressed(LogicalButtons.RightTrigger) ) ps.giveCommand(ShipCommand.FIRE, v1, -1);
                }

                //start venting
                if ( controller.getButtonEvent(LogicalButtons.X) == 1 ) {
                    ps.giveCommand(ShipCommand.VENT_FLUX, null, -1);
                }

                //TODO maybe adjust shield facing in the after input processed method if it got turned on this frame
                //shield/cloak on/off
                if ( controller.getButtonEvent(LogicalButtons.LeftTrigger) == 1 ) {
                    toggleShieldOrCloak(ps);
                }

                //activate system
                if ( controller.getButtonEvent(LogicalButtons.A) == 1 ) {
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
                if ( psCache.hasFighters && controller.getButtonEvent(LogicalButtons.Y) == 1 ) {
                    ps.setPullBackFighters(true);
                    //ps.giveCommand(ShipCommand.PULL_BACK_FIGHTERS, null, -1);
                }
                //toggle autofire
                if ( controller.getButtonEvent(LogicalButtons.DpadLeft) == 1 ) {
                    ps.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, null, ps.getWeaponGroupsCopy().indexOf(ps.getSelectedGroupAPI()));
                }
                //select weapon group
                if ( controller.getButtonEvent(LogicalButtons.BumperRight) == 1 ) {
                    List<WeaponGroupAPI> wgs = ps.getWeaponGroupsCopy();
                    int indx = wgs.indexOf(ps.getSelectedGroupAPI()) + 1;
                    if ( indx >= wgs.size() ) {
                        indx = 0;
                    }
                    ps.giveCommand(ShipCommand.SELECT_GROUP, null, indx);
                }
                if ( controller.getButtonEvent(LogicalButtons.BumperLeft) == 1 ) {
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
        if(controller.getButtonEvent(LogicalButtons.B) == -1) {
            InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
        }
        if(controller.getButtonEvent(LogicalButtons.RightStickButton) == 1) {
            cameraPanningMode = !cameraPanningMode;
            updateIndicators(lastSteeringController, true);
            rightStickActive = false;
        }
        if(cameraPanningMode) {
            var rightStickVal = SSMSControllerModPluginEx.controller.getJoystick(Joystick.Right);
            // make sure the mouse stays at the center when swapping modes, so the view doesn't end up skewed off to the side...
            InputShim.mouseMove((int)(Display.getWidth() / 2.f), (int)(Display.getHeight() / 2.f));
            combatViewport.setExternalControl(false);

            Vector2f displayCenterPos = new Vector2f(Display.getWidth() / 2.f, Display.getHeight() / 2.f);
            InputShim.mouseMove((int)(displayCenterPos.x + (displayCenterPos.x * rightStickVal.getX())), (int)(displayCenterPos.y - (displayCenterPos.y * rightStickVal.getY())));
            //combatViewport.setCenter(new Vector2f(combatViewport.convertScreenXToWorldX(mousePos.x), combatViewport.convertScreenYToWorldY(mousePos.y)));
        } else {
            if(controller.isButtonPressed(LogicalButtons.DpadUp)) {
                InputShim.mouseMove((int)(Display.getWidth() / 2.f), (int)(Display.getHeight() / 2.f));
                combatViewport.setExternalControl(false);
                csr.getZoomTracker().setZoom(csr.getZoomTracker().getZoom() - 0.05f );
            } else if(controller.isButtonPressed(LogicalButtons.DpadDown)) {
                InputShim.mouseMove((int)(Display.getWidth() / 2.f), (int)(Display.getHeight() / 2.f));
                combatViewport.setExternalControl(false);
                csr.getZoomTracker().setZoom(csr.getZoomTracker().getZoom() + 0.1f );
            } else if(ps != null) {
                combatViewport.setExternalControl(true);
                combatViewport.setCenter(ps.getLocation());
            }
        }
    }

    private void toggleShieldOrCloak(ShipAPI ps) {
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
                        ps.getShield().forceFacing(Util.getFacingFromHeading(v));
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
