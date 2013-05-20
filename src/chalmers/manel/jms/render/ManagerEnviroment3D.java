package chalmers.manel.jms.render;

import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LEQUAL;
import static javax.media.opengl.GL.GL_NICEST;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Date;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import chalmers.manel.jms.agents.threedimension.TenBasicMolecule3D;
import chalmers.manel.jms.agents.threedimension.TenSphereMolecule3D;
import chalmers.manel.jms.agents.threedimension.TenSquareMolecule3D;
import chalmers.manel.jms.map.JPSTileMap;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.awt.TextRenderer;

@SuppressWarnings("serial")
public class ManagerEnviroment3D implements GLEventListener {

	/**
	 * Define constants for the top level container
	 */
	/** Title of the application	 **/
	private static String TITLE = "3D Molecule Simulation";
	
	/** Width of the canvas **/
	private static int CANVAS_WIDTH = 800;  
	
	/** Height of the drawable **/
	private static int CANVAS_HEIGHT = 600; 
	
	/**
	 * Define constants of the enviroment
	 */
	/** Information abour the enviroment **/
	public static JPSTileMap myMap; 
	
	/**
	 * Define variables of the involved molecules
	 */
	/** Number of molecules **/
	private int numMolecules = 0;
	
	/** Number of Vertex **/
	private int numVertex = 0;

	/** X Position of the middle of the molecule **/
	public static float[] xPosMolecule = null;
	
	/** Y Position of the middle of the molecule **/
	public static float[] yPosMolecule = null;
	
	/** Z Position of the middle of the molecule **/
	public static float[] zPosMolecule = null;
	
	/** Size of the molecule **/
	public static float[] sizeMolecule = null;
	
	/**
	 * Framered buffer variables
	 */
	/** Index of vertex buffer object. We store interleaved vertex and color data here
     * like this: x0, r0, y0, g0, z0, b0, x1, r1, y1, g1, z1, b1...
     * Stored in an array because glGenBuffers requires it. */
    protected int [] aiVertexBufferIndices = new int [] {-1};
	
	/** Threads to improve the efficiency of our app and controls molecules **/
	private TenBasicMolecule3D molecules[] = null;
	
	/**
	 * FPS Counter
	 */
	/** number of frames **/
	int frameCount = 0;

	/** frames per second **/
	float fps = 0;

	long currentTime = 0;
	long previousTime = 0;
	
