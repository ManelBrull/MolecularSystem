package chalmers.manel.jps.agents;

import chalmers.manel.jps.render.ManagerEnviroment;

public class TenUniqueMolecule extends TenBasicMolecule {

	protected float[] northXPos = new float[10];
	protected float[] northYPos = new float[10];
	
	protected float size = 8.0f;
	
	int seed = 0;
	
	public TenUniqueMolecule(int number, long timer, long cycle) {
		super(number, timer, cycle);
	}

	@Override
	protected void init(){
		for(int i = 0; i < 10; i++){
			float posi[] = new float[2];
			posi = validInitialPosition();
			this.xPos[i] = posi[0];
			this.yPos[i] = posi[1];

			this.northXPos[i] = posi[0];
			this.northYPos[i] = posi[1]-size/2;

			//This shall be deleted in the future
			ManagerEnviroment.sizeAgent[this.id+i] = size;
			seed++;			
		}
	}

	@Override
	protected void update(){
		for(int i = 0; i < 10; i++){
			float xMov = (northXPos[i]-xPos[i])/(size/2);
			float yMov = (northYPos[i]-yPos[i])/(size/2);

			xPos[i] += xMov;
			yPos[i] += yMov;

			northXPos[i] += xMov;
			northYPos[i] += yMov;

			if(wallReached(i)){
				float posi[] = new float[2];
				posi = validInitialPosition();
				seed++;
				xPos[i] = posi[0];
				yPos[i] = posi[1];
				northXPos[i] = posi[0];
				northYPos[i] = posi[1]-(size/2);
			}
		}
		if(seed > 100000)
			seed = 0;
	}
}
