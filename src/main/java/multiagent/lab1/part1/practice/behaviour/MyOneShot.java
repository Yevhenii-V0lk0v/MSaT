package multiagent.lab1.part1.practice.behaviour;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class MyOneShot extends OneShotBehaviour {

	public MyOneShot(Agent agent) {
		super(agent);
	}

	public void action() {
		System.out.println("One-shot behaviour of " + getAgent().getName() + " have been performed");
	}
}
