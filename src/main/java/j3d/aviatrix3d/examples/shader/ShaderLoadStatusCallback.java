package j3d.aviatrix3d.examples.shader;

// External imports
// None

// Local imports
import org.j3d.renderer.aviatrix3d.pipeline.ViewportResizeManager;

import org.j3d.aviatrix3d.*;

/**
 * Simple test helper for getting the status of a shader loading
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class ShaderLoadStatusCallback
    implements ApplicationUpdateObserver
{
    /** Vert shader to print the results from */
    private ShaderObject vertShader;

    /** Frag shader to print the results from */
    private ShaderObject fragShader;

    /** Compiled shader program to print the results from */
    private ShaderProgram completeShader;

    /** Frame counter so we know when to print out shader debug */
    private int frameCount;

    private ViewportResizeManager resizeManager;

    /**
     *
     */
    ShaderLoadStatusCallback(ShaderObject vert,
                             ShaderObject frag,
                             ShaderProgram comp,
                             ViewportResizeManager resizer)
    {
        resizeManager = resizer;
        vertShader = vert;
        fragShader = frag;
        completeShader = comp;
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
        if(frameCount == 20)
        {
            System.out.println("vert log " + vertShader.getLastInfoLog());
            System.out.println("frag log " + fragShader.getLastInfoLog());
            System.out.println("link log " + completeShader.getLastInfoLog());
            frameCount++;
        }
        else if(frameCount < 20)
            frameCount++;
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
}
