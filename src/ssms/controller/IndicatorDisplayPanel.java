package ssms.controller;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Pair;

import lunalib.lunaUI.elements.LunaElement;
import lunalib.lunaUI.panel.LunaBaseCustomPanelPlugin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class IndicatorDisplayPanel extends LunaBaseCustomPanelPlugin  {
    final float lineHeight = 25, spacing = 8f, textWidth = 200f;
    List<Pair<Indicators, String>> indicators;
    UIPanelAPI parentPanel;
    public IndicatorDisplayPanel(UIPanelAPI parentPanel, List<Pair<Indicators, String>> args) throws IllegalArgumentException {
        if ( args == null || args.isEmpty() ) {
            throw new IllegalArgumentException("indicators list cannot be empty!");
        }
        indicators = new ArrayList<>(args);
        this.parentPanel = parentPanel;
        CustomPanelAPI subpanel =  Global.getSettings().createCustom(400, 25 * args.size(), this);
        var tmpElem = subpanel.createUIElement(50.f, lineHeight, false).addPara("TEST ELEMENT", 0.f);
        float maxWidth = 0.f;
        for(var indPair : args) {
            maxWidth = Math.max(tmpElem.computeTextWidth(indPair.two), maxWidth);
        }

        subpanel.getPosition().setSize(maxWidth + lineHeight + (spacing * 2), subpanel.getPosition().getHeight());

        initFromScript(subpanel);
        parentPanel.addComponent(getPanel());
        getPanel().getPosition().inBL(spacing, spacing);
    }
    @Override
    public void init() {
        TooltipMakerAPI prevImgElem = null;
        float minSize = 0.f;
        for ( Pair<Indicators,String> e : indicators ) {
            var txtElem = getPanel().createUIElement(400, lineHeight, false);
            var para = txtElem.addPara(e.two, Color.white, 0.f);
            para.getPosition().inTL(spacing, 4.f);
            if ( e.one != null ) {
                var imgElem = getPanel().createUIElement(lineHeight, lineHeight, false);
                imgElem.addImage(SSMSControllerModPluginEx.defaultIndicators.get(e.one), lineHeight, 0.f);
                getPanel().addUIElement(imgElem);
                txtElem.getPosition().rightOfMid(imgElem, 0.f);
                if(prevImgElem != null) {
                    imgElem.getPosition().belowMid(prevImgElem, 0.f);
                } else {
                    imgElem.getPosition().inTL(0.f, 0.f);
                }
                prevImgElem = imgElem;
            }
            para.autoSizeToWidth(para.computeTextWidth(para.getText()));
            para.setAlignment(Alignment.LMID);
            getPanel().addUIElement(txtElem);
            float tmpWidth = para.getPosition().getX() + para.getPosition().getWidth() - getPanel().getPosition().getX();
            if(tmpWidth > minSize) minSize = tmpWidth;
            //subpanel.getPosition().setSize()
        }
    }
}
