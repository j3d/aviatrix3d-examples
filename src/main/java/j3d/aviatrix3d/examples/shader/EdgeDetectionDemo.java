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
import org.j3d.geom.BoxGenerator;
import org.j3d.geom.SphereGenerator;
import org.j3d.geom.TorusGenerator;
import org.j3d.util.MatrixUtils;
import org.j3d.util.TriangleUtils;

/**
 * Example application demonstrating a simple depth of field renderer.
 * <p>
 *
 * Based on the discussion at:
 * http://www.gamedev.net/community/forums/topic.asp?topic_id=523491
 *
 * Not very good right now. Should look much more like the screen shot in that
 * thread.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class EdgeDetectionDemo extends BaseDemoFrame
{
    /** App name to register preferences under */
    private static final String APP_NAME = "EdgeDetectionDemo";

    /** Image file holding the local normal map */
    private static final String NORMAL_MAP_FILE =
        "images/examples/shader/gbuffer_normal.png";

    /** Render pass vertex shader string */
    private static final String MAT_VTX_SHADER_FILE =
        "shaders/examples/global_illum/edge_depth_vert.glsl";

    /** Fragment shader file name for the rendering pass */
    private static final String MAT_FRAG_SHADER_FILE =
        "shaders/examples/global_illum/edge_depth_frag.glsl";

    /** Render pass vertex shader string */
    private static final String RENDER_VTX_SHADER_FILE =
        "shaders/examples/global_illum/edge_render_vert.glsl";

    /** Fragment shader file name for the rendering pass */
    private static final String RENDER_FRAG_SHADER_FILE =
        "shaders/examples/global_illum/edge_render_frag.glsl";

    /** Width and height of the offscreen texture, in pixels */
    private static final int TEXTURE_SIZE = 256;

    /** Width and height of the main window, in pixels */
    private static final int WINDOW_SIZE = 512;

    /** PI / 4 for rotations */
    private static final float PI_4 = (float)(Math.PI * 0.25f);

    /** The view environment created for the main scene */
    private ViewEnvironment mainSceneEnv;

    /** Utility for doing matrix rotations */
    private MatrixUtils matrixUtils;

    /**
     * Construct a new shader demo instance.
     */
    public EdgeDetectionDemo()
    {
        super("Aviatrix Edge Detection Demo",
              new FrustumCullStage(),
              new StateAndTransparencyDepthSortStage(),
              false);

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.org-j3d-aviatrix3d-resources-core");

        matrixUtils = new MatrixUtils();
    }

    @Override
    protected void setupSceneGraph()
    {
        Vector3d real_view_pos = new Vector3d();
        real_view_pos.set(0, 0, 15f);
        Vector3d render_view_pos = new Vector3d();
        render_view_pos.set(0, 0, 0.9f);

        // two quads to draw to
        float[] quad_coords = { -1, -1, 0, 1, -1, 0, 1, 1, 0, -1, 1, 0 };
        float[] quad_normals = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[][] tex_coord = { { 0, 0,  1, 0,  1, 1,  0, 1 } };

        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };
        float[] ambient_blend = { 1, 1, 1 };

        QuadArray sphere_geom = new QuadArray();
        sphere_geom.setValidVertexCount(4);
        sphere_geom.setVertices(TriangleArray.COORDINATE_3, quad_coords);
        sphere_geom.setNormals(quad_normals);
        sphere_geom.setTextureCoordinates(tex_type, tex_coord, 1);
        sphere_geom.setSingleColor(false, ambient_blend);

        MRTOffscreenTexture2D off_tex = createRenderTargetTexture(real_view_pos);

        TextureUnit[] tex_unit = { new TextureUnit(), new TextureUnit() };
        tex_unit[0].setTexture(off_tex);
        tex_unit[1].setTexture(off_tex.getRenderTarget(1));

        // Create the depth render shader
        String[] vert_shader_txt = loadShaderFile(RENDER_VTX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(RENDER_FRAG_SHADER_FILE);

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

        float[] threshold = { 1.0f };
        float[] tex_size = { TEXTURE_SIZE, TEXTURE_SIZE };

        ShaderArguments shader_args = new ShaderArguments();
        shader_args.setUniform("weight", 1, threshold, 1);
        shader_args.setUniform("texSize", 2, tex_size, 1);
        shader_args.setUniformSampler("normalMap", 0);
        shader_args.setUniformSampler("colorMap", 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shader_args);

        Appearance app_1 = new Appearance();
        app_1.setShader(shader);
        app_1.setTextureUnits(tex_unit, 2);

        Shape3D shape_1 = new Shape3D();
        shape_1.setGeometry(sphere_geom);
        shape_1.setAppearance(app_1);

        Viewpoint vp = new Viewpoint();

        Matrix4d view_mat = new Matrix4d();
        view_mat.setIdentity();
        view_mat.setTranslation(render_view_pos);

        TransformGroup tx = new TransformGroup();
        tx.setTransform(view_mat);
        tx.addChild(vp);

        SharedNode common_geom = new SharedNode();
        common_geom.setChild(shape_1);

        Group root_grp = new Group();
        root_grp.addChild(tx);
        root_grp.addChild(common_geom);

        SimpleScene main_scene = new SimpleScene();
        main_scene.setRenderedGeometry(root_grp);
        main_scene.setActiveView(vp);

        ViewEnvironment env = main_scene.getViewEnvironment();
        env.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        env.setClipDistance(-1, 1);
        env.setOrthoParams(-1.1, 1.1, -1.1, 1.1);

        // Then the basic layer and viewport at the top:
        SimpleViewport viewport = new SimpleViewport();
        viewport.setDimensions(0, 0, WINDOW_SIZE, WINDOW_SIZE);
        viewport.setScene(main_scene);
        resizeManager.addManagedViewport(viewport);

        ShaderLoadStatusCallback cb =
            new ShaderLoadStatusCallback(vert_shader, frag_shader, shader_prog, resizeManager);
        sceneManager.setApplicationObserver(cb);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(viewport);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);
    }

    /**
     * Create the contents of the offscreen texture that is being rendered
     */
    private MRTOffscreenTexture2D createRenderTargetTexture(Vector3d viewPos)
    {
        Viewpoint vp = new Viewpoint();

        Matrix4d view_mat = new Matrix4d();
        view_mat.setIdentity();
        view_mat.setTranslation(viewPos);

        TransformGroup tx = new TransformGroup();
        tx.setTransform(view_mat);
        tx.addChild(vp);

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        // Sphere to represent the light position in the scene.
        SphereGenerator s_generator = new SphereGenerator(2.5f, 32);
        s_generator.generate(data);

        float[] tangents = new float[data.vertexCount * 4];

        TriangleUtils.createTangents(data.indexesCount / 3,
                                     data.indexes,
                                     data.coordinates,
                                     data.normals,
                                     data.textureCoordinates,
                                     tangents);

        IndexedTriangleArray sphere_geom = new IndexedTriangleArray();
        sphere_geom.setValidVertexCount(data.vertexCount);
        sphere_geom.setVertices(TriangleArray.COORDINATE_3, data.coordinates);
        sphere_geom.setIndices(data.indexes, data.indexesCount);
        sphere_geom.setNormals(data.normals);
        sphere_geom.setAttributes(5, 4, tangents, false);

        data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        BoxGenerator b_generator = new BoxGenerator(2.5f, 2.5f, 2.5f);
        b_generator.generate(data);

        // Generate tangent information

        tangents = new float[data.vertexCount * 4];

        TriangleUtils.createTangents(data.indexesCount / 3,
                                     data.indexes,
                                     data.coordinates,
                                     data.normals,
                                     data.textureCoordinates,
                                     tangents);

        IndexedTriangleArray box_geom = new IndexedTriangleArray();
        box_geom.setValidVertexCount(data.vertexCount);
        box_geom.setVertices(TriangleArray.COORDINATE_3, data.coordinates);
        box_geom.setIndices(data.indexes, data.indexesCount);
        box_geom.setNormals(data.normals);
        box_geom.setAttributes(5, 4, tangents, false);

        data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        TorusGenerator t_generator = new TorusGenerator(0.75f, 4.5f);
        t_generator.generate(data);

        tangents = new float[data.vertexCount * 4];

        TriangleUtils.createTangents(data.indexesCount / 3,
                                     data.indexes,
                                     data.coordinates,
                                     data.normals,
                                     data.textureCoordinates,
                                     tangents);

        IndexedTriangleArray torus_geom = new IndexedTriangleArray();
        torus_geom.setValidVertexCount(data.vertexCount);
        torus_geom.setVertices(TriangleArray.COORDINATE_3, data.coordinates);
        torus_geom.setIndices(data.indexes, data.indexesCount);
        torus_geom.setNormals(data.normals);
        torus_geom.setAttributes(5, 4, tangents, false);

        // Create the gbuffer shader
        String[] vert_shader_txt = loadShaderFile(MAT_VTX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(MAT_FRAG_SHADER_FILE);

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
        shader_prog.bindAttributeName("tangent", 5);
        shader_prog.requestInfoLog();
        shader_prog.link();

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);

        Material mat = new Material();
        mat.setDiffuseColor(new float[] {1, 1, 0, 1});
        mat.setSpecularColor(new float[] {1, 1, 1, 1});

        TextureUnit[] tex_units = { new TextureUnit() };
        TextureComponent2D normal_comp = loadTexture(NORMAL_MAP_FILE);
        if(normal_comp != null)
        {
            Texture2D normal_tex = new Texture2D(Texture2D.FORMAT_RGB, normal_comp);
            normal_tex.setBoundaryModeS(Texture.BM_WRAP);
            normal_tex.setBoundaryModeT(Texture.BM_WRAP);

            tex_units[0].setTexture(normal_tex);
        }


        Appearance app = new Appearance();
        app.setTextureUnits(tex_units, 1);
        app.setMaterial(mat);
        app.setShader(shader);

        Shape3D sphere_shape = new Shape3D();
        sphere_shape.setGeometry(sphere_geom);
        sphere_shape.setAppearance(app);

        Shape3D box_shape = new Shape3D();
        box_shape.setGeometry(box_geom);
        box_shape.setAppearance(app);

        Shape3D torus_shape = new Shape3D();
        torus_shape.setGeometry(torus_geom);
        torus_shape.setAppearance(app);

        // Transform the geometry in some way
        Matrix4d geom_mat1 = new Matrix4d();
        matrixUtils.rotateX(PI_4, geom_mat1);

        Matrix4d geom_mat2 = new Matrix4d();
        matrixUtils.rotateY(PI_4, geom_mat2);

        geom_mat2.mul(geom_mat2, geom_mat1);
        geom_mat2.m03 = 3.0f;

        TransformGroup box_tx = new TransformGroup();
        box_tx.setTransform(geom_mat2);
        box_tx.addChild(box_shape);

        geom_mat1.setIdentity();
        geom_mat1.m03 = -3.0f;

        TransformGroup sphere_tx = new TransformGroup();
        sphere_tx.setTransform(geom_mat1);
        sphere_tx.addChild(sphere_shape);

        geom_mat1.setIdentity();
        geom_mat1.m03 = -3.0f;
        geom_mat1.m13 = -1.5f;

        TransformGroup torus_tx = new TransformGroup();
        torus_tx.setTransform(geom_mat1);
        torus_tx.addChild(torus_shape);

        Group root_grp = new Group();
        root_grp.addChild(tx);
        root_grp.addChild(box_tx);
        root_grp.addChild(sphere_tx);
        root_grp.addChild(torus_tx);

        SimpleScene main_scene = new SimpleScene();
        main_scene.setRenderedGeometry(root_grp);
        main_scene.setActiveView(vp);

        mainSceneEnv = main_scene.getViewEnvironment();
        mainSceneEnv.setNearClipDistance(0.5);
        mainSceneEnv.setFarClipDistance(200);

        // Then the basic layer and viewport at the top:
        SimpleViewport viewport = new SimpleViewport();
        viewport.setDimensions(0, 0, TEXTURE_SIZE, TEXTURE_SIZE);
        viewport.setScene(main_scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(viewport);

        Layer[] layers = { layer };

        // The texture requires its own set of capabilities.
        GraphicsRenderingCapabilities caps = new GraphicsRenderingCapabilities();
        caps.doubleBuffered = false;
        caps.depthBits = 24;

        MRTOffscreenTexture2D off_tex =
            new MRTOffscreenTexture2D(caps, TEXTURE_SIZE, TEXTURE_SIZE, 2, false);

        off_tex.setClearColor(0.5f, 0.5f, 0.5f, 1);
        off_tex.setRepaintRequired(true);
        off_tex.setLayers(layers, 1);

//        ShaderLoadStatusCallback cb =
//            new ShaderLoadStatusCallback(vert_shader, frag_shader, shader_prog);
//        sceneManager.setApplicationObserver(cb);

        return off_tex;
    }

    public static void main(String[] args)
    {
        EdgeDetectionDemo demo = new EdgeDetectionDemo();
        demo.setVisible(true);
    }
}