	/** The entry main() method to setup the top-level container and animator */
	public static void main(String args[]) {
		// Run the GUI codes in the event-dispatching thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Create the OpenGL rendering canvas
				GLCanvas canvas = new GLCanvas();
				canvas.addGLEventListener(new ManagerEnviroment3D());
				canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

				// Create a animator that drives canvas' display() at the specified FPS. 
//				final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);
				final Animator animator = new Animator(canvas);
				// Create the top-level container
				final JFrame frame = new JFrame(); // Swing's JFrame or AWT's Frame
				frame.getContentPane().add(canvas);
				frame.addWindowListener(new WindowAdapter() {
					@Override 
					public void windowClosing(WindowEvent e) {
						// Use a dedicate thread to run the stop() to ensure that the
						// animator stops before program exits.
						new Thread() {
							@Override 
							public void run() {
								if (animator.isStarted()) animator.stop();
								System.exit(0);
							}
						}.start();
					}
				});
				frame.setTitle(TITLE);
				frame.pack();
				frame.setVisible(true);
				animator.start(); // start the animation loop
			}
		});
	}

	// Setup OpenGL Graphics Renderer
	private GLU glu;  // for the GL Utility
	// ------ Implement methods declared in GLEventListener ------

	/**
	 * Called back immediately after the OpenGL context is initialized. Can be used 
	 * to perform one-time initialization. Run only once.
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		numMolecules = 5000;
		numVertex = numMolecules*4*6;
		xPosMolecule = new float[numMolecules]; 
		yPosMolecule = new float[numMolecules];
		zPosMolecule = new float[numMolecules];
		sizeMolecule = new float[numMolecules];
		molecules = new TenBasicMolecule3D[numMolecules/10];

		GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context
		glu = new GLU();                         // get GL Utilities
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
		gl.glClearDepth(1.0f);      // set clear depth value to farthest
		gl.glEnable(GL_DEPTH_TEST); // enables depth testing
		gl.glDepthFunc(GL_LEQUAL);  // the type of depth test to do
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best perspective correction
		gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out lighting

		initMolecules();
	}

		
	/**
	 * Call-back handler for window re-size event. Also called when the drawable is 
	 * first set to visible.
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		 GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context

	      if (height == 0) height = 1;   // prevent divide by zero
	      float aspect = (float)width / height;

	      // Set the view port (display area) to cover the entire window
	      gl.glViewport(0, 0, width, height);

	      // Setup perspective projection, with aspect ratio matches viewport
	      gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
	      gl.glLoadIdentity();             // reset projection matrix
	      glu.gluPerspective(45.0, aspect, 0.1, 300.0); // fovy, aspect, zNear, zFar

	      // Enable the model-view transform
	      gl.glMatrixMode(GL_MODELVIEW);
	      gl.glLoadIdentity(); // reset
	}

	/**
	 * Called back by the animator to perform rendering.
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color and depth buffers
		
		calculateFPS();
		//render text
		TextRenderer txt = new TextRenderer(new Font("Tahoma", Font.BOLD, 25));
		txt.beginRendering(CANVAS_WIDTH, CANVAS_HEIGHT); 
		txt.setColor(1.0f, 0.0f, 0.0f, 0.8f); // Recuerda RGB son los tres primeros 
		txt.draw("FPS: "+fps, 400, 580); // La cadena y la posicion 
		txt.endRendering(); 
	/*	
		//create vertex buffer
		int [] aiNumOfVertices = createAndFillVertexBufferSquares(gl);
		
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, aiVertexBufferIndices[0]);
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		gl.glVertexPointer( 3, GL.GL_FLOAT, 6 * Buffers.SIZEOF_FLOAT, 0 );
        gl.glColorPointer( 3, GL.GL_FLOAT, 6 * Buffers.SIZEOF_FLOAT, 3 * Buffers.SIZEOF_FLOAT );
        gl.glPolygonMode( GL.GL_FRONT, GL2.GL_FILL );
        gl.glDrawArrays( GL2.GL_QUADS, 0, aiNumOfVertices[0] );
		
        // disable arrays once we're done
        gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
        gl.glDisableClientState( GL2.GL_VERTEX_ARRAY );
        gl.glDisableClientState( GL2.GL_COLOR_ARRAY );
        gl.glDisable( GL2.GL_COLOR_MATERIAL );

		/*
		for(int i = 0; i < numMolecules; i++){
			if(molecules[(int)i/10] instanceof TenSphereMolecule3D){
			renderSphereMolecule(i, gl);	        
		    }

			if(molecules[(int)i/10] instanceof TenSquareMolecule3D){
				renderSquareMolecule(i, gl);
			}
		}
		*/
	}

	/** 
	 * Called back before the OpenGL context is destroyed. Release resource such as buffers. 
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) { }
	
	private void renderSphereMolecule(int mol, GL2 gl){
		// ------ Render a Sphere with texture ------
		gl.glLoadIdentity();  // reset the model-view matrix
		gl.glTranslatef(xPosMolecule[mol], yPosMolecule[mol], zPosMolecule[mol]);
		// Enables this texture's target in the current GL context's state.
        GLUquadric earth = glu.gluNewQuadric();		        
        glu.gluQuadricDrawStyle(earth, GLU.GLU_FILL);
        glu.gluQuadricNormals(earth, GLU.GLU_FLAT);
        glu.gluQuadricOrientation(earth, GLU.GLU_OUTSIDE);
        glu.gluQuadricTexture(earth, true);
        final float radius = sizeMolecule[mol];
        final int slices = 16;
        final int stacks = 16;
        glu.gluSphere(earth, radius, slices, stacks);	
	}
	
	/**
	 * Create and fill the vertex buffer for squares
	 * @param gl
	 * @return
	 */
	private int[] createAndFillVertexBufferSquares(GL2 gl){
		
		int [] aiNumOfVertices = new int[]{numVertex};
		
		if(aiVertexBufferIndices[0] == -1){
			if(!gl.isFunctionAvailable("glGenBuffers")
					||!gl.isFunctionAvailable("glBindBuffer")
					||!gl.isFunctionAvailable("glBufferData")
					||!gl.isFunctionAvailable("glDeleteBuffers")){
				System.out.println("Error, vertex buffer not supported");
			}
			gl.glGenBuffers(1, aiVertexBufferIndices, 0);
			
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, aiVertexBufferIndices[0]);
			
			gl.glBufferData(GL.GL_ARRAY_BUFFER, numVertex*3*2*Buffers.SIZEOF_FLOAT,
					null, GL2.GL_DYNAMIC_DRAW);
		}
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, aiVertexBufferIndices[0]);
		ByteBuffer byteBuffer = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL2.GL_WRITE_ONLY);
		FloatBuffer floatBuffer = byteBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		storeVerticesAndColors(floatBuffer);
	    gl.glUnmapBuffer( GL.GL_ARRAY_BUFFER );
	 
	    return( aiNumOfVertices );
	}
	
	private void storeVerticesAndColors(FloatBuffer floatBuffer){
		for(int mol = 0; mol < numMolecules; mol++){
			float []color = new float[3];
			color[0] = 0.0f;
			color[1] = 0.0f;
			color[2] = 1.0f;
			
			float halfSize = sizeMolecule[mol]/2;
			float xPosMinus1 = xPosMolecule[mol]-halfSize;
			float xPosPlus1 = xPosMolecule[mol]+halfSize;

			float yPosMinus1 = yPosMolecule[mol]-halfSize;
			float yPosPlus1 = yPosMolecule[mol]+halfSize;

			float zPosMinus1 = zPosMolecule[mol]-halfSize;
			float zPosPlus1 = zPosMolecule[mol]+halfSize;

			// Front Face
			floatBuffer.put(xPosMinus1);
			floatBuffer.put(yPosMinus1);
			floatBuffer.put(zPosPlus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosPlus1);
			floatBuffer.put(yPosMinus1);
			floatBuffer.put(zPosPlus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosPlus1);
			floatBuffer.put(yPosPlus1);
			floatBuffer.put(zPosPlus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosMinus1);
			floatBuffer.put(yPosPlus1);
			floatBuffer.put(zPosPlus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			// Back Face
			floatBuffer.put(xPosMinus1);
			floatBuffer.put(yPosMinus1);
			floatBuffer.put(zPosMinus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosMinus1);
			floatBuffer.put(yPosPlus1);
			floatBuffer.put(zPosPlus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosPlus1);
			floatBuffer.put(yPosPlus1);
			floatBuffer.put(zPosPlus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosPlus1);
			floatBuffer.put(yPosMinus1);
			floatBuffer.put(zPosMinus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			//Top Face
			floatBuffer.put(xPosMinus1);
			floatBuffer.put(yPosPlus1);
			floatBuffer.put(zPosMinus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosMinus1);
			floatBuffer.put(yPosPlus1);
			floatBuffer.put(zPosPlus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosPlus1);
			floatBuffer.put(yPosPlus1);
			floatBuffer.put(zPosPlus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosPlus1);
			floatBuffer.put(yPosPlus1);
			floatBuffer.put(zPosMinus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			//Bottom Face
	
			floatBuffer.put(xPosMinus1);
			floatBuffer.put(yPosMinus1);
			floatBuffer.put(zPosMinus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosPlus1);
			floatBuffer.put(yPosMinus1);
			floatBuffer.put(zPosMinus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosPlus1);
			floatBuffer.put(yPosMinus1);
			floatBuffer.put(zPosPlus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosMinus1);
			floatBuffer.put(yPosMinus1);
			floatBuffer.put(zPosPlus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			//Right Face
			floatBuffer.put(xPosPlus1);
			floatBuffer.put(yPosMinus1);
			floatBuffer.put(zPosMinus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosPlus1);
			floatBuffer.put(yPosPlus1);
			floatBuffer.put(zPosMinus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosPlus1);
			floatBuffer.put(yPosPlus1);
			floatBuffer.put(zPosPlus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosPlus1);
			floatBuffer.put(yPosMinus1);
			floatBuffer.put(zPosPlus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			//Left Face
			floatBuffer.put(xPosMinus1);
			floatBuffer.put(yPosMinus1);
			floatBuffer.put(zPosMinus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosMinus1);
			floatBuffer.put(yPosMinus1);
			floatBuffer.put(zPosPlus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosMinus1);
			floatBuffer.put(yPosPlus1);
			floatBuffer.put(zPosPlus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
			
			floatBuffer.put(xPosMinus1);
			floatBuffer.put(yPosPlus1);
			floatBuffer.put(zPosMinus1);
			floatBuffer.put(color[0]);
			floatBuffer.put(color[1]);
			floatBuffer.put(color[2]);
		}
	}
	
	private void renderSquareMolecule(int mol, GL2 gl){
		gl.glLoadIdentity();  // reset the model-view matrix
		// Enables this texture's target in the current GL context's state.
		gl.glBegin(GL_QUADS);

		float halfSize = sizeMolecule[mol]/2;
		float xPosMinus1 = xPosMolecule[mol]-halfSize;
		float xPosPlus1 = xPosMolecule[mol]+halfSize;

		float yPosMinus1 = yPosMolecule[mol]-halfSize;
		float yPosPlus1 = yPosMolecule[mol]+halfSize;

		float zPosMinus1 = zPosMolecule[mol]-halfSize;
		float zPosPlus1 = zPosMolecule[mol]+halfSize;
		
        gl.glColor3f(0.0f, 0.0f, 1.0f); // Blue
		// Front Face
		gl.glVertex3f(xPosMinus1, yPosMinus1, zPosPlus1); // bottom-left of the texture and quad
		gl.glVertex3f(xPosPlus1, yPosMinus1, zPosPlus1);  // bottom-right of the texture and quad
		gl.glVertex3f(xPosPlus1, yPosPlus1, zPosPlus1);   // top-right of the texture and quad
		gl.glVertex3f(xPosMinus1, yPosPlus1, zPosPlus1);  // top-left of the texture and quad

		// Back Face
		gl.glVertex3f(xPosMinus1, yPosMinus1, zPosMinus1);
		gl.glVertex3f(xPosMinus1, yPosPlus1, zPosPlus1);
		gl.glVertex3f(xPosPlus1, yPosPlus1, zPosPlus1);
		gl.glVertex3f(xPosPlus1, yPosMinus1, zPosMinus1);

		// Top Face
		gl.glVertex3f(xPosMinus1, yPosPlus1, zPosMinus1);
		gl.glVertex3f(xPosMinus1, yPosPlus1, zPosPlus1);
		gl.glVertex3f(xPosPlus1, yPosPlus1, zPosPlus1);
		gl.glVertex3f(xPosPlus1, yPosPlus1, zPosMinus1);

		// Bottom Face
		gl.glVertex3f(xPosMinus1, yPosMinus1, zPosMinus1);
		gl.glVertex3f(xPosPlus1, yPosMinus1, zPosMinus1);
		gl.glVertex3f(xPosPlus1, yPosMinus1, zPosPlus1);
		gl.glVertex3f(xPosMinus1, yPosMinus1, zPosPlus1);

		// Right face
		gl.glVertex3f(xPosPlus1, yPosMinus1, zPosMinus1);
		gl.glVertex3f(xPosPlus1, yPosPlus1, zPosMinus1);
		gl.glVertex3f(xPosPlus1, yPosPlus1, zPosPlus1);
		gl.glVertex3f(xPosPlus1, yPosMinus1, zPosPlus1);

		// Left Face
		gl.glVertex3f(xPosMinus1, yPosMinus1, zPosMinus1);
		gl.glVertex3f(xPosMinus1, yPosMinus1, zPosPlus1);
		gl.glVertex3f(xPosMinus1, yPosPlus1, zPosPlus1);
		gl.glVertex3f(xPosMinus1, yPosPlus1, zPosMinus1);

		gl.glEnd();
	}

	/**
	 * Initialize thread molecules
	 */
	private void initMolecules(){
		Random rnd = new Random();
		for(int i = 0; i < numMolecules/10; i++){
			if(rnd.nextFloat() > 0.5f)
				molecules[i] = new TenSquareMolecule3D(i, 100*i, 20);
			else
				molecules[i] = new TenSquareMolecule3D(i, 100*i, 20);
			molecules[i].start();
		}
	}
	
	private void calculateFPS(){
	//  Increase frame count
	    frameCount++;

	    //  Get the number of milliseconds since glutInit called 
	    //  (or first call to glutGet(GLUT ELAPSED TIME)).
	    Date dt = new Date();
	    currentTime = dt.getTime();

	    //  Calculate time passed
	    long timeInterval = currentTime - previousTime;

	    if(timeInterval > 1000)
	    {
	        //  calculate the number of frames per second
	        fps = frameCount / (timeInterval / 1000.0f);

	        //  Set time
	        previousTime = currentTime;

	        //  Reset frame count
	        frameCount = 0;
	    }
	}
	
}
