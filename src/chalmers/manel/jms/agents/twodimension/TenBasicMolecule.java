package chalmers.manel.jms.agents.twodimension;

import java.util.Random;

import chalmers.manel.jms.render.ManagerEnviroment;

public abstract class TenBasicMolecule extends Thread {
	//Identifier of the agent. Needed to communicate with the render
	protected int id;
	//Update time
	protected long initialDelay;
	protected long updateDelay;
	//Position of the molecule
	protected float[] xPos = new float[10];
	protected float[] yPos = new float[10];

	/**
	 * 
	 * @param number Identifier of the group of molecule.
	 * @param timer Milisecons before init
	 * @param cycle Update time in miliseconds
	 */
	public TenBasicMolecule(int number, long timer, long cycle){
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
	
	/**
	 * Return if the number molecule reach the wall
	 * 
	 * @param number
	 * @return
	 */
	protected boolean wallReached(int number){
		int x = (int) (xPos[number]/ManagerEnviroment.myMap.getSizeTile());
		int y = (int) (yPos[number]/ManagerEnviroment.myMap.getSizeTile());
		return !ManagerEnviroment.myMap.getCanWalk(x, y);
	}

	protected void sendPositionToEnviroment(){
		for(int i = 0; i < 10; i++){
			ManagerEnviroment.xPosAgent[this.id*10+i] = xPos[i];
			ManagerEnviroment.yPosAgent[this.id*10+i] = yPos[i];
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
