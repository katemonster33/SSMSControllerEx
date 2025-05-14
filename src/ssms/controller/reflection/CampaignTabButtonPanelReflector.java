package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CampaignTabButtonPanelReflector {
    UIPanelAPI btnPanel;
    static Class<?> btnPanelType;
    static MethodHandle getButton;
    static MethodHandle getButtons;
    static MethodHandle getTabId;
    private CampaignTabButtonPanelReflector(UIPanelAPI btnPanel) {
        this.btnPanel = btnPanel;
    }

    public List<ButtonAPI> getButtons() {
        List<ButtonAPI> output = new ArrayList<>();
        try {
            Map<?, ?> buttonsMap = (Map<?, ?>) getButtons.invoke(btnPanel);
            for(var btn : buttonsMap.values()) {
                if(ButtonAPI.class.isAssignableFrom(btn.getClass())) {
                    output.add((ButtonAPI) btn);
                }
            }
            output.sort((ButtonAPI left, ButtonAPI right) -> (int)(left.getPosition().getX() - right.getPosition().getX()));
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't fetch buttons!", ex);
        }
        return output;
    }

    public static CampaignTabButtonPanelReflector TryGet(CoreUIAPI coreUi) {
        var children = UIPanelReflector.getChildItems((UIPanelAPI) coreUi);
        for(int idx = children.size() - 1; idx >= 0; idx--) {
            var btnPanel = children.get(idx);
            if(btnPanelType == null) {
                try {
                    getButton = MethodHandles.lookup().findVirtual(btnPanel.getClass(), "getButton", MethodType.methodType(ButtonReflector.getButtonType(), Object.class));

                    getButtons = MethodHandles.lookup().findVirtual(btnPanel.getClass(), "getButtons", MethodType.methodType(Map.class));

                    getTabId = MethodHandles.lookup().findVirtual(btnPanel.getClass(), "getTabId", MethodType.methodType(Object.class, ButtonReflector.getButtonType()));

                    btnPanelType = btnPanel.getClass();

                    return new CampaignTabButtonPanelReflector((UIPanelAPI) btnPanel);
                } catch(Throwable ex) {
                    Global.getLogger(CampaignTabButtonPanelReflector.class).fatal("Couldn't reflect campaign button panel tab!", ex);
                }
            } else if(btnPanelType.isAssignableFrom(btnPanel.getClass())) {
                return new CampaignTabButtonPanelReflector((UIPanelAPI) btnPanel);
            }
        }
        return null;
    }
}
