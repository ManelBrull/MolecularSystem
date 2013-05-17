package chalmers.manel.jps.agents;

import jade.core.Agent;
import java.util.Random;

import chalmers.manel.jps.render.ManagerEnviroment;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
/**
 * Public class for my particles
 * Init is only called one time
 * Update is called every 10 ticks
 * @author Manel Brull
 *
 */
public abstract class basicParticle extends Agent {
	
	protected float xPos;
	protected float yPos;
	
	protected void setup(){
		addBehaviour(new WakerBehaviour(this, 3000) {
			protected void handleElapsedTimeout() {
				init();
				addBehaviour();
			}
		});
	}
	
	private void addBehaviour(){
		addBehaviour(new TickerBehaviour(this, 10){
			@Override
			protected void onTick() {
				update();
			}
		});
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
	
	/**
	 * Initializes the particle 
	 */
	abstract protected void init();
	
	/**
	 * Exectued every 10 ticks
	 */
	abstract protected void update();
	
}
