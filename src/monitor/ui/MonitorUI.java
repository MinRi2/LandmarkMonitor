package monitor.ui;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import mindustry.*;

/**
 * @author minri2
 * Create by 2025/4/2
 */
public class MonitorUI{
    public static PlayerSelector playerSelector;
    public static MainTable main;

    private static Element hitter;

    public static void init(){
        playerSelector = new PlayerSelector();
        main = new MainTable();

        Vars.ui.hudGroup.addChild(main);
    }

    public static void hitter(HitterCons cons){
        if(hitter == null){
            hitter = new Element(){
                @Override
                public void draw(){
                    super.draw();

                    Draw.color(Color.black, 0.25f);
                    Fill.rect(x + width / 2, y + height / 2, width, height);
                }
            };

            hitter.setFillParent(true);
            hitter.update(() -> {
                hitter.toFront();
            });
        }

        Core.scene.add(hitter);

        hitter.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                super.clicked(event, x, y);

                Vec2 v = Core.camera.unproject(x, y);
                if(cons.get(v.x, v.y)){
                    hitter.remove();
                    event.cancel();
                }
            }
        });
    }

    public interface HitterCons{
        boolean get(float x, float y);
    }
}
