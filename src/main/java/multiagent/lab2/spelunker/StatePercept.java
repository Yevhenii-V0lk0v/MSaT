package multiagent.lab2.spelunker;

import multiagent.lab2.environment.EnvironmentState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatePercept {
	private static Pattern perceptPattern = Pattern.compile("Percept\\(([\\D]*)(\\d+)\\)");

	private List<EnvironmentState.Percept> percepts = new ArrayList<>();
	private int tick;

	public StatePercept(String predicate) {
		Matcher matcher = perceptPattern.matcher(predicate);
		if (matcher.matches()) {
			for (String stringPercept : matcher.group(1).split(",")) {
				EnvironmentState.Percept percept = EnvironmentState.Percept.getByInterpretation(stringPercept);
				if (percept != EnvironmentState.Percept.NOTHING) {
					percepts.add(percept);
				} else {
					percepts.clear();
					break;
				}
			}
			tick = Integer.parseInt(matcher.group(2));
		}
	}

	public boolean isStench() {
		return percepts.contains(EnvironmentState.Percept.STENCH);
	}

	public boolean isGlitter() {
		return percepts.contains(EnvironmentState.Percept.GLITTER);
	}

	public boolean isBreeze() {
		return percepts.contains(EnvironmentState.Percept.BREEZE);
	}

	public boolean isBump() {
		return percepts.contains(EnvironmentState.Percept.BUMP);
	}

	public boolean isScream() {
		return percepts.contains(EnvironmentState.Percept.SCREAM);
	}

	public int getTick() {
		return tick;
	}

	public void setTick(int tick) {
		this.tick = tick;
	}

	public List<EnvironmentState.Percept> getPercepts() {
		return new ArrayList<>(percepts);
	}
}
