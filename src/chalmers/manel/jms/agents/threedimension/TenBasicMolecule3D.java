package chalmers.manel.jms.agents.threedimension;

import java.util.Random;

import chalmers.manel.jms.render.ManagerEnviroment;
import chalmers.manel.jms.render.ManagerEnviroment3D;

public abstract class TenBasicMolecule3D extends Thread {
	//Identifier of the agent. Needed to communicate with the render
	protected int id;
	//Update time
	protected long initialDelay;
	protected long updateDelay;
	//Position of the molecule
	protected float[] xPos = new float[10];
	protected float[] yPos = new float[10];
	protected float[] zPos = new float[10];

	/**
	 * 
	 * @param number Identifier of the group of molecule.
	 * @param timer Milisecons before init
	 * @param cycle Update time in miliseconds
	 */
	public TenBasicMolecule3D(int number, long timer, long cycle){
		this.id = number;
		this.initialDelay = timer;
		this.updateDelay = cycle;
	}

	public void run(){
		try {
			init();
			sendPositionToEnviroment();
			sleep(this.initialDelay);
			while(true){
				sleep(this.updateDelay);
				update();
				sendPositionToEnviroment();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Autogenerate a valid position
	 * It return an array of two size
	 * array[0] = x
	 * array[1] = y
	 * @return
	 */
	protected float[] validInitialPosition(){
		float[] ret = new float[3];
		Random rnd = new Random();
		float x;
		float y;
		float z;
		if(rnd.nextFloat() > 0.49f){
			x = -rnd.nextFloat()*10;
			y = -rnd.nextFloat()*10;
			z = -rnd.nextFloat()*10;
		} else {
			x = rnd.nextFloat()*10;
			y = rnd.nextFloat()*10;
			z = rnd.nextFloat()*10;
		}
		ret[0] = x;
		ret[1] = y;
		ret[2] = z;
		return ret;
	}
	
	/**
	 * Return if the number molecule reach the wall
	 * 
	 * @param number
	 * @return
	 */
	protected boolean wallReached(int number){
		return false;
	}

	protected void sendPositionToEnviroment(){
		for(int i = 0; i < 10; i++){
			ManagerEnviroment3D.xPosMolecule[this.id*10+i] = xPos[i];
			ManagerEnviroment3D.yPosMolecule[this.id*10+i] = yPos[i];
			ManagerEnviroment3D.zPosMolecule[this.id*10+i] = zPos[i];
		}
	}

	/**
	 * Initializes the particle 
	 */
	abstract protected void init();

	/**
	 * Exectued every 10 ticks
	 */
	abstract protected void update();

}
