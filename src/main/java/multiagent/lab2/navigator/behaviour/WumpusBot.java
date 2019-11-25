package multiagent.lab2.navigator.behaviour;

import multiagent.lab2.GameAction;
import multiagent.lab2.NaturalLanguageUtils;
import multiagent.lab2.Percept;
import multiagent.lab2.environment.GameplayState;
import multiagent.lab2.navigator.behaviour.goal.ExploreDanger;
import multiagent.lab2.navigator.behaviour.goal.ExploreUnknown;
import multiagent.lab2.navigator.behaviour.goal.Goal;
import multiagent.lab2.navigator.behaviour.goal.HuntWumpus;
import multiagent.lab2.navigator.guess.CaveGuess;
import multiagent.lab2.navigator.guess.RoomGuess;
import multiagent.lab2.navigator.navigation.PathFinder;

import java.util.List;
import java.util.Objects;

import static multiagent.lab2.Percept.*;

public class WumpusBot {
	private CaveGuess cave = new CaveGuess();
	private Goal goal;
	private GameplayState gameplayState = new GameplayState();

	public String getCommand(String content) {
		List<Percept> percepts = NaturalLanguageUtils.perceiveStateFromNatLang(content);
		parsePercepts(percepts);
		System.out.println(cave);
		return chooseAction(percepts);
	}


	private String chooseAction(List<Percept> percepts) {
		if (percepts.contains(GLITTER)) {
			gameplayState.setGoldTaken(true);
			System.out.println("Grabbing gold");
			return GameAction.GRAB.getNatLangValue();
		}
		if (gameplayState.isGoldTaken() && gameplayState.isWumpusKilled()) {
			System.out.println("Leaving the dungeon");
			return GameAction.CLIMB.getNatLangValue();
		}
		if (goal == null || goal.isGoalReached()) {
			setupGoal();
		}
		return goal.performGoal();
	}

	private void setupGoal() {
		if (cave.getUnknownRooms().size() > 0 && cave.getUnknownRooms().stream().anyMatch(r -> PathFinder.isRoomReachable(r, cave.getCurrentPosition(), cave))) {
			goal = new ExploreUnknown(cave);
		} else if (cave.getBreezyRooms().size() > 3 || cave.getStinkyRooms().size() > 1) {
			goal = new ExploreDanger(cave);
		} else if (cave.getStinkyRooms().size() == 1) {
			goal = new HuntWumpus(cave);
		}
	}

	private void parsePercepts(List<Percept> percepts) {
		RoomGuess currentRoom = cave.getCurrentRoom();
		currentRoom.setVisited(true);
		currentRoom.setEmpty(true);
		if (percepts.contains(SCREAM)) {
			gameplayState.setWumpusKilled(true);
			cave.getStinkyRooms().forEach(r -> r.setStench(false));
		}
		if (percepts.contains(STENCH)) {
			currentRoom.getNeighbouringRooms().values().forEach(r -> {
				if (r != null && !r.isVisited()) {
					r.setStench(true);
				}
			});
		}
		if (percepts.contains(BREEZE)) {
			currentRoom.getNeighbouringRooms().values().forEach(r -> {
				if (r != null && !r.isVisited()) {
					r.setBreeze(true);
				}
			});
		}
		if (percepts.contains(BUMP)) {
			switch (cave.getCurrentDirection()) {
				case 0:
					cave.getCurrentPosition().setY(cave.getCurrentPosition().getY() + 1);
					cave.cutTopGuesses(cave.getCurrentPosition(), true);
					break;
				case 1:
					cave.getCurrentPosition().setX(cave.getCurrentPosition().getX() - 1);
					cave.cutRightGuesses(cave.getCurrentPosition(), true);
					break;
				case 2:
					cave.getCurrentPosition().setY(cave.getCurrentPosition().getY() - 1);
					cave.cutBottomGuesses(cave.getCurrentPosition(), true);
					break;
				case 3:
					cave.getCurrentPosition().setX(cave.getCurrentPosition().getX() + 1);
					cave.cutLeftGuesses(cave.getCurrentPosition(), true);
					break;
			}
		}
		if (percepts.isEmpty()) {
			currentRoom.getNeighbouringRooms().values().stream().filter(Objects::nonNull).forEach(r -> r.setEmpty(true));
		}
	}
}
