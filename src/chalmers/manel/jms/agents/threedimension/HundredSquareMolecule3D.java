package chalmers.manel.jms.agents.threedimension;

import chalmers.manel.jms.render.ManagerEnviroment3D;

public class HundredSquareMolecule3D extends HundredBasicMolecule3D {

	//Size of the edge
	protected float size = 4.0f;
	
	public HundredSquareMolecule3D(int number, long timer, long cycle) {
		super(number, timer, cycle);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void init(){
		for(int i = 0; i < 100; i++){
			float posi[] = new float[3];
			posi = validInitialPosition();
			xPos[i] = posi[0];
			yPos[i] = posi[1];
			zPos[i] = posi[2];
			
			//This shall be deleted in the future
			ManagerEnviroment3D.sizeMolecule[this.id*100+i] = size;
		}
	}

	@Override
	protected void update(){
		for(int i = 0; i < 100; i++){
			float xMov = 0.0f;
			float yMov = 0.0f;
			float zMov = -0.01f;
			xPos[i] += xMov;
			yPos[i] += yMov;
			zPos[i] += zMov;
	}
}
}
