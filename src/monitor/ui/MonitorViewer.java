package monitor.ui;

import arc.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import monitor.*;

/**
 * @author minri2
 * Create by 2025/4/3
 */
public class MonitorViewer extends Window{
    public static final int maxWidth = 64, maxHeight = 64, minWidth = 16, minHeight = 16;
    public static final int minUpdateTime = 0, maxUpdateTime = 60;
    public static final int minResolution = 1, maxResolution = 8;

    public static final float minImageSize = 256f, minSizeScale = 4f;
    public static final String VIEWER_NAME = "monitor-viewer";

    public float viewWidth = 32, viewHeight = 32;
    public float updateInterval = 10;
    public int resolution = 2;
    public float alpha = 0.8f;
    public int id;

    public boolean showConfig = true;
    public boolean isRemoved;
    private final Interval timer = new Interval();
    private final TextureRegion region = new TextureRegion();

    public Player monitorEntity;
    public final Monitor monitor;

    private boolean autoShowConfig;
    private BorderImage image;

    @SuppressWarnings("unchecked")
    public MonitorViewer(Monitor monitor, int id){
        super(VIEWER_NAME, true);

        this.monitor = monitor;
        this.id = id;

        setup();
        name = VIEWER_NAME + "-" + id;
        read();

        // no pane
        getCell(pane).setElement(cont);
        pack();

        update(this::updateMonitor);

        monitorEntity = Vars.player;
    }

    // updated by delegate.
    public void updateTexture(){
        if(timer.get(updateInterval)){
            Texture texture = monitor.getTexture();
            texture.setWrap(TextureWrap.clampToEdge);
            region.set(texture);
            region.flip(false, true);
            Draw.flush();
        }
    }

    protected void updateMonitor(){
        if(monitorEntity != null){
            monitor.setPosition(monitorEntity);
        }

        monitor.setSize((int)(viewWidth * Vars.tilesize), (int)(viewHeight * Vars.tilesize));
        monitor.resolution = resolution;
    }

    public void drawMark(){
        Draw.color(Pal.accent, 0.8f);
        Lines.dashCircle(x, y, 4);
        Draw.reset();
    }

    public void drawRange(){
        float x = monitor.x, y = monitor.y;
        float width = monitor.width, height = monitor.height;

        Drawf.dashRect(Pal.accent, x - width / 2, y - height / 2, width, height);
        Draw.reset();
    }

    @Override
    protected void setupTitle(Table table){
        super.setupTitle(table);

        table.table(info -> {
            info.defaults().minWidth(0).growX().right();

            info.label(() -> monitorEntity == null ? "" : monitorEntity.coloredName())
            .style(Styles.outlineLabel).labelAlign(Align.right).ellipsis(true).pad(4f);
            info.label(() -> "" + Tmp.v1.set(World.toTile(monitor.x), World.toTile(monitor.y)))
            .style(Styles.outlineLabel).ellipsis(true).minWidth(80f).pad(4f);
        }).padLeft(4f).grow();
    }

    @Override
    protected void setupButtons(Table buttons){
        buttons.button(Icon.pencilSmall, Styles.clearTogglei, 32, () -> {
            setShowConfig(!showConfig);
        }).checked(b -> showConfig);

        super.setupButtons(buttons);
    }

    @Override
    protected void setupCont(Table cont){
        super.setupCont(cont);

        cont.add(new CollapserExt(this::setupConfig, !showConfig)).margin(8).growX()
        .update(collapser -> collapser.setCollapsed(!showConfig));
        cont.row();

        TextureRegionDrawable drawable = new TextureRegionDrawable(region);
        drawable.setMinWidth(region.width / minSizeScale);
        drawable.setMinHeight(region.height / minSizeScale);

        cont.add(image = new BorderImage(drawable)).scaling(Scaling.fit).touchable(Touchable.disabled)
        .minSize(minImageSize).grow().update(i -> {
            float minWidth = region.width / minSizeScale;
            float minHeight = region.height / minSizeScale;

            if(drawable.getMinWidth() != minWidth || minHeight != drawable.getMinHeight()){
                drawable.setMinWidth(region.width / minSizeScale);
                drawable.setMinHeight(region.height / minSizeScale);
                i.invalidateHierarchy();
            }
        });

        image.actions(Actions.alpha(alpha));
    }

