package chalmers.manel.jps.agents;

import chalmers.manel.jps.render.ManagerEnviroment;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;

public class particle extends basicParticle {
	
	protected float northXPos;
	protected float northYPos;
	
	protected float objXPos;
	protected float objYPos;
	
	protected void setup(){
		super.setup();
	}
	
	@Override
	protected void init(){
		float posi[] = new float[2];
		posi = validInitialPosition();
		this.xPos = posi[0];
		this.yPos = posi[1];
		
		this.northXPos = posi[0];
		this.northYPos = posi[1]-16.0f;
		
		//This shall be deleted in the future
		ManagerEnviroment.xPosAgent[0] = posi[0];
		ManagerEnviroment.yPosAgent[0] = posi[1];
		ManagerEnviroment.sizeAgent[0] = 32.0f;
	}
	
	@Override
	protected void update(){
//		rotate(1);
		float xMov = (northXPos-xPos)/16.0f;
		float yMov = (northYPos-yPos)/16.0f;
		
		this.xPos += xMov;
		this.yPos += yMov;
		
		this.northXPos += xMov;
		this.northYPos += yMov;
		
		if(wallReached()){
			float posi[] = new float[2];
			posi = validInitialPosition();
			this.xPos = posi[0];
			this.yPos = posi[1];
			this.northXPos = posi[0];
			this.northYPos = posi[1]-16.0f;
		}
		//That should be implemented in another method of basic Particle
		ManagerEnviroment.xPosAgent[0] = xPos;
		ManagerEnviroment.yPosAgent[0] = yPos;
	}
	
	public void rotate(float angle){
		angle = (float) ((Math.PI*angle)/180.0);
		float x = (float) (xPos*Math.cos(angle) - yPos*Math.sin(angle));
		float y = (float) (xPos*Math.sin(angle) + yPos*Math.cos(angle));
		xPos = x;
		yPos = y;
	}
}
