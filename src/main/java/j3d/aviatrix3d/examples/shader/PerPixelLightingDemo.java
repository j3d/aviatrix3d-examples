package j3d.aviatrix3d.examples.shader;

// External imports
import org.j3d.maths.vector.AxisAngle4d;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;

import j3d.aviatrix3d.examples.basic.BaseDemoFrame;
import org.j3d.aviatrix3d.pipeline.graphics.TransparencyDepthSortStage;

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;
import org.j3d.geom.SphereGenerator;

/**
 * Example of per pixel lighting.
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class PerPixelLightingDemo extends BaseDemoFrame
	implements ApplicationUpdateObserver, NodeUpdateListener
{
	/** vertex shader */
	private static final String VTX_SHADER_FILE = 
		"shaders/examples/simple/phong_vert.glsl";
	
	/** fragment shader */
	private static final String FRAG_SHADER_FILE = 
		"shaders/examples/simple/phong_frag.glsl";
	
	/** texture file */
	private static final String[] TEXTURE_FILES =
	{
		"images/examples/shader/flags/uk.png",
		"images/examples/shader/flags/usa.png",
		"images/examples/shader/flags/sweden.png",
		"images/examples/shader/flags/switzerland.png",
	};
	
	/** orbit distance */
	private static final float ORBIT_DISTANCE = 20;
	
	/** headlight settings for shader */
	private static final int ENABLE_HEADLIGHT = 1;
	private static final int DISABLE_HEADLIGHT = 0;
	
	/** navigation objs */
	private TransformGroup view_tx;
	private int station;
	
	private AxisAngle4d rotation;
	private Matrix4d view_mtx;
	private Vector3d translation;
	
	private float[] transparency;
	private Material[] material;
	
	/**
	 * Construct a new shader demo instance.
	 */
	public PerPixelLightingDemo()
	{
		super("GLSL Phong", null, new TransparencyDepthSortStage(), false);
	}
	
	//---------------------------------------------------------------
	// ApplicationUpdateObserver methods
	//---------------------------------------------------------------

    @Override
	public void appShutdown()
	{
	}

    @Override
	public void updateSceneGraph()
    {
		resizeManager.sendResizeUpdates();

		if (++station == 360)
        {
			station = 0;
		}

		double angle = station * Math.PI / 180;
		translation.x = (float)(ORBIT_DISTANCE * Math.sin(angle));
		translation.z = (float)(ORBIT_DISTANCE * Math.cos(angle));
		
		rotation.angle = (float)angle;
		view_mtx.set(rotation);
		view_mtx.setTranslation(translation);
		
		view_tx.boundsChanged(this);
		
		transparency[0] = 0.99f;
		transparency[1] = 0.99f;
		transparency[2] = 0.99f;
		transparency[3] = 0.99f;
		if ((angle > 3 * Math.PI/2) || (angle < Math.PI/2)) {
			transparency[2] = 1 - (float)Math.cos(angle);
		}
		if ((angle > 0) && (angle < Math.PI)) {
			transparency[1] = 1 - (float)Math.sin(angle);
		}
		if ((angle > Math.PI/2) && (angle < 3 * Math.PI/2)) {
			transparency[0] = 1 + (float)Math.cos(angle);
		}
		if ((angle > Math.PI) && (angle < 2 * Math.PI)) {
			transparency[3] = 1 + (float)Math.sin(angle);
		}
		material[0].dataChanged(this);
		material[1].dataChanged(this);
		material[2].dataChanged(this);
		material[3].dataChanged(this);
	}
	
	//---------------------------------------------------------------
	// NodeUpdateListener methods
	//---------------------------------------------------------------

    @Override
	public void updateNodeBoundsChanges(Object src)
    {
		if (src == view_tx)
        {
			view_tx.setTransform(view_mtx);
		}
	}

    @Override
	public void updateNodeDataChanges(Object src)
    {
		for (int i = 0; i < 4; i++)
        {
			if (src == material[i])
            {
				material[i].setTransparency(transparency[i]);
			}
		}
	}
	
	//---------------------------------------------------------------
	// X methods
	//---------------------------------------------------------------

    @Override
	protected void setupSceneGraph()
	{
        rotation = new AxisAngle4d();
        rotation.set(0, 1, 0, 0);

        view_mtx = new Matrix4d();
        translation = new Vector3d();

        transparency = new float[4];
        material = new Material[4];

		Group scene_root = new Group();

		// pointing down the -X axis
		SpotLight spot_0 = new SpotLight(
			new float[]{1, 1, 1},
			new float[]{10, 0, 0},
			new float[]{-1, 0, 0});
		spot_0.setGlobalOnly(true);
		spot_0.setEnabled(true);
		scene_root.addChild(spot_0);
		
		// pointing down the -Y axis
		SpotLight spot_1 = new SpotLight(
			new float[]{1, 1, 1},
			new float[]{0, 10, 0},
			new float[]{0, -1, 0});
		spot_1.setGlobalOnly(true);
		spot_1.setEnabled(true);
		scene_root.addChild(spot_1);
		/////////////////////////////////////////////////////////////////
		
		TransformGroup sphere_tx_0 = getSphere();
		
		Vector3d t_0 = new Vector3d();
        t_0.set(0, 2.5f, 0);

        Matrix4d mtx_0 = new Matrix4d();
        mtx_0.set(t_0);

        sphere_tx_0.setTransform(mtx_0);
		scene_root.addChild(sphere_tx_0);
		
		/////////////////////////////////////////////////////////////////
		
		for (int i = 0; i < 4; i++) {
			
			TriangleArray ta = getWall();
			
			material[i] = new Material();
        	material[i].setDiffuseColor(new float[] { 1, 1, 1 });
			material[i].setSpecularColor(new float[] { 1, 1, 1 });
			material[i].setShininess(0.8f);
			
			int num_textures = 1;
            TextureComponent2D[] img_comp = { loadTexture(TEXTURE_FILES[i]) };

            Texture2D texture = new Texture2D();
            texture.setSources(Texture.MODE_BASE_LEVEL,
                               Texture.FORMAT_RGBA,
                               img_comp,
                               1);

            TextureUnit tu = new TextureUnit();
            tu.setTexture(texture);

            TextureUnit[] textures = { tu };

			String[] vert_shader_txt = loadShaderFile(VTX_SHADER_FILE);
			String[] frag_shader_txt = loadShaderFile(FRAG_SHADER_FILE);
			
			ShaderObject vert_shader = new ShaderObject(true);
			vert_shader.setSourceStrings(vert_shader_txt, 1);
			vert_shader.requestInfoLog();
			vert_shader.compile();
			
			ShaderObject frag_shader = new ShaderObject(false);
			frag_shader.setSourceStrings(frag_shader_txt, 1);
			frag_shader.requestInfoLog();
			frag_shader.compile();
			
			ShaderProgram shader_prog = new ShaderProgram();
			shader_prog.addShaderObject(vert_shader);
			shader_prog.addShaderObject(frag_shader);
			shader_prog.requestInfoLog();
			shader_prog.link();
			
			ShaderArguments shader_args = new ShaderArguments();
			shader_args.setUniform("use_headlight", 1, new int[]{DISABLE_HEADLIGHT}, 1);
			shader_args.setUniform("has_texture", 1, new int[]{1}, 1);
			shader_args.setUniformSampler("tex_unit", 0);
			
			GLSLangShader shader = new GLSLangShader();
			shader.setShaderProgram(shader_prog);
			shader.setShaderArguments(shader_args);
			
			Appearance app = new Appearance();
			app.setTextureUnits(textures, num_textures);
			app.setMaterial(material[i]);
			app.setShader(shader);
			
			Shape3D shape = new Shape3D();
			shape.setGeometry(ta);
			shape.setAppearance(app);
			
			Matrix4d mat2 = new Matrix4d();
			mat2.setIdentity();
			Vector3d t = new Vector3d();
			AxisAngle4d r = new AxisAngle4d();
            r.set(0, 1, 0, 0);

            switch(i)
            {
                case 0:
                    t.set(0, 0, -2.5f);
                    r.angle = 0;
                    break;
                case 1:
                    t.set(2.5f, 0, 0);
                    r.angle = (float)Math.PI/2;
                    break;
                case 2:
                    t.set(0, 0, 2.5f);
                    r.angle = (float)Math.PI;
                    break;
                case 3:
                    t.set(-2.5f, 0, 0);
                    r.angle = -(float)Math.PI/2;
                    break;
			}

            mat2.set(r);
            mat2.setTranslation(t);

			TransformGroup shape_tx = new TransformGroup();
			shape_tx.addChild(shape);
			shape_tx.setTransform(mat2);
			
			scene_root.addChild(shape_tx);
		}
		/////////////////////////////////////////////////////////////////
		
		TransformGroup sphere_tx_1 = getSphere();
		
		Vector3d t_1 = new Vector3d();
        t_1.set(0, -2.5f, 0);

        Matrix4d mtx_1 = new Matrix4d();
        mtx_1.set(t_1);

        sphere_tx_1.setTransform(mtx_1);
		scene_root.addChild(sphere_tx_1);
		
		/////////////////////////////////////////////////////////////////
		// View group
		Viewpoint vp = new Viewpoint();
		vp.setHeadlightEnabled(true);
		
		translation.y = 4;
		translation.z = ORBIT_DISTANCE;
		
		view_mtx.setIdentity();
		view_mtx.setTranslation(translation);
		
		view_tx = new TransformGroup();
		view_tx.addChild(vp);
		view_tx.setTransform(view_mtx);
		
		scene_root.addChild(view_tx);
		
		/////////////////////////////////////////////////////////////////
		
		ColorBackground bg = new ColorBackground(new float[]{ 0.80f, 0.85f, 0.90f });
		
		SimpleScene scene = new SimpleScene();
		scene.setActiveBackground(bg);
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

        sceneManager.setApplicationObserver(this);
	}

	//---------------------------------------------------------------
	// Local methods
	//---------------------------------------------------------------
	
	public static void main(String[] args)
	{
		PerPixelLightingDemo demo = new PerPixelLightingDemo();
		demo.setVisible(true);
	}
	
	private TriangleArray getWall() 
	{
		BoxGenerator generator = new BoxGenerator(5, 5, 0.1f);
		GeometryData data = new GeometryData();
		
		data.geometryType = GeometryData.TRIANGLES;
		data.geometryComponents = GeometryData.NORMAL_DATA |
			GeometryData.TEXTURE_2D_DATA;
		
		generator.generate(data);
		
		TriangleArray ta = new TriangleArray(
			false,
			VertexGeometry.VBO_HINT_STATIC);
		
		ta.setVertices(
			TriangleStripArray.COORDINATE_3,
			data.coordinates,
			data.vertexCount);
		
		ta.setNormals(data.normals);
		
		// Make an array of objects for the texture setting
		float[][] tex_coord = {data.textureCoordinates};
		int[] tex_type = {TriangleStripArray.TEXTURE_COORDINATE_2};
		ta.setTextureCoordinates(tex_type, tex_coord, 1);
		
		return(ta);
	}
	
	private TransformGroup getSphere() 
	{
        SphereGenerator generator = new SphereGenerator(2, 64);
        GeometryData data = new GeometryData();

        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        generator.generate(data);

        TriangleStripArray tsa = new TriangleStripArray(
            false,
            VertexGeometry.VBO_HINT_STATIC);

        tsa.setVertices(
            TriangleStripArray.COORDINATE_3,
            data.coordinates,
            data.vertexCount);

        tsa.setStripCount(data.stripCounts, data.numStrips);
        tsa.setNormals(data.normals);

        float[][] tex_coord = {data.textureCoordinates};
        int[] tex_type = {TriangleStripArray.TEXTURE_COORDINATE_2};
        tsa.setTextureCoordinates(tex_type, tex_coord, 1);

        Material material = new Material();
        material.setDiffuseColor(new float[] { 0, 1, 0 });
        material.setSpecularColor(new float[] { 1, 1, 1 });
		material.setShininess(0.8f);

        String[] vert_shader_txt = loadShaderFile(VTX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(FRAG_SHADER_FILE);

        ShaderObject vert_shader = new ShaderObject(true);
        vert_shader.setSourceStrings(vert_shader_txt, 1);
        vert_shader.requestInfoLog();
        vert_shader.compile();

        ShaderObject frag_shader = new ShaderObject(false);
        frag_shader.setSourceStrings(frag_shader_txt, 1);
        frag_shader.requestInfoLog();
        frag_shader.compile();

        ShaderProgram shader_prog = new ShaderProgram();
        shader_prog.addShaderObject(vert_shader);
        shader_prog.addShaderObject(frag_shader);
        shader_prog.requestInfoLog();
        shader_prog.link();

        ShaderArguments shader_args = new ShaderArguments();
		shader_args.setUniform("use_headlight", 1, new int[]{ENABLE_HEADLIGHT}, 1);
		shader_args.setUniform("has_texture", 1, new int[]{0}, 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shader_args);

        Appearance app = new Appearance();
        app.setMaterial(material);
        app.setShader(shader);

        Shape3D shape = new Shape3D();
        shape.setGeometry(tsa);
        shape.setAppearance(app);

        TransformGroup shape_tx = new TransformGroup();
        shape_tx.addChild(shape);
		
		return(shape_tx);
	}
}
