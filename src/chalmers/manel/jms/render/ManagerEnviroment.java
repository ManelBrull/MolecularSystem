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

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import chalmers.manel.jms.agents.twodimension.TenBasicMolecule;
import chalmers.manel.jms.agents.twodimension.TenUniqueMolecule;
import chalmers.manel.jms.exceptions.MapNotFoundInMapsInfoXML;
import chalmers.manel.jms.map.JPSTileMap;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
// GL constants
// GL2 constants


@SuppressWarnings("serial")
public class ManagerEnviroment implements GLEventListener {
	// Define constants for the top-level container
	private static String TITLE = "Tile Map";
	private static int CANVAS_WIDTH = 448;  // width of the drawable
	private static int CANVAS_HEIGHT = 448; // height of the drawable
	private static final int FPS = 60; // animator's target frames per second

	private Texture[] texture; // Place to store the slices of the map
	private int tileSize = 64; // Size of the thile
	
	public static JPSTileMap myMap; //Map with all the information about the map
	
	//Agent information
	private int numAgents;
	public static float xPosAgent[] = null; 
	public static float yPosAgent[] = null;
	public static float sizeAgent[] = null;
	private TenBasicMolecule molecules[] = null;
	
	
	/** The entry main() method to setup the top-level container and animator */
	public static void main(String args[]) {
		// Run the GUI codes in the event-dispatching thread for thread safety
		//Initialize for everything agents need
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Create the OpenGL rendering canvas
				GLCanvas canvas = new GLCanvas();
				canvas.addGLEventListener(new ManagerEnviroment());
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
	// ------ Implement methods declared in GLEventListener ------

	/**
	 * Called back immediately after the OpenGL context is initialized. Can be used 
	 * to perform one-time initialization. Run only once.
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		try {
			this.numAgents = 10000;
			this.xPosAgent = new float[this.numAgents]; 
			this.yPosAgent = new float[this.numAgents];
			this.sizeAgent = new float[this.numAgents];
			this.molecules = new TenUniqueMolecule[this.numAgents/10];
			
			//Load map
			this.myMap = new JPSTileMap(0);
			this.loadSlices(drawable, "maps/twodimension/map_0/", TextureIO.PNG);

			GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context
			glu = new GLU();                         // get GL Utilities
			gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
			gl.glClearDepth(1.0f);      // set clear depth value to farthest
			gl.glEnable(GL_DEPTH_TEST); // enables depth testing
			gl.glEnable(GL_TEXTURE_2D);
			gl.glDepthFunc(GL_LEQUAL);  // the type of depth test to do
			gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best perspective correction
			gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out lighting
			
			//load agents
			for(int i = 0; i < numAgents/10; i++){
				molecules[i] = new TenUniqueMolecule(i, 5, 20);
				molecules[i].start();
			}
		} catch (MapNotFoundInMapsInfoXML e) {
			e.printStackTrace();
		}
	}

	/**
	 * Call-back handler for window re-size event. Also called when the drawable is 
	 * first set to visible.
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context

		if (height == 0) height = 1;   // prevent divide by zero

		// Set the view port (display area) to cover the entire window
		gl.glViewport(0, 0, width, height);

		// Setup perspective projection, with aspect ratio matches viewport
		gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
		gl.glLoadIdentity();             // reset projection matrix
		gl.glOrthof(0.0f, width, height, 0.0f, -1.0f, 1.0f);
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
		gl.glLoadIdentity();  // reset the model-view matrix
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GLU.GLU_FILL);
		
		int actTexture = 0;
		// We have to chek that y is for width
		for(int y = 0; y < myMap.getWidthTiles(); y++){
			for(int x = 0; x < myMap.getHeightTiles(); x++){
				actTexture = myMap.getRenderTiles(y, x);
				texture[actTexture].enable(gl);
				texture[actTexture].bind(gl);
				int pixelX=x*myMap.getSizeTile();
				int pixelY=y*myMap.getSizeTile();
				gl.glBegin(GL_QUADS);
				gl.glTexCoord2f(0.0f,1.0f);
				gl.glVertex2f(pixelX,pixelY);
				gl.glTexCoord2f(1.0f,1.0f);
				gl.glVertex2f(pixelX+tileSize,pixelY);
				gl.glTexCoord2f(1.0f,0.0f);
				gl.glVertex2f(pixelX+tileSize,pixelY+tileSize);
				gl.glTexCoord2f(0.0f,0.0f);
				gl.glVertex2f(pixelX,pixelY+tileSize);
				gl.glEnd();
			}
		}
		
		for(int i = 0; i < this.numAgents; i++){
			float d = (float) (sizeAgent[i]/2.0);
			texture[5].enable(gl);
			texture[5].bind(gl);
			gl.glBegin(GL_QUADS);
			gl.glTexCoord2f(0.0f, 0.0f);
			gl.glVertex2d(xPosAgent[i]-d, yPosAgent[i]-d);
			gl.glTexCoord2f(1.0f, 0.0f);
			gl.glVertex2d(xPosAgent[i]+d, yPosAgent[i]-d);
			gl.glTexCoord2f(1.0f, 1.0f);
			gl.glVertex2d(xPosAgent[i]+d, yPosAgent[i]+d);
			gl.glTexCoord2f(0.0f, 1.0f);
			gl.glVertex2d(xPosAgent[i]-d, yPosAgent[i]+d);
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
	private void loadSlices(GLAutoDrawable drawable, String path, String textureFileType){
		GL2 gl = drawable.getGL().getGL2();
		System.out.println("Num tiles in loadSlices: " + this.myMap.getNumTiles());
		int num = this.myMap.getNumTiles()+2;
		this.texture = new Texture[num];
		for(int i = 0; i < num; i++){
			InputStream is = getClass().getClassLoader().getResourceAsStream(path+"tile"+i+"."+textureFileType);
			try {
				this.texture[i] = TextureIO.newTexture(is, false, textureFileType);
				// Use linear filter for texture if image is larger than the original texture
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
				// Use linear filter for texture if image is smaller than the original texture
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			} catch (GLException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}