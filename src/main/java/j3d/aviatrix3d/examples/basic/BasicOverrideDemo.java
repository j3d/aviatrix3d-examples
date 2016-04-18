package j3d.aviatrix3d.examples.basic;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.graphics.FrustumCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.StateAndTransparencyDepthSortStage;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

/**
 * Example application showing the use of overrides for appearance
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class BasicOverrideDemo extends BaseDemoFrame
{
    /** Red colour constant */
    private static final float[] RED = { 1, 0, 0 };

    /** Blue colour constant */
    private static final float[] BLUE = { 0, 0, 1 };

    public BasicOverrideDemo()
    {
        super("Basic Appearance Override Demo",
              new FrustumCullStage(),
              new StateAndTransparencyDepthSortStage(),
              true);
    }

    /**
     * Setup the basic scene which consists of a quad and a viewpoint
     */
    protected void setupSceneGraph()
    {
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

        Material basic_mat = new Material();
        basic_mat.setEmissiveColor(RED);
        Appearance basic_app = new Appearance();
        basic_app.setMaterial(basic_mat);

        Material override_mat = new Material();
        override_mat.setEmissiveColor(BLUE);
        Appearance override_app = new Appearance();
        override_app.setMaterial(override_mat);

        // Flat panel that has the viewable object as the demo
        float[] coord = { 0, 0, -1, 0.25f, 0, -1, 0, 0.25f, -1 };
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        //float[] color = { 0, 0, 1, 0, 1, 0, 1, 0, 0 };

        TriangleArray geom = new TriangleArray();
        geom.setValidVertexCount(3);
        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setNormals(normal);
        //geom.setColors(false, color);

        Shape3D basic_shape = new Shape3D();
        basic_shape.setGeometry(geom);
        basic_shape.setAppearance(basic_app);

        Shape3D override_shape = new Shape3D();
        override_shape.setGeometry(geom);
        override_shape.setAppearance(basic_app);

        trans.set(0, 0.5f, 0);
        Matrix4d mat2 = new Matrix4d();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        AppearanceOverride app_ovr = new AppearanceOverride();
        app_ovr.setAppearance(override_app);
        app_ovr.setEnabled(true);

        TransformGroup override_transform = new TransformGroup();
        override_transform.addChild(override_shape);
        override_transform.addChild(app_ovr);
        override_transform.setTransform(mat2);

        scene_root.addChild(override_transform);
        scene_root.addChild(basic_shape);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, 600, 600);
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
        BasicOverrideDemo demo = new BasicOverrideDemo();
        demo.setVisible(true);
    }
}
