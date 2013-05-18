package chalmers.manel.jps.agents;

import java.util.Random;

import chalmers.manel.jps.render.ManagerEnviroment;
/**
 * Public class for my particles
 * Init is only called one time
 * Update is called every 10 ticks
 * @author Manel Brull
 *
 */
public abstract class BasicMolecule extends Thread {
	//Identifier of the agent. Needed to communicate with the render
	protected int id;
	//Update time
	protected long initialDelay;
	protected long updateDelay;
	//Position of the molecule
	protected float xPos;
	protected float yPos;
	/**
	 * 
	 * @param number Identifier of the molecule. It should be unique
	 * @param timer Milisecons before init
	 * @param cycle Update time in miliseconds
	 */
	public BasicMolecule(int number, long timer, long cycle){
		this.id = number;
		this.initialDelay = timer;
		this.updateDelay = cycle;
	}
	
	public void run(){
		try {
			sleep(this.initialDelay);
			init();
			sendPositionToEnviroment();
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
		float[] ret = new float[2];
		Random rnd = new Random();
		float x = Math.abs(rnd.nextFloat())*ManagerEnviroment.myMap.getWidthPixels();
		float y = Math.abs(rnd.nextFloat())*ManagerEnviroment.myMap.getHeightPixels();
		while(!ManagerEnviroment.myMap.getCanWalkPixels(x, y)){
			x = Math.abs(rnd.nextFloat())*ManagerEnviroment.myMap.getWidthPixels();
			y = Math.abs(rnd.nextFloat())*ManagerEnviroment.myMap.getHeightPixels();
		}
		ret[0] = x;
		ret[1] = y;
		return ret;
	}
	
	protected boolean wallReached(){
		int x = (int) (xPos/ManagerEnviroment.myMap.getSizeTile());
		int y = (int) (yPos/ManagerEnviroment.myMap.getSizeTile());
		return !ManagerEnviroment.myMap.getCanWalk(x, y);
	}
	
	protected void sendPositionToEnviroment(){
		ManagerEnviroment.xPosAgent[this.id] = xPos;
		ManagerEnviroment.yPosAgent[this.id] = yPos;
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
