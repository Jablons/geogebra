package org.geogebra.desktop.geogebra3D.euclidian3D.opengl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.Stack;

import javax.media.opengl.GL;
import javax.media.opengl.fixedfunc.GLLightingFunc;

import org.geogebra.common.geogebra3D.euclidian3D.EuclidianView3D;
import org.geogebra.common.geogebra3D.euclidian3D.draw.DrawPoint3D;
import org.geogebra.common.geogebra3D.euclidian3D.openGL.GLBuffer;
import org.geogebra.common.geogebra3D.euclidian3D.openGL.GLBufferIndices;
import org.geogebra.common.geogebra3D.euclidian3D.openGL.GPUBuffer;
import org.geogebra.common.geogebra3D.euclidian3D.openGL.Manager;
import org.geogebra.common.geogebra3D.euclidian3D.openGL.Manager.Type;
import org.geogebra.common.geogebra3D.euclidian3D.openGL.ManagerShadersElementsGlobalBuffer;
import org.geogebra.common.geogebra3D.euclidian3D.openGL.Renderer;
import org.geogebra.common.geogebra3D.euclidian3D.openGL.RendererImplShaders;
import org.geogebra.common.kernel.Matrix.CoordMatrix4x4;
import org.geogebra.common.kernel.Matrix.Coords;
import org.geogebra.common.main.Feature;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.geogebra3D.euclidian3D.opengl.RendererJogl.GLlocal;
import org.geogebra.desktop.main.AppD;

/**
 * Renderer using shaders
 * 
 * @author mathieu
 *
 */
public class RendererImplShadersD extends RendererImplShaders {

	private RendererJogl jogl;

	/**
	 * Constructor
	 * 
	 * @param renderer
	 *            GL renderer
	 * 
	 * @param view
	 *            view
	 * @param jogl
	 *            java openGL implementation
	 */
	public RendererImplShadersD(Renderer renderer,
			EuclidianView3D view,
			RendererJogl jogl) {
		super(renderer, view);
		this.jogl = jogl;
		Log.debug("============== RendererImplShadersD: Renderer with shaders created (shaders checked ok)");

	}

	private GL getGL() {

		return jogl.getGL2ES2();
	}

	private int shaderProgram;
	private int vertShader;
	private int fragShader;


	// private int normalMatrixLocation;


	private int[] vboHandles;


	private String loadTextFile(String file) {

		return ((AppD) view3D.getApplication())
				.loadTextFile("/org/geogebra/desktop/geogebra3D/euclidian3D/opengl/shaders/" + file
						+ ".txt");
	}

