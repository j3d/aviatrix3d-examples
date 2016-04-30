/*
 * **************************************************************************
 *                        Copyright j3d.org (c) 2000 - ${year}
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read docs/lgpl.txt for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * **************************************************************************
 */

package j3d.aviatrix3d.examples.basic;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.j3d.renderer.aviatrix3d.pipeline.ViewportResizeManager;
import org.j3d.util.DataUtils;

import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.management.RenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.*;

/**
 * This class does something
 *
 * @author justin
 */
public abstract class BaseDemoFrame extends JFrame
    implements WindowListener
{
    protected RenderManager sceneManager;

    protected ViewportResizeManager resizeManager;

    /** Manager for the layers etc */
    protected SingleDisplayCollection displayManager;

    /** Our drawing surface */
    protected GraphicsOutputDevice surface;

    /**
     * Create a frame using the default null cull and sort stages and will automatically
     * manage resize events.
     *
     * @param title The title to place on the frame for the demo
     */
    protected BaseDemoFrame(String title) {
        this(title, null, null, true);
    }

    /**
     * Create a frame using the default null cull and sort stages and will automatically
     * manage resize events.
     *
     * @param title The title to place on the frame for the demo
     * @param manageResize true if you want the base class to manage the resize events, false
     *    if you want to do it yourself (eg you have a custom animator in use)
     */
    protected BaseDemoFrame(String title, boolean manageResize) {
        this(title, null, null, manageResize);
    }

    /**
     * Fully configurable constructor.
     *
     * @param title The title to place on the frame for the demo
     * @param manageResize true if you want the base class to manage the resize events, false
     *    if you want to do it yourself (eg you have a custom animator in use)
     * @param culler The cull implementation to use. If null will use the default NullCullStage
     * @param sorter The sort implementation to use. If null will use the default NullSortStage
     */
    protected BaseDemoFrame(String title, GraphicsCullStage culler, GraphicsSortStage sorter, boolean manageResize) {
        super(title);

        resizeManager = new ViewportResizeManager();

        setLayout(new BorderLayout());

        setupAviatrix(culler, sorter, manageResize);
        setupSceneGraph();

        addWindowListener(this);

        setSize(500, 500);
    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    @Override
    public void windowActivated(WindowEvent evt)
    {
    }

    @Override
    public void windowClosed(WindowEvent evt)
    {
    }

    @Override
    public void windowClosing(WindowEvent evt)
    {
        sceneManager.shutdown();
        System.exit(0);
    }

    @Override
    public void windowDeactivated(WindowEvent evt)
    {
    }

    @Override
    public void windowDeiconified(WindowEvent evt)
    {
    }

    @Override
    public void windowIconified(WindowEvent evt)
    {
    }

    @Override
    public void windowOpened(WindowEvent evt)
    {
        sceneManager.setEnabled(true);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Called by the constructor to setup the scene graph specific to the demo
     */
    protected abstract void setupSceneGraph();

    /**
     * Overridable method to change the requested capabilities when needed for
     * a specific demo. Most Demos don't need to worry about overriding as the
     * defaults are good enough.
     *
     * @return A configured capabilities object
     */
    protected GraphicsRenderingCapabilities getCapabilities()
    {
        return new GraphicsRenderingCapabilities();
    }

    /**
     * @param culler
     * @param sorter
     * @param handleResize
     */
    private void setupAviatrix(GraphicsCullStage culler, GraphicsSortStage sorter, boolean handleResize)
    {
        if(culler == null)
        {
            culler = new NullCullStage();
        }

        if(sorter == null)
        {
            sorter = new NullSortStage();
        }

        // Assemble a simple single-threaded pipeline.
        GraphicsRenderingCapabilities caps = getCapabilities();

        surface = new DebugAWTSurface(caps);
        surface.setClearColor(0.3f, 0.3f, 0.3f, 1);
        surface.addGraphicsResizeListener(resizeManager);

        DefaultGraphicsPipeline pipeline = new DefaultGraphicsPipeline();

        pipeline.setCuller(culler);
        pipeline.setSorter(sorter);
        pipeline.setGraphicsOutputDevice(surface);

        displayManager = new SingleDisplayCollection();
        displayManager.addPipeline(pipeline);

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.addDisplay(displayManager);
        sceneManager.setMinimumFrameInterval(100);

        if(handleResize)
        {
            ApplicationUpdateObserver obs = new ResizeUpdater(resizeManager);
            sceneManager.setApplicationObserver(obs);
        }

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        Component comp = (Component)surface.getSurfaceObject();
        add(comp, BorderLayout.CENTER);
    }

    /**
     * Convenience method to load an image from a file that is
     * located in the classpath somewhere.
     *
     * @param filename The file name to load. If relative, searches the classpath
     * @return The loaded image or null if there were any problems
     */
    protected BufferedImage loadImage(String filename)
    {
        BufferedImage  ret_val = null;

        try
        {
            File f = DataUtils.lookForFile(filename, getClass(), null);

            if(f == null)
            {
                System.out.println("Can't find texture source file");
                return null;
            }

            FileInputStream is = new FileInputStream(f);

            BufferedInputStream stream = new BufferedInputStream(is);
            ret_val = ImageIO.read(stream);
        }
        catch(IOException ioe)
        {
            System.out.println("Error reading image: " + ioe);
        }

        return ret_val;
    }

    /**
     * Convenience method to load a 2D texture component from a file that is
     * located in the classpath somewhere.
     *
     * @param filename The file name to load. If relative, searches the classpath
     * @return The loaded texture or null if there were any problems
     */
    protected TextureComponent2D loadTexture(String filename)
    {
        TextureComponent2D img_comp = null;
        int img_width = 0;
        int img_height = 0;

        BufferedImage img = loadImage(filename);

        img_width = img.getWidth(null);
        img_height = img.getHeight(null);
        int format = TextureComponent.FORMAT_RGB;

        switch (img.getType())
        {
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_CUSTOM:
            case BufferedImage.TYPE_INT_RGB:
                System.out.println("TD RGB");
                break;

            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_INT_ARGB:
                System.out.println("TD RGBA");
                format = TextureComponent.FORMAT_RGBA;
                break;
        }

        img_comp = new ImageTextureComponent2D(format,
                                               img_width,
                                               img_height,
                                               img);

        return img_comp;
    }

    /**
     * Load the shader file. Find it relative to the classpath.
     *
     * @param name THe name of the file to load
     */
    protected String[] loadShaderFile(String name)
    {
        File file = DataUtils.lookForFile(name, getClass(), null);
        if(file == null)
        {
            System.out.println("Cannot find file " + name);
            return null;
        }

        String ret_val = null;

        try
        {
            FileReader is = new FileReader(file);
            StringBuffer buf = new StringBuffer();
            char[] read_buf = new char[1024];
            int num_read = 0;

            while((num_read = is.read(read_buf, 0, 1024)) != -1)
                buf.append(read_buf, 0, num_read);

            is.close();

            ret_val = buf.toString();
        }
        catch(IOException ioe)
        {
            System.out.println("I/O error " + ioe);
        }

        return new String[] { ret_val };
    }
}
