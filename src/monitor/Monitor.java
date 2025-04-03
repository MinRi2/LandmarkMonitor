package monitor;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
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
    public int resolution = 1;
    private FrameBuffer buffer;

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

        buffer.resize((int)(width * resolution), (int)(height * resolution));

        Draw.flush();
        buffer.begin(Color.clear);
        monitorDraw();
        buffer.end();

        Core.camera = lastCamera;

        return buffer.getTexture();
    }

    public void monitorDraw(){
        camera.update();

        Tmp.m1.set(Draw.proj());
        Tmp.m2.set(Draw.trans());
        Draw.flush();

        Draw.proj().setOrtho(camera.position.x * resolution - buffer.getWidth()/2f, camera.position.y * resolution - buffer.getHeight()/2f, buffer.getWidth(), buffer.getHeight());
        Draw.trans().scale((float)resolution, (float)resolution);

        renderer.blocks.processBlocks();

        Draw.draw(Layer.floor, renderer.blocks.floor::drawFloor);
        Draw.draw(Layer.block - 1, renderer.blocks::drawShadows);
        Draw.draw(Layer.block - 0.09f, () -> {
            renderer.blocks.floor.beginDraw();
            renderer.blocks.floor.drawLayer(CacheLayer.walls);
            renderer.blocks.floor.endDraw();
        });

        //render all matching environments
        for(var renderer : renderer.envRenderers){
            if((renderer.env & state.rules.env) == renderer.env){
                renderer.renderer.run();
            }
        }

        if (Vars.state.rules.lighting && renderer.drawLight) {
            Draw.draw(Layer.light, renderer.lights::draw);
        }

        if (Vars.enableDarkness) {
            Draw.draw(Layer.darkness, renderer.blocks::drawDarkness);
        }

        Bloom bloom = renderer.bloom;
        if (bloom != null) {
            bloom.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
            bloom.setBloomIntensity((float)Core.settings.getInt("bloomintensity", 6) / 4.0F + 1.0F);
            bloom.blurPasses = Core.settings.getInt("bloomblur", 1);
            Draw.draw(Layer.bullet - 0.02f, bloom::capture);
            Draw.draw(Layer.effect + 0.02f, bloom::render);
        }

        FrameBuffer effectBuffer = renderer.effectBuffer;
        if(renderer.animateShields && Shaders.shield != null){
            Draw.drawRange(Layer.shields, 1f, () -> effectBuffer.begin(Color.clear), () -> {
                effectBuffer.end();
                effectBuffer.blit(Shaders.shield);
            });

            Draw.drawRange(Layer.buildBeam, 1f, () -> effectBuffer.begin(Color.clear), () -> {
                effectBuffer.end();
                effectBuffer.blit(Shaders.buildBeam);
            });
        }

        if(state.rules.fog) Draw.draw(Layer.fogOfWar, renderer.fog::drawFog);

        renderer.blocks.drawBlocks();
        Groups.draw.draw(Drawc::draw);

        Draw.proj(Tmp.m1);
        Draw.trans(Tmp.m2);

        Draw.reset();
        Draw.flush();
    }

    @Override
    public void dispose(){
        buffer.dispose();
    }
}
