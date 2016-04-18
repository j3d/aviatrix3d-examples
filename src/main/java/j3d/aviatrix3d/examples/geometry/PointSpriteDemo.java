package j3d.aviatrix3d.examples.geometry;

// Standard imports
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;


// Application Specific imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.graphics.GenericCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.StateAndTransparencyDepthSortStage;

import org.j3d.renderer.aviatrix3d.nodes.SortedPointArray;

/**
 * Example application that demonstrates the use of point sprites.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class PointSpriteDemo extends BaseDemoFrame
{
    public PointSpriteDemo()
    {
        super("Point Sprite Aviatrix Demo", new GenericCullStage(), new StateAndTransparencyDepthSortStage(), false);
    }

    @Override
    protected void setupSceneGraph()
    {
        // Load the texture image
        TextureComponent2D img_comp = loadTexture("images/examples/geometry/halo.jpg");
//        TextureComponent2D img_comp = loadTexture("images/examples/geometry/big_glow.jpg");
        // View group

        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 3);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // The coordinates of the point sprites
        float[] coord = { 0, 0, -1, 0.25f, 0, -1, 0, 0.25f, -1 };
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1 };

        FloatBuffer coordsBuffer = createBuffer(coord.length);
        coordsBuffer.put(coord);
        coordsBuffer.rewind();

        FloatBuffer normalsBuffer = createBuffer(normal.length);
        normalsBuffer.put(normal);
        normalsBuffer.rewind();

        SortedPointArray geom = new SortedPointArray();
        geom.setValidVertexCount(3);
        geom.setVertices(TriangleArray.COORDINATE_3, coordsBuffer);
        geom.setNormals(normalsBuffer);

        // The appearance setup that makes the points into sprites
        PointAttributes point_attr = new PointAttributes();
        point_attr.setPointSpriteEnabled(true);
        point_attr.setAttenuationFactors(0.01f, 0, 0.01f);
        point_attr.setMaxPointSize(50);
        point_attr.setMinPointSize(10);
        point_attr.setFadeThresholdSize(60);

        TextureAttributes tex_attr = new TextureAttributes();
        tex_attr.setPointSpriteCoordEnabled(true);

        TextureUnit[] sprite_units = new TextureUnit[1];
        sprite_units[0] = new TextureUnit();
        sprite_units[0].setTextureAttributes(tex_attr);

        if(img_comp != null)
        {
            Texture2D texture = new Texture2D();
            texture.setSources(Texture.MODE_BASE_LEVEL,
                              Texture.FORMAT_RGB,
                              new TextureComponent[] { img_comp },
                              1);

            sprite_units[0].setTexture(texture);
        }

        Appearance app = new Appearance();
        app.setPointAttributes(point_attr);
        app.setTextureUnits(sprite_units, 1);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

//        trans.set(0.5f, 0, 0);
        Matrix4d mat2 = new Matrix4d();
        mat2.setIdentity();
//        mat2.setTranslation(trans);

        TransformGroup shape_transform = new TransformGroup();
        shape_transform.addChild(shape);
        shape_transform.setTransform(mat2);

        scene_root.addChild(shape_transform);

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

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);

        RotationAnimation anim = new RotationAnimation(shape_transform, resizeManager);
        sceneManager.setApplicationObserver(anim);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Convenience method to allocate a NIO buffer for the vertex handling that
     * handles floats.
     *
     * @param size The number of floats to have in the array
     */
    private FloatBuffer createBuffer(int size)
    {
        // Need to allocate a byte buffer 4 times the size requested because the
        // size is treated as bytes, not number of floats.
        ByteBuffer buf = ByteBuffer.allocateDirect(size * 4);
        buf.order(ByteOrder.nativeOrder());
        FloatBuffer ret_val = buf.asFloatBuffer();

        return ret_val;
    }

    public static void main(String[] args)
    {
        PointSpriteDemo demo = new PointSpriteDemo();
        demo.setVisible(true);
    }
}
