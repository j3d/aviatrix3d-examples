package j3d.aviatrix3d.examples.geometry;

// External imports
import j3d.aviatrix3d.examples.basic.BaseDemoFrame;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;

/**
 * Example application that demonstrates the use of TriStrip Arrays
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class TriStripArrayDemo extends BaseDemoFrame
{
    public TriStripArrayDemo()
    {
        super("Aviatrix TriStripArray Demo");
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

        // Flat panel that has the viewable object as the demo
//        int[] index = {0, 1, 2};
//        float[] coord = { 0, 0, -1,  0.25f, 0, -1, 0, 0.25f, -1 };
//        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1 };
//        int[] fanCount = {3};

        int[] index = {7, 4, 3, 1, 2};
        float[] coord = {0.5f, 0.5f, -0.5f, 0.5f, -0.5f, 0, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0, -0.5f, 0.5f, 0, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0};
        float[] normal = {0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1};
        int[] fanCount = {5};

        IndexedTriangleFanArray geom = new IndexedTriangleFanArray();
        geom.setVertices(IndexedTriangleFanArray.COORDINATE_3, coord);
        geom.setIndices(index, 5);
        geom.setIndices(index, 3);
        geom.setFanCount(fanCount, 1);
        geom.setNormals(normal);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);

        trans.set(0.2f, 0, 0);
        Matrix4d mat2 = new Matrix4d();
        mat2.setIdentity();
        mat2.set(0.25);
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

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        TriStripArrayDemo demo = new TriStripArrayDemo();
        demo.setVisible(true);
    }
}
