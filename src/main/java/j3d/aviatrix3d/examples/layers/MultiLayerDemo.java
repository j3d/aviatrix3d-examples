package j3d.aviatrix3d.examples.layers;

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
import org.j3d.aviatrix3d.pipeline.graphics.SimpleTransparencySortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

/**
 * Example application that demonstrates a multiple top-level layers with
 * single viewports in each layer. One object is placed in each layer and it
 * should show the two objects with the front layer always rendered in front
 * of the background layer. The front layer is fixed, but the back layer
 * animates in such a way that, if both objects were in the one scene, it
 * would move in front and then behind it.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class MultiLayerDemo extends BaseDemoFrame
{
    private static final float[] RED = {1, 0, 0};
    private static final float[] GREEN = {0, 1, 0};
    private static final float[] BLUE = {0, 0, 1};
    private static final float[] GREY = {0.3f, 0.3f, 0.3f};

    public MultiLayerDemo()
    {
        super("Multiple Layer Aviatrix3D Demo", null, new SimpleTransparencySortStage(), false);
    }

    @Override
    protected void setupSceneGraph()
    {
        // View group


        // Flat panel that has the viewable object as the demo
        float[] coord = { 0, 0, -1,  0.25f, 0, -1,  0.25f, 0.25f, -1, 0.25f, 0, -1 };
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1};

        QuadArray geom = new QuadArray();
        geom.setValidVertexCount(4);
        geom.setVertices(QuadArray.COORDINATE_3, coord);
        geom.setNormals(normal);

        // Rear-most layer. Object offset to the left
        Material material = new Material();
        material.setEmissiveColor(RED);
        material.setTransparency(0.5f);

        Appearance app = new Appearance();
        app.setMaterial(material);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        Vector3d trans = new Vector3d();
        trans.set(-0.1f, 0, 0);
        Matrix4d mat2 = new Matrix4d();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        TransformGroup layer1_group = new TransformGroup();
        layer1_group.addChild(shape);
        layer1_group.setTransform(mat2);

        Viewpoint vp1 = new Viewpoint();
        Background bg = new ColorBackground(GREY);
        trans = new Vector3d();
        trans.set(0, 0, 1);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp1);
        tx.setTransform(mat);

        Group view_group1 = new Group();
        view_group1.addChild(layer1_group);
        view_group1.addChild(tx);
        view_group1.addChild(bg);

        SimpleScene scene1 = new SimpleScene();
        scene1.setRenderedGeometry(view_group1);
        scene1.setActiveView(vp1);
        scene1.setActiveBackground(bg);

        // Middle layer. Object in the middle of the screen, no background
        material = new Material();
        material.setEmissiveColor(GREEN);

        app = new Appearance();
        app.setMaterial(material);

        shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        trans = new Vector3d();
        trans.set(0, 0, 1);
        mat2 = new Matrix4d();
        mat2.setIdentity();
        mat2.setTranslation(trans);


        TransformGroup layer2_group = new TransformGroup();
        layer2_group.addChild(shape);
        layer2_group.setTransform(mat2);

        Viewpoint vp2 = new Viewpoint();

        mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        tx = new TransformGroup();
        tx.addChild(vp2);
        tx.setTransform(mat);

        Group view_group2 = new Group();
        view_group2.addChild(layer2_group);
        view_group2.addChild(tx);

        SimpleScene scene2 = new SimpleScene();
        scene2.setRenderedGeometry(view_group2);
        scene2.setActiveView(vp2);

        // Top layer. Object on the right of the screen, no background
        material = new Material();
        material.setEmissiveColor(BLUE);
        material.setTransparency(0.5f);

        app = new Appearance();
        app.setMaterial(material);

        shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        trans = new Vector3d();
        trans.set(0.1f, 0, 1);
        mat2 = new Matrix4d();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        Viewpoint vp3 = new Viewpoint();

        trans = new Vector3d();
        trans.set(0, 0, 1);
        mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        tx = new TransformGroup();
        tx.addChild(vp3);
        tx.setTransform(mat);

        TransformGroup layer3_group = new TransformGroup();
        layer3_group.addChild(shape);
        layer3_group.setTransform(mat2);

        Group view_group3 = new Group();
        view_group3.addChild(layer3_group);
        view_group3.addChild(tx);

        SimpleScene scene3 = new SimpleScene();
        scene3.setRenderedGeometry(view_group3);
        scene3.setActiveView(vp3);


        // Then the viewports. Divide the screen up into 4 viewports
        SimpleViewport view1 = new SimpleViewport();
        view1.setDimensions(0, 0, 800, 800);
        view1.setScene(scene1);
        resizeManager.addManagedViewport(view1);

        SimpleViewport view2 = new SimpleViewport();
        view2.setDimensions(0, 0, 800, 800);
        view2.setScene(scene2);
        resizeManager.addManagedViewport(view2);

        SimpleViewport view3 = new SimpleViewport();
        view3.setDimensions(0, 0, 800, 800);
        view3.setScene(scene3);
        resizeManager.addManagedViewport(view3);

        SimpleLayer layer1 = new SimpleLayer();
        layer1.setViewport(view1);

        SimpleLayer layer2 = new SimpleLayer();
        layer2.setViewport(view2);

        SimpleLayer layer3 = new SimpleLayer();
        layer3.setViewport(view3);

        Layer[] layers = { layer1, layer2, layer3 };
        displayManager.setLayers(layers, 3);

        ObjectLayerAnimation anim = new ObjectLayerAnimation(layer2_group, resizeManager);
        sceneManager.setApplicationObserver(anim);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        MultiLayerDemo demo = new MultiLayerDemo();
        demo.setVisible(true);
    }
}
