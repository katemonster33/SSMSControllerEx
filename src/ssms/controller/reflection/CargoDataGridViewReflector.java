package ssms.controller.reflection;

import java.util.ArrayList;
import java.util.List;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.ui.trade.CargoDataGridView;
import com.fs.starfarer.campaign.ui.trade.CargoStackView;

public class CargoDataGridViewReflector {
    CargoDataGridView cargoDataGridView;
    Object processInputImpl;
    public CargoDataGridViewReflector(CargoDataGridView cargoGridView) {
        this.cargoDataGridView = cargoGridView;

        try {
            processInputImpl = ClassReflector.GetInstance().findDeclaredMethod(cargoGridView.getClass(), "processInputImpl");
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Couldn't reflect input handler of CargoDataGridView!", ex);
        }
    }

    public CargoDataGridView getPrivateObject() {
        return cargoDataGridView;
    }

    public  List<CargoStackView> getStacks() {
        try {
            var children = UIPanelReflector.getChildItems((UIPanelAPI) cargoDataGridView);
            List<CargoStackView> stacks = new ArrayList<>();
            for(var child : children) {
                if(CargoStackView.class.isAssignableFrom(child.getClass())) {
                    stacks.add((CargoStackView) child);
                }
            }
            return stacks;
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get CargoDataGridView children!", ex);
            return null;
        }
    }

    public void processInputImpl(List<?> lstInputs) {
        try {
            MethodReflector.GetInstance().setAccessible(processInputImpl, true);

            MethodReflector.GetInstance().invoke(processInputImpl, cargoDataGridView, lstInputs);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Can't send fake inputs to cargo view!", ex);
        }
    }
}
