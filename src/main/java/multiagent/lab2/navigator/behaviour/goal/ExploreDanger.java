package multiagent.lab2.navigator.behaviour.goal;

import multiagent.lab2.navigator.guess.CaveGuess;
import multiagent.lab2.navigator.guess.RoomGuess;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ExploreDanger extends Goal {
	private List<RoomGuess> checkedRooms;
	private RoomGuess currentRoom;

	public ExploreDanger(CaveGuess cave) {
		super(cave);
		List<RoomGuess> dangerousRooms = cave.getDangerousRooms();
		checkedRooms = dangerousRooms.stream()
			.filter(this::isRoomDangerValid)
			.collect(Collectors.toList());
		dangerousRooms.removeIf(r -> checkedRooms.contains(r));
		if (!dangerousRooms.isEmpty()) {
			currentRoom = dangerousRooms.get(0);
			setupDangerToCheck(dangerousRooms.get(0));
		}
	}

	private void setupDangerToCheck(RoomGuess danger) {
		cave.getAllRooms().forEach(r -> r.setDistanceToGoal(-1));
		cave.getCurrentRoom().setDistanceToGoal(0);
		cave.getCurrentRoom().propagateDistance();
		destination = danger.getNeighbouringRooms().values().stream()
			.filter(n ->n != null && n.getDistanceToGoal() > 0 && !n.isVisited())
			.min(Comparator.comparing(RoomGuess::getDistanceToGoal))
			.map(RoomGuess::getPosition)
			.orElse(currentRoom.getPosition());
	}

	@Override
	public boolean isGoalReached() {
		return currentRoom == null || cave.getBreezyRooms().size() == 3 && cave.getStinkyRooms().size() == 1;
	}

	@Override
	public String performGoalAction() {
		if ((currentRoom.isBreeze() || currentRoom.isStench())) {
			checkedRooms.add(currentRoom);
		}
		List<RoomGuess> dangerousRooms = cave.getDangerousRooms();
		dangerousRooms.removeIf(r -> checkedRooms.contains(r));
		if (!dangerousRooms.isEmpty()) {
			currentRoom = dangerousRooms.get(0);
			setupDangerToCheck(dangerousRooms.get(0));
		} else {
			currentRoom = null;
		}
		return "";
	}

	private boolean isRoomDangerValid(RoomGuess r) {
		return r.getNeighbouringRooms().values().stream()
			.filter(RoomGuess::isVisited)
			.count() >= 2;
	}
}
