/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2009
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.aviatrix3d.examples.texture;

// External imports

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JFrame;

import org.j3d.maths.vector.*;

import org.j3d.renderer.aviatrix3d.pipeline.ViewportResizeManager;
import org.j3d.util.*;

import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.FrustumCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.StateAndTransparencyDepthSortStage;

import org.j3d.renderer.aviatrix3d.geom.*;

// Internal imports
// None

/**
 * Main class that creates a hard shadowing scene using shadow mapping technique.
 * <p/>
 * <p/>
 * Shadow mapping technique is an image space based algorithm, which means unlike
 * stencil shadow volume technique, there is no need for the knowledge of scene's
 * geometry to carry out necessary computation.
 * <p/>
 * To create a shadow in the scene using the shadow mapping technique, first step is
 * to render the whole scene from the light's point-of-view.  From this rendering,
 * depth value is saved into a texture.  Second, and final step is to render the scene
 * from camera's viewpoint.  By using a depth comparison between the depth texture and the
 * rendered scene, object is either drawn in shadow or in light.
 * <p/>
 * <p/>
 * This example code demonstrates how the shadow mapping effects can be implemented
 * using the aviatrix3d scene graph.
 * <p/>
 * Since every geometries needs to be represented in scene graph hierarchy in aviatrix3d
 * two scene graph scenes are created.  As described above about the implementation details
 * of shadow mapping, first, scene from the light's point-of-view is created.  After the
 * creation of the scene, that scene is then copied onto a OffscreenTexture2D which copies
 * an rendered image into a texture buffer.
 * <p/>
 * The next step is then to create a scene from camera's viewpoint.  Each
 * geometries in the scene will use the depth values which is stored inside of the
 * OffscreenTexture2D to compare their depths with the depths stored in the depth texture,
 * thus determining whether they are in shadow or outside of shadow.
 * <p/>
 * <p/>
 * Note that, to get a correct shadow effect, all the objects has to be within
 * the bounds of the light point of view's view frustum.
 * <p/>
 * LPOV
 * __________
 * /          \
 * /            \ <- View frustum
 * /              \
 * /                \
 * /  O               \
 * /     O <- Objects   \
 * /                      \
 * /  _  _ <- Shadows       \
 * /                          \
 * ----------------------------
 *
 * FIXME
 *
 * @author Sang Park
 * @version $Revision: 1.8 $
 */
