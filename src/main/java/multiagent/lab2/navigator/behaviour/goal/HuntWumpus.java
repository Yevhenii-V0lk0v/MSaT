package multiagent.lab2.navigator.behaviour.goal;

import multiagent.lab2.GameAction;
import multiagent.lab2.environment.Coordinate;
import multiagent.lab2.navigator.guess.CaveGuess;
import multiagent.lab2.navigator.guess.RoomGuess;
import multiagent.lab2.navigator.navigation.PathFinder;

import java.util.Comparator;

public class HuntWumpus extends Goal {
	private boolean wumpusShot;
	private int goalDirection;

	public HuntWumpus(CaveGuess cave) {
		super(cave);
		RoomGuess wumpusRoom = cave.getStinkyRooms().get(0);
		RoomGuess alignedRoom = cave.getRoomAt(new Coordinate(cave.getCurrentPosition().getX(), wumpusRoom.getPosition().getY()));
		if (alignedRoom.isSafe()) {
			destination = alignedRoom.getPosition();
			if (destination.getX() > cave.getCurrentPosition().getX()) {
				goalDirection = 1;
			} else {
				goalDirection = 3;
			}
		} else {
			alignedRoom = cave.getRoomAt(new Coordinate(wumpusRoom.getPosition().getX(), cave.getCurrentPosition().getY()));
			if (alignedRoom.isSafe()) {
				destination = alignedRoom.getPosition();
				if (destination.getY() > cave.getCurrentPosition().getY()) {
					goalDirection = 2;
				} else {
					goalDirection = 0;
				}
			}
		}
		if (destination == null) {
			destination = wumpusRoom.getNeighbouringRooms().values().stream()
				.filter(r -> r.isSafe() && PathFinder.isRoomReachable(wumpusRoom, cave.getCurrentPosition(), cave))
			.min(Comparator.comparing(RoomGuess::getDistanceToGoal))
			.map(RoomGuess::getPosition)
			.orElse(null);
			if (destination != null) {
				goalDirection = cave.getRoomAt(destination).getDirectionTo(wumpusRoom);
			}
		}
	}

	@Override
	public String performGoalAction() {
		if (cave.getCurrentDirection() != goalDirection) {
			System.out.println("Turning to face the enemy");
			return getTurnCommand(cave.getCurrentDirection() - goalDirection);
		}
		wumpusShot = true;
		System.out.println("Shooting the Wumpus");
		return GameAction.SHOOT.getNatLangValue();
	}

	@Override
	public boolean isGoalReached() {
		return wumpusShot || destination == null;
	}
}
