package multiagent.lab2.navigator.navigation;

import multiagent.lab2.navigator.guess.RoomGuess;

public class RoomWrapper {
	private RoomGuess room;
	private int distanceToGoal;

	public RoomWrapper(RoomGuess room) {
		if (room == null) {
			throw new NullPointerException("A room must not be null");
		}
		this.room = room;
	}

	public int getDistanceToGoal() {
		return distanceToGoal;
	}

	public void setDistanceToGoal(int distanceToGoal) {
		this.distanceToGoal = distanceToGoal;
	}

	public RoomGuess getRoom() {
		return room;
	}
}
