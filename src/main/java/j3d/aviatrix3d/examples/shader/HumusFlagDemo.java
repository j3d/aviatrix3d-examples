/*****************************************************************************
 *                        Yumtech, Inc Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.aviatrix3d.examples.shader;

// External imports
import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import javax.imageio.ImageIO;

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

import org.j3d.geom.GeometryData;
import org.j3d.geom.CylinderGenerator;
import org.j3d.util.DataUtils;
import org.j3d.util.I18nManager;

/**
 * Demo that is a port of Humus' waving flag demo. Uses most of the same code
 * as the cloth demo, but with some different behaviour to simulate the flag
 * and wind physics.
 *
 * The original demo and code can be found here:
 * http://esprit.campus.luth.se/~humus/3D/index.php?page=OpenGL
 */
public class HumusFlagDemo extends BaseDemoFrame
{
    private static final String APP_NAME = "examples.HumusFlagDemo";

    /** Names of the shader vertex file for the sphere */
    private static final String POLE_VERTEX_SHADER =
        "shaders/examples/simple/humus_flag_pole.vert";

    /** Names of the shader fragment file for the sphere */
    private static final String POLE_FRAG_SHADER =
        "shaders/examples/simple/humus_flag_pole.frag";

    /** Names of the shader vertex file for the sphere */
    private static final String LIGHTING_VERTEX_SHADER =
        "shaders/examples/simple/humus_flag_cloth.vert";

    /** Names of the shader fragment file for the sphere */
    private static final String LIGHTING_FRAG_SHADER =
        "shaders/examples/simple/humus_flag_cloth.frag";

    /** The number of vertices for the cloth in the X direction */
    private static final int CLOTH_SIZE_X = HumusFlagAnimator.CLOTH_SIZE_X;

    /** The number of vertices for the cloth in the T direction */
    private static final int CLOTH_SIZE_Y = HumusFlagAnimator.CLOTH_SIZE_Y;

    /** Value of the real size of the cloth */
    private static final float C_SIZE  = HumusFlagAnimator.C_SIZE;

    /** The texture type constant needed when updating texture coordinates */
    private static final int[] TEX_TYPES =
        { VertexGeometry.TEXTURE_COORDINATE_2 };

    /** Shader program shared between all the spheres */
    private ShaderProgram poleProgram;

    /** Create a new demo */
    public HumusFlagDemo()
    {
        super("Aviatrix3D Port of Humus Cloth Demo");

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.org-j3d-aviatrix3d-resources-core");
    }

