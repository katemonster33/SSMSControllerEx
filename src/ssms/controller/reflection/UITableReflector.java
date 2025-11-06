package ssms.controller.reflection;

import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.ui.UITable;

import java.util.ArrayList;
import java.util.List;

public class UITableReflector {
    UITable uiTable;
    static MethodReflector getList;
    UIPanelAPI uiTableList;
    static Class<?> listType;
    static MethodReflector ensureVisible;
    static MethodReflector getItems;
    static {
        ClassReflector uiTableReflector = new ClassReflector(UITable.class);
        getList = uiTableReflector.getDeclaredMethod("getList");

        listType = getList.getReturnType();

        ClassReflector listReflector = new ClassReflector(listType);
        ensureVisible = listReflector.findDeclaredMethod("ensureVisible");
        getItems = listReflector.getDeclaredMethod("getItems");
    }
    public UITableReflector(UITable uiTable) {
        this.uiTable = uiTable;

        uiTableList = (UIPanelAPI) getList.invoke(uiTable);
    }

    public void ensureVisible(UIComponentAPI componentAPI) {
        ensureVisible.invoke(uiTableList, componentAPI);
    }

    public List<UIComponentAPI> getItems() {
        List<?> items = (List<?>) getItems.invoke(uiTableList);

        List<UIComponentAPI> components = new ArrayList<>();
        for(var item : items) {
            if(item instanceof  UIComponentAPI uiComponentAPI) {
                components.add(uiComponentAPI);
            }
        }
        return components;
    }
}
