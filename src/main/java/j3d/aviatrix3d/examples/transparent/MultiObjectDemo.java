package j3d.aviatrix3d.examples.transparent;

// Standard imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Application Specific imports
import j3d.aviatrix3d.examples.basic.BaseDemoFrame;
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.pipeline.graphics.TransparencyDepthSortStage;

/**
 * Example application that demonstrates using transparent material nodes with Aviatrix.
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public class MultiObjectDemo extends BaseDemoFrame
{
    public MultiObjectDemo()
    {
        super("Transparency Sorting Demo", null, new TransparencyDepthSortStage(), true);
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

        TriangleArray geom = new TriangleArray();
        geom.setValidVertexCount(3);
        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setNormals(normal);

        Material material = new Material();
        material.setDiffuseColor(new float[] { 0, 0, 1 });
        material.setEmissiveColor(new float[] { 0, 0, 1 });
        material.setSpecularColor(new float[] { 1, 1, 1 });
        material.setTransparency(0.5f);

        Appearance app = new Appearance();
        app.setMaterial(material);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        trans = new Vector3d();
        trans.set(0.15f, 0, -2);

        TransformGroup tg = new TransformGroup();
        Matrix4d transform = new Matrix4d();
        transform.set(trans);
        tg.setTransform(transform);

        Shape3D backShape = new Shape3D();
        Material material2 = new Material();
        material2.setDiffuseColor(new float[] { 1, 0, 0 });
        material2.setEmissiveColor(new float[] { 1, 0, 0 });
        material2.setSpecularColor(new float[] { 1, 1, 1 });
        material2.setTransparency(0.5f);

        Appearance app2 = new Appearance();
        app2.setMaterial(material2);
        backShape.setGeometry(geom);
        backShape.setAppearance(app2);
        tg.addChild(backShape);

        ColorBackground cbg = new ColorBackground(new float[] {0, 0.3f, 0, 1});

        scene_root.addChild(cbg);
        scene_root.addChild(shape);
        scene_root.addChild(tg);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);
        scene.setActiveBackground(cbg);

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
        MultiObjectDemo demo = new MultiObjectDemo();
        demo.setVisible(true);
    }
}
