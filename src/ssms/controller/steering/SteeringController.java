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
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.util.Pair;
import java.util.List;
import ssms.controller.HandlerController;
import ssms.controller.enums.Indicators;

/**
 * This interface defines the contract for custom steering behaviour. The user can select which behaviours he wants to use in either first or second mode.
 * A behaviour is qualified for the first mode if it supports all possible ship targets including none. This is because the first mode is also the fallback in case
 * the second mode cannot handle the current ship target. An implementation is expected to implement the methods getAllowsEveryTarget() and getLabel() .<br>
 * <br>
 * If the steering controller has custom options they can be configured on the already existing controller mapping. During the {@link com.fs.starfarer.api.ModPlugin#onApplicationLoad() ModPlugin.onApplicationLoad()}
 * call grab the default mapping and add the desired properties storing them in the mappings {@link ssms.controller.ControllerMapping#customProperties ControllerMapping.customProperties} map.<br>
 * <br>
 * <pre>PropertiesContainerConfiguration&lt;ControllerMapping&gt; confControllerMapping = PropertiesContainerConfigurationFactory.getInstance().getOrCreatePropertiesContainerConfiguration("SSMSControllerMapping", ControllerMapping.class);
confControllerMapping.addProperty(new PropertyConfigurationInteger&lt;&gt;("MyMod.MyOption","[B] My Option","Does something with my steering controller.",null,1000,
    new PropertyValueGetter&lt;ControllerMapping, Integer&gt;() {
        &#64;Override
        public Integer get(ControllerMapping sourceObject) {
            return (Integer)sourceObject.customProperties.get("MyMod.MyOption");
        }
    }, new PropertyValueSetter&lt;ControllerMapping, Integer&gt;() {
        &#64;Override
        public void set(ControllerMapping sourceObject, Integer value) {
            if ( value == null ) sourceObject.customProperties.remove("MyMod.MyOption");
            else sourceObject.customProperties.put("MyMod.MyOption", value);
        }
    }, true, 0, 100));</pre>
 * <br>
 * The steering controller can now evaluate the property by grabbing the mapping from the {@link ssms.controller.HandlerController#mapping HandlerController.mapping} and 
 * inspecting the {@link ssms.controller.ControllerMapping#customProperties ControllerMapping.customProperties} map.<br>
 * The {@link ssms.controller.HandlerController#mapping HandlerController.mapping} can be null if no controller is in use.<br>
 * If the properties are valid for all controllers then instead of grabbing "SSMSControllerMapping" use "SSMSController" as a base and store the values in your own code.<br>
 * <br>
 * <pre>PropertiesContainerConfiguration&lt;SSMSControllerModPlugin&gt; confController = confFactory.getOrCreatePropertiesContainerConfiguration("SSMSController", SSMSControllerModPlugin.class);</pre>
 * <br>
 * 
 * @author Malte Schulze
 */
public interface SteeringController {
    /**
     * This method is immediately being called after an instance was created and before the steering controller is put into service. It hands off environment variables that 
     * stay constant for the existence of the controller.
     * 
     * @param playerShip Ship that is being steered.
     * @param controller Controller that is being used as an input device for steering.
     * @param engine Combat engine that runs the current combat and provides access to all combat related objects.
     * @return True if the activation succeeded.
     */
    boolean activate(ShipAPI playerShip, HandlerController controller, CombatEngineAPI engine);
    /**
     * Called whenever the user switches targets manually. The {@link com.fs.starfarer.api.combat.ShipAPI#getShipTarget() playerShip.getShipTarget()} can change regardless of this
     * call if the targeted ship got e.g. destroyed.
     */
    void onTargetSelected();
    /**
     * @return True if the {@link com.fs.starfarer.api.combat.ShipAPI#getShipTarget() playerShip.getShipTarget()} is still supported by this steering controller.
     */
    boolean isTargetValid();
    /**
     * This method applies the per frame steering to the playerShip. It is being called during the {@link com.fs.starfarer.api.combat.EveryFrameCombatPlugin#processInputPreCoreControls(float, java.util.List) EveryFrameCombatPlugin.processInputPreCoreControls(...)} and can send commands to the playerShip that will be executed in the current frame.
     * 
     * @param timeAdvanced How much combat progressed, this can be zero.
     * @param offsetFacingAngle How much the ships front should be turned to allow e.g. broadside combat.
     */
    void steer(float timeAdvanced, float offsetFacingAngle);
    /**
     * Allows rendering elements as part of the combat scene.
     * 
     * @param viewport Rendering parameters.
     * @param offsetFacingAngle How much the ships front should be turned to allow e.g. broadside combat.
     */
    void renderInWorldCoords(ViewportAPI viewport, float offsetFacingAngle);
    /**
     * Called whenever a steering controller is put out of service. Any exterior references and cached data should be cleared to speed up garbage collection.
     */
    void discard();
    
    /**
     * Used by the input screen to display the control scheme of the currently chosen steering controller. The first entry should be a null indicator with text. It will
     * be displayed as a section header and should contain a short label to identify the steering controller. Entries with an indicator display the corresponding icon
     * followed by the text that should be a label for the functionality behind the indicator.
     * 
     * @return A list of all indicators and their purpose that are used by this steering controller.
     */
    List<Pair<Indicators, String>> getIndicators();

    boolean getAllowsEveryTarget();

    String getLabel();
}
