package chalmers.manel.jps.agents;

import chalmers.manel.jps.render.ManagerEnviroment;

public class UniqueMolecule extends BasicMolecule {
	
	public UniqueMolecule(int number, long timer, long cycle) {
		super(number, timer, cycle);
		// TODO Auto-generated constructor stub
	}

	protected float northXPos;
	protected float northYPos;
	
	protected float objXPos;
	protected float objYPos;

	protected float size = 8.0f;
	
	@Override
	protected void init(){
		float posi[] = new float[2];
		posi = validInitialPosition();
		this.xPos = posi[0];
		this.yPos = posi[1];
		
		this.northXPos = posi[0];
		this.northYPos = posi[1]-size/2;
		
		//This shall be deleted in the future
		xPos = posi[0];
		yPos = posi[1];
		ManagerEnviroment.sizeAgent[this.id] = size;
	}
	
	@Override
	protected void update(){
//		rotate(1);
		float xMov = (northXPos-xPos)/(size/2);
		float yMov = (northYPos-yPos)/(size/2);
		
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
			this.northYPos = posi[1]-(size/2);
		}
	}
	
	public void rotate(float angle){
		angle = (float) ((Math.PI*angle)/180.0);
		float x = (float) (xPos*Math.cos(angle) - yPos*Math.sin(angle));
		float y = (float) (xPos*Math.sin(angle) + yPos*Math.cos(angle));
		xPos = x;
		yPos = y;
	}
}
