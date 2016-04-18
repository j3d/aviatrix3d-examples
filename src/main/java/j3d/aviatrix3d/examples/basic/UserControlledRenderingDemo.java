package j3d.aviatrix3d.examples.basic;

// Standard imports
import java.awt.*;
import java.awt.event.*;

// Application Specific imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

/**
 * Example application that demonstrates how to make use of the explicit
 * rendering call renderOnce().
 *
 * The demo puts up a frame and only repaints it each time the render button
 * gets pushed or the screen is iconified and brought back to the screen. Each
 * render pass changes the colour.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class UserControlledRenderingDemo extends BaseDemoFrame
    implements ActionListener
{
    /** Where we are going to change the material colour */
    private Material material;

    /** Which iteration are we up to */
    private int colourIteration;

    public UserControlledRenderingDemo()
    {
        super("renderOnce() Demo");
    }

    @Override
    protected void setupSceneGraph()
    {

        Button btn = new Button("Press to Render");
        btn.addActionListener(this);

        add(btn, BorderLayout.SOUTH);

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

        TriangleArray geom = new TriangleArray();
        geom.setValidVertexCount(3);
        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setNormals(normal);

        material = new Material();
        material.setEmissiveColor(new float[] { 0, 0, 1 });

        Appearance app = new Appearance();
        app.setMaterial(material);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        trans.set(0.2f, 0.5f, 0);
        Matrix4d mat2 = new Matrix4d();
        mat2.setIdentity();
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

    /**
     * Override the base class version so that we don't start the complete rendering
     * engine. We only want to render it a single time on showing.
     *
     * @param evt The event that caused this to be triggered.
     */
    @Override
    public void windowOpened(WindowEvent evt)
    {
        sceneManager.renderOnce();
    }

    //----------------------------------------------------------
    // Methods defined by ActionListener
    //----------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e)
    {
        changeNodeColour();
        sceneManager.renderOnce();
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    private void changeNodeColour()
    {
        colourIteration = ++colourIteration % 7;

        switch(colourIteration)
        {
            case 0:
                material.setEmissiveColor(new float[] { 0, 0, 1 });
                break;

            case 1:
                material.setEmissiveColor(new float[] { 0, 1, 0 });
                break;

            case 2:
                material.setEmissiveColor(new float[] { 0, 1, 1 });
                break;

            case 3:
                material.setEmissiveColor(new float[] { 1, 0, 0 });
                break;

            case 4:
                material.setEmissiveColor(new float[] { 1, 0, 1 });
                break;

            case 5:
                material.setEmissiveColor(new float[] { 1, 1, 0 });
                break;

            case 6:
                material.setEmissiveColor(new float[] { 1, 1, 1 });
                break;
        }
    }

    public static void main(String[] args)
    {
        UserControlledRenderingDemo demo = new UserControlledRenderingDemo();
        demo.setVisible(true);
    }
}
