package multiagent.lab1.part1.practice.behaviour;

import jade.core.behaviours.Behaviour;

public class MyGeneric extends Behaviour {
	private int counter = 0;
	private int step = 0;

	public void action() {
		System.out.println("Generic behaviour: counter=" + counter);
		if (step % 3 == 0) {
			counter += 3;
			step++;
		} else if (step % 3 == 1) {
			counter *= counter;
			step++;
		} else {
			counter /= 2;
			step++;
		}
	}

	public boolean done() {
		return counter >= 16;
	}
}
