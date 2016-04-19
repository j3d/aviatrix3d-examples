package j3d.aviatrix3d.examples.transparent;

// Standard imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Application Specific imports
import org.j3d.aviatrix3d.*;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;
import org.j3d.aviatrix3d.pipeline.graphics.TransparencyDepthSortStage;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public class TextureDemo extends BaseDemoFrame
{
    public TextureDemo()
    {
        super("Transparent Texture Demo", null, new TransparencyDepthSortStage(), false);
    }

    @Override
    protected void setupSceneGraph()
    {

        TextureComponent2D custImg = loadTexture("images/examples/transparency/ButtonForward.png");
        TextureComponent2D argbImg = loadTexture("images/examples/transparency/mytree.png");

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

        // Flat panel that has the viewable object as the demo
        float[] coord = { 0, 0, -1,     0.25f, 0, -1,     0, 0.25f, -1,
                          0.25f, 0, -1, 0.25f, 0.25f, -1, 0, 0.25f, -1,
                          0.25f, 0, -1, 0.5f, 0, -1,      0.25f, 0.25f, -1,
                          0.5f, 0, -1,  0.5f, 0.25f, -1,  0.25f, 0.25f, -1 };

        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1,
                           0, 0, 1, 0, 0, 1, 0, 0, 1,
                           0, 0, 1, 0, 0, 1, 0, 0, 1,
                           0, 0, 1, 0, 0, 1, 0, 0, 1};
        float[][] tex_coord = { { 0, 0,  1, 0,  0, 1,   1, 0,  1, 1, 0, 1,
                                  0, 0,  1, 0,  0, 1,   1, 0,  1, 1, 0, 1 } };
        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3, coord, 6);
        geom.setTextureCoordinates(tex_type, tex_coord, 1);

        Material material = new Material();
        material.setDiffuseColor(new float[] { 0, 0, 1 });
        material.setEmissiveColor(new float[] { 0, 0, 1 });
        material.setSpecularColor(new float[] { 1, 1, 1 });

        Appearance app = new Appearance();
        app.setMaterial(material);

        if(custImg != null)
        {
//            Texture2D texture = new Texture2D(Texture.FORMAT_RGB,
//                                              custImg);

            Texture2D texture = new Texture2D();
            texture.setSources(Texture.MODE_BASE_LEVEL,
                              Texture.FORMAT_RGB,
                              new TextureComponent[] { custImg },
                              1);

            TextureUnit[] tu = new TextureUnit[1];
            tu[0] = new TextureUnit();
            tu[0].setTexture(texture);

            app.setTextureUnits(tu, 1);
        }

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);


        Appearance app2 = new Appearance();
        app2.setMaterial(material);

        if(argbImg != null)
        {
//            Texture2D texture = new Texture2D(Texture.FORMAT_RGB,
//                                              argbImg);

            Texture2D texture2 = new Texture2D();
            texture2.setSources(Texture.MODE_BASE_LEVEL,
                              Texture.FORMAT_RGB,
                              new TextureComponent[] { argbImg },
                              1);

            TextureUnit[] tu2 = new TextureUnit[1];
            tu2[0] = new TextureUnit();
            tu2[0].setTexture(texture2);

            app2.setTextureUnits(tu2, 1);
        }

        trans = new Vector3d();
        trans.set(-0.5f, 0, 0);

        TransformGroup tg2 = new TransformGroup();
        Matrix4d transform2 = new Matrix4d();
        transform2.set(trans);
        tg2.setTransform(transform2);

        Shape3D shape2 = new Shape3D();
        shape2.setGeometry(geom);
        shape2.setAppearance(app2);
        tg2.addChild(shape2);


        trans = new Vector3d();
        trans.set(0, 0, -1);

        TransformGroup tg = new TransformGroup();
        Matrix4d transform = new Matrix4d();
        transform.set(trans);
        tg.setTransform(transform);

        Shape3D backShape = new Shape3D();
        Material material2 = new Material();
        material2.setDiffuseColor(new float[] { 1, 0, 0 });
        material2.setEmissiveColor(new float[] { 1, 0, 0 });
        material2.setSpecularColor(new float[] { 1, 1, 1 });

        Appearance backApp = new Appearance();
        backApp.setMaterial(material2);
        backShape.setGeometry(geom);
        backShape.setAppearance(backApp);
        tg.addChild(backShape);


        ColorBackground cbg = new ColorBackground(new float[] {0,0,1,1});

        scene_root.addChild(cbg);
        scene_root.addChild(tg);
        scene_root.addChild(tg2);
        scene_root.addChild(shape);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveBackground(cbg);
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
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        TextureDemo demo = new TextureDemo();
        demo.setVisible(true);
    }
}
