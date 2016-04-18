package j3d.aviatrix3d.examples.basic;

// External imports
import java.awt.*;
import java.awt.event.*;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.pipeline.OutputDevice;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class GraphicsCardInfoDemo extends BaseDemoFrame
    implements SurfaceInfoListener
{
    /** App name to register preferences under */
    private static final String APP_NAME = "examples.GraphicsCardInfoDemo";

    public GraphicsCardInfoDemo()
    {
        super("Graphics Info Demo");

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.org-j3d-aviatrix3d-resources-core");
    }

    //---------------------------------------------------------------
    // Methods defined by SurfaceInfoListener
    //---------------------------------------------------------------

    @Override
    public void surfaceInfoChanged(OutputDevice surface, SurfaceInfo info)
    {
        System.out.println("Graphics card info");
        System.out.println("GL Version " + info.getGLMajorVersion() +
                           "." + info.getGLMinorVersion());
        System.out.println("Vendor: " + info.getVendorString());
        System.out.println("Driver: " + info.getDriverInfo());
        System.out.println("Shader Version " + info.getShaderMajorVersion() +
                           "." + info.getShaderMinorVersion());
        System.out.println("Max lights:      " + info.getMaxLightCount());
        System.out.println("Max clip planes: " + info.getMaxClipPlanesCount());
        System.out.println("Max textures:    " + info.getMaxTextureCount());
        System.out.println("Max RTs:         " + info.getMaxMRTCount());
        System.out.println("Max Color Atch:  " + info.getMaxColorAttachmentsCount());
    }

    @Override
    protected void setupSceneGraph()
    {
        surface.addSurfaceInfoListener(this);

        // View group

        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 1);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Flat panel that has the viewable object as the demo
        float[] coord = { 0, 0, -1, 0.25f, 0, -1, 0, 0.25f, -1 };
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        //float[] color = { 0, 0, 1, 0, 1, 0, 1, 0, 0 };

        TriangleArray geom = new TriangleArray();
        geom.setValidVertexCount(3);
        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setNormals(normal);
        //geom.setColors(false, color);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);

        trans.set(0.2f, 0.5f, 0);
        Matrix4d mat2 = new Matrix4d();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        TransformGroup shape_transform = new TransformGroup();
        shape_transform.addChild(shape);
        shape_transform.setTransform(mat2);

        scene_root.addChild(shape_transform);

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

    public static void main(String[] args)
    {
        GraphicsCardInfoDemo demo = new GraphicsCardInfoDemo();
        demo.setVisible(true);
    }
}