    @Override
    protected void setupSceneGraph()
    {
        // View group
        Viewpoint vp = new Viewpoint();

        // We could set these directly, but we don't because we'd like to
        // pass the values through to the shaders as well. More convenient and
        // we guarantee the same values then.
        Vector3d trans = new Vector3d();
        trans.set(0, 100, 600);
        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Run through the flags directory and load every texture found into
        // the flags array as texture objects.
        File flags_dir = DataUtils.lookForFile("images/examples/shader/flags", getClass(), null);
        File[] files_in_dir = flags_dir.listFiles();
        Texture2D[] flag_textures = new Texture2D[files_in_dir.length];
        int num_flags = 0;

        for (File file_in_dir : files_in_dir)
        {
            if (file_in_dir.isDirectory())
                continue;

            System.out.println("loading " + file_in_dir);
            TextureComponent2D img_comp = loadTexture(file_in_dir.getAbsolutePath());

            if (img_comp != null)
            {
                Texture2D tex = new Texture2D(Texture2D.FORMAT_RGBA, img_comp);
                tex.setBoundaryModeS(Texture.BM_CLAMP_TO_EDGE);
                tex.setBoundaryModeT(Texture.BM_CLAMP_TO_EDGE);

                flag_textures[num_flags++] = tex;
            }
        }

        // Big spheres for the cloth to drape over
        TransformGroup pole_tx = createPole(trans, new float[]{0.9f, 0.9f, 0.9f, 0});
        TransformGroup arrow_tx = createArrow(trans, new float[]{1, 0, 1, 0});
        scene_root.addChild(arrow_tx);
        scene_root.addChild(pole_tx);

        TransformGroup cloth_tx = createCloth(trans);
        TransformGroup light_tx = createLight();

        scene_root.addChild(cloth_tx);
        scene_root.addChild(light_tx);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        HumusFlagAnimator anim =
            new HumusFlagAnimator(flag_textures,
                                  cloth_tx,
                                  light_tx,
                                  arrow_tx,
                                  pole_tx);
        sceneManager.setApplicationObserver(anim);

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

    /**
     * Create a single sphere object and it's parent transform
     *
     * @param viewPos The fixed position of the viewer
     * @param
     */
    private TransformGroup createPole(Vector3d viewPos,
                                      float[] colour)
    {
        poleProgram = new ShaderProgram();
        ShaderObject vert_object = new ShaderObject(true);
        String[] source = loadShaderFile(POLE_VERTEX_SHADER);
        vert_object.setSourceStrings(source, 1);
        vert_object.compile();

        ShaderObject frag_object = new ShaderObject(false);
        source = loadShaderFile(POLE_FRAG_SHADER);
        frag_object.setSourceStrings(source, 1);
        frag_object.compile();

        poleProgram.addShaderObject(vert_object);
        poleProgram.addShaderObject(frag_object);
        poleProgram.link();

        CylinderGenerator generator = new CylinderGenerator(C_SIZE * (CLOTH_SIZE_Y - 1) * 2 + 10, 5, 64);
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generator.generate(data);

        TriangleStripArray geom = new TriangleStripArray();
        geom.setVertices(TriangleArray.COORDINATE_3,
                               data.coordinates,
                               data.vertexCount);
        geom.setStripCount(data.stripCounts, data.numStrips);
        geom.setNormals(data.normals);

        ShaderArguments args = new ShaderArguments();
        float[] f_args = new float[4];
        args.setUniform("lightPos", 3, f_args, 1);
        args.setUniform("color", 4, colour, 1);

        f_args[0] = (float)viewPos.x;
        f_args[1] = (float)viewPos.y;
        f_args[2] = (float)viewPos.z;
        args.setUniform("camPos", 3, f_args, 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderArguments(args);
        shader.setShaderProgram(poleProgram);

        Appearance app = new Appearance();
        app.setVisible(true);
        app.setShader(shader);

        Shape3D shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(geom);

        TransformGroup tg = new TransformGroup();
        tg.addChild(shape);

        return tg;
    }

    /**
     * Create an arrow object and it's parent transform
     *
     * @param viewPos The fixed position of the viewer
     * @param
     */
    private TransformGroup createArrow(Vector3d viewPos,
                                       float[] colour)
    {
        float[] normals =
        {
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
        };

        float rad = (float)Math.random() * 0.5f + 0.3f;
        float[] coords =
        {
            rad * 80 + 40,
            C_SIZE * (CLOTH_SIZE_Y - 1) + 5,
            0,
            rad * 80,
            C_SIZE * (CLOTH_SIZE_Y - 1) + 5,
            30,
            rad * 80,
            C_SIZE * (CLOTH_SIZE_Y - 1) + 5,
            10,
            0,
            C_SIZE * (CLOTH_SIZE_Y - 1) + 5,
            10,
            0,
            C_SIZE * (CLOTH_SIZE_Y - 1) + 5,
            -10,
            rad * 80,
            C_SIZE * (CLOTH_SIZE_Y - 1) + 5,
            -10,
            rad * 80,
            C_SIZE * (CLOTH_SIZE_Y - 1) + 5,
            -30,
        };

        int[] fan_counts = { 7 };

        TriangleFanArray geom = new TriangleFanArray();
        geom.setVertices(TriangleFanArray.COORDINATE_3, coords, 7);
        geom.setFanCount(fan_counts, 1);
        geom.setNormals(normals);

        ShaderArguments args = new ShaderArguments();
        float[] f_args = new float[4];
        args.setUniform("lightPos", 3, f_args, 1);
        args.setUniform("color", 4, colour, 1);

        f_args[0] = (float)viewPos.x;
        f_args[1] = (float)viewPos.y;
        f_args[2] = (float)viewPos.z;
        args.setUniform("camPos", 3, f_args, 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderArguments(args);
        shader.setShaderProgram(poleProgram);

        Appearance app = new Appearance();
        app.setVisible(true);
        app.setShader(shader);

        Shape3D shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(geom);

        TransformGroup tg = new TransformGroup();
        tg.addChild(shape);

        return tg;
    }

    /**
     * Convenience method to make the geometry that represents the cloth.
     *
     * @return The group that contains the cloth geometry
     */
    private TransformGroup createCloth(Vector3d viewPos)
    {
        // Leave this uninitiliazed
        IndexedTriangleStripArray geom = new IndexedTriangleStripArray();

        int vtx_pos = 0;
        int tex_pos = 0;
        float[] vertices = new float[CLOTH_SIZE_Y * CLOTH_SIZE_X * 3];
        float[] normals = new float[CLOTH_SIZE_Y * CLOTH_SIZE_X * 3];
        float[][] texCoords = new float[1][CLOTH_SIZE_Y * CLOTH_SIZE_X * 2];

        int num_index = (CLOTH_SIZE_Y - 1) * CLOTH_SIZE_X * 2;
        int[] c_indicies = new int[num_index];
        int[] strip_len = new int[CLOTH_SIZE_Y - 1];

        for(int i = 0; i < CLOTH_SIZE_Y; i++)
        {
            for(int j = 0; j < CLOTH_SIZE_X; j++)
            {
                vertices[vtx_pos++] = C_SIZE * j;
                vertices[vtx_pos++] = C_SIZE * (CLOTH_SIZE_Y - 1 - i);
                vertices[vtx_pos++] = 0;

                texCoords[0][tex_pos++] = (float)j / (CLOTH_SIZE_X - 1);
                texCoords[0][tex_pos++] = (float)i / (CLOTH_SIZE_Y - 1);
            }
        }


        int idx = 0;
        for(int i = 0; i < CLOTH_SIZE_Y - 1; i++)
        {
            int i_offset = i * CLOTH_SIZE_X * 2;
            int x_offset = i * CLOTH_SIZE_X;

            for(int j = 0; j < CLOTH_SIZE_X; j++)
            {
                c_indicies[idx++] = x_offset + CLOTH_SIZE_X + j;
                c_indicies[idx++] = x_offset + j;
            }
        }

        for(int i = 0; i < CLOTH_SIZE_Y - 1; i++)
            strip_len[i] = CLOTH_SIZE_X * 2;

        geom.setVertices(VertexGeometry.COORDINATE_3,
                                  vertices,
                                  CLOTH_SIZE_Y * CLOTH_SIZE_X);
        geom.setIndices(c_indicies, num_index);
        geom.setStripCount(strip_len, CLOTH_SIZE_Y - 1);
        geom.setNormals(normals);
        geom.setTextureCoordinates(TEX_TYPES, texCoords, 1);

        // Create a new empty texture unit. It will have the texture set each
        // time the update changes.
        TextureUnit[] tex_units = { new TextureUnit() };

        ShaderObject vert_object = new ShaderObject(true);
        String[] source = loadShaderFile(LIGHTING_VERTEX_SHADER);
        vert_object.setSourceStrings(source, 1);
        vert_object.compile();

        ShaderObject frag_object = new ShaderObject(false);
        source = loadShaderFile(LIGHTING_FRAG_SHADER);
        frag_object.setSourceStrings(source, 1);
        frag_object.compile();

        ShaderProgram program = new ShaderProgram();
        program.addShaderObject(vert_object);
        program.addShaderObject(frag_object);
        program.link();

        ShaderArguments args = new ShaderArguments();
        float[] f_args = new float[4];
        args.setUniform("lightPos", 3, f_args, 1);

        f_args[0] = (float)viewPos.x;
        f_args[1] = (float)viewPos.y;
        f_args[2] = (float)viewPos.z;

        args.setUniform("camPos", 3, f_args, 1);

        // Need to tell it that texture unit 0 is a sampler named Base
        int[] tex_sampler = new int[1];
        args.setUniform("Base", 1, tex_sampler, 1);

        GLSLangShader cloth_shader = new GLSLangShader();
        cloth_shader.setShaderProgram(program);
        cloth_shader.setShaderArguments(args);

        PolygonAttributes pa = new PolygonAttributes();
        pa.setCulledFace(PolygonAttributes.CULL_NONE);

        Appearance app = new Appearance();
        app.setTextureUnits(tex_units, 1);
        app.setShader(cloth_shader);
        app.setPolygonAttributes(pa);

        Shape3D shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(geom);

        TransformGroup tg = new TransformGroup();
        tg.addChild(shape);

        return tg;
    }

    /**
     * Convenience method to make the geometry that represents a light, though
     * is not a real light.
     *
     * @return The group that contains the light geometry
     */
    private TransformGroup createLight()
    {
        float[] coords = { 0, 0, 0,  1, 0, 0,  1, 1, 0,  0, 1, 0 };
        float[][] tex_coords = { { 0, 0,  1, 0,  1, 1,  0, 1 } };
        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };

        QuadArray geom = new QuadArray();
        geom.setVertices(QuadArray.COORDINATE_3,coords, 4);
        geom.setTextureCoordinates(tex_type, tex_coords, 1);

        TextureComponent2D img_comp = loadTexture("images/examples/shader/humus_particle.png");
        Texture2D tex = new Texture2D(Texture2D.FORMAT_RGBA, img_comp);
        tex.setBoundaryModeS(Texture.BM_CLAMP_TO_EDGE);
        tex.setBoundaryModeT(Texture.BM_CLAMP_TO_EDGE);

        TextureUnit[] tex_units = { new TextureUnit() };
        tex_units[0].setTexture(tex);

        Appearance app = new Appearance();
        app.setTextureUnits(tex_units, 1);

        Shape3D shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(geom);

        TransformGroup tg = new TransformGroup();
        tg.addChild(shape);

        return tg;
    }

    public static void main(String[] args)
    {
        HumusFlagDemo demo = new HumusFlagDemo();
        demo.setVisible(true);
    }
}
