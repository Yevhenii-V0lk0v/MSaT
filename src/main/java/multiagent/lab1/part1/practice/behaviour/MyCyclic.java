package multiagent.lab1.part1.practice.behaviour;

import jade.core.behaviours.CyclicBehaviour;

public class MyCyclic extends CyclicBehaviour {
	public void action() {
		System.out.println("This message will be sent evey second");
		this.block(1000);
	}
}
