package j3d.aviatrix3d.examples.basic;

// External imports
import java.awt.*;
import java.awt.event.*;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.FrustumCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.StateAndTransparencyDepthSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

/**
 * Example application showing the use of overrides for appearance
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class CompoundOverrideDemo extends BaseDemoFrame
{
    /** Red colour constant */
    private static final float[] RED = { 1, 0, 0 };

    /** Green colour constant */
    private static final float[] GREEN = { 0, 1, 0 };

    /** Blue colour constant */
    private static final float[] BLUE = { 0, 0, 1 };

    public CompoundOverrideDemo()
    {
        super("Compound Appearance Override Demo",
              new FrustumCullStage(),
              new StateAndTransparencyDepthSortStage(),
              true);
    }

    @Override
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

        Material middle_mat = new Material();
        middle_mat.setEmissiveColor(GREEN);
        Appearance middle_app = new Appearance();
        middle_app.setMaterial(middle_mat);

        Material override_mat = new Material();
        override_mat.setEmissiveColor(BLUE);
        Appearance override_app = new Appearance();
        override_app.setMaterial(override_mat);

        // Flat panel that has the viewable object as the demo
        float[] coord = { 0, 0, -1, 0.25f, 0, -1, 0, 0.25f, -1 };
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1 };

        TriangleArray geom = new TriangleArray();
        geom.setValidVertexCount(3);
        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setNormals(normal);

        // Shape that should always appear red
        Shape3D basic_shape = new Shape3D();
        basic_shape.setGeometry(geom);
        basic_shape.setAppearance(basic_app);

        // Shapes on the left that will appear green due to parent override
        Shape3D demo_shape = new Shape3D();
        demo_shape.setGeometry(geom);
        demo_shape.setAppearance(basic_app);

        SharedNode shared_shape = new SharedNode();
        shared_shape.setChild(demo_shape);

        trans.set(-0.5f, 0, 0);
        Matrix4d left_mat = new Matrix4d();
        left_mat.setIdentity();
        left_mat.setTranslation(trans);

        trans.set(0.5f, 0, 0);
        Matrix4d right_mat = new Matrix4d();
        right_mat.setIdentity();
        right_mat.setTranslation(trans);

        trans.set(0, 0.25f, 0);
        Matrix4d p1_mat = new Matrix4d();
        p1_mat.setIdentity();
        p1_mat.setTranslation(trans);

        trans.set(0, -0.25f, 0);
        Matrix4d p3_mat = new Matrix4d();
        p3_mat.setIdentity();
        p3_mat.setTranslation(trans);

        AppearanceOverride l1_ovr = new AppearanceOverride();
        l1_ovr.setAppearance(override_app);
        l1_ovr.setOverrideLower(true);
        l1_ovr.setEnabled(true);

        AppearanceOverride l2_ovr = new AppearanceOverride();
        l2_ovr.setAppearance(middle_app);
        l2_ovr.setOverrideLower(true);
        l2_ovr.setEnabled(true);

        AppearanceOverride r1_ovr = new AppearanceOverride();
        r1_ovr.setAppearance(override_app);
        r1_ovr.setOverrideLower(false);
        r1_ovr.setEnabled(true);

        AppearanceOverride r2_ovr = new AppearanceOverride();
        r2_ovr.setAppearance(middle_app);
        r2_ovr.setOverrideLower(false);
        r2_ovr.setEnabled(true);

        // Build the transforms on the left side.
        TransformGroup l3_transform = new TransformGroup();
        l3_transform.addChild(shared_shape);
        l3_transform.setTransform(p3_mat);

        TransformGroup l2_transform = new TransformGroup();
        l2_transform.addChild(shared_shape);
        l2_transform.addChild(l2_ovr);
        l2_transform.addChild(l3_transform);
        l2_transform.setTransform(p3_mat);

        TransformGroup l1_transform = new TransformGroup();
        l1_transform.addChild(l1_ovr);
        l1_transform.addChild(shared_shape);
        l1_transform.addChild(l2_transform);
        l1_transform.setTransform(p1_mat);

        TransformGroup l_transform = new TransformGroup();
        l_transform.addChild(l1_transform);
        l_transform.setTransform(left_mat);

        // Build the transforms on the right side.
        TransformGroup r3_transform = new TransformGroup();
        r3_transform.addChild(shared_shape);
        r3_transform.setTransform(p3_mat);

        TransformGroup r2_transform = new TransformGroup();
        r2_transform.addChild(shared_shape);
        r2_transform.addChild(r2_ovr);
        r2_transform.addChild(r3_transform);
        r2_transform.setTransform(p3_mat);

        TransformGroup r1_transform = new TransformGroup();
        r1_transform.addChild(r1_ovr);
        r1_transform.addChild(shared_shape);
        r1_transform.addChild(r2_transform);
        r1_transform.setTransform(p1_mat);

        TransformGroup r_transform = new TransformGroup();
        r_transform.addChild(r1_transform);
        r_transform.setTransform(right_mat);

        scene_root.addChild(l_transform);
        scene_root.addChild(shared_shape);
        scene_root.addChild(r_transform);

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
        CompoundOverrideDemo demo = new CompoundOverrideDemo();
        demo.setVisible(true);
    }
}
