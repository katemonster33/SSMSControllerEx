package ssms.controller;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;

import lunalib.lunaUI.panel.LunaBaseCustomPanelPlugin;
import org.jetbrains.annotations.NotNull;
import ssms.controller.enums.Indicators;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class IndicatorDisplayPanel extends LunaBaseCustomPanelPlugin  {
    final float lineHeight = 25, spacing = 8f, textWidth = 200f;
    List<Pair<Indicators, String>> indicators;
    UIPanelAPI parentPanel;
    CustomPanelAPI subpanel;
    Fader fader;
    float elapsed = 0;
    static final float FADE_DELAY = 2;
    public IndicatorDisplayPanel(UIPanelAPI parentPanel, List<Pair<Indicators, String>> args, Alignment panelAlignment) throws IllegalArgumentException {
        if ( args == null || args.isEmpty() ) {
            throw new IllegalArgumentException("indicators list cannot be empty!");
        }
        elapsed = 0;
        fader = new Fader(1.f, 2.f);
        indicators = new ArrayList<>(args);
        this.parentPanel = parentPanel;
        subpanel =  Global.getSettings().createCustom(400, 25 * args.size(), this);
        var tmpElem = subpanel.createUIElement(50.f, lineHeight, false).addPara("TEST ELEMENT", 0.f);
        float maxWidth = 0.f;
        for(var indPair : args) {
            maxWidth = Math.max(tmpElem.computeTextWidth(indPair.two), maxWidth);
        }

        subpanel.getPosition().setSize(maxWidth + lineHeight + (spacing * 2), subpanel.getPosition().getHeight());

        initFromScript(subpanel);
        parentPanel.addComponent(getPanel());
        switch(panelAlignment) {
            case BL -> getPanel().getPosition().inBL(spacing, spacing);
            case BR -> getPanel().getPosition().inBR(spacing, spacing);
            case RMID -> getPanel().getPosition().inRMid(spacing);
            case TR -> getPanel().getPosition().inTR(spacing, spacing);

            default -> getPanel().getPosition().inMid();
        }
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        elapsed += amount;
        if(elapsed > FADE_DELAY) {
            fader.fadeOut();
            fader.advance(amount);
            if(fader.isFadedOut()) {
                cleanup();
            }
        }
        if(InputShim.mouseX != null && InputShim.mouseY != null) {

        }
    }

    @Override
    public void render(float alphaMult) {
        super.render(fader.getBrightness() * alphaMult);
    }

    @Override
    public void processInput(@NotNull List<InputEventAPI> inputs) {
        // do nothing, we don't handle inputs
    }

    public CustomPanelAPI getSubpanel() {
        return subpanel;
    }

    @Override
    public void init() {
        TooltipMakerAPI prevImgElem = null;
        for ( Pair<Indicators,String> e : indicators ) {
            var txtElem = getPanel().createUIElement(getPanel().getPosition().getWidth(), lineHeight, false);
            var para = txtElem.addPara(e.two, Color.white, 0.f);
            para.getPosition().inTL(spacing, 4.f);
            var imgElem = getPanel().createUIElement(lineHeight, lineHeight, false);
            if ( e.one != null ) {
                imgElem.addImage(SSMSControllerModPluginEx.defaultIndicators.get(e.one), lineHeight, 0.f);
                txtElem.getPosition().rightOfMid(imgElem, 0.f);
                para.setAlignment(Alignment.LMID);
            } else {
                txtElem.getPosition().rightOfMid(imgElem, -25.f);
                para.setAlignment(Alignment.MID);
            }

            if(prevImgElem != null) {
                imgElem.getPosition().belowMid(prevImgElem, 0.f);
            } else {
                imgElem.getPosition().inTL(0.f, 0.f);
            }
            getPanel().addUIElement(imgElem);
            getPanel().addUIElement(txtElem);
            prevImgElem = imgElem;
        }
    }

    public void cleanup() {
        parentPanel.removeComponent(getPanel());
    }
}
