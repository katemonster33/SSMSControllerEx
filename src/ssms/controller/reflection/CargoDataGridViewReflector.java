package ssms.controller.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.ui.trade.CargoDataGridView;
import com.fs.starfarer.campaign.ui.trade.CargoStackView;
import com.fs.starfarer.campaign.util.CollectionView;

public class CargoDataGridViewReflector {
    CargoDataGridView cargoDataGridView;
    UIPanelAPI dataGridParent;
    Object stackViewField;
    public CargoDataGridViewReflector(CargoDataGridView cargoGridView, UIPanelAPI parent) {
        this.cargoDataGridView = cargoGridView;
        this.dataGridParent = parent;
        try {
            stackViewField = ClassReflector.GetInstance().getDeclaredField(cargoGridView.getClass(), "stackView");
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get the field for the CargoDataGridView ItemView(s)!", ex);
        }
    }

    public PositionAPI getPosition() {
        return dataGridParent.getPosition();
    }

    public CargoDataGridView getPrivateObject() {
        return cargoDataGridView;
    }

    public  List<CargoStackView> getStacks() {
        try {
            Object stackViewObj = FieldReflector.GetInstance().GetVariable(stackViewField, cargoDataGridView);
            if(stackViewObj instanceof CollectionView<?> stackView) {
                List<CargoStackView> output = new ArrayList<>();
                for(var child : stackView.getViews()) {
                    if(child instanceof CargoStackView cargoStackView) {
                        output.add(cargoStackView);
                    }
                }
                return output;
            }
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get CargoDataGridView children!", ex);
        }
        return null;
    }
}
