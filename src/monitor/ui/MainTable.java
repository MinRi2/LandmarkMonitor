package monitor.ui;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.actions.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.core.Renderer.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.meta.*;
import monitor.*;

/**
 * @author minri2
 * Create by 2025/4/2
 */
public class MainTable extends Window{
    public static final int maxMonitorSize = 8;

    public boolean drawRange, hideViewer;
    public WidgetGroup monitorGroup;

    private final Seq<MonitorViewer> viewers = new Seq<>(maxMonitorSize);
    private boolean renderingTexture = false;

    public MainTable(){
        super("monitor-main", false);

        Core.scene.add(monitorGroup = new WidgetGroup());

        setup();
        read();

        Vars.renderer.addEnvRenderer(Env.none, () -> {
            for(MonitorViewer viewer : viewers){
                viewer.monitor.afterProj();
            }
        });

        Events.run(Trigger.draw, () -> {
            if(renderingTexture || !monitorGroup.visible) return;

            viewers.removeAll(viewer -> viewer.isRemoved);

            Draw.z(Layer.overlayUI);
            for(MonitorViewer viewer : viewers){
                if(!viewer.visible) continue;
                viewer.drawMark();

                if(drawRange){
                    viewer.drawRange();
                }
            }
            Draw.reset();
        });

        Events.run(Trigger.postDraw, () -> {
            if(renderingTexture || !monitorGroup.visible) return;

            renderingTexture = true;
            for(MonitorViewer viewer : viewers){
                if(!viewer.visible) continue;
                viewer.updateTexture();
            }
            renderingTexture = false;
        });

        Events.on(ResetEvent.class, e -> {
            for(MonitorViewer viewer : viewers){
                viewer.remove();
            }

            viewers.clear();
        });
    }

    @Override
    protected void setupCont(Table cont){
        super.setupCont(cont);

        cont.table(buttons -> {
            buttons.defaults().pad(4f).margin(4f).growX();

            buttons.button("@monitor.create", Icon.addSmall, Styles.flatt, 32, () -> {
                Vec2 pos = Core.camera.position;
                createMonitorAt(pos.x, pos.y);
            }).disabled(b -> viewers.size >= maxMonitorSize).row();

            buttons.defaults().padTop(4);

            buttons.button("@monitor.drawRange", Icon.infoCircleSmall, Styles.flatTogglet, 32, () -> {
                drawRange = !drawRange;
            }).checked(b -> drawRange).row();

            buttons.button("@monitor.hideViewer", Icon.eyeOffSmall, Styles.flatTogglet, 32, () -> {
                hideViewer = !hideViewer;
                monitorGroup.visible = true;
                monitorGroup.actions(
                    Actions.alpha(hideViewer ? 0 : 1, 0.2f, hideViewer ? Interp.pow2In : Interp.pow2Out),
                    Actions.visible(!hideViewer)
                );
            }).checked(b -> hideViewer).row();

            buttons.button("@monitor.remove", Icon.cancelSmall, Styles.flatt, 32, () -> {
                for(MonitorViewer viewer : viewers){
                    viewer.actions(Actions.delay(0.05f * viewer.id), Actions.remove());
                }
            }).row();
        }).growX();
    }

    public void createMonitorAt(float x, float y){
        if(viewers.size >= maxMonitorSize) return;

        int id = findViewerId();

        Monitor monitor = new Monitor(x, y);
        MonitorViewer monitorViewer = new MonitorViewer(monitor, id);

        if(viewers.any()){
            MonitorViewer lastViewer = viewers.peek();
            monitorViewer.alpha = lastViewer.alpha;
            monitorViewer.resolution = lastViewer.resolution;
            monitorViewer.viewWidth = lastViewer.viewWidth;
            monitorViewer.viewHeight = lastViewer.viewHeight;
            monitorViewer.updateInterval = lastViewer.updateInterval;
            monitorViewer.showConfig = lastViewer.showConfig;
        }

        monitorViewer.actions(
        Actions.alpha(0),
        Actions.alpha(1, 0.2f, Interp.pow2In)
        );

        monitorGroup.addChild(monitorViewer);
        viewers.add(monitorViewer);
    }

    private int findViewerId(){
        int id = 1;
        if(viewers.isEmpty()) return id;
        for(MonitorViewer viewer : viewers){
            if(id++ != viewer.id) return id;
        }
        return id;
    }
}
