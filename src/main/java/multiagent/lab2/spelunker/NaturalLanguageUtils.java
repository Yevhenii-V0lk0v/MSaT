package multiagent.lab2.spelunker;

import multiagent.lab2.environment.EnvironmentState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NaturalLanguageUtils {
	private static final Map<EnvironmentState.Percept, String> perceptPhrases = new HashMap<>();

	static {
		perceptPhrases.put(EnvironmentState.Percept.BREEZE, "a slight breeze is flowing in the air");
		perceptPhrases.put(EnvironmentState.Percept.BUMP, "you feel a bump into a solid wall");
		perceptPhrases.put(EnvironmentState.Percept.GLITTER, "a dim glitter is under your feet");
		perceptPhrases.put(EnvironmentState.Percept.NOTHING, "the pitch black of the dungeon surrounds you");
		perceptPhrases.put(EnvironmentState.Percept.SCREAM, "you hear an echoing scream");
		perceptPhrases.put(EnvironmentState.Percept.STENCH, "you feel a horrible stench");
	}

	public static String transformPerceptToNaturalLanguage(StatePercept percept) {
		StringBuilder nlTextBuilder = new StringBuilder("Current tick is ");
		nlTextBuilder.append(percept.getTick());
		List<EnvironmentState.Percept> percepts = percept.getPercepts();
		if (percepts.isEmpty()) {
			nlTextBuilder.append("\nand ").append(perceptPhrases.get(EnvironmentState.Percept.NOTHING));
		} else {
			percepts.forEach(p -> nlTextBuilder.append("\nand ").append(perceptPhrases.get(p)));
		}
		return nlTextBuilder.toString();
	}

	public static EnvironmentState.GameAction transformPhraseIntoAction(String phrase) {
		for (EnvironmentState.GameAction value : EnvironmentState.GameAction.values()) {
			if (phrase.toLowerCase().contains(value.getNatLangValue().toLowerCase())) {
				return value;
			}
		}
		return null;
	}
}
