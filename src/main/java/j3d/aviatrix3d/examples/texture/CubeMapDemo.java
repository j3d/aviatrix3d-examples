package j3d.aviatrix3d.examples.texture;

// Standard imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Application Specific imports
import org.j3d.aviatrix3d.*;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;

import org.j3d.geom.GeometryData;
import org.j3d.geom.SphereGenerator;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public class CubeMapDemo extends BaseDemoFrame
{
    public CubeMapDemo()
    {
        super("Aviatrix Cubic Environment Map Demo");
    }

    @Override
    protected void setupSceneGraph()
    {
        // Load the texture image
        TextureComponent2D[] img_sides = new TextureComponent2D[6];

        String[] targets =
        {
                "images/examples/texture/left_cube_map.gif",
                "images/examples/texture/right_cube_map.jpg",
                "images/examples/texture/top_cube_map.jpg",
                "images/examples/texture/bottom_cube_map.jpg",
                "images/examples/texture/back_cube_map.jpg",
                "images/examples/texture/front_cube_map.jpg"
        };

        for (int i = 0; i < 6; i++)
            img_sides[i] = loadTexture(targets[i]);

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

        // Sphere to render the shader onto
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                GeometryData.TEXTURE_2D_DATA;

        SphereGenerator generator = new SphereGenerator(0.2f);
//        BoxGenerator generator = new BoxGenerator(0.2f, 0.2f, 0.2f);
        generator.generate(data);

//data.prettyPrint();

        int[] tex_type = {VertexGeometry.TEXTURE_COORDINATE_2};
        float[][] tex_coord = new float[1][data.vertexCount * 2];

        System.arraycopy(data.textureCoordinates, 0, tex_coord[0], 0,
                         data.vertexCount * 2);

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


        TextureCubicEnvironmentMap texture = new TextureCubicEnvironmentMap();
        texture.setSources(Texture.MODE_BASE_LEVEL,
                           Texture.FORMAT_RGB,
                           img_sides,
                           6);

        TexCoordGeneration coord_gen = new TexCoordGeneration();
        coord_gen.setParameter(TexCoordGeneration.TEXTURE_S,
                               TexCoordGeneration.MODE_GENERIC,
//                               TexCoordGeneration.MAP_NORMALS,
                               TexCoordGeneration.MAP_REFLECTIONS,
                               null);

        coord_gen.setParameter(TexCoordGeneration.TEXTURE_T,
                               TexCoordGeneration.MODE_GENERIC,
//                               TexCoordGeneration.MAP_NORMALS,
                               TexCoordGeneration.MAP_REFLECTIONS,
                               null);

        coord_gen.setParameter(TexCoordGeneration.TEXTURE_R,
                               TexCoordGeneration.MODE_GENERIC,
//                               TexCoordGeneration.MAP_NORMALS,
                               TexCoordGeneration.MAP_REFLECTIONS,
                               null);

        TextureUnit[] tu = new TextureUnit[1];
        tu[0] = new TextureUnit();
        tu[0].setTexture(texture);
        tu[0].setTexCoordGeneration(coord_gen);

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
        CubeMapDemo demo = new CubeMapDemo();
        demo.setVisible(true);
    }
}
