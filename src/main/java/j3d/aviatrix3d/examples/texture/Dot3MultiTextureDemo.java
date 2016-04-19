package j3d.aviatrix3d.examples.texture;

// Standard imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Application Specific imports
import org.j3d.aviatrix3d.*;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;

/**
 * Example application that demonstrates how to put together a simple multitextured
 * objects using just two texture units. Should run on any hardware.
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public class Dot3MultiTextureDemo extends BaseDemoFrame
{
    public Dot3MultiTextureDemo()
    {
        super("Aviatrix Dot3 Bump Map Demo");
    }

    @Override
    protected void setupSceneGraph()
    {
        // Load the texture image
        TextureComponent2D base_img = loadTexture("images/examples/texture/bump_map.jpg");
        TextureComponent2D filter_img = loadTexture("images/examples/texture/colour_map.jpg");

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0.1f, 0.8f);

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

        BoxGenerator generator = new BoxGenerator(0.2f, 0.2f, 0.2f);
        generator.generate(data);

        int[] tex_type = {VertexGeometry.TEXTURE_COORDINATE_2};
        float[][] tex_coord = new float[1][data.vertexCount * 2];
        int[] tex_sets = {0, 0};

        System.arraycopy(data.textureCoordinates, 0, tex_coord[0], 0,
                         data.vertexCount * 2);

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
//        geom.setNormals(data.normals);
        geom.setTextureCoordinates(tex_type, tex_coord, 1);
        geom.setTextureSetMap(tex_sets);

        Material material = new Material();
        material.setDiffuseColor(new float[]{1, 1, 1});
        material.setEmissiveColor(new float[]{1, 1, 1});
        material.setSpecularColor(new float[]{1, 1, 1});
        material.setLightingEnabled(true);

        Texture2D base_texture = new Texture2D();
        base_texture.setSources(Texture.MODE_BASE_LEVEL,
                                Texture.FORMAT_RGB,
                                new TextureComponent2D[] { base_img },
                                1);

        Texture2D filter_texture = new Texture2D();
        filter_texture.setSources(Texture.MODE_BASE_LEVEL,
                                  Texture.FORMAT_RGB,
                                  new TextureComponent2D[] { filter_img },
                                  1);

        TextureAttributes base_ta = new TextureAttributes();
        base_ta.setBlendColor(0.5f, 0, 0, 1);
        base_ta.setTextureMode(TextureAttributes.MODE_COMBINE);
        base_ta.setCombineMode(false, TextureAttributes.COMBINE_DOT3_RGB);
        base_ta.setCombineMode(true, TextureAttributes.COMBINE_REPLACE);
        base_ta.setCombineSource(false, 0, TextureAttributes.SOURCE_CURRENT_TEXTURE);
//        base_ta.setCombineSource(false, 1, TextureAttributes.SOURCE_CURRENT_TEXTURE);
//        base_ta.setCombineSource(false, 1, TextureAttributes.SOURCE_PREVIOUS_UNIT);
        base_ta.setCombineSource(true, 0, TextureAttributes.SOURCE_CONSTANT_COLOR);

        TextureAttributes filter_ta = new TextureAttributes();
        filter_ta.setTextureMode(TextureAttributes.MODE_MODULATE);

        TextureUnit[] tu = new TextureUnit[2];
        tu[0] = new TextureUnit();
        tu[0].setTexture(base_texture);
        tu[0].setTextureAttributes(base_ta);

        tu[1] = new TextureUnit();
        tu[1].setTexture(filter_texture);
        tu[1].setTextureAttributes(filter_ta);

        Appearance app = new Appearance();
        app.setMaterial(material);
        app.setTextureUnits(tu, 2);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        DirectionalLight light = new DirectionalLight();
        light.setDirection(1, 0, 1);

        scene_root.addChild(shape);
        scene_root.addChild(light);

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
        Dot3MultiTextureDemo demo = new Dot3MultiTextureDemo();
        demo.setVisible(true);
    }
}
