package multiagent.lab2;

public enum GameAction {
	CLIMB("climb", "Climb"),
	SHOOT("shoot", "Shoot"),
	GRAB("grab", "Grab"),
	FORWARD("forward", "Forward"),
	TURN_LEFT("left", "Turn(Left)"),
	TURN_RIGHT("right", "Turn(Right)");

	private final String natLangValue;
	private final String predicateValue;

	GameAction(String natLangValue, String predicateValue) {
		this.natLangValue = natLangValue;
		this.predicateValue = predicateValue;
	}

	public String getNatLangValue() {
		return natLangValue;
	}

	public String getPredicateValue() {
		return predicateValue;
	}

	public boolean isTurn(GameAction action) {
		return action == TURN_LEFT || action == TURN_RIGHT;
	}

	public boolean isTurn(String actionString) {
		return isTurn(getByNatLangValue(actionString)) || isTurn(getByPredicateValue(actionString));
	}

	public static GameAction getByNatLangValue(String actionString) {
		for (GameAction value : values()) {
			if (value.getNatLangValue().equals(actionString)) {
				return value;
			}
		}
		return null;
	}

	public static GameAction getByPredicateValue(String actionString) {
		for (GameAction value : values()) {
			if (value.getPredicateValue().equals(actionString)) {
				return value;
			}
		}
		return null;
	}
}