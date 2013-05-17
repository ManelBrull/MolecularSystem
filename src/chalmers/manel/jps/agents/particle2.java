package chalmers.manel.jps.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import chalmers.manel.jps.render.ManagerEnviroment;

public class particle2 extends basicParticle {
	protected void setup(){
		super.setup();
	}

	@Override
	protected void init() {
		
		ManagerEnviroment.xPosAgent[1] = 400.0f;
		ManagerEnviroment.yPosAgent[1] = 15.0f;
		ManagerEnviroment.sizeAgent[1] = 32.0f;
	}

	@Override
	protected void update() {
		ManagerEnviroment.xPosAgent[1] -= 1.0f;
		ManagerEnviroment.yPosAgent[1] += 1.0f;
	}
}

