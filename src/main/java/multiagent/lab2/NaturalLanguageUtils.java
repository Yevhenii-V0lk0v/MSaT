package multiagent.lab2;

import multiagent.lab2.spelunker.StatePercept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaturalLanguageUtils {
	private static final Map<Percept, String> perceptPhrases = new HashMap<>();

	static {
		perceptPhrases.put(Percept.BREEZE, "a slight breeze is flowing in the air");
		perceptPhrases.put(Percept.BUMP, "you bump into a solid wall");
		perceptPhrases.put(Percept.GLITTER, "a dim glitter is under your feet");
		perceptPhrases.put(Percept.NOTHING, "the pitch black of the dungeon surrounds you");
		perceptPhrases.put(Percept.SCREAM, "you hear an echoing scream");
		perceptPhrases.put(Percept.STENCH, "you smell a horrible stench");
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
		List<Percept> percepts = percept.getPercepts();
		if (percepts.isEmpty()) {
			nlTextBuilder.append("\nand ").append(perceptPhrases.get(Percept.NOTHING));
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

	public static GameAction transformPhraseIntoAction(String phrase) {
		for (GameAction value : GameAction.values()) {
			if (phrase.toLowerCase().contains(value.getNatLangValue().toLowerCase())) {
				return value;
			}
		}
		return null;
	}

	public static List<Percept> perceiveStateFromNatLang(String natLangState) {
		List<Percept> result = new ArrayList<>();
		natLangState = natLangState.toLowerCase();
		for (Percept percept : Percept.values()) {
			if (percept != Percept.NOTHING && natLangState.contains(percept.getStringInterpretation())) {
				result.add(percept);
			}
		}
		return result;
	}
}
