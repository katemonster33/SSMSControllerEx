package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.reflection.FleetTabReflector;
import ssms.controller.reflection.UIPanelReflector;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class FleetTabUI extends InputScreenBase {
    public static final String ID = "FleetTab";
    List<Pair<Indicators, String>> indicators;
    HandlerController controller;
    int numCols = -1;
    int curRow = -1, curCol = -1;
    int itemCount = -1;
    Vector2f itemSize;
    Vector2f topLeftPos;
    Vector2f mousePos;
    FleetTabReflector fleetTabReflector;
    float leftStickDownTime = 0.f;
    float buttonXDownTime = 0.f;
    float buttonYDownTime = 0.f;
    float buttonADownTime = 0.f;
    int itemInfoOffset = -1;
    List<Vector2f> itemInfoOffsets;
    boolean allRepairsSuspended = false;
    CampaignScope campaignScope;

    @Override
    public String getId() {
        return ID;
    }

    void hoverCurrentItem() {
        if (itemInfoOffset != -1 && itemInfoOffset < itemInfoOffsets.size()) {
            mousePos.setX(topLeftPos.x + (curCol * itemSize.x) + itemInfoOffsets.get(itemInfoOffset).getX());
            mousePos.setY(topLeftPos.y - (curRow * itemSize.y) + itemInfoOffsets.get(itemInfoOffset).getY());
        } else {
            mousePos.set(topLeftPos.x + (curCol * itemSize.x) + (itemSize.x / 2),
                    topLeftPos.y - (curRow * itemSize.y) - (itemSize.y / 2));
        }

        InputShim.mouseMove((int) mousePos.getX(), (int) mousePos.getY());
    }

    @Override
    public String[] getScopes() {
        return new String[]{CampaignScope.ID};
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if (indicators == null) {
            indicators = new ArrayList<>();
            indicators.add(new Pair<>(Indicators.LeftStick, "Navigate ships"));
            indicators.add(new Pair<>(Indicators.LeftStickButton, "Cycle ship info"));
            if(itemInfoOffset == -1) {
                indicators.add(new Pair<>(Indicators.A, "Move ship"));
            } else if(itemInfoOffset == 0) {
                indicators.add(new Pair<>(Indicators.A, "Open codex"));
            } else if(itemInfoOffset == 1) {
                indicators.add(new Pair<>(Indicators.A, "assign commander"));
                indicators.add(new Pair<>(Indicators.A, "(hold) re-assign commander"));
            } else if(itemInfoOffset == 2) {
                indicators.add(new Pair<>(Indicators.A, "Show more info"));
            }
            indicators.add(new Pair<>(Indicators.B, "Close Dialog"));
            indicators.add(new Pair<>(Indicators.X, "Suspend/Resume repairs (current ship)"));
            indicators.add(new Pair<>(Indicators.X, "(hold) Suspend/Resume repairs (current ship)"));
            indicators.add(new Pair<>(Indicators.Y, "Mothball ship"));
            indicators.add(new Pair<>(Indicators.Y, "(hold) Scuttle ship"));
            indicators.add(new Pair<>(Indicators.Select, "Refit"));

            indicators.addAll(campaignScope.getIndicators());
        }
        return indicators;
    }

    @Override
    public void activate(Object... args) {
        this.fleetTabReflector = (FleetTabReflector) args[0];

        campaignScope = (CampaignScope) InputScreenManager.getInstance().getCurrentScope();
        controller = SSMSControllerModPluginEx.controller;

        itemSize = new Vector2f(fleetTabReflector.getItemWidth(), fleetTabReflector.getItemHeight());
        numCols = fleetTabReflector.getColumns();
        mousePos = new Vector2f(0.f, 0.f);

        var items = fleetTabReflector.getItems();
        itemCount = items.size();
        itemInfoOffsets = new ArrayList<>();
        if (itemCount > 0 && UIPanelAPI.class.isAssignableFrom(items.get(0).getClass())) {
            UIPanelAPI itemPnl = (UIPanelAPI) items.get(0);
            topLeftPos = new Vector2f(itemPnl.getPosition().getX(), itemPnl.getPosition().getY() + itemPnl.getPosition().getHeight());

            var itemChildren = UIPanelReflector.getChildItems(itemPnl);
            if(itemChildren.size() > 4 && UIPanelAPI.class.isAssignableFrom(itemChildren.get(4).getClass())) {
                UIPanelAPI buttonPanel = (UIPanelAPI) itemChildren.get(4);

                var buttons = UIPanelReflector.getChildItems(buttonPanel);
                if (buttons.size() > 4 && UIComponentAPI.class.isAssignableFrom(buttons.get(4).getClass())) {
                    var pos = ((UIComponentAPI) buttons.get(4)).getPosition();
                    itemInfoOffsets.add(new Vector2f(pos.getCenterX() - topLeftPos.getX(), pos.getCenterY() - topLeftPos.getY()));
                }
            }
            if (itemChildren.size() > 3 && UIPanelAPI.class.isAssignableFrom(itemChildren.get(3).getClass())) {
                var itemInfoPanel = (UIPanelAPI) itemChildren.get(3);
                var itemInfoItems = UIPanelReflector.getChildItems(itemInfoPanel);
                for (var itemInfo : itemInfoItems) {
                    if (UIComponentAPI.class.isAssignableFrom(itemInfo.getClass())) {
                        var pos = ((UIComponentAPI) itemInfo).getPosition();
                        itemInfoOffsets.add(new Vector2f(pos.getCenterX() - topLeftPos.getX(), pos.getCenterY() - topLeftPos.getY()));
                    }
                }
            }
        } else {
            topLeftPos = new Vector2f(-1.f, -1.f);

        }
        buttonXDownTime = leftStickDownTime = buttonYDownTime = buttonADownTime = -1.f;
    }

    @Override
    public void preInput(float amount) {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.FLEET) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        }

        if (controller.getButtonEvent(HandlerController.Buttons.LeftStickLeft) == 1) {
            if (curCol == -1 || curRow == -1) {
                curCol = curRow = 0;
            } else if (curCol > 0) {
                curCol--;
            }
            itemInfoOffset = -1;
            hoverCurrentItem();
        } else if (controller.getButtonEvent(HandlerController.Buttons.LeftStickRight) == 1) {
            if (curCol == -1 || curRow == -1) {
                curCol = curRow = 0;
            } else if (curCol < (numCols - 1) && ((curRow * numCols) + curCol) < (itemCount - 1)) {
                curCol++;
            }
            itemInfoOffset = -1;
            hoverCurrentItem();
        } else if (controller.getButtonEvent(HandlerController.Buttons.LeftStickUp) == 1) {
            if (curCol == -1 || curRow == -1) {
                curCol = curRow = 0;
            } else if (curRow > 0) {
                curRow--;
            }
            itemInfoOffset = -1;
            hoverCurrentItem();
        } else if (controller.getButtonEvent(HandlerController.Buttons.LeftStickDown) == 1) {
            if (curCol == -1 || curRow == -1) {
                curCol = curRow = 0;
            } else if ((((curRow + 1) * numCols) + curCol) < itemCount) {
                curRow++;
            }
            itemInfoOffset = -1;
            hoverCurrentItem();
        } else if (controller.getButtonEvent(HandlerController.Buttons.B) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
        } else if (controller.getButtonEvent(HandlerController.Buttons.Select) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_R, 'r');
        }
        if(itemInfoOffset == -1) {
            if (controller.getButtonEvent(HandlerController.Buttons.A) == 1) {
                InputShim.mouseDownUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
            }
        } else if(itemInfoOffset == 0) {
            if (controller.getButtonEvent(HandlerController.Buttons.A) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_F2, '\0');
            }
        } else if(itemInfoOffset == 1) {
            if(controller.isButtonAPressed()) {
                if(buttonADownTime != -1.f) {
                    buttonADownTime += amount;
                }
                if(buttonADownTime > 1.f) {
                    InputShim.mouseDownUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.RIGHT);
                    buttonADownTime = -1.f;
                }
            } else {
                if(buttonADownTime > 0.f) {
                    InputShim.mouseDownUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT);
                }
                buttonADownTime = 0.f;
            }
        } else if(itemInfoOffset == 2) {
            if (controller.getButtonEvent(HandlerController.Buttons.A) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_F1, '\0');
            }
        }
        if (controller.getButtonEvent(HandlerController.Buttons.LeftStickButton) == 1) {
            if(itemInfoOffset != -1 && itemInfoOffset < (itemInfoOffsets.size() - 1)) {
                itemInfoOffset++;
            } else {
                itemInfoOffset = 0;
            }
            indicators = null;
            InputScreenManager.getInstance().refreshIndicators();
            hoverCurrentItem();
        }
        if(controller.isButtonXPressed()) {
            if(buttonXDownTime != -1.f) {
                buttonXDownTime += amount;
            }
            if(buttonXDownTime > 1.f) {
                if(allRepairsSuspended) {
                    InputShim.keyDownUp(Keyboard.KEY_W, 'W');
                } else {
                    InputShim.keyDownUp(Keyboard.KEY_Q, 'Q');
                }
                allRepairsSuspended = !allRepairsSuspended;
                buttonXDownTime = -1.f;
            }
        } else {
            if(buttonXDownTime > 0.f) {
                InputShim.keyDownUp(Keyboard.KEY_G, 'g');
            }
            buttonXDownTime = 0.f;
        }
        if(controller.isButtonYPressed()) {
            if(buttonYDownTime != -1.f) {
                buttonYDownTime += amount;
            }
            if(buttonYDownTime > 1.f) {
                InputShim.keyDownUp(Keyboard.KEY_U, 'u');
                buttonYDownTime = -1.f;
            }
        } else {
            if(buttonYDownTime > 0.f) {
                InputShim.keyDownUp(Keyboard.KEY_M, 'm');
            }
            buttonYDownTime = 0.f;
        }
        campaignScope.handleInput(amount, true);
    }
}
