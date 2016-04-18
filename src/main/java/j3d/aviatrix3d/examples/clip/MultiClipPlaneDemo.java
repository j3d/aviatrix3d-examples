package j3d.aviatrix3d.examples.clip;

// Standard imports
import j3d.aviatrix3d.examples.basic.BaseDemoFrame;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Application Specific imports
import org.j3d.aviatrix3d.*;

import org.j3d.geom.GeometryData;
import org.j3d.geom.SphereGenerator;

/**
 * Example application that demonstrates using multiple clip planes at
 * different levels of the heirarchy.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class MultiClipPlaneDemo extends BaseDemoFrame
{
    public MultiClipPlaneDemo()
    {
        super("Multiple Clip Plane Demo");
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

        // Sphere to render the shader onto
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        SphereGenerator generator = new SphereGenerator(0.1f);
        generator.generate(data);

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setNormals(data.normals);

        Material material1 = new Material();
        material1.setDiffuseColor(new float[] { 0, 0, 1 });
        material1.setEmissiveColor(new float[] { 0, 0, 1 });
        material1.setSpecularColor(new float[] { 1, 1, 1 });

        PolygonAttributes pa = new PolygonAttributes();
//        pa.setDrawMode(true, PolygonAttributes.DRAW_LINE);
        pa.setCulledFace(PolygonAttributes.CULL_BACK);


        Appearance app1 = new Appearance();
        app1.setMaterial(material1);
        app1.setPolygonAttributes(pa);

        Material material2 = new Material();
        material2.setDiffuseColor(new float[] { 1, 0, 0 });
        material2.setEmissiveColor(new float[] { 1, 0, 0 });
        material2.setSpecularColor(new float[] { 1, 1, 1 });

        Appearance app2 = new Appearance();
        app2.setMaterial(material2);
        app2.setPolygonAttributes(pa);

        Shape3D shape1 = new Shape3D();
        shape1.setGeometry(geom);
        shape1.setAppearance(app1);

        Shape3D shape2 = new Shape3D();
        shape2.setGeometry(geom);
        shape2.setAppearance(app2);

        double[] plane_eq1 = { 0, 1, 0, 0 };
        double[] plane_eq2 = { 1, 0, 0, 0 };
        double[] plane_eq3 = { 1, 1, 0, 0 };

        ClipPlane cp1 = new ClipPlane();
        cp1.setPlaneEquation(plane_eq1);
        cp1.setEnabled(true);

        ClipPlane cp2 = new ClipPlane();
        cp2.setPlaneEquation(plane_eq2);
        cp2.setEnabled(true);

        ClipPlane cp3 = new ClipPlane();
        cp3.setPlaneEquation(plane_eq3);
        cp3.setEnabled(true);

        scene_root.addChild(cp1);

        trans = new Vector3d();
        trans.set(-0.2f, 0.025f, 0);

        mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup left_tx = new TransformGroup();
        left_tx.setTransform(mat);
        left_tx.addChild(shape1);
        left_tx.addChild(cp2);

        scene_root.addChild(left_tx);

        trans = new Vector3d();
        trans.set(0.2f, 0, 0);
        mat.setTranslation(trans);

        TransformGroup right_tx = new TransformGroup();
        right_tx.setTransform(mat);
        right_tx.addChild(shape2);
        right_tx.addChild(cp3);

        scene_root.addChild(right_tx);

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
        MultiClipPlaneDemo demo = new MultiClipPlaneDemo();
        demo.setVisible(true);
    }
}
