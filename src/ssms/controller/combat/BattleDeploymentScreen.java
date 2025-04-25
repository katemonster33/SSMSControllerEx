package ssms.controller.combat;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.util.Pair;
import ssms.controller.*;
import ssms.controller.generic.MessageBoxScreen;
import ssms.controller.reflection.*;

import java.util.ArrayList;
import java.util.List;

public class BattleDeploymentScreen extends InputScreenBase {
    public static final String ID = "BattleDeployment";
    List<Pair<Indicators, String>> indicators;
    Object deploymentUi;
    DeploymentUiReflector dui; // hehe, dui
    HandlerController controller;
    int selectedButton = -1;
    List<ButtonAPI> dialogOptions;
    List<ButtonAPI> ships;
    int selectedShip = -1;
    boolean selectingShips = false;

    public BattleDeploymentScreen() {
        indicators = null;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object ...args) {
        controller = SSMSControllerModPluginEx.controller;
        deploymentUi = null;
        dui = null;
    }

    public void selectNextShip() {
        if(ships != null && !ships.isEmpty()) {
            int oldSelectedButton = selectedShip;
            if(selectedShip == -1) {
                selectedShip = 0;
            } else if(selectedShip < (ships.size() - 1)) {
                selectedShip++;
            }
            if(!ships.get(selectedShip).isEnabled()) {
                selectedShip++;
            }
            if(selectedShip >= ships.size()) {
                selectedShip = 0;
            }
            if(selectedShip != oldSelectedButton && oldSelectedButton != -1) {
                ships.get(oldSelectedButton).unhighlight();
            }
            ships.get(selectedShip).highlight();
        }
    }

    public void selectPrevShip() {
        if(ships != null && !ships.isEmpty()) {
            int oldSelectedButton = selectedShip;
            if(selectedShip == -1) {
                selectedShip = 0;
            } else if(selectedShip > 0) {
                selectedShip--;
            }
            if(!ships.get(selectedShip).isEnabled()) {
                selectedShip--;
            }
            if(selectedShip < 0) {
                selectedShip = ships.size() - 1;
            }
            if(selectedShip != oldSelectedButton && oldSelectedButton != -1) {
                ships.get(oldSelectedButton).unhighlight();
            }
            ships.get(selectedShip).highlight();
        }
    }

    public void clickShip() {
        if(selectedShip != -1 && ships != null && selectedShip < ships.size()) {
            dui.doActionPerformedShip(ships.get(selectedShip));
        }
    }

    public void selectNextButton() {
        if(dialogOptions != null && !dialogOptions.isEmpty()) {
            int oldSelectedButton = selectedButton;
            if(selectedButton == -1) {
                selectedButton = 0;
            } else if(selectedButton < (dialogOptions.size() - 1)) {
                selectedButton++;
            }
            if(!dialogOptions.get(selectedButton).isEnabled()) {
                selectedButton++;
            }
            if(selectedButton >= dialogOptions.size()) {
                selectedButton = 0;
            }
            if(selectedButton != oldSelectedButton && oldSelectedButton != -1) {
                dialogOptions.get(oldSelectedButton).unhighlight();
            }
            dialogOptions.get(selectedButton).highlight();
        }
    }

    public void selectPrevButton() {
        if(dialogOptions != null && !dialogOptions.isEmpty()) {
            int oldSelectedButton = selectedButton;
            if(selectedButton == -1) {
                selectedButton = 0;
            } else if(selectedButton > 0) {
                selectedButton--;
            }
            if(!dialogOptions.get(selectedButton).isEnabled()) {
                selectedButton--;
            }
            if(selectedButton < 0) {
                selectedButton = dialogOptions.size() - 1;
            }
            if(selectedButton != oldSelectedButton && oldSelectedButton != -1) {
                dialogOptions.get(oldSelectedButton).unhighlight();
            }
            dialogOptions.get(selectedButton).highlight();
        }
    }

    public void clickButton() {
        if(selectedButton != -1 && dialogOptions != null && selectedButton < dialogOptions.size()) {
            dui.doActionPerformed(null, dialogOptions.get(selectedButton));
        }
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }

    Object lastUIOnTop = null;
    @Override
    public void preInput(float advance) {
        if(Global.getCurrentState() == GameState.COMBAT) {
            if(Global.getCombatEngine().getCombatUI().isShowingDeploymentDialog()) {
                var panel = CombatStateReflector.GetInstance().getWidgetPanel();
                if (panel != null) {
                    var items = UIPanelReflector.getChildItems(panel);
                    if (!items.isEmpty()) {
                        var lastChild = items.get(items.size() - 1);
                        if (lastChild != null && lastChild != lastUIOnTop) {
                            lastUIOnTop = lastChild;
                            if (InputScreenManager.getInstance().getDisplayPanel() == null || lastUIOnTop != InputScreenManager.getInstance().getDisplayPanel().getSubpanel()) {
                                // this nonsense tries to tell if the topmost UI element is the message box that shows up the first time we enter combat
                                MessageBoxReflector dr = MessageBoxReflector.TryGet(lastUIOnTop);
                                if (dr != null) {
                                    InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, MessageBoxScreen.ID, new Object[]{dr, getId()});
                                } else if(deploymentUi == null) {
                                    // if it ain't, then we hope that we're looking at the actual deployment dialog
                                    deploymentUi = lastUIOnTop;
                                    dui = DeploymentUiReflector.TryGet(deploymentUi);
                                    if(dui != null) {
                                        dialogOptions = dui.getAllButtons();
                                        ships = dui.getShips();

                                        // hopefully it's safe to add the indicators now
                                        indicators = new ArrayList<>();
                                        indicators.add(new Pair<>(Indicators.LeftStickUp, "Select ships"));
                                        indicators.add(new Pair<>(Indicators.LeftStickDown, "Select buttons"));
                                        indicators.add(new Pair<>(Indicators.LeftStickRight, "Next ship/button"));
                                        indicators.add(new Pair<>(Indicators.LeftStickLeft, "Prev ship/button"));
                                        indicators.add(new Pair<>(Indicators.A, "Select ship/button"));
                                    }
                                }
                            }
                        }
                    }
                }
                if(selectingShips) {
                    if(controller.getButtonEvent(HandlerController.Buttons.LeftStickRight) == 1) {
                        selectNextShip();
                    } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickLeft) == 1) {
                        selectPrevShip();
                    } else if(controller.getButtonEvent(HandlerController.Buttons.A) == 1) {
                        clickShip();
                    }
                } else {
                    if(controller.getButtonEvent(HandlerController.Buttons.LeftStickRight) == 1) {
                        selectNextButton();
                    } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickLeft) == 1) {
                        selectPrevButton();
                    } else if(controller.getButtonEvent(HandlerController.Buttons.A) == 1) {
                        clickButton();
                    }
                }
                if(controller.getButtonEvent(HandlerController.Buttons.LeftStickUp) == 1 && !selectingShips) {
                    selectingShips = true;
                    selectedShip = -1;
                    if(selectedButton != -1) {
                        dialogOptions.get(selectedButton).unhighlight();
                    }
                    selectedButton = -1;
                    selectNextShip();
                } else  if(controller.getButtonEvent(HandlerController.Buttons.LeftStickDown) == 1 && selectingShips) {
                    selectingShips = false;
                    if(selectedShip != -1) {
                        ships.get(selectedShip).unhighlight();
                    }
                    selectedShip = -1;
                    selectedButton = -1;
                    selectNextButton();
                }
            } else {
                InputScreenManager.getInstance().transitionToScope(BattleScope.ID, Global.getCombatEngine());
            }
        }
    }
}
