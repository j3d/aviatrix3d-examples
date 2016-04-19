package j3d.aviatrix3d.examples.shader;

// External imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;
import org.j3d.geom.GeometryData;
import org.j3d.geom.SphereGenerator;

/**
 * Example application that demonstrates a wood texture from the ogl2sdk.
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public class WoodDemo extends BaseDemoFrame
{
    /** Vertex shader source file */
    private static final String VTX_SHADER_FILE =
        "shaders/examples/simple/wood.vert";

    /** Fragment shader source file */
    private static final String FRAG_SHADER_FILE =
        "shaders/examples/simple/wood.frag";

    /**
     * Construct a new shader demo instance.
     */
    public WoodDemo()
    {
        super("Demo Illustrating OpenGL Shaders from the ogl2sdk", false);
    }

    @Override
    protected void setupSceneGraph()
    {
        // View group
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 2f);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

/*
        // Flat panel that has the viewable object as the demo
        float[] coord = { 0, 0, -1,     0.75f, 0, -1,     0, 0.75f, -1,
                          0.75f, 0, -1, 0.75f, 0.75f, -1, 0, 0.75f, -1,
                          0.75f, 0, -1, 0.5f, 0, -1,      0.75f, 0.75f, -1,
                          0.5f, 0, -1,  0.5f, 0.75f, -1,  0.75f, 0.75f, -1 };

        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1,
                           0, 0, 1, 0, 0, 1, 0, 0, 1,
                           0, 0, 1, 0, 0, 1, 0, 0, 1,
                           0, 0, 1, 0, 0, 1, 0, 0, 1};

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3, coord, 6);
        geom.setNormals(normal);
*/
        Geometry geom = generateSphere(0.3f, 32);

        Material material = new Material();
        material.setDiffuseColor(new float[] { 0, 0, 1 });
        material.setEmissiveColor(new float[] { 0, 0, 1 });
        material.setSpecularColor(new float[] { 1, 1, 1 });

        String[] vert_shader_txt = loadShaderFile(VTX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(FRAG_SHADER_FILE);

        ShaderObject vert_shader = new ShaderObject(true);
        vert_shader.setSourceStrings(vert_shader_txt, 1);
        vert_shader.compile();

        ShaderObject frag_shader = new ShaderObject(false);
        frag_shader.setSourceStrings(frag_shader_txt, 1);
        frag_shader.compile();

        ShaderProgram shader_prog = new ShaderProgram();
        shader_prog.addShaderObject(vert_shader);
        shader_prog.addShaderObject(frag_shader);
        shader_prog.link();

        float color_spread[] = new float[] { 0.3f / 2, 0.15f / 2, 0.0f / 2 };
        float light_position[] = new float[] { 0.0f, 0.0f, 4.0f };
        float dark_color[] = new float[] { 0.6f, 0.3f, 0.1f};
        float grain_size_recip[] = new float[] { 2.0f / 1.0f };
        float scale[] = new float[] { 2.0f };

        ShaderArguments shader_args = new ShaderArguments();
        shader_args.setUniform("LightPosition", 3, light_position, 1);
        shader_args.setUniform("Scale", 1, scale, 1);
        shader_args.setUniform("GrainSizeRecip", 1, grain_size_recip, 1);
        shader_args.setUniform("DarkColor", 3, dark_color, 1);
        shader_args.setUniform("colorSpread", 3, color_spread, 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shader_args);

        Appearance app = new Appearance();
        app.setMaterial(material);
        app.setShader(shader);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

//        trans.set(0, 0, -8);
        Matrix4d mat2 = new Matrix4d();
        mat2.setIdentity();
//        mat2.setTranslation(trans);
//        mat2.setScale(0.05f);

        TransformGroup shape_tx = new TransformGroup();
        shape_tx.addChild(shape);
        //shape_tx.setTransform(mat2);

        scene_root.addChild(shape_tx);

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

        ModelRotationAnimation anim = new ModelRotationAnimation(shape_tx, resizeManager);
        sceneManager.setApplicationObserver(anim);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    private Geometry generateSphere(float radius, int facets)
    {
        SphereGenerator generator = new SphereGenerator(radius, facets);
        GeometryData data = new GeometryData();

        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        generator.generate(data);

        TriangleStripArray impl = new TriangleStripArray();
        impl.setVertices(TriangleStripArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        impl.setStripCount(data.stripCounts, data.numStrips);
        impl.setNormals(data.normals);

        // Make an array of objects for the texture setting
        float[][] textures = { data.textureCoordinates };
        int[] tex_type = { TriangleStripArray.TEXTURE_COORDINATE_2 };
        impl.setTextureCoordinates(tex_type, textures, 1);

        // Setup 4 texture units
        int[] tex_maps = new int[4];

        for(int i=0; i < 4; i++)
            tex_maps[i] = 0;

        impl.setTextureSetMap(tex_maps,4);

        return impl;
    }

    public static void main(String[] args)
    {
        WoodDemo demo = new WoodDemo();
        demo.setVisible(true);
    }
}


