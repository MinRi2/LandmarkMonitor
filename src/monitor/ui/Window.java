package monitor.ui;

import MinRi2.ModCore.ui.*;
import MinRi2.ModCore.ui.element.*;
import MinRi2.ModCore.ui.operator.*;
import arc.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import monitor.io.*;

/**
 * @author minri2
 * Create by 2025/4/2
 */
public class Window extends SavedTable{
    public boolean removable;
    public boolean minimized;

    public int packAlign = Align.topLeft;
    private final Rect lastBounds = new arc.math.geom.Rect();

    private final RotatedImage image = new RotatedImage(Icon.upSmall, 180);
    protected ScrollPane pane;
    protected Table cont;

    public Window(String name, boolean removable){
        super(MonitorSettings.settings, name, !removable, !removable);

        this.removable = removable;

        setPosition(Core.scene.getWidth() * 0.3f, Core.scene.getHeight() * 0.5f, Align.bottomLeft);
        setSize(200f, 250f);
    }

    protected void read(){
        pack();
        readPosition();
        readSize();
        keepInStage();
    }

    protected void setup(){
        background(Styles.black3);

        table(table -> {
            table.table(this::setupTitle).marginRight(4f).grow();

            table.table(buttons -> {
                buttons.defaults().size(Vars.iconMed).pad(2f);
                setupButtons(buttons);
            }).padRight(4f).padLeft(4f);
        }).growX();

        row();

        pane = pane(Styles.noBarPane, this::setupCont).update(pane -> {
            if(pane.hasScroll() && !pane.hasMouse()){
                Core.scene.unfocus(pane);
            }
        }).pad(8f).grow().get();
    }

    protected void setupTitle(Table table){
        table.left();
        table.background(MinTex.getColoredRegion(Pal.gray));

        table.add(Core.bundle.get("window." + name + ".name", name)).style(Styles.outlineLabel).expandX().left();
    }

    protected void setupButtons(Table buttons){
        buttons.button(Icon.upSmall, Styles.clearTogglei, this::toggle).checked(b -> minimized).get().replaceImage(image);
        buttons.button(Icon.editSmall, Styles.cleari, this::operate);
        buttons.button(Icon.cancelSmall, Styles.cleari, this::remove).disabled(!removable);
    }

    protected void setupCont(Table cont){
        this.cont = cont;
        cont.top().left();
    }

    public void toggle(){
        minimized = !minimized;

        float x = getX(packAlign);
        float y = getY(packAlign);
        if(minimized){
            minimized();

            if(removable){
                ElementUtils.getBounds(this, lastBounds);
            }
        }else {
            if(removable){
                setBounds(lastBounds.x, lastBounds.y, lastBounds.width, lastBounds.height);
            }else{
                readSize();
            }
        }

        setPosition(x, y, packAlign);

        image.rotate(Mathf.num(minimized), 0.25f, Interp.pow2);

        invalidate();
        invalidateHierarchy();
    }

    public void minimized(){
        setSize(getMinWidth(), getMinHeight());
        invalidate();
    }

    protected void onRemoved(){

    }

    @Override
    public final boolean remove(){
        onRemoved();
        return super.remove();
    }

    @Override
    public void pack(){
        float x = getX(packAlign);
        float y = getY(packAlign);
        super.pack();
        setPosition(x, y, packAlign);
    }
}
