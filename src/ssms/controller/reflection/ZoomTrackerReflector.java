package ssms.controller.reflection;

import com.fs.starfarer.combat.CombatState;

public class ZoomTrackerReflector {
    Object zoomAField;
    Object zoomBField;
    Object zoomMinField;
    Object zoomMaxField;
    public ZoomTrackerReflector() throws Throwable {
        Object zoomTrackerField = ClassReflector.GetInstance().getDeclaredField(CombatState.class, "zoomTracker");

        Class<?> zoomTrackerType = FieldReflector.GetInstance().GetVariableType(zoomTrackerField);

        var fields = ClassReflector.GetInstance().getDeclaredFields(zoomTrackerType);
        int numFloatFields = 0;
        for(int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
            if(FieldReflector.GetInstance().GetVariableType(fields[fieldIndex]) == float.class) {
                switch(numFloatFields) {
                    case 0: zoomAField = fields[fieldIndex]; break;
                    case 1: zoomBField = fields[fieldIndex]; break;
                    case 2: zoomMinField = fields[fieldIndex]; break;
                    case 3: zoomMaxField = fields[fieldIndex]; break;
                }
                if(++numFloatFields == 4) break;
            }
        }
    }

    public void SetZoom(Object combatStateObj, float desiredZoom) throws Throwable
    {
        FieldReflector.GetInstance().SetVariable(zoomMaxField, combatStateObj, desiredZoom);
        FieldReflector.GetInstance().SetVariable(zoomMinField, combatStateObj, desiredZoom);
    }
}