public class ShadowMappingDemo extends JFrame
        implements WindowListener
{

    // Before putting the pipeline into run mode, put the canvas on
    // screen first.
    /** Surface containing aviatrx3d scene */
    private Component sceneSurfaceComp;

    // Color settings
    // ----------------------------------------------------

    /** Light's diffuse color */
    private static final float[] lightDiffuse = {0.8f, 0.8f, 0.8f, 0.0f};
    /** Light's specular color */
    private static final float[] lightSpecular = {0.2f, 0.2f, 0.2f, 0.0f};
    /** Light's ambient color */
    private static final float[] lightAmbient = {0.7f, 0.7f, 0.7f, 0.0f};

    /** Torus's diffuse color */
    private static final float[] torusDiffuse = {0.25f, 0.25f, 1.0f, 0.0f};
    /** Torus's specular color */
    private static final float[] torusSpecular = {0.25f, 0.25f, 0.25f, 0.0f};
    /** Torus's ambient color */
    private static final float[] torusAmbient = {0.25f, 0.25f, 0.25f, 0.0f};

    /** Floor's diffuse color */
    private static final float[] floorDiffuse = {0.7f, 0.7f, 1.0f, 0.0f};
    /** Floor's specular color */
    private static final float[] floorSpecular = {0.25f, 0.25f, 0.25f, 0.0f};
    /** Floor's ambient color */
    private static final float[] floorAmbient = {0.6f, 0.6f, 0.8f, 0.0f};

    // Light settings
    // ----------------------------------------------------

    /** Position of the light */
    private Point3d lightPos;

    /** Light's look at position */
    private Point3d lightLookAt;

    /** Direction light is pointing at */
    private Vector3d lightDir;

    /** Spotlight from light's point of view */
    private SpotLight lightViewSpotLight;

    /** Spotlight form camera's viewpoint */
    private SpotLight cameraViewSpotLight;

    // View settings
    // ----------------------------------------------------

    /** Global up vector */
    private Vector3d globalUpVec;

    /** Camera's inverse transformation */
    private Matrix4d cameraInverseTransform;

    /** Camera's transform matrix */
    private Matrix4d cameraTransform;

    /** Spotlight's transformation matrix */
    private Matrix4d spotlightTransform;

    /** Inverse spotlight transformation */
    private Matrix4d spotlightInverseTransform;

    /** Position of the camera */
    private Point3d cameraPosition;

    /** Torus transformation matrix */
    private Matrix4d torusTransform;

    /** TG of torus from light's point of view */
    private TransformGroup lightPointofView;

    /** TG of torus from camera's point of view */
    private TransformGroup camerasPointofView;

    /** TG of the light look at position from light's point of view */
    private TransformGroup lightLookAtFromLight;

    /** Geometry that represent light */
    private Cone lightCone;

    /** TG that contains the geometry that represents light */
    private TransformGroup lightGroup;

    /**
     * Texture coordinate generator that generates eye linear
     * texture coordinates from the light's point of view
     */
    private TexCoordGeneration coord_gen;

    /**
     * Projection matrix from light's point of view with bias matrix multiplied
     */
    private Matrix4d projMtxBias;

    // Aviatrix
    // ----------------------------------------------------
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    private ViewportResizeManager resizeManager;

    /**
     * Constructor
     */
    public ShadowMappingDemo()
    {
        super("Shadow Mapping Demo");

        setSize(640, 480);
        setLocation(40, 40);

        sceneSurfaceComp = setupAviatrix();
        sceneSurfaceComp.setPreferredSize(new Dimension(640, 480));

        setupSceneGraph();

        add(sceneSurfaceComp);

        // Need to set visible first before starting the rendering thread due
        // to a bug in JOGL. See JOGL Issue #54 for more information on this.
        // http://jogl.dev.java.net
        setVisible(true);

        addWindowListener(this);
        setResizable(false);
    }

    //---------------------------------------------------------------
    // Methods defined by BaseExampleFrame
    //---------------------------------------------------------------

    /**
     * Setup the avaiatrix pipeline here
     */
    protected Component setupAviatrix()
    {
        resizeManager = new ViewportResizeManager();

        // Assemble a simple single-threaded pipeline.
        GraphicsRenderingCapabilities caps = new GraphicsRenderingCapabilities();
        caps.depthBits = 24;

        GraphicsCullStage culler = new FrustumCullStage();
        culler.setOffscreenCheckEnabled(true);

        GraphicsSortStage sorter = new StateAndTransparencyDepthSortStage();
        surface = new SimpleAWTSurface(caps);
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

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        Component comp = (Component) surface.getSurfaceObject();

        return comp;
    }

    /**
     * Setup the basic scene which consists of a quad and a viewpoint
     */
    protected void setupSceneGraph()
    {
        setupViewSettings();

        SimpleViewport viewport = new SimpleViewport();
        viewport.setDimensions(0, 0, 640, 480);
        viewport.setScene(setupFinalPassScene());
        resizeManager.addManagedViewport(viewport);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(viewport);

        Layer[] layers = {layer};
        displayManager.setLayers(layers, 1);
    }

    /**
     * Setup view settings from the light's point of view and
     * also camera's view.
     */
    private void setupViewSettings()
    {
        lightPos = new Point3d();
        lightPos.set(0.0, 5.45, -4.0);
        lightLookAt = new Point3d();
        lightLookAt.set(0, 0, 0);
        globalUpVec = new Vector3d();
        globalUpVec.set(0, 1, 0);

        coord_gen = new TexCoordGeneration();
        projMtxBias = new Matrix4d();

        spotlightTransform = new Matrix4d();
        spotlightInverseTransform = new Matrix4d();
        cameraTransform = new Matrix4d();
        cameraInverseTransform = new Matrix4d();

        lightLookAtFromLight = new TransformGroup();

        Vector3d trans = new Vector3d();
        trans.set(0, 1, 0);

        torusTransform = new Matrix4d();
        torusTransform.set(trans);

        lightPointofView = new TransformGroup();
        lightPointofView.setTransform(torusTransform);

        camerasPointofView = new TransformGroup();
        camerasPointofView.setTransform(torusTransform);

        lightDir = new Vector3d();
        lightDir.set(lightLookAt.x - lightPos.x,
                     lightLookAt.y - lightPos.y,
                     lightLookAt.z - lightPos.z);
        lightDir.normalise();

        // First setup the matrices.
        MatrixUtils matrixUtil = new MatrixUtils();
        matrixUtil.lookAt(lightPos,
                          lightLookAt,
                          globalUpVec,
                          spotlightTransform);
        spotlightInverseTransform.set(spotlightTransform);

        matrixUtil.inverse(spotlightTransform, spotlightTransform);

        cameraPosition = new Point3d();
        cameraPosition.set(1, 7.5f, -11.5f);

        matrixUtil = new MatrixUtils();

        matrixUtil.lookAt(cameraPosition,
                          lightLookAt,
                          globalUpVec,
                          cameraTransform);

        cameraInverseTransform.set(cameraTransform);

        matrixUtil.inverse(cameraTransform, cameraTransform);

        lightLookAtFromLight.setTransform(spotlightTransform);

        lightViewSpotLight = new SpotLight();
        lightViewSpotLight.setPosition((float)lightPos.x, (float)lightPos.y, (float)lightPos.z);
        lightViewSpotLight.setDirection((float)lightDir.x, (float)lightDir.y, (float)lightDir.z);
        lightViewSpotLight.setDiffuseColor(lightDiffuse);
        lightViewSpotLight.setAmbientColor(lightAmbient);
        lightViewSpotLight.setSpecularColor(lightSpecular);
        lightViewSpotLight.setGlobalOnly(true);
        lightViewSpotLight.setEnabled(true);

        cameraViewSpotLight = new SpotLight();
        cameraViewSpotLight.setPosition((float)lightPos.x, (float)lightPos.y, (float)lightPos.z);
        cameraViewSpotLight.setDirection((float)lightDir.x, (float)lightDir.y, (float)lightDir.z);
        cameraViewSpotLight.setDiffuseColor(lightDiffuse);
        cameraViewSpotLight.setAmbientColor(lightAmbient);
        cameraViewSpotLight.setSpecularColor(lightSpecular);
        cameraViewSpotLight.setGlobalOnly(true);
        cameraViewSpotLight.setEnabled(true);

        Material material1 = new Material();
        material1.setDiffuseColor(new float[]{0.5f, 0.5f, 0.5f, 0.5f});
        material1.setAmbientColor(new float[]{0.5f, 0.5f, 0.5f, 0.5f});
        material1.setAmbientColor(new float[]{0.5f, 0.5f, 0.5f, 0.5f});
        material1.setEmissiveColor(new float[]{1, 0.5f, 0, 1});

        Appearance app1 = new Appearance();
        app1.setMaterial(material1);

        lightCone = new Cone(0.5f, 0.2f);
        lightCone.setAppearance(app1);

        lightGroup = new TransformGroup();
        lightGroup.setTransform(spotlightTransform);
        lightGroup.addChild(lightCone);

        ShadowMappingAnimator shadowAnim =
                new ShadowMappingAnimator(resizeManager,
                                          lightPointofView,
                                          camerasPointofView,
                                          lightViewSpotLight,
                                          cameraViewSpotLight,
                                          lightLookAtFromLight,
                                          lightGroup,
                                          lightPos,
                                          lightLookAt,
                                          globalUpVec,
                                          projMtxBias,
                                          coord_gen);

        sceneManager.setApplicationObserver(shadowAnim);
    }

    /**
     * Renders scene from light's point of view.
     * <p/>
     * <p/>
     * In this render pass, model view matrix and the projection matrix of this render pass
     * is set relative to the light's position and direction.  Before rendering
     * the scene from the light's perspective, front face culling is enabled to draw only
     * the back facing polygons.
     *
     * @return SimpleScene containing a scene that is rendered
     * from the light's point of view
     */
    private SimpleScene setupFirstPassScene()
    {
        SimpleScene pass = new SimpleScene();

        // Grab projection matrix from the current view environment.
        float[] projMatrixArray = new float[16];
        ViewEnvironment viewEnv = pass.getViewEnvironment();
        viewEnv.setFieldOfView(45.0f);
        viewEnv.setAspectRatio(640.0f / 480.0f);
        viewEnv.getProjectionMatrix(projMatrixArray);

        // Enable front face culling.
        PolygonAttributes polyAttrib = new PolygonAttributes();
        polyAttrib.setCulledFace(PolygonAttributes.CULL_FRONT);

        // To eliminate the self shadowing error offset is added to the polygon
        // attribute.
        polyAttrib.setPolygonOffset(2.5f, 10.0f);
        polyAttrib.setDrawMode(true, PolygonAttributes.DRAW_FILLED);

        // Viewpoint from the light's point of view
        Viewpoint vp = new Viewpoint();

        lightLookAtFromLight.addChild(vp);

        // Add torus and a floor geometry to the scene.
        Material material1 = new Material();
        material1.setDiffuseColor(torusDiffuse);
        material1.setAmbientColor(torusAmbient);
        material1.setSpecularColor(torusSpecular);

        Appearance app1 = new Appearance();
        app1.setMaterial(material1);
        app1.setPolygonAttributes(polyAttrib);

        Material material2 = new Material();
        material2.setDiffuseColor(floorDiffuse);
        material2.setAmbientColor(floorAmbient);
        material2.setSpecularColor(floorSpecular);

        Appearance app2 = new Appearance();
        app2.setMaterial(material2);
        app2.setPolygonAttributes(polyAttrib);

        Torus torus = new Torus(0.15f, 0.45f);
        torus.setAppearance(app1);

        lightPointofView.setTransform(torusTransform);
        lightPointofView.addChild(torus);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();

        Box floor = new Box(7.5f, 0.1f, 7.5f);
        floor.setAppearance(app2);

        TransformGroup floorGroup = new TransformGroup();
        floorGroup.addChild(floor);
        floorGroup.addChild(lightPointofView);
        floorGroup.setTransform(mat);

        // Setup scene root
        Group sceneRoot = new Group();
        sceneRoot.addChild(lightLookAtFromLight);
        sceneRoot.addChild(floorGroup);
        sceneRoot.addChild(lightViewSpotLight);

        pass.setRenderedGeometry(sceneRoot);
        pass.setActiveView(vp);

        return pass;
    }

    /**
     * Creates a depth texture from a scene rendered from light's point of view
     *
     * @return TextureUnit containing a depth texture.
     */
    TextureUnit createShadowTexture()
    {

        // Grab scene graph that was set from the light's point of view.
        SimpleScene lightPovScene = setupFirstPassScene();

        // Get projection matrix from the view environment.
        float[] projMatrixArray = new float[16];
        ViewEnvironment viewEnv = lightPovScene.getViewEnvironment();
        viewEnv.getProjectionMatrix(projMatrixArray);

        Matrix4d projectionMatrix = new Matrix4d();
        projectionMatrix.m00 = projMatrixArray[0];
        projectionMatrix.m01 = projMatrixArray[1];
        projectionMatrix.m02 = projMatrixArray[2];
        projectionMatrix.m03 = projMatrixArray[3];

        projectionMatrix.m10 = projMatrixArray[4];
        projectionMatrix.m11 = projMatrixArray[5];
        projectionMatrix.m12 = projMatrixArray[6];
        projectionMatrix.m13 = projMatrixArray[7];

        projectionMatrix.m20 = projMatrixArray[8];
        projectionMatrix.m21 = projMatrixArray[9];
        projectionMatrix.m22 = projMatrixArray[10];
        projectionMatrix.m23 = projMatrixArray[11];

        projectionMatrix.m30 = projMatrixArray[12];
        projectionMatrix.m31 = projMatrixArray[13];
        projectionMatrix.m32 = projMatrixArray[14];
        projectionMatrix.m33 = projMatrixArray[15];

        // Calculate the texture matrix for projection.
        // This matrix transforms from eye space to light's clip space
        Matrix4d biasMatrix = new Matrix4d();
        biasMatrix.m00 = 0.5;
        biasMatrix.m11 = 0.5;
        biasMatrix.m22 = 0.5;
        biasMatrix.m33 = 1.0;

        Vector3d trans = new Vector3d();
        trans.set(0.5, 0.5, 0.5);

        biasMatrix.setTranslation(trans);
        biasMatrix.mul(biasMatrix, projectionMatrix);
        projMtxBias.set(biasMatrix);
        biasMatrix.mul(biasMatrix, spotlightInverseTransform);

        float[] sRow = new float[4];
        float[] tRow = new float[4];
        float[] rRow = new float[4];
        float[] qRow = new float[4];

        sRow[0] = (float)biasMatrix.m00;
        sRow[1] = (float)biasMatrix.m01;
        sRow[2] = (float)biasMatrix.m02;
        sRow[3] = (float)biasMatrix.m03;

        tRow[0] = (float)biasMatrix.m10;
        tRow[1] = (float)biasMatrix.m11;
        tRow[2] = (float)biasMatrix.m12;
        tRow[3] = (float)biasMatrix.m13;

        rRow[0] = (float)biasMatrix.m20;
        rRow[1] = (float)biasMatrix.m21;
        rRow[2] = (float)biasMatrix.m22;
        rRow[3] = (float)biasMatrix.m23;

        qRow[0] = (float)biasMatrix.m30;
        qRow[1] = (float)biasMatrix.m31;
        qRow[2] = (float)biasMatrix.m32;
        qRow[3] = (float)biasMatrix.m33;

        GraphicsRenderingCapabilities caps = new GraphicsRenderingCapabilities();
        caps.doubleBuffered = false;
        caps.depthBits = 24;

        // Create depth only offscreen texture.
        OffscreenTexture2D positionBuffer =
                new OffscreenTexture2D(caps,
                                       640,
                                       480,
                                       Texture.FORMAT_DEPTH_COMPONENT);

        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, 640, 480);
        view.setScene(lightPovScene);

        // Set layer
        SimpleLayer mainLayer = new SimpleLayer();
        mainLayer.setViewport(view);

        Layer[] layers = {mainLayer};
        positionBuffer.setClearColor(0, 0, 0, 0);
        positionBuffer.setLayers(layers, 1);
        positionBuffer.setRepaintRequired(true);
        positionBuffer.setMagFilter(Texture.MAGFILTER_BASE_LEVEL_LINEAR);
        positionBuffer.setMinFilter(Texture.MINFILTER_BASE_LEVEL_LINEAR);
        positionBuffer.setBoundaryModeS(OffscreenTexture2D.BM_CLAMP_TO_EDGE);
        positionBuffer.setBoundaryModeT(OffscreenTexture2D.BM_CLAMP_TO_EDGE);
        positionBuffer.setCompareMode(Texture.COMPARE_MODE_R2TEX);
        positionBuffer.setCompareFunction(Texture.COMPARE_FUNCTION_LEQUAL);

        // Setup texture coordinate generations
        coord_gen.setParameter(TexCoordGeneration.TEXTURE_S,
                               TexCoordGeneration.MODE_GENERIC,
                               TexCoordGeneration.MAP_EYE_LINEAR,
                               TexCoordGeneration.MODE_EYE_PLANE,
                               sRow);

        coord_gen.setParameter(TexCoordGeneration.TEXTURE_T,
                               TexCoordGeneration.MODE_GENERIC,
                               TexCoordGeneration.MAP_EYE_LINEAR,
                               TexCoordGeneration.MODE_EYE_PLANE,
                               tRow);

        coord_gen.setParameter(TexCoordGeneration.TEXTURE_R,
                               TexCoordGeneration.MODE_GENERIC,
                               TexCoordGeneration.MAP_EYE_LINEAR,
                               TexCoordGeneration.MODE_EYE_PLANE,
                               rRow);

        coord_gen.setParameter(TexCoordGeneration.TEXTURE_Q,
                               TexCoordGeneration.MODE_GENERIC,
                               TexCoordGeneration.MAP_EYE_LINEAR,
                               TexCoordGeneration.MODE_EYE_PLANE,
                               qRow);

        TextureUnit texUnits = new TextureUnit();
        texUnits.setTexture(positionBuffer);
        texUnits.setTexCoordGeneration(coord_gen);

        return texUnits;
    }

    /**
     * The final SimpleScene that renders a scene from camera's viewpoint.
     *
     * @return SimpleScene containing a final rendered scene.
     */
    SimpleScene setupFinalPassScene()
    {

        SimpleScene pass = new SimpleScene();

        // View group
        Viewpoint vp = new Viewpoint();

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(cameraTransform);

        Material material1 = new Material();
        material1.setDiffuseColor(torusDiffuse);
        material1.setAmbientColor(torusAmbient);
        material1.setSpecularColor(torusSpecular);

        TextureAttributes texAttrib = new TextureAttributes();
        texAttrib.setTextureMode(TextureAttributes.MODE_MODULATE);

        TextureComponent2D[] filter_img = loadImage("images/examples/texture/colour_map.jpg");
        Texture2D filter_texture = new Texture2D();
        filter_texture.setSources(Texture.MODE_BASE_LEVEL,
                                  Texture.FORMAT_RGBA,
                                  filter_img,
                                  1);

        // Use the depth texture to compare with the current depth.
        TextureUnit[] texUnits = new TextureUnit[2];
        texUnits[0] = createShadowTexture();
        texUnits[0].setTextureAttributes(texAttrib);

        texUnits[1] = new TextureUnit();

        float[] sRow = {1.0f, 0.0f, 0.0f, 0.0f};
        float[] tRow = {0.0f, 1.0f, 0.0f, 0.0f};

        TexCoordGeneration coordGen = new TexCoordGeneration();
        coordGen.setParameter(TexCoordGeneration.TEXTURE_S,
                              TexCoordGeneration.MODE_GENERIC,
                              TexCoordGeneration.MAP_OBJECT_LINEAR,
                              sRow);
        coordGen.setParameter(TexCoordGeneration.TEXTURE_T,
                              TexCoordGeneration.MODE_GENERIC,
                              TexCoordGeneration.MAP_OBJECT_LINEAR,
                              tRow);

        filter_texture.setBoundaryModeS(OffscreenTexture2D.BM_WRAP);
        filter_texture.setBoundaryModeT(OffscreenTexture2D.BM_WRAP);

        texUnits[1].setTexCoordGeneration(coordGen);
        texUnits[1].setTexture(filter_texture);
        texUnits[1].setTextureAttributes(texAttrib);

        Appearance app1 = new Appearance();
        app1.setMaterial(material1);
        app1.setTextureUnits(texUnits, 1);

        Material material2 = new Material();
        material2.setDiffuseColor(floorDiffuse);
        material2.setAmbientColor(floorAmbient);
        material2.setSpecularColor(floorSpecular);

        Appearance app2 = new Appearance();
        app2.setMaterial(material2);
        app2.setTextureUnits(texUnits, 2);

        Torus torus = new Torus(0.15f, 0.45f);
        torus.setAppearance(app1);

        Vector3d trans = new Vector3d();
        trans.set(0, 1, 0);

        Matrix4d mat2 = new Matrix4d();
        mat2.setTranslation(trans);

        camerasPointofView.addChild(torus);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();

        Box floor = new Box(7.5f, 0.1f, 7.5f);
        floor.setAppearance(app2);

        TransformGroup floorGroup = new TransformGroup();
        floorGroup.addChild(floor);
        floorGroup.addChild(camerasPointofView);
        floorGroup.setTransform(mat);

        // Setup scene root
        Group sceneRoot = new Group();
        sceneRoot.addChild(tx);
        sceneRoot.addChild(floorGroup);
        sceneRoot.addChild(cameraViewSpotLight);
        sceneRoot.addChild(lightGroup);

        pass.setRenderedGeometry(sceneRoot);
        pass.setActiveView(vp);

        return pass;
    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    /**
     * Ignored
     */
    public void windowActivated(WindowEvent evt)
    {

    }

    /**
     * Ignored
     */
    public void windowClosed(WindowEvent evt)
    {

    }

    /**
     * Exit the application
     *
     * @param evt The event that caused this method to be called.
     */
    public void windowClosing(WindowEvent evt)
    {

        sceneManager.shutdown();

        System.exit(0);
    }

    /**
     * Ignored
     */
    public void windowDeactivated(WindowEvent evt)
    {

    }

    /**
     * Ignored
     */
    public void windowDeiconified(WindowEvent evt)
    {

    }

    /**
     * Ignored
     */
    public void windowIconified(WindowEvent evt)
    {

    }

    /**
     * When the window is opened, start everything up.
     */
    public void windowOpened(WindowEvent evt)
    {

        sceneManager.setEnabled(true);
    }


    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Load a single image
     */
    private TextureComponent2D[] loadImage(String name)
    {
        TextureComponent2D img_comp = null;

        try
        {
            File file = DataUtils.lookForFile(name, getClass(), null);
            if(file == null)
            {
                System.out.println("Can't find texture source file");
                return null;
            }

            FileInputStream is = new FileInputStream(file);

            BufferedInputStream stream = new BufferedInputStream(is);
            BufferedImage img = ImageIO.read(stream);

            int img_width = img.getWidth(null);
            int img_height = img.getHeight(null);
            int format = TextureComponent.FORMAT_RGB;

            switch (img.getType())
            {
                case BufferedImage.TYPE_3BYTE_BGR:
                case BufferedImage.TYPE_CUSTOM:
                case BufferedImage.TYPE_INT_RGB:
                    break;

                case BufferedImage.TYPE_4BYTE_ABGR:
                case BufferedImage.TYPE_INT_ARGB:
                    format = TextureComponent.FORMAT_RGBA;
                    break;
            }

            img_comp = new ImageTextureComponent2D(format,
                                                   img_width,
                                                   img_height,
                                                   img);
        }
        catch (IOException ioe)
        {
            System.out.println("Error reading image: " + ioe);
        }

        return new TextureComponent2D[]{img_comp};
    }

    /**
     * Starts application's main process
     */
    public static void main(String[] args)
    {
        ShadowMappingDemo shadowDemo = new ShadowMappingDemo();
    }
}

