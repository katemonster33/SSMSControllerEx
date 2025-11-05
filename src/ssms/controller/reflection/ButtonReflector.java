package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

public final class ButtonReflector extends UIComponentReflector {
    static Class<?> buttonType;
    static MethodReflector getButtonCheckboxRenderer;
    static MethodReflector getRendererPanel;

    static FieldReflector rendererPanelInnerPanel;

    public static void init(Class<? extends ButtonAPI> buttonType) {
        var btnReflector = new ClassReflector(buttonType);
        getButtonCheckboxRenderer = btnReflector.findDeclaredMethod("getRendererCheckbox");
        getRendererPanel = btnReflector.findDeclaredMethod("getRendererPanel");
        ButtonReflector.buttonType = buttonType;
    }
    ButtonAPI button;

    public ButtonReflector(ButtonAPI buttonAPI) {
        super(buttonAPI);
        this.button = buttonAPI;
    }

    public UIPanelAPI getRendererPanel() {
        var btnPanel = getRendererPanel.invoke(button);
        UIPanelAPI btnInnerPanel = null;
        if(btnPanel != null) {
            if(rendererPanelInnerPanel == null) {
                rendererPanelInnerPanel = new ClassReflector(btnPanel.getClass()).getDeclaredFields()[0];
            }
            btnInnerPanel = (UIPanelAPI) rendererPanelInnerPanel.get(btnPanel);
        }
        return btnInnerPanel;
    }

    public ButtonAPI getButton() {
        return button;
    }

    public boolean isCheckbox() {
        return getButtonCheckboxRenderer.invoke(button) != null;
    }

    public static Class<?> getButtonType() {
        return buttonType;
    }
}
