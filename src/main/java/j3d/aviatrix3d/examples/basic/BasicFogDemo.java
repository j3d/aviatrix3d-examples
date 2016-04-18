package j3d.aviatrix3d.examples.basic;

// External imports

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;
import org.j3d.renderer.aviatrix3d.geom.*;

import org.j3d.util.MatrixUtils;

/**
 * Example application that demonstrates a global fog effect.
 *
 * The fog is global and there is a ring of boxes that rotate in a circular
 * path in and out of the fog.
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public class BasicFogDemo extends BaseDemoFrame
{
    /** The colour to use for the fog and background */
    private static final float[] FOG_COLOUR = { 0, 0, 0.5f };

    public BasicFogDemo()
    {
        super("Global fog effect Aviatrix Demo");
    }

    @Override
    protected void setupSceneGraph()
    {
        // View group
        Viewpoint vp = new Viewpoint();
        vp.setHeadlightEnabled(true);

        Vector3d trans = new Vector3d();
        trans.set(0, 1, 2);

        MatrixUtils mu = new MatrixUtils();

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();

        mu.rotateX(Math.PI / -8, mat);
        mat.setTranslation(trans);


        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // The root transform that we'll put all our fogged primitives
        // under.
        TransformGroup shape_transform = new TransformGroup();

        // Create the number of primitives to use
        createPrimitives(4, shape_transform);

        scene_root.addChild(shape_transform);

        Fog fog = new Fog(FOG_COLOUR);
        fog.setEnabled(true);
        fog.setLinearDistance(0.1f, 3);
        scene_root.addChild(fog);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);
        scene.setActiveFog(fog);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, 500, 500);
        view.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        resizeManager.addManagedViewport(view);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);

        FogObjectAnimation anim = new FogObjectAnimation(shape_transform, resizeManager);
        sceneManager.setApplicationObserver(anim);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Create a collection of primitives to use and place them under the
     * parent group.
     *
     * @param num The number of primitives to make
     * @param parent The group node to add these to
     */
    private void createPrimitives(int num, Group parent)
    {
        double angle_inc = 2 * Math.PI / num;
        double angle = 0;
        Vector3d translation = new Vector3d();

        Matrix4d matrix = new Matrix4d();
        matrix.setIdentity();

        Material material = new Material();
        material.setDiffuseColor(new float[] { 1, 0, 0 });
        material.setSpecularColor(new float[] { 0.4f, 0.4f, 0.4f });
        material.setLightingEnabled(true);

        Appearance app = new Appearance();
        app.setMaterial(material);

        for(int i = 0; i < num; i++)
        {
            float x = 0.5f * (float)Math.sin(angle);
            float y = 0.5f * (float)Math.cos(angle);

            angle += angle_inc;

            translation.x = x;
            translation.z = y;

            matrix.set(translation);

            TransformGroup tg = new TransformGroup();
            tg.setTransform(matrix);

            parent.addChild(tg);

            switch(i % 4)
            {
                case 0:
                    // Box
                    Box box = new Box(0.125f, 0.125f, 0.125f, app);
                    tg.addChild(box);
                    break;

                case 1:
                    // Cone
                    Cone cone = new Cone(0.25f, 0.125f, app);
                    tg.addChild(cone);
                    break;

                case 2:
                    // cylinder
                    Cylinder cyl = new Cylinder(0.25f, 0.125f, app);
                    tg.addChild(cyl);
                    break;

                case 3:
                    // sphere
                    Sphere sphere = new Sphere(0.125f, app);
                    tg.addChild(sphere);

            }
        }
    }

    public static void main(String[] args)
    {
        BasicFogDemo demo = new BasicFogDemo();
        demo.setVisible(true);
    }
}