	@Override
	public void initShaders() {

		/*
		 * The initialization below will use the OpenGL ES 2 API directly to
		 * setup the two shader programs that will be run on the GPU.
		 * 
		 * Its recommended to use the jogamp/opengl/util/glsl/ classes import
		 * com.jogamp.opengl.util.glsl.ShaderCode; import
		 * com.jogamp.opengl.util.glsl.ShaderProgram; import
		 * com.jogamp.opengl.util.glsl.ShaderState; to simplify shader
		 * customization, compile and loading.
		 * 
		 * You may also want to look at the JOGL RedSquareES2 demo
		 * http://jogamp.
		 * org/git/?p=jogl.git;a=blob;f=src/test/com/jogamp/opengl/
		 * test/junit/jogl/demos/es2/RedSquareES2.java;hb=HEAD#l78 to see how
		 * the shader customization, compile and loading is done using the
		 * recommended JogAmp GLSL utility classes.
		 */

		String vertexShaderString, fragmentShaderString;

		if (view3D.getApplication().has(Feature.SHINY_3D)) {
			vertexShaderString = loadTextFile("vertexShaderSpecular");
			fragmentShaderString = loadTextFile("fragmentShaderSpecular");
		} else {
			vertexShaderString = loadTextFile("vertexShader");
			fragmentShaderString = loadTextFile("fragmentShader");
		}


		// Make the shader strings compatible with OpenGL 3 core if needed
		// GL2ES2 also includes the intersection of GL3 core
		// The default implicit GLSL version 1.1 is now depricated in GL3 core
		// GLSL 1.3 is the minimum version that now has to be explicitly set.
		// This allows the shaders to compile using the latest
		// desktop OpenGL 3 and 4 drivers.
		if (jogl.getGL2ES2().isGL3core()) {
			Log.debug("GL3 core detected: explicit add #version 130 to shaders");
			vertexShaderString = "#version 130\n" + vertexShaderString;
			fragmentShaderString = "#version 130\n" + fragmentShaderString;
		}

		// Create GPU shader handles
		// OpenGL ES retuns a index id to be stored for future reference.
		vertShader = jogl.getGL2ES2().glCreateShader(
				javax.media.opengl.GL2ES2.GL_VERTEX_SHADER);
		fragShader = jogl.getGL2ES2().glCreateShader(
				javax.media.opengl.GL2ES2.GL_FRAGMENT_SHADER);

		// Compile the vertexShader String into a program.
		String[] vlines = new String[] { vertexShaderString };

		// for (int i = 0; i < vlines.length; i++)
		// System.out.println(vlines[i]);

		int[] vlengths = new int[] { vlines[0].length() };
		jogl.getGL2ES2().glShaderSource(vertShader, vlines.length, vlines,
				vlengths, 0);
		jogl.getGL2ES2().glCompileShader(vertShader);

		// Check compile status.
		int[] compiled = new int[1];
		jogl.getGL2ES2().glGetShaderiv(vertShader,
				javax.media.opengl.GL2ES2.GL_COMPILE_STATUS,
				compiled, 0);
		if (compiled[0] != 0) {
			Log.debug("Vertex shader compiled");
		} else {
			int[] logLength = new int[1];
			jogl.getGL2ES2().glGetShaderiv(vertShader,
					javax.media.opengl.GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0);

			byte[] log = new byte[logLength[0]];
			jogl.getGL2ES2().glGetShaderInfoLog(vertShader, logLength[0],
					(int[]) null, 0, log, 0);

			Log.error("Error compiling the vertex shader: "
					+ new String(log));
			System.exit(1);
		}

		// Compile the fragmentShader String into a program.
		String[] flines = new String[] { fragmentShaderString };
		int[] flengths = new int[] { flines[0].length() };
		jogl.getGL2ES2().glShaderSource(fragShader, flines.length, flines,
				flengths, 0);
		jogl.getGL2ES2().glCompileShader(fragShader);

		// Check compile status.
		jogl.getGL2ES2().glGetShaderiv(fragShader,
				javax.media.opengl.GL2ES2.GL_COMPILE_STATUS,
				compiled, 0);
		if (compiled[0] != 0) {
			Log.debug("Fragment shader compiled");
		} else {
			int[] logLength = new int[1];
			jogl.getGL2ES2().glGetShaderiv(fragShader,
					javax.media.opengl.GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0);

			byte[] log = new byte[logLength[0]];
			jogl.getGL2ES2().glGetShaderInfoLog(fragShader, logLength[0],
					(int[]) null, 0, log, 0);

			Log.error("Error compiling the fragment shader: "
					+ new String(log));
			System.exit(1);
		}

		// Each shaderProgram must have
		// one vertex shader and one fragment shader.
		shaderProgram = jogl.getGL2ES2().glCreateProgram();
		jogl.getGL2ES2().glAttachShader(shaderProgram, vertShader);
		jogl.getGL2ES2().glAttachShader(shaderProgram, fragShader);

		// Associate attribute ids with the attribute names inside
		// the vertex shader.
		GLSL_ATTRIB_POSITION = 0;
		GLSL_ATTRIB_COLOR = 1;
		GLSL_ATTRIB_NORMAL = 2;
		GLSL_ATTRIB_TEXTURE = 3;
		GLSL_ATTRIB_INDEX = 4;

		jogl.getGL2ES2().glBindAttribLocation(shaderProgram,
				GLSL_ATTRIB_POSITION, "attribute_Position");
		jogl.getGL2ES2().glBindAttribLocation(shaderProgram, GLSL_ATTRIB_COLOR,
				"attribute_Color");
		jogl.getGL2ES2().glBindAttribLocation(shaderProgram,
				GLSL_ATTRIB_NORMAL, "attribute_Normal");
		jogl.getGL2ES2().glBindAttribLocation(shaderProgram,
				GLSL_ATTRIB_TEXTURE, "attribute_Texture");

		jogl.getGL2ES2().glLinkProgram(shaderProgram);

		// Get a id number to the uniform_Projection matrix
		// so that we can update it.
		matrixLocation = jogl.getGL2ES2().glGetUniformLocation(shaderProgram,
				"matrix");

		// normalMatrixLocation =
		// jogl.getGL2ES2().glGetUniformLocation(shaderProgram, "normalMatrix");
		lightPositionLocation = jogl.getGL2ES2().glGetUniformLocation(
				shaderProgram, "lightPosition");
		ambiantDiffuseLocation = jogl.getGL2ES2().glGetUniformLocation(
				shaderProgram, "ambiantDiffuse");
		eyePositionLocation = jogl.getGL2ES2().glGetUniformLocation(
				shaderProgram, "eyePosition");
		enableLightLocation = jogl.getGL2ES2().glGetUniformLocation(
				shaderProgram, "enableLight");
		if (view3D.getApplication().has(Feature.SHINY_3D)) {
			enableShineLocation = jogl.getGL2ES2().glGetUniformLocation(
					shaderProgram, "enableShine");
		}

		cullingLocation = jogl.getGL2ES2().glGetUniformLocation(shaderProgram,
				"culling");

		dashValuesLocation = jogl.getGL2ES2().glGetUniformLocation(
				shaderProgram, "dashValues");

		// texture
		textureTypeLocation = jogl.getGL2ES2().glGetUniformLocation(
				shaderProgram, "textureType");

		// color
		colorLocation = jogl.getGL2ES2().glGetUniformLocation(shaderProgram,
				"color");

		// normal
		normalLocation = jogl.getGL2ES2().glGetUniformLocation(shaderProgram,
				"normal");

		// center
		centerLocation = jogl.getGL2ES2().glGetUniformLocation(shaderProgram,
				"center");

		// clip planes
		enableClipPlanesLocation = jogl.getGL2ES2().glGetUniformLocation(
				shaderProgram, "enableClipPlanes");
		clipPlanesMinLocation = jogl.getGL2ES2().glGetUniformLocation(
				shaderProgram, "clipPlanesMin");
		clipPlanesMaxLocation = jogl.getGL2ES2().glGetUniformLocation(
				shaderProgram, "clipPlanesMax");

		// label rendering
		labelRenderingLocation = jogl.getGL2ES2().glGetUniformLocation(
				shaderProgram, "labelRendering");
		labelOriginLocation = jogl.getGL2ES2().glGetUniformLocation(
				shaderProgram, "labelOrigin");

		/*
		 * GL2ES2 also includes the intersection of GL3 core GL3 core and later
		 * mandates that a "Vector Buffer Object" must be created and bound
		 * before calls such as gl.glDrawArrays is used. The VBO lines in this
		 * demo makes the code forward compatible with OpenGL 3 and ES 3 core
		 * and later where a default vector buffer object is deprecated.
		 * 
		 * Generate two VBO pointers / handles VBO is data buffers stored inside
		 * the graphics card memory.
		 */
		vboHandles = new int[5];
		jogl.getGL2ES2().glGenBuffers(5, vboHandles, 0);

		vboColors = new GPUBufferD();
		vboVertices = new GPUBufferD();
		vboNormals = new GPUBufferD();
		vboTextureCoords = new GPUBufferD();
		vboIndices = new GPUBufferD();
		((GPUBufferD) vboColors).set(vboHandles[GLSL_ATTRIB_COLOR]);
		((GPUBufferD) vboVertices).set(vboHandles[GLSL_ATTRIB_POSITION]);
		((GPUBufferD) vboNormals).set(vboHandles[GLSL_ATTRIB_NORMAL]);
		((GPUBufferD) vboTextureCoords).set(vboHandles[GLSL_ATTRIB_TEXTURE]);
		((GPUBufferD) vboIndices).set(vboHandles[GLSL_ATTRIB_INDEX]);

		attribPointers();

	}


