package multiagent.lab1.practice;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import multiagent.lab1.practice.behaviour.MyCyclic;
import multiagent.lab1.practice.behaviour.MyGeneric;
import multiagent.lab1.practice.behaviour.MyOneShot;

public class MyAgent extends Agent {
	@Override
	protected void setup() {
		addBehaviour(new MyOneShot(this));
		addBehaviour(new MyCyclic());
		addBehaviour(new MyGeneric());
		addBehaviour(new TickerBehaviour(this, 1000) {
			@Override
			protected void onTick() {
				System.out.println("This ticker ticks every second");
			}
		});
		addBehaviour(new WakerBehaviour(this, 4000) {
			@Override
			protected void onWake() {
				System.out.println("This message will be written after 4 sec from start");
			}
		});
	}
}
