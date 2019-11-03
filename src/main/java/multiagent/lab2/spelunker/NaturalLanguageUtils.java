package multiagent.lab2.spelunker;

import multiagent.lab2.environment.EnvironmentState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaturalLanguageUtils {
	private static final Map<EnvironmentState.Percept, String> perceptPhrases = new HashMap<>();

	static {
		perceptPhrases.put(EnvironmentState.Percept.BREEZE, "a slight breeze is flowing in the air");
		perceptPhrases.put(EnvironmentState.Percept.BUMP, "you bump into a solid wall");
		perceptPhrases.put(EnvironmentState.Percept.GLITTER, "a dim glitter is under your feet");
		perceptPhrases.put(EnvironmentState.Percept.NOTHING, "the pitch black of the dungeon surrounds you");
		perceptPhrases.put(EnvironmentState.Percept.SCREAM, "you hear an echoing scream");
		perceptPhrases.put(EnvironmentState.Percept.STENCH, "you smell a horrible stench");
	}

	private static final Map<String, String> winPhrases = new HashMap<>();

	static {
		winPhrases.put("W", "You have slain the Wumpus!");
		winPhrases.put("G", "You found a pile of Gold!");
	}

	private static final Map<String, String> lossPhrases = new HashMap<>();

	static {
		lossPhrases.put("W", "You have been slain by the Wumpus!");
		lossPhrases.put("P", "You have fallen into the pit!");
		lossPhrases.put("F", "You have failed to slay the Wumpus and didn't find any Gold!");
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

	public static String transformWinToNaturalLanguage(String winPredicate) {
		Pattern winPredicatePattern = Pattern.compile("Win\\(([\\w,]+)\\)");
		Matcher matcher = winPredicatePattern.matcher(winPredicate);
		if (matcher.matches()) {
			StringBuilder builder = new StringBuilder("You win!\n");
			for (String winCondition : matcher.group(1).split(",")) {
				builder.append(winPhrases.get(winCondition)).append("\n");
			}
			return builder.toString();
		} else {
			return "";
		}
	}

	public static String transformLossToNaturalLanguage(String lossPredicate) {
		Pattern lossPredicatePattern = Pattern.compile("Loss\\((\\w)\\)");
		Matcher matcher = lossPredicatePattern.matcher(lossPredicate);
		if (matcher.matches()) {
			return "You lost!\n" + lossPhrases.get(matcher.group(1));
		} else {
			return "";
		}
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
