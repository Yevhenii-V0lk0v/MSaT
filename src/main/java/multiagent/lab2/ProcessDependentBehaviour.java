package multiagent.lab2;

import jade.core.behaviours.Behaviour;


/**
 * An abstract behaviour that executes until an agent sets {@link ProcessDependentBehaviour#done} flag is set to
 * {@code true}.
 */
public abstract class ProcessDependentBehaviour extends Behaviour {
	protected boolean done = false;

	@Override
	public boolean done() {
		return done;
	}
}
