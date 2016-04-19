package j3d.aviatrix3d.examples.multipass;

// External imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;

/**
 * Example application that demonstrates a using the accumulation buffer with
 * multipass rendering to achieve a motion blur effect.
 *
 * FIXME
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class MotionBlurDemo extends BaseDemoFrame
{
    /** The number of passes for the motion blur */
    private static final int NUM_PASSES = 4;

    public MotionBlurDemo()
    {
        super("Motion Blur Multipass Demo", false);
    }

    @Override
    protected GraphicsRenderingCapabilities getCapabilities()
    {
        // Assemble a simple single-threaded pipeline.
        GraphicsRenderingCapabilities caps = new GraphicsRenderingCapabilities();
        caps.accumAlphaBits = 16;
        caps.accumBlueBits = 16;
        caps.accumGreenBits = 16;
        caps.accumRedBits = 16;

        return caps;
    }

    /**
     * Setup the basic scene which consists of a quad and a viewpoint
     */
    protected void setupSceneGraph()
    {
        // Transform group for each pass
        TransformGroup[] shape_movers = new TransformGroup[NUM_PASSES];


        // Triangle fan array
        float[] coord = { 0, 0, -1,
                          0.25f, 0, -1,
                          0, 0.25f, -1,
                          -0.25f, 0, -1,
                          0, -0.25f, -1,
                          0.25f, 0, -1,
                          };

        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[] color = { 1, 1, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0 };

        TriangleFanArray geom = new TriangleFanArray();

        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setFanCount(new int[] { 6 }, 1);
        geom.setNormals(normal);
        geom.setColors(false, color);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);

        SharedNode shared_geom = new SharedNode();
        shared_geom.setChild(shape);

        MultipassScene scene = new MultipassScene();

        for(int i = 0; i < NUM_PASSES; i++)
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

            TransformGroup shape_transform = new TransformGroup();
            shape_transform.addChild(shared_geom);

            scene_root.addChild(shape_transform);
            shape_movers[i] = shape_transform;

            AccumulationBufferState accum_state = new AccumulationBufferState();

            if(i == 0)
            {
                accum_state.setAccumFunction(AccumulationBufferState.FUNCTION_LOAD);
                accum_state.setValue(0.8f);
            }
            else
            {
                accum_state.setAccumFunction(AccumulationBufferState.FUNCTION_ACCUMULATE);
                accum_state.setValue(1f / NUM_PASSES);
                accum_state.setClearBufferState(false);
            }

            DepthBufferState depth_state = new DepthBufferState();
            ColorBufferState color_state = new ColorBufferState();

            RenderPass pass = new RenderPass();
            pass.setRenderedGeometry(scene_root);
            pass.setActiveView(vp);
            pass.setDepthBufferState(depth_state);
            pass.setColorBufferState(color_state);
            pass.setAccumulationBufferState(accum_state);

            scene.addRenderPass(pass);
        }

        // Then the basic layer and viewport at the top:
        MultipassViewport view = new MultipassViewport();
        view.setDimensions(0, 0, 500, 500);
        view.setScene(scene);
        resizeManager.addManagedViewport(view);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);

        MotionBlurAnimation anim = new MotionBlurAnimation(shape_movers, resizeManager);
        sceneManager.setApplicationObserver(anim);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        MotionBlurDemo demo = new MotionBlurDemo();
        demo.setVisible(true);
    }
}
