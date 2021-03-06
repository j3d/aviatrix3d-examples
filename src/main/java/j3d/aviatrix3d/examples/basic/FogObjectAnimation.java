package j3d.aviatrix3d.examples.basic;

// Standard imports
// None

// Application Specific imports
import org.j3d.aviatrix3d.*;
import org.j3d.renderer.aviatrix3d.pipeline.ViewportResizeManager;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.util.MatrixUtils;

/**
 * Simple animator to move the transform in a rotation about the Y axis.
 * Used to illustrate fog effects by moving objects into and out of the
 * fog range.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class FogObjectAnimation
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    /** Matrix used to update the transform */
    private Matrix4d matrix;

    /** The scene graph node to update */
    private TransformGroup transform;

    /** The current angle */
    private float angle;

    /** Utils class for performing rotation */
    private MatrixUtils matrixUtils;

    private ViewportResizeManager resizeManager;

    /**
     *
     */
    public FogObjectAnimation(TransformGroup tx, ViewportResizeManager resizer)
    {
        resizeManager = resizer;
        matrix = new Matrix4d();
        matrix.setIdentity();
        transform = tx;

        matrixUtils = new MatrixUtils();
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph()
    {
        resizeManager.sendResizeUpdates();
        transform.boundsChanged(this);
    }

    /**
     * Notification that the AV3D internal shutdown handler has detected a
     * system-wide shutdown. The aviatrix code has already terminated rendering
     * at the point this method is called, only the user's system code needs to
     * terminate before exiting here.
     */
    public void appShutdown()
    {
        // do nothing
    }

    //----------------------------------------------------------
    // Methods required by the UpdateListener interface.
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src)
    {
        angle += Math.PI / 500;

        matrixUtils.rotateY(angle, matrix);

        transform.setTransform(matrix);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src)
    {
    }
}
