package j3d.aviatrix3d.examples.volume;

// Standard imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;
import org.j3d.renderer.aviatrix3d.pipeline.ViewportResizeManager;

// Application Specific imports
import org.j3d.aviatrix3d.*;

/**
 * Animator that moves the viewpoint in and out to illustrate the octtree
 * rendering demo.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
class VpAnimation
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    /** Work variable to update the translation with */
    private Vector3d translation;

    /** Matrix used to update the transform */
    private Matrix4d matrix;

    /** The scene graph node to update */
    private TransformGroup transform;

    /** The current angle of orientation */
    private float angle;

    /** The current distance from the center */
    private float distance;

    /** Handles the viewport being resized and ensuring the rendering works correctly */
    private ViewportResizeManager resizeManager;

    /**
     *
     */
    VpAnimation(TransformGroup tx, ViewportResizeManager resizer)
    {
        resizeManager = resizer;
        translation = new Vector3d();
        matrix = new Matrix4d();
        matrix.setIdentity();
        transform = tx;
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    @Override
    public void updateSceneGraph()
    {
        resizeManager.sendResizeUpdates();
        transform.boundsChanged(this);
    }

    @Override
    public void appShutdown()
    {
        // do nothing
    }

    //----------------------------------------------------------
    // Methods required by the UpdateListener interface.
    //----------------------------------------------------------

    @Override
    public void updateNodeBoundsChanges(Object src)
    {
        angle += Math.PI / 1000;
        distance -= Math.PI / 50;

        float radius = (float)(0.3d * (float)Math.sin(distance) + 1.5f);

        translation.z = radius;

        matrix.setTranslation(translation);

        transform.setTransform(matrix);
    }

    @Override
    public void updateNodeDataChanges(Object src)
    {
    }
}
