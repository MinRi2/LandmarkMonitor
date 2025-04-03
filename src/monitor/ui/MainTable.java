package monitor.ui;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import monitor.*;

/**
 * @author minri2
 * Create by 2025/4/2
 */
public class MainTable extends Window{
    public boolean drawRange;
    public WidgetGroup monitorGroup;

    private final Seq<MonitorViewer> viewers = new Seq<>();
    private boolean renderingTexture = false;

    public MainTable(){
        super("monitor-main", false);

        setup();
        read();

        Events.run(Trigger.draw, () -> {
            if(renderingTexture) return;

            Draw.z(Layer.overlayUI);
            for(MonitorViewer viewer : viewers){
                viewer.drawMark();
                if(drawRange){
                    viewer.drawRange();
                }
            }
        });

        Events.run(Trigger.postDraw, () -> {
            if(renderingTexture) return;

            viewers.removeAll(MonitorViewer::isRemoved);

            renderingTexture = true;
            for(MonitorViewer viewer : viewers){
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

            buttons.button("创建监视器", Icon.addSmall, Styles.flatt, 32, () -> {
                Vec2 pos = Core.camera.position;
                createMonitorAt(pos.x, pos.y);
            }).row();

            buttons.button("绘制范围", Icon.infoCircleSmall, Styles.flatTogglet, 32, () -> {
                drawRange = !drawRange;
            }).checked(b -> drawRange).padTop(4);
        }).growX();
    }

    public void createMonitorAt(float x, float y){
        if(monitorGroup == null){
            monitorGroup = new WidgetGroup();
            Core.scene.add(monitorGroup);
        }

        Monitor monitor = new Monitor(x, y);
        MonitorViewer monitorViewer = new MonitorViewer(monitor);

        monitorGroup.addChild(monitorViewer);
        viewers.add(monitorViewer);
    }
}
