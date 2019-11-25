package multiagent.lab2.navigator.behaviour.goal;

import multiagent.lab2.navigator.guess.CaveGuess;
import multiagent.lab2.navigator.guess.RoomGuess;
import multiagent.lab2.navigator.navigation.PathFinder;

public class ExploreUnknown extends Goal {

	public ExploreUnknown(CaveGuess cave) {
		super(cave);
		this.destination = cave.getUnknownRooms().stream()
			.filter(r -> PathFinder.isRoomReachable(r, cave.getCurrentPosition(), cave))
			.findFirst()
			.map(RoomGuess::getPosition)
			.orElse(null);
	}

	@Override
	public boolean isGoalReached() {
		return cave.getUnknownRooms().size() == 0 || destination == null;
	}

	@Override
	public String performGoalAction() {
		destination = cave.getUnknownRooms().stream()
			.filter(r -> PathFinder.isRoomReachable(r, cave.getCurrentPosition(), cave))
			.findFirst()
			.map(RoomGuess::getPosition)
			.orElse(null);
		return "";
	}
}
