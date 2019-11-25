package multiagent.lab2.navigator.behaviour.goal;

import multiagent.lab2.GameAction;
import multiagent.lab2.environment.Coordinate;
import multiagent.lab2.navigator.guess.CaveGuess;
import multiagent.lab2.navigator.guess.RoomGuess;
import multiagent.lab2.navigator.navigation.PathFinder;

import java.util.List;

public abstract class Goal {
	protected CaveGuess cave;
	protected Coordinate destination;

	public Goal(CaveGuess cave) {
		this.cave = cave;
	}

	public abstract String performGoalAction();

	public abstract boolean isGoalReached();

	protected String moveToGoal() {
		List<RoomGuess> route = PathFinder.buildRoute(cave.getCurrentPosition(), destination, cave);
		route.remove(0);
		if (!route.isEmpty()) {
			int directionToNextPoint = cave.getCurrentRoom().getDirectionTo(route.get(0));
			if (directionToNextPoint >= 0) {
				int dirDiff = cave.getCurrentDirection() - directionToNextPoint;
				if (dirDiff != 0) {
					String turnCommand = getTurnCommand(dirDiff);
					System.out.println("Turning " + turnCommand);
					return turnCommand;
				} else {
					moveCoordinate(cave.getCurrentPosition(), cave.getCurrentDirection());
					System.out.println("Moving forward");
					return GameAction.FORWARD.getNatLangValue();
				}
			}
		}
		System.out.println("The bot is stuck in danger. Performing a leap of faith");
		return GameAction.FORWARD.getNatLangValue();
	}

	protected String getTurnCommand(int dirDiff) {
		if (Math.abs(dirDiff) == 2 || (dirDiff == 3 || dirDiff == -1)) {
			cave.setCurrentDirection((cave.getCurrentDirection() + 1) % 4);
			return GameAction.TURN_RIGHT.getNatLangValue();
		} else {
			cave.setCurrentDirection(cave.getCurrentDirection() == 0 ? 3 : cave.getCurrentDirection() - 1);
			return GameAction.TURN_LEFT.getNatLangValue();
		}
	}

	private void moveCoordinate(Coordinate coordinate, int direction) {
		int newX = coordinate.getX();
		int newY = coordinate.getY();
		switch (direction) {
			case 0:
				newY--;
				break;
			case 1:
				newX++;
				break;
			case 2:
				newY++;
				break;
			case 3:
				newX--;
				break;
		}
		coordinate.setX(newX);
		coordinate.setY(newY);
	}

	public String performGoal() {
		if (cave.getCurrentPosition().equals(destination)) {
			return performGoalAction();
		} else {
			return moveToGoal();
		}
	}
}
