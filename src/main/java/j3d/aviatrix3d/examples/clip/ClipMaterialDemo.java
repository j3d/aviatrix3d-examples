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
 * Example application that demonstrates a clip plane usage with two-sided
 * material values to show the front and back surfaces.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class ClipMaterialDemo extends BaseDemoFrame
{
    public ClipMaterialDemo()
    {
        super("Basic Clip Plane Demo");
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

        SphereGenerator generator = new SphereGenerator(0.2f);
        generator.generate(data);

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setNormals(data.normals);

        Material material = new Material();
        material.setSeparateBackfaceEnabled(true);
        material.setDiffuseColor(new float[] { 0, 0, 1 });
        material.setBackDiffuseColor(new float[] { 0, 1, 0 });
        material.setEmissiveColor(new float[] { 0, 0, 1 });
        material.setBackEmissiveColor(new float[] { 0, 1, 0 });
        material.setSpecularColor(new float[] { 1, 1, 1 });

        PolygonAttributes pa = new PolygonAttributes();
        pa.setTwoSidedLighting(true);

        Appearance app = new Appearance();
        app.setMaterial(material);
        app.setPolygonAttributes(pa);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        TransformGroup shape_transform = new TransformGroup();
        shape_transform.addChild(shape);

        scene_root.addChild(shape_transform);

        double[] plane_eq = { 1, 1, -1, 0 };

        ClipPlane cp = new ClipPlane();
        cp.setPlaneEquation(plane_eq);
        cp.setEnabled(true);

        scene_root.addChild(cp);

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
        ClipMaterialDemo demo = new ClipMaterialDemo();
        demo.setVisible(true);
    }
}
