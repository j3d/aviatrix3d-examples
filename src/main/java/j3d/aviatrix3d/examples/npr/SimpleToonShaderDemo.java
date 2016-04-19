package j3d.aviatrix3d.examples.npr;

// External imports
import java.awt.*;
import java.awt.event.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

import org.j3d.util.DataUtils;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.geom.GeometryData;
import org.j3d.geom.SphereGenerator;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class SimpleToonShaderDemo extends BaseDemoFrame
{
    /** App name to register preferences under */
    private static final String APP_NAME = "examples.SimpleToonShaderDemo";

    /** List of available vertex shaders for the geometry */
    private static final String VTX_SHADER_FILE =
        "shaders/examples/npr/SimpleToonVert.glsl";

    /** List of available fragment shaders for the geometry */
    private static final String FRAG_SHADER_FILE =
        "shaders/examples/npr/SimpleToonFrag.glsl";

    /**
     * Construct a new shader demo instance.
     */
    public SimpleToonShaderDemo()
    {
        super("Toon Shader Demo",
              new FrustumCullStage(),
              new StateAndTransparencyDepthSortStage(),
              false);

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.org-j3d-aviatrix3d-resources-core");
    }

    @Override
    protected void setupSceneGraph()
    {
        // View group
        Viewpoint vp = new Viewpoint();
        vp.setHeadlightEnabled(false);
        Vector3d trans = new Vector3d();
        trans.set(0, 0, 4.0f);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup vp_tx = new TransformGroup();
        vp_tx.setTransform(mat);
        vp_tx.addChild(vp);

        // Put a light in to the scene. We'll animate this to show off the
        // toon shading effects moving
        PointLight light = new PointLight();
        light.setEnabled(true);
        light.setGlobalOnly(true);

        trans = new Vector3d();
        trans.set(5, 0, 10f);
        mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup light_tx = new TransformGroup();
        light_tx.setTransform(mat);
        light_tx.addChild(light);

        // Sphere to render the shader onto
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        SphereGenerator generator = new SphereGenerator(1.0f, 50);
        generator.generate(data);

        TriangleArray geom = new TriangleArray();
        geom.setValidVertexCount(data.vertexCount);
        geom.setVertices(TriangleArray.COORDINATE_3, data.coordinates);
        geom.setNormals(data.normals);

        Material material = new Material();
        material.setDiffuseColor(new float[] { 0, 1, 1 });
        material.setEmissiveColor(new float[] { 0, 0, 1 });

        String[] vert_shader_txt = loadShaderFile(VTX_SHADER_FILE);
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

        int[] light_id = { 1 };
        float[] outline_width = { 0.75f };
        float[] outline_colour = { 0, 0, 0, 1 };

        ShaderArguments shader_args = new ShaderArguments();
        shader_args.setUniform("selectedLight", 1, light_id, 1);
        shader_args.setUniform("outlineWidth", 1, outline_width, 1);
        shader_args.setUniform("outlineColour", 4, outline_colour, 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shader_args);

        Appearance app = new Appearance();
        app.setMaterial(material);
        app.setShader(shader);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        Group scene_root = new Group();
        scene_root.addChild(vp_tx);
        scene_root.addChild(light_tx);
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

        SimpleToonCallback stc =
            new SimpleToonCallback(light_tx,
                                   vert_shader,
                                   frag_shader,
                                   shader_prog,
                                   resizeManager);
        sceneManager.setApplicationObserver(stc);

    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    /**
     * Ignored
     */
    public void windowActivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowClosed(WindowEvent evt)
    {
    }

    /**
     * Exit the application
     *
     * @param evt The event that caused this method to be called.
     */
    public void windowClosing(WindowEvent evt)
    {
        sceneManager.shutdown();
        System.exit(0);
    }

    /**
     * Ignored
     */
    public void windowDeactivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowDeiconified(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowIconified(WindowEvent evt)
    {
    }

    /**
     * When the window is opened, start everything up.
     */
    public void windowOpened(WindowEvent evt)
    {
        sceneManager.setEnabled(true);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Load the shader file. Find it relative to the classpath.
     *
     * @param name THe name of the file to load
     */
    private String[] loadShaderFile(String name)
    {
        File file = DataUtils.lookForFile(name, getClass(), null);
        if(file == null)
        {
            System.out.println("Cannot find file " + name);
            return null;
        }

        String ret_val = null;

        try
        {
            FileReader is = new FileReader(file);
            StringBuffer buf = new StringBuffer();
            char[] read_buf = new char[1024];
            int num_read = 0;

            while((num_read = is.read(read_buf, 0, 1024)) != -1)
                buf.append(read_buf, 0, num_read);

            is.close();

            ret_val = buf.toString();
        }
        catch(IOException ioe)
        {
            System.out.println("I/O error " + ioe);
        }

        return new String[] { ret_val };
    }

    public static void main(String[] args)
    {
        SimpleToonShaderDemo demo = new SimpleToonShaderDemo();
        demo.setVisible(true);
    }
}