	@Override
	protected void createBuffer(GPUBuffer buffer, Stack<Integer> stack) {
		if (stack.isEmpty()) {
			int[] b = new int[1];
			jogl.getGL2ES2().glGenBuffers(1, b, 0);
			((GPUBufferD) buffer).set(b[0]);
		} else {
			((GPUBufferD) buffer).set(stack.pop());
		}

	}

	@Override
	protected void removeBuffer(GPUBuffer buffer, Stack<Integer> stack) {
		stack.push(((GPUBufferD) buffer).get());
	}



	@Override
	final protected int getStoreBufferNumBytes(int length, int size) {
		return length * size * 4; // 4 bytes per float
	}

	@Override
	public void storeElementBuffer(short[] fb, int length,
			GPUBuffer buffers) {
		// Select the VBO, GPU memory data
		bindBufferForIndices(buffers);

		// transfer data to VBO, this perform the copy of data from CPU -> GPU
		// memory
		jogl.getGL2ES2().glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, length * 2,
				ShortBuffer.wrap(fb), RendererJogl.GL_STREAM_DRAW);

	}


	@Override
	final protected void bindBuffer(int bufferType, GPUBuffer buffer) {
		jogl.getGL2ES2().glBindBuffer(bufferType, ((GPUBufferD) buffer).get());
	}

	@Override
	final protected int getGL_ELEMENT_ARRAY_BUFFER() {
		return GL.GL_ELEMENT_ARRAY_BUFFER;
	}

	@Override
	final protected int getGL_ARRAY_BUFFER() {
		return GL.GL_ARRAY_BUFFER;
	}

	@Override
	protected void enableAttrib(int attrib) {
		jogl.getGL2ES2().glEnableVertexAttribArray(attrib);
	}

	@Override
	protected void disableAttrib(int attrib) {
		jogl.getGL2ES2().glDisableVertexAttribArray(attrib);
	}

	@Override
	protected void vertexAttribPointer(int attrib, int size) {
		jogl.getGL2ES2().glVertexAttribPointer(attrib, size, GL.GL_FLOAT,
				false, 0, 0);
	}

	@Override
	protected void glUniform3fv(Object location, float[] values) {
		jogl.getGL2ES2().glUniform3fv((Integer) location, 1, values, 0);
	}

	@Override
	protected void glUniform3f(Object location, float x, float y,
			float z){
		jogl.getGL2ES2().glUniform3f((Integer) location, x, y, z);
	}





	@Override
	protected void glEnableVertexAttribArray(int attrib) {
		jogl.getGL2ES2().glEnableVertexAttribArray(attrib);
	}


	@Override
	protected void glBufferData(int numBytes, GLBuffer fb) {
		jogl.getGL2ES2().glBufferData(GL.GL_ARRAY_BUFFER, numBytes,
				((GLBufferD) fb).getBuffer(), RendererJogl.GL_STREAM_DRAW);

	}

	@Override
	protected void glBufferDataIndices(int numBytes, GLBufferIndices arrayI) {
		jogl.getGL2ES2().glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, numBytes,
				((GLBufferIndicesD) arrayI).getBuffer(),
				RendererJogl.GL_STREAM_DRAW);

	}






	/**
	 * attribute vertex pointers
	 */
	private void attribPointers() {

		bindBuffer(vboVertices);
		vertexAttribPointer(GLSL_ATTRIB_POSITION, 3);

		bindBuffer(vboNormals);
		vertexAttribPointer(GLSL_ATTRIB_NORMAL, 3);

		bindBuffer(vboColors);
		vertexAttribPointer(GLSL_ATTRIB_COLOR, 4);

		bindBuffer(vboTextureCoords);
		vertexAttribPointer(GLSL_ATTRIB_TEXTURE, 2);
	}

	@Override
	public void draw(Manager.Type type, int length) {

		jogl.getGL2().glDrawElements(getGLType(type), length,
				GL.GL_UNSIGNED_SHORT, 0);
	}

	/**
	 * 
	 * @param type
	 *            Manager type
	 * @return GL type
	 */
	protected static int getGLType(Type type) {
		switch (type) {
		case TRIANGLE_STRIP:
			return GL.GL_TRIANGLE_STRIP;
		case TRIANGLE_FAN:
			return GL.GL_TRIANGLE_STRIP;
		case TRIANGLES:
			return GL.GL_TRIANGLES;
		case LINE_LOOP:
			return GL.GL_LINE_LOOP;
		case LINE_STRIP:
			return GL.GL_LINE_STRIP;
		}

		return 0;
	}

	private final void setModelViewIdentity() {
		projectionMatrix.getForGL(tmpFloat16);
		jogl.getGL2ES2().glUniformMatrix4fv(matrixLocation, 1, false,
				tmpFloat16, 0);
	}

	@Override
	public void draw() {

		resetOneNormalForAllVertices();
		disableTextures();

		setModelViewIdentity();

	}

	private boolean objDone = false;

	private void doObj() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					"geogebra3D/test.obj"));
			writer.write("######## CREATED WITH GEOGEBRA ########");

			((ManagerShadersObj) renderer.getGeometryManager())
					.startObjFile(writer);

			// App.debug("=== Creating .OBJ === ");
			renderer.drawable3DLists.drawInObjFormat(renderer);

			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public void useShaderProgram() {
		jogl.getGL2ES2().glUseProgram(shaderProgram);
	}


	private void releaseVBOs() {
		jogl.getGL2ES2().glDisableVertexAttribArray(GLSL_ATTRIB_POSITION); // Allow
																			// release
																			// of
																			// vertex
																			// position
																			// memory
		jogl.getGL2ES2().glDisableVertexAttribArray(GLSL_ATTRIB_COLOR); // Allow
																		// release
																		// of
																		// vertex
																		// color
																		// memory
		jogl.getGL2ES2().glDisableVertexAttribArray(GLSL_ATTRIB_NORMAL); // Allow
																			// release
																			// of
																			// vertex
																			// normal
																			// memory
		jogl.getGL2ES2().glDisableVertexAttribArray(GLSL_ATTRIB_TEXTURE); // Allow
																			// release
																			// of
																			// vertex
																			// texture
																			// memory

		jogl.getGL2ES2().glDeleteBuffers(4, vboHandles, 0); // Release VBO,
															// color and
															// vertices, buffer
															// GPU memory.
	}

	@Override
	public void dispose() {

		jogl.getGL2ES2().glUseProgram(0);
		jogl.getGL2ES2().glDetachShader(shaderProgram, vertShader);
		jogl.getGL2ES2().glDeleteShader(vertShader);
		jogl.getGL2ES2().glDetachShader(shaderProgram, fragShader);
		jogl.getGL2ES2().glDeleteShader(fragShader);
		jogl.getGL2ES2().glDeleteProgram(shaderProgram);
		// System.exit(0);
	}

	@Override
	public void setMatrixView() {

		if (renderer.isExportingImageEquirectangular()) {
			tmpMatrix2.set(view3D.getToScreenMatrix());
			tmpMatrix2.set(3, 4,
					tmpMatrix2.get(3, 4) + renderer.getEyeToScreenDistance());
			tmpMatrix1.setMul(projectionMatrix, tmpMatrix2);
		} else {
			tmpMatrix1.setMul(projectionMatrix, view3D.getToScreenMatrix());
		}

		tmpMatrix1.getForGL(tmpFloat16);

		jogl.getGL2ES2().glUniformMatrix4fv(matrixLocation, 1, false,
				tmpFloat16, 0);
	}

	@Override
	public void unsetMatrixView() {
		setModelViewIdentity();
	}


	@Override
	public void setColor(float r, float g, float b, float a) {
		jogl.getGL2ES2().glUniform4f(colorLocation, r, g, b, a);
	}

	private float[] tmpFloat16 = new float[16];

	@Override
	public void initMatrix() {

		if (renderer.isExportingImageEquirectangular()) {
			tmpMatrix1.set(view3D.getToScreenMatrix());
			tmpMatrix1.set(3, 4,
					tmpMatrix1.get(3, 4) + renderer.getEyeToScreenDistance());
			tmpMatrix2.setMul(tmpMatrix1, renderer.getMatrix());
		} else {
			tmpMatrix2.setMul(view3D.getToScreenMatrix(), renderer.getMatrix());
		}

		tmpMatrix1.setMul(projectionMatrix, tmpMatrix2);
		tmpMatrix1.getForGL(tmpFloat16);

		jogl.getGL2ES2().glUniformMatrix4fv(matrixLocation, 1, false,
				tmpFloat16, 0);
	}

	@Override
	public void initMatrixForFaceToScreen() {

		tmpMatrix1.setMul(projectionMatrix, renderer.getMatrix());
		tmpMatrix1.getForGL(tmpFloat16);

		jogl.getGL2ES2().glUniformMatrix4fv(matrixLocation, 1, false,
				tmpFloat16, 0);
	}

	@Override
	public void resetMatrix() {
		setMatrixView();
	}



	@Override
	public void pushSceneMatrix() {
		// TODO Auto-generated method stub

	}



	@Override
	public void glLoadName(int loop) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLightPosition(float[] values) {
		jogl.getGL2ES2().glUniform3fv(lightPositionLocation, 1, values, 0);
		if (view3D.getMode() == EuclidianView3D.PROJECTION_PERSPECTIVE
				|| view3D.getMode() == EuclidianView3D.PROJECTION_PERSPECTIVE) {
			jogl.getGL2ES2().glUniform4fv(eyePositionLocation, 1,
					view3D.getViewDirection().get4ForGL(), 0);
		} else {
			jogl.getGL2ES2().glUniform4fv(eyePositionLocation, 1,
					view3D.getEyePosition().get4ForGL(), 0);
		}
	}

	private float[][] ambiantDiffuse;

	@Override
	public void setLightAmbiantDiffuse(float ambiant0, float diffuse0,
			float ambiant1, float diffuse1) {

		float coeff = 1.414f;

		float a0 = ambiant0 * coeff;
		float d0 = 1 - a0;
		float a1 = ambiant1 * coeff;
		float d1 = 1 - a1;

		ambiantDiffuse = new float[][] { { a0, d0 }, { a1, d1 } };

	}

	@Override
	public void setLight(int light) {

		jogl.getGL2ES2().glUniform2fv(ambiantDiffuseLocation, 1,
				ambiantDiffuse[light], 0);
	}

	@Override
	public void setColorMaterial() {
		getGL().glEnable(GLLightingFunc.GL_COLOR_MATERIAL);

	}

	@Override
	public void setLightModel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAlphaFunc() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setView() {

		renderer.setProjectionMatrix();

		jogl.getGL2ES2().glViewport(0, 0, renderer.getWidth(),
				renderer.getHeight());

	}
	

	private CoordMatrix4x4 projectionMatrix = new CoordMatrix4x4();

	private CoordMatrix4x4 tmpMatrix1 = new CoordMatrix4x4(),
			tmpMatrix2 = new CoordMatrix4x4();

	@Override
	public void viewOrtho() {
		// the projection matrix is updated in updateOrthoValues()
	}

	@Override
	final public void updateOrthoValues() {

		projectionMatrix.set(1, 1, 2.0 / renderer.getWidth());
		projectionMatrix.set(2, 2, 2.0 / renderer.getHeight());
		projectionMatrix.set(3, 3, -2.0 / renderer.getVisibleDepth());
		projectionMatrix.set(4, 4, 1);

		projectionMatrix.set(2, 1, 0);
		projectionMatrix.set(3, 1, 0);
		projectionMatrix.set(4, 1, 0);

		projectionMatrix.set(1, 2, 0);
		projectionMatrix.set(3, 2, 0);
		projectionMatrix.set(4, 2, 0);

		projectionMatrix.set(1, 3, 0);
		projectionMatrix.set(2, 3, 0);
		projectionMatrix.set(4, 3, 0);

		projectionMatrix.set(1, 4, 0);
		projectionMatrix.set(2, 4, 0);
		projectionMatrix.set(3, 4, 0);

	}

	@Override
	public void viewPersp() {
		// the projection matrix is updated in updatePerspValues()

	}

	@Override
	public void updatePerspValues() {

		projectionMatrix
				.set(1,
						1,
						2
								* renderer.perspNear[renderer.eye]
								/ (renderer.perspRight[renderer.eye] - renderer.perspLeft[renderer.eye]));
		projectionMatrix.set(2, 1, 0);
		projectionMatrix.set(3, 1, 0);
		projectionMatrix.set(4, 1, 0);

		projectionMatrix.set(1, 2, 0);
		projectionMatrix
				.set(2,
						2,
						2
								* renderer.perspNear[renderer.eye]
								/ (renderer.perspTop[renderer.eye] - renderer.perspBottom[renderer.eye]));
		projectionMatrix.set(3, 2, 0);
		projectionMatrix.set(4, 2, 0);

		perspXZ = (renderer.perspRight[renderer.eye] + renderer.perspLeft[renderer.eye])
				/ (renderer.perspRight[renderer.eye] - renderer.perspLeft[renderer.eye]);

		projectionMatrix.set(1, 3, perspXZ);
		projectionMatrix
				.set(2,
						3,
						(renderer.perspTop[renderer.eye] + renderer.perspBottom[renderer.eye])
								/ (renderer.perspTop[renderer.eye] - renderer.perspBottom[renderer.eye]));
		projectionMatrix.set(3, 3, 2 * renderer.perspFocus[renderer.eye]
				/ renderer.getVisibleDepth());
		projectionMatrix.set(4, 3, -1);

		projectionMatrix.set(1, 4, 0);// (perspRight+perspLeft)/(perspRight-perspLeft)
										// * perspFocus);
		projectionMatrix.set(2, 4, 0);// (perspTop+perspBottom)/(perspTop-perspBottom)
										// * perspFocus);
		projectionMatrix.set(3, 4, renderer.getVisibleDepth() / 2);
		projectionMatrix.set(4, 4, -renderer.perspFocus[renderer.eye]);

	}

	private double perspXZ, glassesXZ;

	@Override
	public void updateGlassesValues() {
		glassesXZ = (renderer.perspNear[renderer.eye]
				* (renderer.glassesEyeX[Renderer.EYE_LEFT] - renderer.glassesEyeX[Renderer.EYE_RIGHT]) / renderer.perspFocus[renderer.eye])
				/ (renderer.perspRight[renderer.eye] - renderer.perspLeft[renderer.eye]);
	}

	@Override
	public void viewGlasses() {

		if (renderer.eye == Renderer.EYE_LEFT) {
			projectionMatrix.set(1, 3, perspXZ + glassesXZ);
		} else {
			projectionMatrix.set(1, 3, perspXZ - glassesXZ);
		}

	}

	@Override
	public void viewOblique() {
		// the projection matrix is updated in updateProjectionObliqueValues()
	}

	@Override
	public void updateProjectionObliqueValues() {

		projectionMatrix.set(1, 1, 2.0 / renderer.getWidth());
		projectionMatrix.set(2, 1, 0);
		projectionMatrix.set(3, 1, 0);
		projectionMatrix.set(4, 1, 0);

		projectionMatrix.set(1, 2, 0);
		projectionMatrix.set(2, 2, 2.0 / renderer.getHeight());
		projectionMatrix.set(3, 2, 0);
		projectionMatrix.set(4, 2, 0);

		projectionMatrix.set(1, 3,
				renderer.obliqueX * 2.0 / renderer.getWidth());
		projectionMatrix.set(2, 3,
				renderer.obliqueY * 2.0 / renderer.getHeight());
		projectionMatrix.set(3, 3, -2.0 / renderer.getVisibleDepth());
		projectionMatrix.set(4, 3, 0);

		projectionMatrix.set(1, 4, 0);
		projectionMatrix.set(2, 4, 0);
		projectionMatrix.set(3, 4, 0);
		projectionMatrix.set(4, 4, 1);

	}

	@Override
	public void setStencilLines() {
		// TODO Auto-generated method stub

	}

	@Override
	public Manager createManager() {
		return new ManagerShadersElementsGlobalBuffer(renderer, view3D);
	}


	@Override
	protected void glUniform1i(Object location, int value) {
		jogl.getGL2ES2().glUniform1i((Integer) location, value);
	}

	@Override
	protected void glUniform1fv(Object location, int length, float[] values) {
		jogl.getGL2ES2().glUniform1fv((Integer) location, length, values, 0);
	}

	@Override
	public float[] getLightPosition() {
		return Renderer.LIGHT_POSITION_D;
	}





	private float[] clipPlanesMin = new float[3];
	private float[] clipPlanesMax = new float[3];

	@Override
	public void setClipPlanes(double[][] minMax) {
		for (int i = 0; i < 3; i++) {
			clipPlanesMin[i] = (float) minMax[i][0];
			clipPlanesMax[i] = (float) minMax[i][1];
		}

	}

	private void setClipPlanesToShader() {

		jogl.getGL2ES2().glUniform3fv(clipPlanesMinLocation, 1, clipPlanesMin,
				0);
		jogl.getGL2ES2().glUniform3fv(clipPlanesMaxLocation, 1, clipPlanesMax,
				0);

	}

	@Override
	public void initRenderingValues() {

		// clip planes
		setClipPlanesToShader();
	}

	@Override
	public void drawFaceToScreenAbove() {
		jogl.getGL2ES2().glUniform1i(labelRenderingLocation, 1);
		resetCenter();
	}
	
	@Override
	public void drawFaceToScreenBelow() {
		jogl.getGL2ES2().glUniform1i(labelRenderingLocation, 0);
	}

	@Override
	public void setLabelOrigin(Coords origin) {
		jogl.getGL2ES2().glUniform3fv(labelOriginLocation, 1,
				origin.get3ForGL(), 0);
	}


	@Override
	public void enableLighting() {
		if (view3D.getUseLight()){
			jogl.getGL2ES2().glUniform1i(enableLightLocation, 1);
		}
	}
	
	@Override
	public void initLighting() {
		if (view3D.getUseLight()) {
			jogl.getGL2ES2().glUniform1i(enableLightLocation, 1);
		} else {
			jogl.getGL2ES2().glUniform1i(enableLightLocation, 0);
		}
		if (view3D.getApplication().has(Feature.SHINY_3D)) {
			jogl.getGL2ES2().glUniform1i(enableShineLocation, 0);
		}
	}

	@Override
	public void disableLighting() {
		if (view3D.getUseLight()){
			jogl.getGL2ES2().glUniform1i(enableLightLocation, 0);
		}
	}

	@Override
	public void disableShine() {
		if (view3D.getApplication().has(Feature.SHINY_3D)) {
			if (view3D.getUseLight()) {
				jogl.getGL2ES2().glUniform1i(enableShineLocation, 0);
			}
		}
	}

	@Override
	public void enableShine() {
		if (view3D.getApplication().has(Feature.SHINY_3D)) {
			if (view3D.getUseLight()) {
				jogl.getGL2ES2().glUniform1i(enableShineLocation, 1);
			}
		}
	}



	@Override
	public void setCenter(Coords center) {
		float[] c = center.get4ForGL();
		// set radius info
		c[3] *= DrawPoint3D.DRAW_POINT_FACTOR / view3D.getScale();
		jogl.getGL2ES2().glUniform4fv(centerLocation, 1, c, 0);
	}

	private float[] resetCenter = { 0f, 0f, 0f, 0f };

	@Override
	public void resetCenter() {
		jogl.getGL2ES2().glUniform4fv(centerLocation, 1, resetCenter, 0);
	}

	@Override
	public void disableCulling() {
		glDisable(getGL_CULL_FACE());
		jogl.getGL2ES2().glUniform1i(cullingLocation, 1);
	}

	@Override
	public void setCullFaceFront() {
		getGL().glCullFace(GLlocal.GL_FRONT);
		jogl.getGL2ES2().glUniform1i(cullingLocation, -1);
	}

	@Override
	public void setCullFaceBack() {
		getGL().glCullFace(GLlocal.GL_BACK);
		jogl.getGL2ES2().glUniform1i(cullingLocation, 1);
	}

	@Override
	public void drawTranspNotCurved() {
		renderer.enableCulling();
		renderer.setCullFaceFront();
		renderer.drawable3DLists.drawTransp(renderer);
		renderer.drawable3DLists.drawTranspClosedNotCurved(renderer);
		renderer.setCullFaceBack();
		renderer.drawable3DLists.drawTransp(renderer);
		renderer.drawable3DLists.drawTranspClosedNotCurved(renderer);

	}

	@Override
	public void enableLightingOnInit() {
		// no need for shaders
	}

	@Override
	public void initCulling() {
		// no need for shaders
	}

	@Override
	public boolean useShaders() {
		return true;
	}

	@Override
	public void setBufferLeft() {
		jogl.getGL2().glDrawBuffer(GLlocal.GL_BACK_LEFT);
		// zspace seems to be swapped
		// jogl.getGL2().glDrawBuffer(GLlocal.GL_BACK_RIGHT);
	}

	@Override
	public void setBufferRight() {
		jogl.getGL2().glDrawBuffer(GLlocal.GL_BACK_RIGHT);
		// zspace seems to be swapped
		// jogl.getGL2().glDrawBuffer(GLlocal.GL_BACK_LEFT);
	}

	@Override
	public void setStencilFunc(int value) {
		getGL().glStencilFunc(GLlocal.GL_EQUAL, value, 0xFF);
	}

	@Override
	public void enableDepthMask() {
		getGL().glDepthMask(true);
	}

	@Override
	public void disableDepthMask() {
		getGL().glDepthMask(false);
	}

	@Override
	public void setColorMask(boolean r, boolean g, boolean b, boolean a) {
		getGL().glColorMask(r, g, b, a);
	}

	@Override
	public void setClearColor(float r, float g, float b, float a) {
		getGL().glClearColor(r, g, b, a);
	}

	@Override
	public void setPolygonOffset(float factor, float units) {
		getGL().glPolygonOffset(factor, units);
	}

	@Override
	public void genTextures2D(int number, int[] index) {
		getGL().glGenTextures(number, index, 0);
	}

	@Override
	public void bindTexture(int index) {
		getGL().glBindTexture(GL.GL_TEXTURE_2D, index);
	}

	@Override
	public void glEnable(int flag) {
		getGL().glEnable(flag);
	}

	@Override
	public void glDisable(int flag) {
		getGL().glDisable(flag);
	}

	@Override
	public int getGL_ALPHA_TEST() {
		return GLlocal.GL_ALPHA_TEST;
	}

	@Override
	public int getGL_MULTISAMPLE() {
		return GLlocal.GL_MULTISAMPLE;
	}

	@Override
	public int getGL_BLEND() {
		return GLlocal.GL_BLEND;
	}

	@Override
	public int getGL_CULL_FACE() {
		return GLlocal.GL_CULL_FACE;
	}

	@Override
	public void glClear(int flag) {
		getGL().glClear(flag);
	}

	@Override
	public int getGL_COLOR_BUFFER_BIT() {
		return GLlocal.GL_COLOR_BUFFER_BIT;
	}

	@Override
	public int getGL_DEPTH_BUFFER_BIT() {
		return GLlocal.GL_DEPTH_BUFFER_BIT;
	}

	@Override
	public int getGL_DEPTH_TEST() {
		return GLlocal.GL_DEPTH_TEST;
	}

	@Override
	public int getGL_TEXTURE_2D() {
		return GL.GL_TEXTURE_2D;
	}

}