package j3d.aviatrix3d.examples.shader;

// External imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;

/**
 * Example application that demonstrates using the GLSLang Brick shader from
 * Chapter 6 of the Orange Book but with animated colour based on constantly
 * updating one of the input uniforms
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class UniformUpdateDemo extends BaseDemoFrame
{
    /** Vertex shader source file */
    private static final String VTX_SHADER_FILE =
        "shaders/examples/orangebook/CH06-brick.vert";

    /** Fragment shader source file */
    private static final String FRAG_SHADER_FILE =
        "shaders/examples/orangebook/CH06-brick.frag";

    /**
     * Construct a new shader demo instance.
     */
    public UniformUpdateDemo()
    {
        super("Demo Illustrating OpenGL Shaders from the orange book", false);
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

        float[] brick_colour = { 1, 0.3f, 0.2f };
        float[] mortar_colour = { 0.85f, 0.86f, 0.84f };
        float[] brick_size = { 0.3f, 0.15f };
        float[] brick_pct = { 0.9f, 0.85f };
        float[] light_pos = { 0, 0, 4 };

        ShaderArguments shaderArgs = new ShaderArguments();
        shaderArgs.setUniform("BrickColor", 3, brick_colour, 1);
        shaderArgs.setUniform("MortarColor", 3, mortar_colour, 1);
        shaderArgs.setUniform("BrickSize", 2, brick_size, 1);
        shaderArgs.setUniform("BrickPct", 2, brick_pct, 1);
        shaderArgs.setUniform("LightPosition", 3, light_pos, 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shaderArgs);

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
        shape_tx.setTransform(mat2);

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

        UniformAnimation anim = new UniformAnimation(shaderArgs, resizeManager);
        sceneManager.setApplicationObserver(anim);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        UniformUpdateDemo demo = new UniformUpdateDemo();
        demo.setVisible(true);
    }
}
