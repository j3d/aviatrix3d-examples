package j3d.aviatrix3d.examples.background;

// Standard imports
import j3d.aviatrix3d.examples.basic.BaseDemoFrame;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Application Specific imports
import org.j3d.aviatrix3d.*;

/**
 * Example application that demonstrates the simple SphereBackground class
 * usage.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class SphereBackgroundDemo extends BaseDemoFrame
{
    public SphereBackgroundDemo()
    {
        super("SkySphere Background Demo");
    }

    @Override
    protected void setupSceneGraph()
    {
        // Load the texture image
        TextureComponent2D img_comp = loadTexture("images/examples/background/globe_map_2.jpg");

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 1);

        Matrix4d mat = new Matrix4d();
        mat.set(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

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

        scene_root.addChild(shape);

        SphereBackground bg = new SphereBackground();
        bg.setColor(1, 0, 0, 0);
        bg.setTexture(img_comp);

        scene_root.addChild(bg);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveBackground(bg);
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
        SphereBackgroundDemo demo = new SphereBackgroundDemo();
        demo.setVisible(true);
    }
}
