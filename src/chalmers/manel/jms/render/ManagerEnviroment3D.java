package chalmers.manel.jms.render;

import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LEQUAL;
import static javax.media.opengl.GL.GL_LINEAR;
import static javax.media.opengl.GL.GL_NICEST;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import chalmers.manel.jms.agents.TenBasicMolecule3D;
import chalmers.manel.jms.agents.TenSquareMolecule3D;
import chalmers.manel.jms.map.JPSTileMap;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureIO;

@SuppressWarnings("serial")
public class ManagerEnviroment3D implements GLEventListener {
	// Define constants for the top-level container
	private static String TITLE = "3D Molecule Simulation";
	private static int CANVAS_WIDTH = 800;  // width of the drawable
	private static int CANVAS_HEIGHT = 600; // height of the drawable
	private static final int FPS = 60; // animator's target frames per second

//	private Texture[] texture; // Place to store the slices of the map
//	private int tileSize = 64; // Size of the thile
	
	public static JPSTileMap myMap; //Map with all the information about the map
	
	//Molecule variables
	private int numMolecules = 0;

	//Positions
	public static float[] xPosMolecule = null;
	public static float[] yPosMolecule = null;
	public static float[] zPosMolecule = null;
	public static float[] sizeMolecule = null;
	
	//Threads
	private TenBasicMolecule3D molecules[] = null;
//  Textures
	private Texture texture1;
	private String textureFileName = "maps/threedimension/map_0/texture0.png";
	private String textureFileType = TextureIO.PNG;
	
	
	// Texture image flips vertically. Shall use TextureCoords class to retrieve the
	// top, bottom, left and right coordinates.
	private float textureTop, textureBottom, textureLeft, textureRight;

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
				final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);

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
	// Rotational angle about the x, y and z axes in degrees
	private static float angleX = 0.0f;
	private static float angleY = 0.0f;
	private static float angleZ = 0.0f;
	// Rotational speed about x, y, z axes in degrees per refresh
	private static float rotateSpeedX = 0.3f;
	private static float rotateSpeedY = 0.2f;
	private static float rotateSpeedZ = 0.4f;
	// ------ Implement methods declared in GLEventListener ------

	/**
	 * Called back immediately after the OpenGL context is initialized. Can be used 
	 * to perform one-time initialization. Run only once.
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		numMolecules = 10000;
		xPosMolecule = new float[numMolecules]; 
		yPosMolecule = new float[numMolecules];
		zPosMolecule = new float[numMolecules];
		sizeMolecule = new float[numMolecules];
		molecules = new TenSquareMolecule3D[numMolecules/10];

		GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context
		glu = new GLU();                         // get GL Utilities
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
		gl.glClearDepth(1.0f);      // set clear depth value to farthest
		gl.glEnable(GL_DEPTH_TEST); // enables depth testing
		gl.glDepthFunc(GL_LEQUAL);  // the type of depth test to do
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best perspective correction
		gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out lighting

		// Load texture from image

		// Create a OpenGL Texture object from (URL, mipmap, file suffix)
		// Use URL so that can read from JAR and disk file.

		InputStream is = getClass().getClassLoader().getResourceAsStream(textureFileName);
		try {
			texture1 = TextureIO.newTexture(is, false, textureFileType);
			// Use linear filter for texture if image is larger than the original texture
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			// Use linear filter for texture if image is smaller than the original texture
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			// Texture image flips vertically. Shall use TextureCoords class to retrieve
			// the top, bottom, left and right coordinates, instead of using 0.0f and 1.0f.
			TextureCoords textureCoords = texture1.getImageTexCoords();
			textureTop = textureCoords.top();
			textureBottom = textureCoords.bottom();
			textureLeft = textureCoords.left();
			textureRight = textureCoords.right();
		} catch (GLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	      glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear, zFar

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

	      // ------ Render a Cube with texture ------
	      for(int i = 0; i < numMolecules; i++){
	    	  gl.glLoadIdentity();  // reset the model-view matrix

	    	  // Enables this texture's target in the current GL context's state.
	    	  texture1.enable(gl); 
	    	  texture1.bind(gl);  
	    	  gl.glBegin(GL_QUADS);

	    	  float halfSize = sizeMolecule[i]/2;
	    	  float xPosMinus1 = xPosMolecule[i]-halfSize;
	    	  float xPosPlus1 = xPosMolecule[i]+halfSize;
	    	  
	    	  float yPosMinus1 = yPosMolecule[i]-halfSize;
	    	  float yPosPlus1 = yPosMolecule[i]+halfSize;
	    	  
	    	  float zPosMinus1 = zPosMolecule[i]-halfSize;
	    	  float zPosPlus1 = zPosMolecule[i]+halfSize;
	    	  
	    	  // Front Face
	    	  gl.glTexCoord2f(textureLeft, textureBottom);
	    	  gl.glVertex3f(xPosMinus1, -1.0f, 1.0f); // bottom-left of the texture and quad
	    	  gl.glTexCoord2f(textureRight, textureBottom);
	    	  gl.glVertex3f(xPosPlus1, -1.0f, 1.0f);  // bottom-right of the texture and quad
	    	  gl.glTexCoord2f(textureRight, textureTop);
	    	  gl.glVertex3f(xPosPlus1, 1.0f, 1.0f);   // top-right of the texture and quad
	    	  gl.glTexCoord2f(textureLeft, textureTop);
	    	  gl.glVertex3f(xPosMinus1, 1.0f, 1.0f);  // top-left of the texture and quad

	    	  // Back Face
	    	  gl.glTexCoord2f(textureRight, textureBottom);
	    	  gl.glVertex3f(xPosMinus1, -1.0f, -1.0f);
	    	  gl.glTexCoord2f(textureRight, textureTop);
	    	  gl.glVertex3f(xPosMinus1, 1.0f, -1.0f);
	    	  gl.glTexCoord2f(textureLeft, textureTop);
	    	  gl.glVertex3f(xPosPlus1, 1.0f, -1.0f);
	    	  gl.glTexCoord2f(textureLeft, textureBottom);
	    	  gl.glVertex3f(xPosPlus1, -1.0f, -1.0f);

	    	  // Top Face
	    	  gl.glTexCoord2f(textureLeft, textureTop);
	    	  gl.glVertex3f(xPosMinus1, 1.0f, -1.0f);
	    	  gl.glTexCoord2f(textureLeft, textureBottom);
	    	  gl.glVertex3f(xPosMinus1, 1.0f, 1.0f);
	    	  gl.glTexCoord2f(textureRight, textureBottom);
	    	  gl.glVertex3f(xPosPlus1, 1.0f, 1.0f);
	    	  gl.glTexCoord2f(textureRight, textureTop);
	    	  gl.glVertex3f(xPosPlus1, 1.0f, -1.0f);

	    	  // Bottom Face
	    	  gl.glTexCoord2f(textureRight, textureTop);
	    	  gl.glVertex3f(xPosMinus1, -1.0f, -1.0f);
	    	  gl.glTexCoord2f(textureLeft, textureTop);
	    	  gl.glVertex3f(xPosPlus1, -1.0f, -1.0f);
	    	  gl.glTexCoord2f(textureLeft, textureBottom);
	    	  gl.glVertex3f(xPosPlus1, -1.0f, 1.0f);
	    	  gl.glTexCoord2f(textureRight, textureBottom);
	    	  gl.glVertex3f(xPosMinus1, -1.0f, 1.0f);

	    	  // Right face
	    	  gl.glTexCoord2f(textureRight, textureBottom);
	    	  gl.glVertex3f(xPosPlus1, -1.0f, -1.0f);
	    	  gl.glTexCoord2f(textureRight, textureTop);
	    	  gl.glVertex3f(xPosPlus1, 1.0f, -1.0f);
	    	  gl.glTexCoord2f(textureLeft, textureTop);
	    	  gl.glVertex3f(xPosPlus1, 1.0f, 1.0f);
	    	  gl.glTexCoord2f(textureLeft, textureBottom);
	    	  gl.glVertex3f(xPosPlus1, -1.0f, 1.0f);

	    	  // Left Face
	    	  gl.glTexCoord2f(textureLeft, textureBottom);
	    	  gl.glVertex3f(xPosMinus1, -1.0f, -1.0f);
	    	  gl.glTexCoord2f(textureRight, textureBottom);
	    	  gl.glVertex3f(xPosMinus1, -1.0f, 1.0f);
	    	  gl.glTexCoord2f(textureRight, textureTop);
	    	  gl.glVertex3f(xPosMinus1, 1.0f, 1.0f);
	    	  gl.glTexCoord2f(textureLeft, textureTop);
	    	  gl.glVertex3f(xPosMinus1, 1.0f, -1.0f);

	    	  gl.glEnd();
	      }
	}

	/** 
	 * Called back before the OpenGL context is destroyed. Release resource such as buffers. 
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) { }
	
	/**
	 * Method for loading slices
	 */
//	private void loadSlices(GLAutoDrawable drawable, String path, String textureFileType){
//		GL2 gl = drawable.getGL().getGL2();
//		System.out.println("Num tiles in loadSlices: " + this.myMap.getNumTiles());
//		int num = this.myMap.getNumTiles()+2;
//		this.texture = new Texture[num];
//		for(int i = 0; i < num; i++){
//			InputStream is = getClass().getClassLoader().getResourceAsStream(path+"tile"+i+"."+textureFileType);
//			try {
//				this.texture[i] = TextureIO.newTexture(is, false, textureFileType);
//				// Use linear filter for texture if image is larger than the original texture
//				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//				// Use linear filter for texture if image is smaller than the original texture
//				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//			} catch (GLException | IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
	/**
	 * Initialize thread molecules
	 */
	private void initMolecules(){
		for(int i = 0; i < numMolecules/10; i++){
			molecules[i] = new TenSquareMolecule3D(i, 100*i, 20);
			molecules[i].start();
		}
	}
	
}
