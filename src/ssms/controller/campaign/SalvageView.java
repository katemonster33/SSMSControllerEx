package ssms.controller.campaign;

import java.util.List;
import com.fs.starfarer.api.util.Pair;
import ssms.controller.Indicators;
import ssms.controller.InputScreenBase;

import java.util.ArrayList;

public class SalvageView extends InputScreenBase {
    public static final String ID = "Salvage";


    List<Pair<Indicators, String>> indicators;


    public SalvageView() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.LeftStick, "Navigate list"));
        indicators.add(new Pair<>(Indicators.X, "Pick up stack"));
        indicators.add(new Pair<>(Indicators.Y, "Take all"));
        indicators.add(new Pair<>(Indicators.B, "Abort"));
        indicators.add(new Pair<>(Indicators.A, "Confirm"));
        indicators.add(new Pair<>(Indicators.Select, "Toggle hangar"));
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }
}
