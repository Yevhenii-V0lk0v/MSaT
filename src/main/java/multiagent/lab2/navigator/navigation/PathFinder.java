package multiagent.lab2.navigator.navigation;

import multiagent.lab2.environment.Coordinate;
import multiagent.lab2.navigator.guess.CaveGuess;
import multiagent.lab2.navigator.guess.RoomGuess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PathFinder {

	public static List<RoomGuess> buildRoute(Coordinate from, Coordinate to, CaveGuess cave) {
		RoomGuess goalRoom = cave.getGuessAt(to);
		RoomGuess startingRoom = cave.getGuessAt(from);
		if (startingRoom != null && goalRoom != null) {
			cave.getAllRooms().forEach(g -> g.setDistanceToGoal(-1));
			goalRoom.setDistanceToGoal(0);
			goalRoom.propagateDistance();
			return startingRoom.getRouteToRoom(goalRoom);
		}
		return new ArrayList<>();
	}
}
