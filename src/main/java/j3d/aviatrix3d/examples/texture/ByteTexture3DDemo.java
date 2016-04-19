package j3d.aviatrix3d.examples.texture;

// Standard imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Application Specific imports
import org.j3d.aviatrix3d.*;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;
import org.j3d.texture.procedural.PerlinNoiseGenerator;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public class ByteTexture3DDemo extends BaseDemoFrame
{
    private static final int XSIZE = 64;
    private static final int YSIZE = 64;
    private static final int ZSIZE = 64;
    private static final float XSCALE = 0.16f;
    private static final float YSCALE = 0.16f;
    private static final float ZSCALE = 0.16f;

    public ByteTexture3DDemo()
    {
        super("Aviatrix 3D Texture Demo using in memory textures");
    }

    @Override
    protected void setupSceneGraph()
    {
        PerlinNoiseGenerator noise_gen = new PerlinNoiseGenerator();

        byte[] tex_buffer = new byte[XSIZE * YSIZE * ZSIZE];
        int min = 255;
        int max = 0;
        int pos = 0;

        for (int z = 0; z < ZSIZE; z++)
        {
            for (int y = 0; y < YSIZE; y++)
            {
                for (int x = 0; x < XSIZE; x++)
                {
                    float noise = noise_gen.tileableTurbulence3(XSCALE * x,
                                                                YSCALE * y,
                                                                ZSCALE * z,
                                                                XSIZE * XSCALE,
                                                                YSIZE * YSCALE,
                                                                ZSIZE * ZSCALE,
                                                                16);
                    int t = (int) (127.5f * (1 + noise));
                    if (t > max)
                        max = t;
                    if (t < min)
                        min = t;
                    tex_buffer[pos++] = (byte) t;
                }
            }
        }

        float min_max = 1.0f / (max - min);
        for (int i = 0; i < XSIZE * YSIZE * ZSIZE; i++)
            tex_buffer[i] = (byte) ((255 * (tex_buffer[i] - min)) * min_max);

        ByteTextureComponent3D img_comp =
                new ByteTextureComponent3D(TextureComponent.FORMAT_SINGLE_COMPONENT,
                                           XSIZE,
                                           YSIZE,
                                           ZSIZE,
                                           tex_buffer);

/*
        // Simpler versions to see the output
        byte[] tex_buffer =
        {
              (byte)0xFF, (byte)0,
              (byte)0,    (byte)0xFF,
              (byte)0,    (byte)0xFF,
              (byte)0xFF, (byte)0

// 3 Color texture
//            (byte)0xFF, (byte)0, (byte)0,       (byte)0, (byte)0xFF, (byte)0,
//            (byte)0,    (byte)0, (byte)0xFF,    (byte)0, (byte)0xFF, (byte)0xFF,

//            (byte)0xFF, (byte)0xFF, (byte)0,    (byte)0xFF, (byte)0, (byte)0xFF,
//            (byte)0,    (byte)0xFF, (byte)0xFF, (byte)0,    (byte)0, (byte)0
        };

        ByteTextureComponent3D img_comp =
            new ByteTextureComponent3D(TextureComponent.FORMAT_SINGLE_COMPONENT,
                                       2,
                                       2,
                                       2,
                                       tex_buffer);

//        Texture texture = new Texture3D(Texture.FORMAT_RGB, img_comp);
*/

        Texture texture = new Texture3D(Texture.FORMAT_INTENSITY, img_comp);

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0.1d, 1);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Sphere to render the shader onto
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                GeometryData.TEXTURE_3D_DATA;

        BoxGenerator generator = new BoxGenerator(0.2f, 0.2f, 0.2f);
        generator.generate(data);

        int[] tex_type = {VertexGeometry.TEXTURE_COORDINATE_3};
        float[][] tex_coord = new float[1][data.vertexCount * 3];

        System.arraycopy(data.textureCoordinates, 0, tex_coord[0], 0,
                         data.vertexCount * 3);

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setNormals(data.normals);
        geom.setTextureCoordinates(tex_type, tex_coord, 1);

        Material material = new Material();
        material.setDiffuseColor(new float[]{0, 0, 1});
        material.setEmissiveColor(new float[]{0, 0, 1});
        material.setSpecularColor(new float[]{1, 1, 1});

/*
        Texture3D texture = new Texture3D();
        texture.setSources(Texture.MODE_BASE_LEVEL,
                          Texture.FORMAT_RGB,
                          img_comp,
                          1);
*/
        TexCoordGeneration coord_gen = new TexCoordGeneration();
        coord_gen.setParameter(TexCoordGeneration.TEXTURE_S,
                               TexCoordGeneration.MODE_GENERIC,
                               TexCoordGeneration.MAP_OBJECT_LINEAR,
                               null);

        coord_gen.setParameter(TexCoordGeneration.TEXTURE_T,
                               TexCoordGeneration.MODE_GENERIC,
                               TexCoordGeneration.MAP_OBJECT_LINEAR,
                               null);

        coord_gen.setParameter(TexCoordGeneration.TEXTURE_R,
                               TexCoordGeneration.MODE_GENERIC,
                               TexCoordGeneration.MAP_OBJECT_LINEAR,
                               null);

        TextureUnit[] tu = new TextureUnit[1];
        tu[0] = new TextureUnit();
        tu[0].setTexture(texture);
//        tu[0].setTexCoordGeneration(coord_gen);

        Appearance app = new Appearance();
        app.setMaterial(material);
        app.setTextureUnits(tu, 1);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        scene_root.addChild(shape);

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

        Layer[] layers = {layer};
        displayManager.setLayers(layers, 1);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        ByteTexture3DDemo demo = new ByteTexture3DDemo();
        demo.setVisible(true);
    }
}
