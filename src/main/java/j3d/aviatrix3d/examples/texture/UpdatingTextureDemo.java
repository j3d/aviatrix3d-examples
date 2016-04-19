package j3d.aviatrix3d.examples.texture;

// Standard imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Application Specific imports
import org.j3d.aviatrix3d.*;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;

/**
 * Example application that demonstrates updating textures on the fly with
 * different subsection updates.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class UpdatingTextureDemo extends BaseDemoFrame
{
    private static final int XSIZE = 64;
    private static final int YSIZE = 64;

    public UpdatingTextureDemo()
    {
        super("Aviatrix Demo Showing Dynamic Textures", false);
    }

    @Override
    protected void setupSceneGraph()
    {
        byte[] tex_buffer = new byte[XSIZE * YSIZE * 3];
        int pos = 0;

        for (int y = 0; y < YSIZE; y++)
        {
            for (int x = 0; x < XSIZE; x++)
            {
                tex_buffer[pos++] = (byte) 0x0;
                tex_buffer[pos++] = (byte) 0xFF;
                tex_buffer[pos++] = (byte) 0x0;
            }
        }

        ByteTextureComponent2D img_comp =
                new ByteTextureComponent2D(TextureComponent.FORMAT_RGB,
                                           XSIZE,
                                           YSIZE,
                                           tex_buffer);


        Texture texture = new Texture2D(Texture.FORMAT_RGB, img_comp);

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 0.5);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Flat panel that has the viewable object as the demo
        float[] coord = {
                0, 0, -0.5f, 0.25f, 0, -0.5f, 0, 0.25f, -0.5f,
                0.25f, 0, -0.5f, 0.25f, 0.25f, -0.5f, 0, 0.25f, -0.5f
        };

        float[] normal = {
                0, 0, 1, 0, 0, 1, 0, 0, 1,
                0, 0, 1, 0, 0, 1, 0, 0, 1
        };
        float[][] tex_coord = {{0, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0, 1,}};
        int[] tex_type = {VertexGeometry.TEXTURE_COORDINATE_2};

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3, coord, 6);
        geom.setTextureCoordinates(tex_type, tex_coord, 1);

        Material material = new Material();
        material.setDiffuseColor(new float[]{0, 0, 1});
        material.setEmissiveColor(new float[]{0, 0, 1});
        material.setSpecularColor(new float[]{1, 1, 1});

        TextureUnit[] tu = new TextureUnit[1];
        tu[0] = new TextureUnit();
        tu[0].setTexture(texture);

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

        TextureUpdater anim = new TextureUpdater(XSIZE, YSIZE,  img_comp, resizeManager);
        sceneManager.setApplicationObserver(anim);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        UpdatingTextureDemo demo = new UpdatingTextureDemo();
        demo.setVisible(true);
    }
}
