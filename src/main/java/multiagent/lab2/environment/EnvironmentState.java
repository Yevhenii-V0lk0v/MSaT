package multiagent.lab2.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnvironmentState {
	private Random gameRand;
	private int tick;
	private Coordinate gold;
	private List<Coordinate> pits;
	private Coordinate wumpus;
	private boolean gameOver;

	private Coordinate spelunkerPosition;

	/**
	 * Shows spelunkers current direction (0 - N, 1 - E, 2 - S, 3 - W)
	 */
	private int spelunkerRotation;
	private boolean spelunkerHitAWall;

	public EnvironmentState() {
		gameRand = new Random();

		wumpus = Coordinate.asRandom(gameRand, 4);

		pits = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			pits.add(Coordinate.asRandom(gameRand, 4));
		}

		while (gold == null) {
			Coordinate newGold = Coordinate.asRandom(gameRand, 4);
			if (!pits.contains(newGold) && !wumpus.equals(newGold)) {
				gold = newGold;
			}
		}

		do {
			spelunkerPosition = Coordinate.asRandom(gameRand, 4);
		} while (pits.contains(spelunkerPosition) || wumpus.equals(spelunkerPosition));
		spelunkerRotation = 0;

		tick = 0;
	}

	public String getStatePercept() {
		StringBuilder builder = new StringBuilder("Percept(");
		if (wumpus == null) {
			builder.append(Percept.SCREAM.getPunctuatedInterpretation());
		} else if (spelunkerPosition.isNextTo(wumpus)) {
			builder.append(Percept.STENCH.getPunctuatedInterpretation());
		} else if (spelunkerPosition.equals(wumpus)) {
			// TODO: 02.11.2019 Add death perception
			gameOver = true;
		}
		for (Coordinate pit : pits) {
			if (spelunkerPosition.equals(pit)) {
				// TODO: 02.11.2019 Add death perception
				gameOver = true;
			} else if (spelunkerPosition.isNextTo(pit)) {
				builder.append(Percept.BREEZE.getPunctuatedInterpretation());
				break;
			}
		}
		if (spelunkerPosition.equals(gold)) {
			builder.append(Percept.GLITTER.getPunctuatedInterpretation());
		}
		if (spelunkerHitAWall) {
			builder.append(Percept.BUMP.getPunctuatedInterpretation());
			spelunkerHitAWall = false;
		}
		builder.append(tick).append(")");
		return builder.toString();
	}

	public void performShot() {
		if (spelunkerRotation % 2 == 0 &&
			spelunkerPosition.getX() == wumpus.getX()) {
			if ((
				spelunkerRotation == 0 &&
					spelunkerPosition.getY() > wumpus.getY()
			) || (
				spelunkerRotation == 2 &&
					spelunkerPosition.getY() < wumpus.getY()
			)) {
				wumpus = null;
			}
		} else if (spelunkerRotation % 2 == 1 &&
			spelunkerPosition.getY() == wumpus.getY()) {
			if ((
				spelunkerRotation == 1 &&
					spelunkerPosition.getX() > wumpus.getX()
			) || (
				spelunkerRotation == 3 &&
					spelunkerPosition.getX() < wumpus.getX()
			)) {
				wumpus = null;
			}
		}
		if (wumpus != null) {
			moveCoordinate(wumpus, gameRand.nextInt(4));
		}
		tick++;
	}

	private void moveCoordinate(Coordinate coordinate, int direction) {
		if (direction % 2 == 0) {
			int y = coordinate.getY() + direction - 1;
			if (y >= 0 && y < 4) {
				coordinate.setY(y);
			}
		} else {
			int x = coordinate.getX() + direction - 2;
			if (x >= 0 && x < 4) {
				coordinate.setX(x);
			}
		}
	}

	public void performTurn(String direction) {
		if ("left".equals(direction)) {
			if (spelunkerRotation == 0) {
				spelunkerRotation = 3;
			} else {
				spelunkerRotation--;
			}
		} else if ("right".equals(direction)) {
			spelunkerRotation = (spelunkerRotation + 1) % 4;
		}
		tick++;
	}

	public void performGrab() {
		if (spelunkerPosition.equals(gold)) {
			gold = null;
		}
		tick++;
	}

	public void performForward() {
		Coordinate oldPosition = spelunkerPosition.getClone();
		moveCoordinate(spelunkerPosition, spelunkerRotation);
		if (spelunkerPosition.equals(oldPosition)) {
			spelunkerHitAWall = true;
		}
		tick++;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public enum Percept {
		BREEZE("breeze"),
		BUMP("bump"),
		GLITTER("glitter"),
		SCREAM("scream"),
		STENCH("stench"),
		NOTHING("");

		Percept(String stringInterpretation) {
			this.stringInterpretation = stringInterpretation;
		}

		private final String stringInterpretation;

		public String getStringInterpretation() {
			return stringInterpretation;
		}

		public String getPunctuatedInterpretation() {
			return stringInterpretation + ",";
		}

		public static Percept getByInterpretation(String interpretation) {
			for (Percept value : values()) {
				if (value.stringInterpretation.equals(interpretation)) {
					return value;
				}
			}
			return NOTHING;
		}
	}

	public enum GameAction {
		CLIMB("climb", "Climb"),
		SHOOT("shoot", "Shoot"),
		GRAB("grab", "Grab"),
		FORWARD("forward", "Forward"),
		TURN_LEFT("left","Turn(Left)"),
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
}
