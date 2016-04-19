package j3d.aviatrix3d.examples.shader;

// External imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;

import org.j3d.geom.GeometryData;
import org.j3d.geom.SphereGenerator;

/**
 * Example application demonstrating the use multiple render targets. Two
 * pieces of geometry, with different colours.
 * <p>
 *
 * FIXME
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class MultipleRenderTargetDemo extends BaseDemoFrame
{
    /** App name to register preferences under */
    private static final String APP_NAME = "MultipleRenderTargetDemo";

    /** Render pass vertex shader string */
    private static final String VERTEX_SHADER_FILE =
        "shaders/examples/simple/mrt_vert.glsl";

    /** Fragment shader file name for the rendering pass */
    private static final String FRAG_SHADER_FILE =
        "shaders/examples/simple/mrt_frag.glsl";

    /** Width and height of the offscreen texture, in pixels */
    private static final int TEXTURE_SIZE = 256;

    /** Width and height of the main window, in pixels */
    private static final int WINDOW_SIZE = 512;

    /** PI / 4 for rotations */
    private static final float PI_4 = (float)(Math.PI * 0.25f);

    /**
     * Construct a new shader demo instance.
     */
    public MultipleRenderTargetDemo()
    {
        super("Multiple Render Target Demo",
              new FrustumCullStage(),
              new StateAndTransparencyDepthSortStage(),
              true);

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.org-j3d-aviatrix3d-resources-core");
    }

    @Override
    protected void setupSceneGraph()
    {
        // two quads to draw to
        float[] quad_coords = { -1, -1, 0, 1, -1, 0, 1, 1, 0, -1, 1, 0 };
        float[] quad_normals = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[][] tex_coord = { { 0, 0,  1, 0,  1, 1,  0, 1 } };

        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };

        QuadArray real_geom = new QuadArray();
        real_geom.setValidVertexCount(4);
        real_geom.setVertices(TriangleArray.COORDINATE_3, quad_coords);
        real_geom.setNormals(quad_normals);
        real_geom.setTextureCoordinates(tex_type, tex_coord, 1);

        MRTOffscreenTexture2D off_tex = createRenderTargetTexture();
//        OffscreenTexture2D off_tex = createRenderTargetTexture();

        TextureUnit[] tex_unit = { new TextureUnit() };
        tex_unit[0].setTexture(off_tex);

        Material mater_1 = new Material();
        mater_1.setEmissiveColor(new float[] { 0, 1, 0, 1} );

        Appearance app_1 = new Appearance();
        app_1.setMaterial(mater_1);
        app_1.setTextureUnits(tex_unit, 1);

        Shape3D shape_1 = new Shape3D();
        shape_1.setGeometry(real_geom);
        shape_1.setAppearance(app_1);

        Vector3d trans = new Vector3d();
        trans.set(-1.5f, 0, 0);

        Matrix4d mat_1 = new Matrix4d();
        mat_1.set(trans);

        TransformGroup tg_1 = new TransformGroup();
        tg_1.setTransform(mat_1);
        tg_1.addChild(shape_1);

        tex_unit = new TextureUnit[] { new TextureUnit() };
        tex_unit[0].setTexture(off_tex.getRenderTarget(1));

        Material mater_2 = new Material();
        mater_2.setEmissiveColor(new float[] { 0, 0, 1, 1} );

        Appearance app_2 = new Appearance();
        app_2.setMaterial(mater_2);
        app_2.setTextureUnits(tex_unit, 1);

        Shape3D shape_2 = new Shape3D();
        shape_2.setGeometry(real_geom);
        shape_2.setAppearance(app_2);

        trans = new Vector3d();
        trans.set(1.5f, 0, 0);

        Matrix4d mat_2 = new Matrix4d();
        mat_2.set(trans);

        TransformGroup tg_2 = new TransformGroup();
        tg_2.setTransform(mat_2);
        tg_2.addChild(shape_2);


        Viewpoint vp = new Viewpoint();

        trans = new Vector3d();
        trans.set(0, 0, 10);

        Matrix4d view_mat = new Matrix4d();
        view_mat.set(trans);

        TransformGroup tx = new TransformGroup();
        tx.setTransform(view_mat);
        tx.addChild(vp);

        Group root_grp = new Group();
        root_grp.addChild(tx);
        root_grp.addChild(tg_1);
        root_grp.addChild(tg_2);


        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(root_grp);
        scene.setActiveView(vp);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, WINDOW_SIZE, WINDOW_SIZE);
        view.setScene(scene);
        resizeManager.addManagedViewport(view);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);

    }

    /**
     * Create the contents of the offscreen texture that is being rendered
     */
    private MRTOffscreenTexture2D createRenderTargetTexture()
    {
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 10);

        Matrix4d view_mat = new Matrix4d();
        view_mat.set(trans);

        TransformGroup tx = new TransformGroup();
        tx.setTransform(view_mat);
        tx.addChild(vp);

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        // Sphere to represent the light position in the scene.
        SphereGenerator generator = new SphereGenerator(2.5f, 32);
        generator.generate(data);

        TriangleArray real_geom = new TriangleArray();
        real_geom.setValidVertexCount(data.vertexCount);
        real_geom.setVertices(TriangleArray.COORDINATE_3, data.coordinates);
        real_geom.setNormals(data.normals);

        String[] vert_shader_txt = loadShaderFile(VERTEX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(FRAG_SHADER_FILE);

        ShaderObject vert_shader = new ShaderObject(true);
        vert_shader.setSourceStrings(vert_shader_txt, 1);
        vert_shader.requestInfoLog();
        vert_shader.compile();

        ShaderObject frag_shader = new ShaderObject(false);
        frag_shader.setSourceStrings(frag_shader_txt, 1);
        frag_shader.requestInfoLog();
        frag_shader.compile();

        ShaderProgram shader_prog = new ShaderProgram();
        shader_prog.addShaderObject(vert_shader);
        shader_prog.addShaderObject(frag_shader);
        shader_prog.requestInfoLog();
        shader_prog.link();

        ShaderArguments shader_args = new ShaderArguments();

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);

        Material mat = new Material();
        mat.setEmissiveColor(new float[] {1, 0, 0, 1});

        Appearance app = new Appearance();
        app.setMaterial(mat);
        app.setShader(shader);

        Shape3D shape = new Shape3D();
        shape.setGeometry(real_geom);
        shape.setAppearance(app);

        Group root_grp = new Group();
        root_grp.addChild(tx);
        root_grp.addChild(shape);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(root_grp);
        scene.setActiveView(vp);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, TEXTURE_SIZE, TEXTURE_SIZE);
        view.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };

        // The texture requires its own set of capabilities.
        GraphicsRenderingCapabilities caps = new GraphicsRenderingCapabilities();
        caps.doubleBuffered = false;

        MRTOffscreenTexture2D off_tex =
            new MRTOffscreenTexture2D(caps, TEXTURE_SIZE, TEXTURE_SIZE, 2);

//        OffscreenTexture2D off_tex =
//            new OffscreenTexture2D(caps, TEXTURE_SIZE, TEXTURE_SIZE);
        off_tex.setClearColor(0.5f, 0.5f, 0.5f, 1);
        off_tex.setRepaintRequired(true);
        off_tex.setLayers(layers, 1);

        ShaderLoadStatusCallback cb =
            new ShaderLoadStatusCallback(vert_shader, frag_shader, shader_prog, resizeManager);
        sceneManager.setApplicationObserver(cb);

        return off_tex;
    }

    public static void main(String[] args)
    {
        MultipleRenderTargetDemo demo = new MultipleRenderTargetDemo();
        demo.setVisible(true);
    }
}
