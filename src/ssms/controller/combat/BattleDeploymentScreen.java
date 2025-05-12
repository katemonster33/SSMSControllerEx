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
    List<Pair<Indicators, String>> indicators = null;
    DeploymentUiReflector dui; // hehe, dui
    HandlerController controller;
    int selectedButton = -1;
    List<ButtonAPI> dialogOptions;
    int selectedShip = -1;
    boolean selectingShips = false;

    public BattleDeploymentScreen() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.LeftStickUp, "Select ships"));
        indicators.add(new Pair<>(Indicators.LeftStickDown, "Select buttons"));
        indicators.add(new Pair<>(Indicators.LeftStickRight, "Next ship/button"));
        indicators.add(new Pair<>(Indicators.LeftStickLeft, "Prev ship/button"));
        indicators.add(new Pair<>(Indicators.A, "Select ship/button"));
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object ...args) {
        controller = SSMSControllerModPluginEx.controller;
        CombatStateReflector csr = (CombatStateReflector) args[0];
        dui = new DeploymentUiReflector(csr.getDeploymentDialog());
        dialogOptions = dui.getAllButtons();
        selectingShips = false;
        if(!dialogOptions.isEmpty()) {
            for (selectedButton = 0; selectedButton < dialogOptions.size(); selectedButton++) {
                if (dialogOptions.get(selectedButton).isEnabled()) {
                    dialogOptions.get(selectedButton).highlight();
                    break;
                }
            }
            if(selectedButton >= dialogOptions.size()) {
                selectedButton = -1;
            }
        }
    }

    public void selectNextShip(List<ButtonAPI> ships) {
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

    public void selectPrevShip(List<ButtonAPI> ships) {
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

    public void clickShip(List<ButtonAPI> ships) {
        if(selectedShip != -1 && ships != null && selectedShip < ships.size()) {
            dui.doActionPerformedShip(ships.get(selectedShip));
        }
    }

    public void selectNextButton() {
        if(dialogOptions != null && !dialogOptions.isEmpty()) {
            int oldSelectedButton = selectedButton;
            selectedButton++;
            for(; selectedButton < dialogOptions.size(); selectedButton++) {
                if(dialogOptions.get(selectedButton).isEnabled()) {
                    break;
                }
            }
            if(selectedButton >= dialogOptions.size()) {
                selectedButton = oldSelectedButton;
            } else {
                if (oldSelectedButton != -1) {
                    dialogOptions.get(oldSelectedButton).unhighlight();
                }
                dialogOptions.get(selectedButton).highlight();
            }
        }
    }

    public void selectPrevButton() {
        if(dialogOptions != null && !dialogOptions.isEmpty()) {
            int oldSelectedButton = selectedButton;
            selectedButton--;
            for(; selectedButton >= 0; selectedButton--) {
                if(dialogOptions.get(selectedButton).isEnabled()) {
                    break;
                }
            }
            if(selectedButton < 0) {
                selectedButton = oldSelectedButton;
            } else {
                if (oldSelectedButton != -1) {
                    dialogOptions.get(oldSelectedButton).unhighlight();
                }
                dialogOptions.get(selectedButton).highlight();
            }
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

    Object getUiOnTop() {
        var panel = CombatStateReflector.GetInstance().getWidgetPanel();
        if (panel != null) {
            var items = UIPanelReflector.getChildItems(panel);
            if (!items.isEmpty()) {
                var lastChild = items.get(items.size() - 1);
                if(InputScreenManager.getInstance().getDisplayPanel() != null && lastChild == InputScreenManager.getInstance().getDisplayPanel().getSubpanel()) {
                    lastChild = items.get(items.size() - 2);
                }
                if(lastChild == dui.getDialogObject()) {
                    return null;
                }
                return lastChild;
            }
        }
        return null;
    }

    Object lastUIOnTop = null;
    @Override
    public void preInput(float advance) {
        if(Global.getCombatEngine().getCombatUI().isShowingDeploymentDialog()) {
            var lastChild = getUiOnTop();
            if(lastChild != null && lastChild != lastUIOnTop) {
                lastUIOnTop = lastChild;
                // this nonsense tries to tell if the topmost UI element is the message box that shows up the first time we enter combat
                MessageBoxReflector dr = MessageBoxReflector.TryGet(lastUIOnTop);
                if (dr != null) {
                    InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, MessageBoxScreen.ID, new Object[]{ dr, getId() });
                    return;
                }
            }
        } else {
            InputScreenManager.getInstance().transitionToScope(BattleScope.ID, Global.getCombatEngine());
            return;
        }
        if(selectingShips) {
            List<ButtonAPI> ships = dui.getShips();
            if(selectedShip >= 0 && selectedShip < ships.size()) {
                if(!ships.get(selectedShip).isEnabled()) {
                    ships.get(selectedShip).unhighlight();

                    for(selectedShip = 0; selectedShip < ships.size(); selectedShip++) {
                        if(ships.get(selectedShip).isEnabled()) {
                            ships.get(selectedShip).highlight();
                            break;
                        }
                    }
                    if(selectedShip >= ships.size()) {
                        selectedShip = -1;
                    }
                }
            }
            if (controller.getButtonEvent(HandlerController.Buttons.LeftStickRight) == 1) {
                selectNextShip(ships);
            } else if (controller.getButtonEvent(HandlerController.Buttons.LeftStickLeft) == 1) {
                selectPrevShip(ships);
            } else if (controller.getButtonEvent(HandlerController.Buttons.A) == 1) {
                clickShip(ships);
            } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickDown) == 1) {
                selectingShips = false;
                if(selectedShip != -1 && selectedShip < ships.size()) {
                    ships.get(selectedShip).unhighlight();
                }
                selectedShip = -1;
                selectedButton = -1;
                selectNextButton();
            }
        } else {
            if(!dialogOptions.isEmpty()) {
                if (controller.getButtonEvent(HandlerController.Buttons.LeftStickRight) == 1) {
                    selectNextButton();
                } else if (controller.getButtonEvent(HandlerController.Buttons.LeftStickLeft) == 1) {
                    selectPrevButton();
                } else if (controller.getButtonEvent(HandlerController.Buttons.A) == 1) {
                    clickButton();
                } else if(controller.getButtonEvent(HandlerController.Buttons.LeftStickUp) == 1) {
                    selectingShips = true;
                    selectedShip = -1;
                    if(selectedButton != -1 && selectedButton < dialogOptions.size()) {
                        dialogOptions.get(selectedButton).unhighlight();
                    }
                    selectedButton = -1;
                    var ships = dui.getShips();
                    selectNextShip(ships);
                }
            }
        }
    }
}
