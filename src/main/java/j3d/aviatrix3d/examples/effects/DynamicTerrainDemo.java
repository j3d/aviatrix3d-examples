/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.aviatrix3d.examples.effects;

// External imports
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.geom.GeometryData;
import org.j3d.geom.terrain.ElevationGridGenerator;
import org.j3d.texture.procedural.TextureGenerator;
import org.j3d.util.MatrixUtils;

/**
 * Demonstration of generating terrain and textures using entirely procedural
 * techniques.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public class DynamicTerrainDemo extends BaseDemoFrame
    implements ApplicationUpdateObserver,
               NodeUpdateListener,
               ActionListener
{
    /** Names of the images to load from the local dir */
    private static final String[] IMAGE_FILES =
    {
        "images/examples/effects/sand_128x128.gif",
        "images/examples/effects/grass_128x128.jpg",
        "images/examples/effects/rock_320x240.gif",
        "images/examples/effects/snow_128x128.jpg",
    };

    /** The texture type constant needed when updating texture coordinates */
    private static final int[] TEX_TYPES =
        { VertexGeometry.TEXTURE_COORDINATE_2 };

    /** Base width of the terrain */
    private static final int TERRAIN_SIZE = 60;

    /** Size of the texture, in pixels to create */
    private static final int TEXTURE_SIZE = 1024;

    /** Number of points to divide TEXTURE_SIZE by to get grid points */
    private static final int GRID_SPACING = 8;

    /** The height of the water level */
    private static final float WATER_HEIGHT = -5;

    // bunch of text fields for the parameters
    private TextField scaleTf;
    private TextField freqTf;
    private TextField passesTf;
    private TextField yScaleTf;
    private TextField freqDiffTf;

    private TextField snowHeightTf;
    private TextField rockHeightTf;
    private TextField grassHeightTf;
    private TextField sandHeightTf;

    /** Button used to start the regeneration process */
    private Button regenButton;

    /** Panel describing the fog colour */
    private ColorPanel fogColorPanel;

    /** Last value of fog read from the UI */
    private float[] fogColor;

    /** Fog class used to render the geometry */
    private Fog fog;

    /** Images used for each colour */
    private BufferedImage[] colorImages;

    /** Heights for each color image */
    private float[] imageHeights;

    /** Generator for synthetic textures */
    private TextureGenerator imageGen;

    /** Generator for the terrain */
    private ElevationGridGenerator heightGen;

    /** Texture object holding the generated texture */
    private ByteTextureComponent2D terrainTexture;

    /** Geometry from the dynamically created terrain */
    private TriangleStripArray terrainGeometry;

    /** Data for the generated terrain */
    private GeometryData geomData;

    /** Flag indicating we have newly generated terrain data ready */
    private boolean newTerrainAvailable;

    /** The bytes of the new texture image */
    private byte[] texturePixels;

    /** Set of fog coordinates for this geometry */
    private float[] fogCoords;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public DynamicTerrainDemo()
    {
        super("Aviatrix Procedural Terrain Demo", false);

        setBackground(SystemColor.menu);

        newTerrainAvailable = false;

        imageGen = new TextureGenerator();
        heightGen = new ElevationGridGenerator(TERRAIN_SIZE,
                                               TERRAIN_SIZE,
                                               TEXTURE_SIZE / GRID_SPACING + 1,
                                               TEXTURE_SIZE / GRID_SPACING + 1);

        geomData = new GeometryData();
        geomData.geometryType = GeometryData.TRIANGLE_STRIPS;
        geomData.geometryComponents = GeometryData.NORMAL_DATA |
                                      GeometryData.TEXTURE_2D_DATA;

        fogCoords = new float[TEXTURE_SIZE * TEXTURE_SIZE * 2];
        fogColor = new float[3];

        colorImages = new BufferedImage[IMAGE_FILES.length];
        for(int i = 0; i < IMAGE_FILES.length; i++)
            colorImages[i] = loadImage(IMAGE_FILES[i]);

        imageHeights = new float[5];

        createParamsPanel();

        setSize(600, 400);
    }

    @Override
    protected void setupSceneGraph()
    {
        // View group
        Viewpoint vp = new Viewpoint();
        vp.setHeadlightEnabled(true);

        // We could set these directly, but we don't because we'd like to
        // pass the values through to the shaders as well. More convenient and
        // we guarantee the same values then.
        Vector3d trans = new Vector3d();
        trans.set(0, 30, 70);

        Matrix4d mat = new Matrix4d();

        MatrixUtils mu = new MatrixUtils();
        mu.rotateX(Math.PI / -8, mat);
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Now the placeholder geom and texture for the terrain.
        terrainGeometry = new TriangleStripArray();

        terrainTexture =
            new ByteTextureComponent2D(ByteTextureComponent2D.FORMAT_RGB,
                                       TEXTURE_SIZE,
                                       TEXTURE_SIZE,
                                       new byte[TEXTURE_SIZE * TEXTURE_SIZE * 3]);

        Texture2D tex = new Texture2D(Texture.FORMAT_RGB, terrainTexture);

        TextureUnit[] textures = new TextureUnit[1];
        textures[0] = new TextureUnit();
        textures[0].setTexture(tex);

        Material material = new Material();
        material.setDiffuseColor(new float[] {1, 1, 0, 1 });

        Appearance app = new Appearance();
        app.setTextureUnits(textures, 1);
        app.setMaterial(material);

        Shape3D t_shape = new Shape3D();
        t_shape.setGeometry(terrainGeometry);
        t_shape.setAppearance(app);

        Fog t_fog = new Fog();
        t_fog.setGlobalOnly(false);
        t_fog.setColor(new float[] { 0, 0, 1, 1});

        Group t_group = new Group();
        t_group.addChild(t_fog);
        t_group.addChild(t_shape);
        scene_root.addChild(t_group);

        // Create another semi-transparent plane that is used to represent the
        // water level at a height just above zero.
        float[] coords =
            {
                -TERRAIN_SIZE / 2, WATER_HEIGHT,  TERRAIN_SIZE / 2,
                TERRAIN_SIZE / 2, WATER_HEIGHT,  TERRAIN_SIZE / 2,
                TERRAIN_SIZE / 2, WATER_HEIGHT, -TERRAIN_SIZE / 2,
                -TERRAIN_SIZE / 2, WATER_HEIGHT, -TERRAIN_SIZE / 2,
            };

        QuadArray water_geom = new QuadArray();
        water_geom.setVertices(VertexGeometry.COORDINATE_3, coords, 4);

        material = new Material();
        material.setDiffuseColor(new float[] {0, 0, 1, 0.5f });
        material.setSpecularColor(new float[] {0, 0, 1, 0.5f });
        material.setTransparency(0.5f);

        app = new Appearance();
        app.setMaterial(material);

        Shape3D w_shape = new Shape3D();
        w_shape.setGeometry(water_geom);
        w_shape.setAppearance(app);
        scene_root.addChild(w_shape);

        fog = new Fog();
        scene_root.addChild(fog);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);
        scene.setActiveFog(fog);

        sceneManager.setApplicationObserver(this);

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

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    @Override
    public void updateSceneGraph()
    {
        if(!newTerrainAvailable)
            return;

        resizeManager.sendResizeUpdates();

        terrainGeometry.boundsChanged(this);
        terrainGeometry.dataChanged(this);

        terrainTexture.dataChanged(this);
        fog.dataChanged(this);

        newTerrainAvailable = false;
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
        terrainGeometry.setVertices(VertexGeometry.COORDINATE_3,
                                    geomData.coordinates,
                                    geomData.vertexCount);
        terrainGeometry.setStripCount(geomData.stripCounts,
                                      geomData.numStrips);
    }

    @Override
    public void updateNodeDataChanges(Object src)
    {
        if(src == terrainGeometry)
        {
            terrainGeometry.setNormals(geomData.normals);

            float[][] tex_coords = { geomData.textureCoordinates };
            terrainGeometry.setTextureCoordinates(TEX_TYPES, tex_coords, 1);
            terrainGeometry.setFogCoordinates(fogCoords);
        }
        else if(src == terrainTexture)
        {
            terrainTexture.updateSubImage(0,
                                          0,
                                          TEXTURE_SIZE,
                                          TEXTURE_SIZE,
                                          0,
                                          texturePixels);
        }
        else if(src == fog)
        {
            fog.setColor(fogColor);
        }

        regenButton.setEnabled(true);
        sceneManager.setMinimumFrameInterval(30);
    }

    //---------------------------------------------------------------
    // Methods defined by ActionListener
    //---------------------------------------------------------------

    /**
     * Process a button request - for regeneration of the terrain
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt)
    {
        sceneManager.setMinimumFrameInterval(300);
        regenButton.setEnabled(false);

        // Strip the various fields
        try
        {
            int width = TEXTURE_SIZE;
            int depth = TEXTURE_SIZE;
            int passes = Integer.parseInt(passesTf.getText());
            float scale = Float.parseFloat(scaleTf.getText());
            float freq = Float.parseFloat(freqTf.getText());
            float y_scale = Float.parseFloat(yScaleTf.getText());
            float freq_diff = Float.parseFloat(freqDiffTf.getText());

            Color f_col = fogColorPanel.getColor();
            fogColor[0] = f_col.getRed() / 255;
            fogColor[1] = f_col.getGreen() / 255;
            fogColor[2] = f_col.getBlue() / 255;

            float[] raw_img = new float[width * depth];

            imageGen.generateSynthesisTexture(raw_img,
                                              freq,
                                              scale,
                                              width,
                                              depth);

            for(int i = 1; i < passes; i++)
            {
                freq += freq_diff;
                scale *= y_scale;
                imageGen.generateSynthesisTexture(raw_img,
                                                  freq,
                                                  scale,
                                                  width,
                                                  depth);
            }

            // find min and max values of the floats
            float min_y = raw_img[0];
            float max_y = raw_img[0];

            for(int i = 0; i < width * depth; i++)
            {
                if(raw_img[i] > max_y)
                    max_y = raw_img[i];
                else if(raw_img[i] < min_y)
                    min_y = raw_img[i];
            }

            byte[] pixels = new byte[width * depth];
            float[] pixel_heights = new float[width * depth];
            float diff = 1 / (max_y - min_y);

            for(int i = 0; i < width * depth; i++)
            {
                pixels[i] = (byte)(((raw_img[i] - min_y) * diff) * 255);
                pixel_heights[i] = ((raw_img[i] - min_y) * diff) * 255;
            }

/*
 Don't need this, just a demo to show how to turn the basic values into an
 image if you need that as a source for something else.

            WritableRaster raster =
                Raster.createPackedRaster(DataBuffer.TYPE_BYTE,
                                          width,
                                          depth,
                                          1,
                                          8,
                                          null);

            raster.setDataElements(0, 0, width, depth, pixels);
            BufferedImage img =
                new BufferedImage(width, depth, BufferedImage.TYPE_BYTE_GRAY);

            img.setData(raster);
*/

            // Now generate the geometry updates. First need to down-sample the
            // height map image. A 512x512 source image equates to 500K
            // triangles. Take every 4th height
            int sample_size = (TEXTURE_SIZE / GRID_SPACING + 1);
            float[] terrain_heights = new float[sample_size * sample_size];
            int t_idx = 0;
            for(int y = 0; y < TEXTURE_SIZE / GRID_SPACING; y++)
            {
                for(int x = 0; x < TEXTURE_SIZE / GRID_SPACING; x++)
                    terrain_heights[t_idx++] = raw_img[y * width * GRID_SPACING + x * GRID_SPACING];

                // Then pick up the one at the end
                terrain_heights[t_idx++] = raw_img[y * width * GRID_SPACING + width - 1];
            }

            // Then pick up the one at the end
            for(int x = 0; x < TEXTURE_SIZE / GRID_SPACING; x++)
                terrain_heights[t_idx++] = raw_img[width * (depth - 1) + x * GRID_SPACING];

            terrain_heights[t_idx++] = raw_img[TEXTURE_SIZE * TEXTURE_SIZE - 1];

            heightGen.setTerrainDetail(terrain_heights, 0);
            heightGen.generate(geomData);

            // Setup the height values.
            imageHeights[3] = Integer.parseInt(snowHeightTf.getText());
            imageHeights[2] = Integer.parseInt(rockHeightTf.getText());
            imageHeights[1] = Integer.parseInt(grassHeightTf.getText());
            imageHeights[0] = Integer.parseInt(sandHeightTf.getText());

            if(texturePixels == null)
                texturePixels = new byte[width * depth * 3];

            imageGen.generateMixedTerrainTexture(texturePixels,
                                                 pixel_heights,
                                                 width,
                                                 depth,
                                                 colorImages,
                                                 imageHeights,
                                                 4);

            // Work our way through the geom data and set the fog coords
            // up. Anything greater than 0 has no fog and full fog by -10.
            for(int i = 0; i < geomData.vertexCount; i++)
            {
                float h = geomData.coordinates[i * 3 + 1];
                fogCoords[i] = (h < 0) ? 1 - (h + -WATER_HEIGHT * 3) / (-WATER_HEIGHT * 3): 0;
            }

            newTerrainAvailable = true;
        }
        catch(NumberFormatException nfe)
        {
            System.out.println("Number formatting problem");
        }
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Convenience method to create the parameter panel.
     */
    private void createParamsPanel()
    {
        Panel main_panel = new Panel(new GridLayout(13, 1));

        Panel p1 = new Panel(new BorderLayout());
        p1.add(new Label("Parameters..."), BorderLayout.WEST);

        Panel p4 = new Panel(new BorderLayout());
        scaleTf = new TextField("8", 5);
        p4.add(new Label("Base Height:"), BorderLayout.WEST);
        p4.add(scaleTf, BorderLayout.EAST);

        Panel p5 = new Panel(new BorderLayout());
        yScaleTf = new TextField("0.4", 5);
        p5.add(new Label("Pass Scale Mult:"), BorderLayout.WEST);
        p5.add(yScaleTf, BorderLayout.EAST);

        Panel p6 = new Panel(new BorderLayout());
        freqTf = new TextField("4", 5);
        p6.add(new Label("Base Frequency:"), BorderLayout.WEST);
        p6.add(freqTf, BorderLayout.EAST);

        Panel p7 = new Panel(new BorderLayout());
        freqDiffTf = new TextField("6.0", 5);
        p7.add(new Label("Frequency Inc:"), BorderLayout.WEST);
        p7.add(freqDiffTf, BorderLayout.EAST);

        Panel p8 = new Panel(new BorderLayout());
        passesTf = new TextField("3", 5);
        p8.add(new Label("Passes:"), BorderLayout.WEST);
        p8.add(passesTf, BorderLayout.EAST);


        Panel p9 = new Panel(new BorderLayout());
        snowHeightTf = new TextField("192", 3);
        p9.add(new Label("Snow Height"), BorderLayout.WEST);
        p9.add(snowHeightTf, BorderLayout.EAST);

        Panel p10 = new Panel(new BorderLayout());
        rockHeightTf = new TextField("128", 3);
        p10.add(new Label("Rock Height"), BorderLayout.WEST);
        p10.add(rockHeightTf, BorderLayout.EAST);

        Panel p11 = new Panel(new BorderLayout());
        grassHeightTf = new TextField("64", 3);
        p11.add(new Label("Grass Height"), BorderLayout.WEST);
        p11.add(grassHeightTf, BorderLayout.EAST);

        Panel p12 = new Panel(new BorderLayout());
        sandHeightTf = new TextField("0", 3);
        p12.add(new Label("Sand Height"), BorderLayout.WEST);
        p12.add(sandHeightTf, BorderLayout.EAST);

        regenButton = new Button("Regenerate");
        regenButton.addActionListener(this);

        Label l1 = new Label("Fog Color (0-255)   [r,g,b]");
        fogColorPanel = new ColorPanel(255, 255, 255);

        imageHeights[0] = 0;
        imageHeights[1] = 64;
        imageHeights[2] = 128;
        imageHeights[3] = 192;
        imageHeights[4] = 256;

        main_panel.add(p1);
        main_panel.add(p4);
        main_panel.add(p5);
        main_panel.add(p6);
        main_panel.add(p7);
        main_panel.add(p8);
        main_panel.add(p9);
        main_panel.add(p10);
        main_panel.add(p11);
        main_panel.add(p12);
        main_panel.add(l1);
        main_panel.add(fogColorPanel);
        main_panel.add(regenButton);

        Panel spacer = new Panel(new BorderLayout());

        spacer.add(main_panel, BorderLayout.NORTH);
        add(spacer, BorderLayout.EAST);
    }

    public static void main(String[] argv)
    {
        DynamicTerrainDemo demo = new DynamicTerrainDemo();
        demo.setVisible(true);
    }
}
