package j3d.aviatrix3d.examples.shader;

// External imports
import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.io.IOException;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.texture.procedural.PerlinNoiseGenerator;
import org.j3d.util.DataUtils;

/**
 * Example application that demonstrates a shader using fragment shading and
 * 3D textures to get an arc of lighting effect.
 * <p>
 *
 * The source of this code is directly pulled from:
 * <a href="http://esprit.campus.luth.se/~humus/">http://esprit.campus.luth.se/~humus/</a>
 *
 * @author Justin Couch
 * @version $Revision: 1.13 $
 */
public class ElectroEffectDemo extends BaseDemoFrame
{
    private static final int XSIZE = 128;
    private static final int YSIZE = 32;
    private static final int ZSIZE = 32;
    private static final float XSCALE = 0.08f;
    private static final float YSCALE = 0.16f;
    private static final float ZSCALE = 0.16f;

    /** Fragment shaders file name */
    private static final String FRAG_SHADER_FILE = "shaders/examples/simple/electro.fp";

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** The shader for fragment processing */
    private FragmentShader fragShader;

    /**
     * Construct a new shader demo instance.
     */
    public ElectroEffectDemo()
    {
        super("OpenGL Shaders: Fragment + 3D Texture");
    }

    @Override
    protected void setupSceneGraph()
    {
        PerlinNoiseGenerator noise_gen = new PerlinNoiseGenerator();

        int Size = XSIZE * YSIZE * ZSIZE;

        int[] noise_buffer = new int[Size];
        byte[] tex_buffer = new byte[Size];
        int min = 255;
        int max = 0;
        int pos = 0;

        for(int z = 0; z < ZSIZE; z++) {
            for(int y = 0; y < YSIZE; y++) {
                for(int x = 0; x < XSIZE; x++) {
                    double noise = noise_gen.tileableTurbulence3(XSCALE * x, YSCALE * y, ZSCALE * z, XSIZE * XSCALE, YSIZE * YSCALE, ZSIZE * ZSCALE, 16);

                    int t = (int)(127.5f * (1.0f + noise));

                    if(t > max)
                        max = t;
                    if(t < min)
                        min = t;

                    noise_buffer[pos++] = t;
                }
            }
        }

        for(int i = 0; i < Size; i++) {
        	tex_buffer[i] = (byte)((255 * (noise_buffer[i] - min)) / (max - min));
        }

        ByteTextureComponent3D image = new ByteTextureComponent3D(TextureComponent.FORMAT_SINGLE_COMPONENT,
									                              XSIZE,
									                              YSIZE,
									                              ZSIZE,
									                              tex_buffer);

        Texture3D tex = new Texture3D(Texture.FORMAT_INTENSITY, image);
        tex.setMagFilter(Texture.MAGFILTER_NICEST);
        tex.setMinFilter(Texture.MINFILTER_NICEST);
        tex.setBoundaryModeT(Texture.BM_WRAP);
        tex.setBoundaryModeR(Texture.BM_WRAP);
        tex.setBoundaryModeS(Texture.BM_WRAP);

        TextureUnit[] textures = { new TextureUnit() };
        textures[0].setTexture(tex);

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 3.0f);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        TriangleStripArray geom = new TriangleStripArray();

        String[] shader_txt = loadShaderFile(FRAG_SHADER_FILE);
        fragShader = new FragmentShader();
        fragShader.setProgramString(shader_txt[0]);

        GL14Shader shader = new GL14Shader();
        shader.setFragmentShader(fragShader);

        Appearance app = new Appearance();
        app.setShader(shader);
        app.setTextureUnits(textures, 1);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        ElectroAnimation anim = new ElectroAnimation(geom);
        sceneManager.setApplicationObserver(anim);

        scene_root.addChild(shape);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, 500, 500);
        view.setScene(scene);
        resizeManager.addManagedViewport(view);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        ElectroEffectDemo demo = new ElectroEffectDemo();
        demo.setVisible(true);
    }
}
