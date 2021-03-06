package j3d.aviatrix3d.examples.picking;

// External imports
import java.awt.*;
import java.awt.event.*;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;
import org.j3d.util.MatrixUtils;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 * <p>
 *
 * Also adds in some prettiness so you can see what is happening through the
 * objects.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class MovingBatchPickerDemo extends BaseDemoFrame
{
    public MovingBatchPickerDemo()
    {
        super("Aviatrix Simple Picking Demo", false);
    }

    @Override
    protected void setupSceneGraph()
    {
        // View group
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 0.5f);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);
        tx.setUserData("View TG");

        Group scene_root = new Group();
        scene_root.addChild(tx);

        Material outline_material = new Material();
        outline_material.setEmissiveColor(new float[] { 1, 1, 1 });

        PolygonAttributes pa = new PolygonAttributes();
        pa.setDrawMode(true, PolygonAttributes.DRAW_LINE);
        pa.setDrawMode(false, PolygonAttributes.DRAW_LINE);

        Appearance outline_app = new Appearance();
        outline_app.setMaterial(outline_material);
        outline_app.setPolygonAttributes(pa);

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.QUADS;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        BoxGenerator generator = new BoxGenerator(0.1f, 0.025f, 0.1f);
        generator.generate(data);

        QuadArray geom = new QuadArray();
        geom.setVertices(QuadArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setNormals(data.normals);

        Material material = new Material();
        material.setEmissiveColor(new float[] { 0, 0, 1 });
        material.setTransparency(0.5f);

        Appearance app = new Appearance();
        app.setMaterial(material);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        Shape3D outline_shape = new Shape3D();
        outline_shape.setGeometry(geom);
        outline_shape.setAppearance(outline_app);
        outline_shape.setPickMask(0);

        SharedNode outline_group = new SharedNode();
        outline_group.setChild(outline_shape);

        Matrix4d mat2 = new Matrix4d();
        trans.set(0.1f, 0.05f, 0);

        MatrixUtils mu = new MatrixUtils();
        mu.rotateZ(Math.PI / 4, mat2);
        mat2.setTranslation(trans);

        TransformGroup shape_transform = new TransformGroup();
        shape_transform.addChild(shape);
        shape_transform.addChild(outline_group);
        shape_transform.setTransform(mat2);
        shape_transform.setUserData("Right Blue");

        scene_root.addChild(shape_transform);

        shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        trans.set(-0.1f, 0.025f, 0);
        mat2.setIdentity();
        mat2.setTranslation(trans);

        shape_transform = new TransformGroup();
        shape_transform.addChild(shape);
        shape_transform.addChild(outline_group);
        shape_transform.setTransform(mat2);
        shape_transform.setUserData("Left Blue");

        scene_root.addChild(shape_transform);

        // second geometry
        data = new GeometryData();
        data.geometryType = GeometryData.QUADS;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generator = new BoxGenerator(0.2f, 0.025f, 0.2f);
        generator.generate(data);

        QuadArray geom_2 = new QuadArray();
        geom_2.setVertices(QuadArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom_2.setNormals(data.normals);

        Material material_2 = new Material();
        material_2.setEmissiveColor(new float[] { 1, 0, 0 });
        material_2.setTransparency(0.5f);

        Appearance app_2 = new Appearance();
        app_2.setMaterial(material_2);

        Shape3D shape_2 = new Shape3D();
        shape_2.setGeometry(geom_2);
        shape_2.setAppearance(app_2);

        mat2.setIdentity();

        outline_shape = new Shape3D();
        outline_shape.setGeometry(geom_2);
        outline_shape.setAppearance(outline_app);
        outline_shape.setPickMask(0);

        TransformGroup shape_transform_2 = new TransformGroup();
        shape_transform_2.addChild(shape_2);
        shape_transform_2.addChild(outline_shape);
        shape_transform_2.setTransform(mat2);
        shape_transform_2.setUserData("Bottom Red");

        scene_root.addChild(shape_transform_2);


        // Place a point object where the picker is
        float[] coords = new float[6];
        coords[1] = 0.3f;
        coords[4] = -0.5f;
        LineArray line_geom = new LineArray();
        line_geom.setVertices(LineArray.COORDINATE_3,
                              coords,
                              2);

        Material line_material = new Material();

        app = new Appearance();
        app.setMaterial(line_material);

        shape = new Shape3D();
        shape.setGeometry(line_geom);
        shape.setAppearance(app);
        shape.setPickMask(0);


        TransformGroup line_transform = new TransformGroup();
        line_transform.setPickMask(0);
        line_transform.addChild(shape);
        line_transform.setUserData("Pick Line");

        // Place a point object where the picker is
        coords = new float[3];
        PointArray point_geom = new PointArray();
        point_geom.setVertices(PointArray.COORDINATE_3,
                               coords,
                               1);

        material = new Material();
        material.setEmissiveColor(new float[] { 1, 1, 0 });

        PointAttributes p_attr = new PointAttributes();
        p_attr.setPointSize(4);

        app = new Appearance();
        app.setMaterial(material);
        app.setPointAttributes(p_attr);

        shape = new Shape3D();
        shape.setGeometry(point_geom);
        shape.setAppearance(app);
        shape.setPickMask(0);

        TransformGroup point_transform = new TransformGroup();
        point_transform.setPickMask(0);
        point_transform.addChild(shape);
        point_transform.setUserData("Pick Point");


        scene_root.addChild(line_transform);
        scene_root.addChild(point_transform);

        MovingBatchPickerHandler anim =
            new MovingBatchPickerHandler(scene_root,
                                    line_transform,
                                    point_transform,
                                    line_material,
                                    material_2,
                                    resizeManager);

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
        sceneManager.setApplicationObserver(anim);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        MovingBatchPickerDemo demo = new MovingBatchPickerDemo();
        demo.setVisible(true);
    }
}