    private void setupConfig(Table table){
        table.defaults().pad(4).growX();

        table.table(null, markTable -> {
            markTable.defaults().pad(4).growX();

            markTable.button("$monitor-position", Icon.info, Styles.flatt, 32, () -> {
                MonitorUI.hitter((x, y) -> {
                    Tile tile = Vars.world.tileWorld(x, y);

                    if(tile == null){
                        Vars.ui.showInfoToast("$monitor-position-invalid", 3);
                        return true;
                    }

                    monitor.x = tile.worldx();
                    monitor.y = tile.worldy();
                    monitorEntity = null;
                    return true;
                });
                Vars.ui.showInfoToast("$hit-hint", 2f);
            });

            markTable.button("$monitor-player", Icon.players, Styles.flatt, 32, () -> {
                MonitorUI.playerSelector.show(player -> {
                    monitorEntity = player;
                    return true;
                });
            }).row();
        });

        table.row();

        table.table(null, t -> {
            t.label(() -> Core.bundle.format("monitor-updateTime", updateInterval))
            .style(Styles.outlineLabel).minWidth(32);

            t.slider(minUpdateTime, maxUpdateTime, 5, updateInterval, f -> {
                updateInterval = f;
            }).padLeft(8).grow();
        }).row();
        table.table(null, t -> {
            t.label(() -> Core.bundle.format("monitor-alpha", alpha * 100))
            .style(Styles.outlineLabel).minWidth(32);

            t.slider(0, 100, 5 , alpha * 100, f -> {
                alpha = f / 100;
                image.actions(Actions.alpha(alpha, 0.5f, Interp.pow2));
            }).padLeft(8).grow();
        }).row();
        table.table(null, t -> {
            t.label(() -> Core.bundle.format("monitor-resolution", resolution))
            .style(Styles.outlineLabel).minWidth(64);

            t.slider(minResolution, maxResolution, 1, resolution, f -> {
                resolution = (int)f;
            }).padLeft(8).grow();
        }).row();
        table.table(null, t -> {
            t.label(() -> Core.bundle.format("monitor-width", viewWidth))
            .style(Styles.outlineLabel).minWidth(64);

            t.slider(minWidth, maxWidth, 1, viewWidth, f -> {
                viewWidth = f;
            }).padLeft(8).grow();
        }).row();
        table.table(null, t -> {
            t.label(() -> Core.bundle.format("monitor-height", viewHeight))
            .style(Styles.outlineLabel).minWidth(64);

            t.slider(minHeight, maxHeight, 1, viewHeight, f -> {
                viewHeight = f;
            }).padLeft(8).grow();
        });
    }

    @Override
    public void toggle(){
        if(minimized && autoShowConfig){
            setShowConfig(true);
        }

        super.toggle();
    }

    @Override
    public void minimized(){
        super.minimized();

        autoShowConfig = showConfig;
        if(showConfig){
            setShowConfig(false);
        }
    }

    public void setShowConfig(boolean showConfig){
        this.showConfig = showConfig;

        if(showConfig){
            act(0); // children update first
            unpack();
        }
    }

    @Override
    protected void onRemoved(){
        super.onRemoved();

        isRemoved = true;
        monitor.dispose();
    }

    @Override
    public float getMinWidth(){
        return Math.max(super.getMinWidth(), image.getPrefWidth() / minSizeScale);
    }

    @Override
    public float getMinHeight(){
        return Math.max(super.getMinHeight(), image.getPrefHeight() / minSizeScale);
    }
}
