package j3d.aviatrix3d.examples.multipass;

// External imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;

/**
 * Example application that demonstrates a very basic 2-pass multipass
 * rendering. Pass 1 draws a red triangle on the left, pass 2 draws a blue
 * triangle on the right.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class BasicMultipassDemo extends BaseDemoFrame
{
    public BasicMultipassDemo()
    {
        super("Basic Aviatrix Multipass Rendering Demo");
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
        float[] coord = { 0, 0, -1, 0.25f, 0, -1, 0, 0.25f, -1 };
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[] color1 = { 1, 0, 0, 1, 0, 0, 1, 0, 0 };
        float[] color2 = { 0, 0, 1, 0, 0, 1, 0, 0, 1 };

        TriangleArray geom = new TriangleArray();
        geom.setValidVertexCount(3);
        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setNormals(normal);
        geom.setColors(false, color1);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);

        trans.set(-0.2f, 0, 0);
        Matrix4d mat2 = new Matrix4d();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        TransformGroup shape_transform = new TransformGroup();
        shape_transform.addChild(shape);
        shape_transform.setTransform(mat2);

        scene_root.addChild(shape_transform);

        RenderPass pass1 = new RenderPass();
        pass1.setRenderedGeometry(scene_root);
        pass1.setActiveView(vp);

        // And now the second pass - a blue triangle on the right
        vp = new Viewpoint();

        trans = new Vector3d();
        trans.set(0, 0, 1);

        mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        scene_root = new Group();
        scene_root.addChild(tx);

        geom = new TriangleArray();
        geom.setValidVertexCount(3);
        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setNormals(normal);
        geom.setColors(false, color2);

        shape = new Shape3D();
        shape.setGeometry(geom);

        trans.set(0.2f, 0, 0);
        mat2 = new Matrix4d();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        shape_transform = new TransformGroup();
        shape_transform.addChild(shape);
        shape_transform.setTransform(mat2);

        scene_root.addChild(shape_transform);

        RenderPass pass2 = new RenderPass();
        pass2.setRenderedGeometry(scene_root);
        pass2.setActiveView(vp);


        MultipassScene scene = new MultipassScene();
        scene.addRenderPass(pass1);
        scene.addRenderPass(pass2);

        // Then the basic layer and viewport at the top:
        MultipassViewport view = new MultipassViewport();
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
        BasicMultipassDemo demo = new BasicMultipassDemo();
        demo.setVisible(true);
    }
}
