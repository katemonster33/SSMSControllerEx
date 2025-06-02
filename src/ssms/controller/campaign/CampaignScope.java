package ssms.controller.campaign;

import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.util.Pair;
import ssms.controller.*;
import ssms.controller.reflection.CampaignStateReflector;
import ssms.controller.reflection.CampaignTabButtonPanelReflector;

import java.util.ArrayList;
import java.util.List;

public class CampaignScope extends InputScopeBase {
    public static String ID = "CampaignScope";
    CampaignTabButtonPanelReflector campaignButtonTabPanel;
    HandlerController controller;
    int selectedTab = -1;
    List<Pair<Indicators, String>> indicators;
    List<ButtonAPI> campaignTabBtns = new ArrayList<>();

    public CampaignScope() {
        indicators = new ArrayList<>();

        indicators.add(new Pair<>(Indicators.BumperLeft, "Select previous tab"));
        indicators.add(new Pair<>(Indicators.BumperLeft, "Select next tab"));
        //indicators.add(new Pair<>(Indicators.Start, "Pause"));
    }

    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object ... args) {
        campaignButtonTabPanel = CampaignTabButtonPanelReflector.TryGet(CampaignStateReflector.GetInstance().getCoreUI());
        if(campaignButtonTabPanel != null) {
            campaignTabBtns = campaignButtonTabPanel.getButtons();
        }
        controller = SSMSControllerModPluginEx.controller;
        selectedTab = -1;
    }

    public CampaignTabButtonPanelReflector getCampaignButtonTabPanel() {
        return campaignButtonTabPanel;
    }

    public void handleInput(float advance, boolean autoNavigate) {
        if(controller.getButtonEvent(Buttons.BumperLeft) == 1) {
            highlightPrevTab(autoNavigate);
            if(autoNavigate) {
                selectTab();
            }
        } else if(controller.getButtonEvent(Buttons.BumperRight) == 1) {
            highlightNextTab(autoNavigate);
            if(autoNavigate) {
                selectTab();
            }
        }
    }

    public void refreshSelectedIndex() {
        for(int i =0; i < campaignTabBtns.size(); i++) {
            if(campaignTabBtns.get(i).isHighlighted()) {
                selectedTab = i;
                break;
            }
        }
    }

    void highlightPrevTab(boolean autoNavigate) {
        int oldTab = selectedTab;
        if(selectedTab == -1) {
            selectedTab = 0;
        } else if(selectedTab > 0) {
            selectedTab--;
        } else {
            return;
        }
        if(!autoNavigate) {
            if (oldTab != selectedTab && oldTab != -1) {
                campaignTabBtns.get(oldTab).unhighlight();
            }
            campaignTabBtns.get(selectedTab).highlight();
        }
    }

    void highlightNextTab(boolean autoNavigate) {
        int oldTab = selectedTab;
        if(selectedTab == -1) {
            selectedTab = 0;
        } else if(selectedTab < (campaignTabBtns.size() - 1)) {
            selectedTab++;
        } else {
            return;
        }
        if(!autoNavigate) {
            if (oldTab != selectedTab && oldTab != -1) {
                campaignTabBtns.get(oldTab).unhighlight();
            }
            campaignTabBtns.get(selectedTab).highlight();
        }
    }

    void selectTab() {
        if(selectedTab >= 0 && selectedTab < campaignTabBtns.size()) {
            float mouseX = campaignTabBtns.get(selectedTab).getPosition().getCenterX(), mouseY = campaignTabBtns.get(selectedTab).getPosition().getCenterY();
            InputShim.mouseMove((int)mouseX, (int)mouseY);
            InputShim.mouseDownUp((int)mouseX, (int)mouseY, InputEventMouseButton.LEFT);
        }
    }
}
