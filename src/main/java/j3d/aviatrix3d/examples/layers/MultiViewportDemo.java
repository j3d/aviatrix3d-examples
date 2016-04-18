package j3d.aviatrix3d.examples.layers;

// External imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;

/**
 * Example application that demonstrates a single layer with multiple viewports
 * onto a common scene. To illustrate each viewport, a different background
 * colour is selected and a slightly different viewpoint location is used to
 * illustrate how you could create a multiple view environment like a CAD
 * application.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class MultiViewportDemo extends MultiViewBaseDemoFrame
{
    private static final float[] RED = {1, 0, 0};
    private static final float[] GREEN = {0, 1, 0};
    private static final float[] BLUE = {0, 0, 1};

    public MultiViewportDemo()
    {
        super("Multiple Viewport Aviatrix3D Demo");
    }

    /**
     * Setup the basic scene which consists of a quad and a viewpoint
     */
    protected void setupSceneGraph()
    {
        // Flat panel that has the viewable object as the demo
        float[] coord = { 0, 0, -1, 0.25f, 0, -1, 0, 0.25f, -1 };
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[] color = { 0, 0, 1, 0, 1, 0, 1, 0, 0 };

        TriangleArray geom = new TriangleArray();
        geom.setValidVertexCount(3);
        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setNormals(normal);
        geom.setColors(false, color);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);

        Vector3d trans = new Vector3d();
        trans.set(0.2f, 0.5f, 0);
        Matrix4d mat2 = new Matrix4d();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        TransformGroup shape_transform = new TransformGroup();
        shape_transform.addChild(shape);
        shape_transform.setTransform(mat2);

        SharedGroup shared_scene = new SharedGroup();
        shared_scene.addChild(shape_transform);

        Viewpoint vp1 = new Viewpoint();
        Background bg1 = new ColorBackground(RED);
        trans = new Vector3d();
        trans.set(0, 0, 1);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp1);
        tx.setTransform(mat);

        Group view_group1 = new Group();
        view_group1.addChild(shared_scene);
        view_group1.addChild(bg1);
        view_group1.addChild(tx);


        Viewpoint vp2 = new Viewpoint();
        Background bg2 = new ColorBackground(GREEN);
        trans = new Vector3d();
        trans.set(1, 0, 1);

        mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        tx = new TransformGroup();
        tx.addChild(vp2);
        tx.setTransform(mat);

        Group view_group2 = new Group();
        view_group2.addChild(shared_scene);
        view_group2.addChild(bg2);
        view_group2.addChild(tx);

        Viewpoint vp3 = new Viewpoint();
        Background bg3 = new ColorBackground(BLUE);
        trans = new Vector3d();
        trans.set(0, 1, 1);

        mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        tx = new TransformGroup();
        tx.addChild(vp3);
        tx.setTransform(mat);

        Group view_group3 = new Group();
        view_group3.addChild(shared_scene);
        view_group3.addChild(bg3);
        view_group3.addChild(tx);

        // Group 4 has no background. It should default back to black, as
        // the global default setting.
        Viewpoint vp4 = new Viewpoint();
        trans = new Vector3d();
        trans.set(0, -1, 1);

        mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        tx = new TransformGroup();
        tx.addChild(vp4);
        tx.setTransform(mat);

        Group view_group4 = new Group();
        view_group4.addChild(shared_scene);
        view_group4.addChild(tx);

        SimpleScene scene1 = new SimpleScene();
        scene1.setRenderedGeometry(view_group1);
        scene1.setActiveView(vp1);
        scene1.setActiveBackground(bg1);

        SimpleScene scene2 = new SimpleScene();
        scene2.setRenderedGeometry(view_group2);
        scene2.setActiveView(vp2);
        scene2.setActiveBackground(bg2);

        SimpleScene scene3 = new SimpleScene();
        scene3.setRenderedGeometry(view_group3);
        scene3.setActiveView(vp2);
        scene3.setActiveBackground(bg3);

        SimpleScene scene4 = new SimpleScene();
        scene4.setRenderedGeometry(view_group4);
        scene4.setActiveView(vp2);

        // Then the viewports. Divide the screen up into 4 viewports
        SimpleViewport view1 = new SimpleViewport();
        view1.setDimensions(0, 0, 400, 400);
        view1.setScene(scene1);

        SimpleViewport view2 = new SimpleViewport();
        view2.setDimensions(400, 0, 400, 400);
        view2.setScene(scene2);

        SimpleViewport view3 = new SimpleViewport();
        view3.setDimensions(0, 400, 400, 400);
        view3.setScene(scene3);

        SimpleViewport view4 = new SimpleViewport();
        view4.setDimensions(400, 400, 400, 400);
        view4.setScene(scene4);

        CompositeLayer layer = new CompositeLayer();
        layer.addViewport(view1);
        layer.addViewport(view2);
        layer.addViewport(view3);
        layer.addViewport(view4);

        resizeManager.addManagedViewport(view1, 0, 0, 0.5f, 0.5f);
        resizeManager.addManagedViewport(view2, 0.5f, 0, 0.5f, 0.5f);
        resizeManager.addManagedViewport(view3, 0, 0.5f, 0.5f, 0.5f);
        resizeManager.addManagedViewport(view4, 0.5f, 0.5f, 0.5f, 0.5f);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        MultiViewportDemo demo = new MultiViewportDemo();
        demo.setVisible(true);
    }
}
