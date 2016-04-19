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
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
public class MovingPickerDemo extends BaseDemoFrame
{
    public MovingPickerDemo()
    {
        super("Aviatrix Simple Picking Demo", false);
    }

    @Override
    protected void setupSceneGraph()
    {
        // View group

        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0.5f, 0.5f);

        MatrixUtils mu = new MatrixUtils();

        Matrix4d mat = new Matrix4d();
        mu.rotateX(-0.7075f, mat);
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);
        tx.setUserData("View TG");

        Group scene_root = new Group();
        scene_root.addChild(tx);

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

        Appearance app = new Appearance();
        app.setMaterial(material);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        Matrix4d mat2 = new Matrix4d();
        trans.set(0.1f, 0.05f, 0);

        mu.rotateZ(Math.PI / 4, mat2);
        mat2.setTranslation(trans);

        TransformGroup shape_transform = new TransformGroup();
        shape_transform.addChild(shape);
        shape_transform.setTransform(mat2);
        shape_transform.setUserData("Right Red");

        scene_root.addChild(shape_transform);

        shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        trans.set(-0.1f, 0.025f, 0);
        mat2.setIdentity();
        mat2.setTranslation(trans);

        shape_transform = new TransformGroup();
        shape_transform.addChild(shape);
        shape_transform.setTransform(mat2);
        shape_transform.setUserData("Left Red");

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

        Appearance app_2 = new Appearance();
        app_2.setMaterial(material_2);

        Shape3D shape_2 = new Shape3D();
        shape_2.setGeometry(geom_2);
        shape_2.setAppearance(app_2);
//

        mat2.setIdentity();

        TransformGroup shape_transform_2 = new TransformGroup();
        shape_transform_2.addChild(shape_2);
        shape_transform_2.setTransform(mat2);
        shape_transform_2.setUserData("Bottom Blue");

        scene_root.addChild(shape_transform_2);


        // Place a point object where the picker is
        float[] coords = new float[6];
        coords[1] = 0.3f;
        coords[4] = -0.5f;
        LineArray line_geom = new LineArray();
        line_geom.setVertices(LineArray.COORDINATE_3,
                              coords,
                              2);

        material = new Material();

        app = new Appearance();
        app.setMaterial(material);

        shape = new Shape3D();
        shape.setGeometry(line_geom);
        shape.setAppearance(app);
        shape.setPickMask(0);


        TransformGroup line_transform = new TransformGroup();
        line_transform.setPickMask(0);
        line_transform.addChild(shape);
        line_transform.setUserData("Pick Line");

        scene_root.addChild(line_transform);

        MovingPickerHandler anim =
            new MovingPickerHandler(scene_root,
                                    line_transform,
                                    material,
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
        MovingPickerDemo demo = new MovingPickerDemo();
        demo.setVisible(true);
    }
}
