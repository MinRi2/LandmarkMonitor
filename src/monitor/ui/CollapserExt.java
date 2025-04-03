package monitor.ui;

import arc.func.*;
import arc.scene.ui.layout.*;
import arc.util.*;

/**
 * @author minri2
 * Create by 2025/4/3
 */
public class CollapserExt extends Collapser{
    public Table table;

    public CollapserExt(Cons<Table> cons, boolean collapsed){
        super(cons, collapsed);

        table = Reflect.get(Collapser.class, this, "table");
    }

    public CollapserExt(Table table, boolean collapsed){
        super(table, collapsed);
        this.table = table;
    }

    @Override
    public void setTable(Table table){
        super.setTable(table);
        this.table = table;
    }

    @Override
    public float getMinWidth(){
        return table == null || isCollapsed() ? 0 : table.getPrefWidth();
    }

    @Override
    public float getMinHeight(){
        return table == null || isCollapsed() ? 0 : table.getPrefHeight();
    }
}
