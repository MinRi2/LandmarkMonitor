package monitor;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;

/**
 * @author minri2
 * Create by 2025/4/3
 */
public class Monitor implements Disposable{
    public static final Camera camera = new Camera();

    public float x, y;
    public float width, height;
    public int resolutionScale = 1;
    private FrameBuffer buffer;
    private boolean generatingTexture;

    public Monitor(float x, float y){
        this.x = x;
        this.y = y;
    }

    public void setPosition(Position position){
        x = position.getX();
        y = position.getY();
    }

    public void setSize(float width, float height){
        if(this.width != width || this.height != height){
            this.width = width;
            this.height = height;
        }
    }

    public Texture getTexture(){
        if(buffer == null){
            buffer = new FrameBuffer();
        }

        Camera lastCamera = Core.camera;
        Core.camera = camera;
        camera.position.set(x, y);
        camera.width = width;
        camera.height = height;

        generatingTexture = true;

        Draw.flush();

        Tmp.m1.set(Draw.proj());
        Tmp.m2.set(Draw.trans());

        Draw.trans().scale(resolutionScale, resolutionScale);

        buffer.resize((int)(width * resolutionScale), (int)(height * resolutionScale));
        buffer.begin(Color.clear);
        rendererDraw();
        buffer.end();

        Draw.proj(Tmp.m1);
        Draw.trans(Tmp.m2);
        Core.camera = lastCamera;

        generatingTexture = false;

        return buffer.getTexture();
    }

    public void afterProj(){
        if(!generatingTexture) return;

        float x = this.x * resolutionScale;
        float y = this.y * resolutionScale;
        float width = buffer.getWidth();
        float height = buffer.getHeight();
        Draw.proj().setOrtho(x - width/2, y - height/2, width, height);

        // floor 顶点需要相机原投影矩阵
//        Draw.draw(Layer.block - 0.09f, () -> {
//            blocks.floor.beginDraw();
//            blocks.floor.drawLayer(CacheLayer.walls);
//            blocks.floor.endDraw();
//        });
        Draw.drawRange(Layer.block - 0.09f, () -> {
            Draw.proj().set(camera.mat);
        }, () -> {
            Draw.proj().setOrtho(x - width/2, y - height/2, width, height);
        });
    }

    private void rendererDraw(){
        Draw.sort(true);

        Draw.flush();

        renderer.draw();
    }

    @Override
    public void dispose(){
        buffer.dispose();
    }
}
