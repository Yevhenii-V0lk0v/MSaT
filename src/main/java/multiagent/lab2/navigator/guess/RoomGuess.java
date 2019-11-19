package multiagent.lab2.navigator.guess;

import multiagent.lab2.environment.Coordinate;

import java.util.HashMap;
import java.util.Map;

public class RoomGuess {
	private Coordinate position;
	private Map<Integer, RoomGuess> neighbouringRooms = new HashMap<>();

	private boolean empty;
	private boolean breeze;
	private boolean stench;

	public RoomGuess(int x, int y) {
		position = new Coordinate(x, y);
	}

	public boolean isUnknown() {
		return !empty && !breeze && !stench;
	}

	public void setUnknown() {
		empty = false;
		breeze = false;
		stench = false;
	}

	public Coordinate getPosition() {
		return position;
	}

	public void setPosition(Coordinate position) {
		this.position = position;
	}

	public Map<Integer, RoomGuess> getNeighbouringRooms() {
		return neighbouringRooms;
	}

	public void setNorthernNeighbour(RoomGuess neighbour) {
		neighbouringRooms.put(0, neighbour);
	}

	public void setEasternNeighbour(RoomGuess neighbour) {
		neighbouringRooms.put(1, neighbour);
	}

	public void setSouthernNeighbour(RoomGuess neighbour) {
		neighbouringRooms.put(2, neighbour);
	}

	public void setWesternNeighbour(RoomGuess neighbour) {
		neighbouringRooms.put(3, neighbour);
	}

	public void setNeighbouringRooms(Map<Integer, RoomGuess> neighbouringRooms) {
		this.neighbouringRooms = neighbouringRooms;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		if (empty) {
			stench = false;
			breeze = false;
		}
		this.empty = empty;
	}

	public boolean isBreeze() {
		return breeze;
	}

	public void setBreeze(boolean breeze) {
		this.breeze = breeze;
	}

	public boolean isStench() {
		return stench;
	}

	public void setStench(boolean stench) {
		if (stench) {
			empty = false;
		}
		this.stench = stench;
	}
}
