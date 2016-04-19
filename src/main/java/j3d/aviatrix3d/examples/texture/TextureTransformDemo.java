package j3d.aviatrix3d.examples.texture;

// Standard imports

import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import javax.imageio.ImageIO;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Application Specific imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;
import org.j3d.util.DataUtils;
import org.j3d.util.MatrixUtils;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public class TextureTransformDemo extends BaseDemoFrame
{
    public TextureTransformDemo()
    {
        super("Aviatrix 2D Texture Transform Demo");
    }

    @Override
    protected void setupSceneGraph()
    {
        // Load the texture image
        TextureComponent2D img_comp = loadTexture("images/examples/texture/transform_test.gif");

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
        geom.setNormals(data.normals);
        geom.setTextureCoordinates(tex_type, tex_coord, 1);
        geom.setTextureSetMap(tex_sets);

        Material material = new Material();
        material.setDiffuseColor(new float[]{0, 0, 1});
        material.setEmissiveColor(new float[]{0, 0, 1});
        material.setSpecularColor(new float[]{1, 1, 1});

        if (img_comp != null)
        {
            Texture2D texture = new Texture2D();
            texture.setSources(Texture.MODE_BASE_LEVEL,
                               Texture.FORMAT_RGB,
                               new TextureComponent[]{img_comp},
                               1);


            // place 4 shapes into the scene with different transformations
            // applied.
            // None    shift
            // rotate  scale
            TextureUnit[] tu = new TextureUnit[1];
            Matrix4d tex_transform = new Matrix4d();
            Matrix4d obj_transform = new Matrix4d();
            Vector3d translation = new Vector3d();

            tex_transform.setIdentity();
            obj_transform.setIdentity();

            tu[0] = new TextureUnit();
            tu[0].setTexture(texture);

            Appearance app = new Appearance();
            app.setMaterial(material);
            app.setTextureUnits(tu, 1);

            Shape3D shape = new Shape3D();
            shape.setGeometry(geom);
            shape.setAppearance(app);

            translation.x = -0.15f;
            translation.y = 0.15f;
            obj_transform.setTranslation(translation);

            TransformGroup tg = new TransformGroup();
            tg.setTransform(obj_transform);
            tg.addChild(shape);
            scene_root.addChild(tg);

            // Translated texture
            translation.x = 0.25f;
            tex_transform.setTranslation(translation);

            tu[0] = new TextureUnit();
            tu[0].setTexture(texture);
            tu[0].setTextureTransform(tex_transform);

            app = new Appearance();
            app.setMaterial(material);
            app.setTextureUnits(tu, 1);

            shape = new Shape3D();
            shape.setGeometry(geom);
            shape.setAppearance(app);

            translation.x = 0.15f;
            translation.y = 0.15f;
            obj_transform.setTranslation(translation);

            tg = new TransformGroup();
            tg.setTransform(obj_transform);
            tg.addChild(shape);
            scene_root.addChild(tg);

            // Rotated texture
            MatrixUtils mu = new MatrixUtils();
            mu.rotateY((float) (Math.PI / 2), tex_transform);

            tu[0] = new TextureUnit();
            tu[0].setTexture(texture);
            tu[0].setTextureTransform(tex_transform);

            app = new Appearance();
            app.setMaterial(material);
            app.setTextureUnits(tu, 1);

            shape = new Shape3D();
            shape.setGeometry(geom);
            shape.setAppearance(app);

            translation.x = -0.15f;
            translation.y = -0.15f;
            obj_transform.setTranslation(translation);

            tg = new TransformGroup();
            tg.setTransform(obj_transform);
            tg.addChild(shape);
            scene_root.addChild(tg);

            // Scaled texture
            tex_transform.set(2);

            tu[0] = new TextureUnit();
            tu[0].setTexture(texture);
            tu[0].setTextureTransform(tex_transform);

            app = new Appearance();
            app.setMaterial(material);
            app.setTextureUnits(tu, 1);

            shape = new Shape3D();
            shape.setGeometry(geom);
            shape.setAppearance(app);

            translation.x = 0.15f;
            translation.y = -0.15f;
            obj_transform.setTranslation(translation);

            tg = new TransformGroup();
            tg.setTransform(obj_transform);
            tg.addChild(shape);
            scene_root.addChild(tg);
        }

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
        TextureTransformDemo demo = new TextureTransformDemo();
        demo.setVisible(true);
    }
}
