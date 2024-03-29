package multiagent.lab2.navigator.guess;

import multiagent.lab2.environment.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomGuess {
	private Coordinate position;
	private Map<Integer, RoomGuess> neighbouringRooms = new HashMap<>();

	private int distanceToGoal;

	private boolean visited;
	private boolean empty;
	private boolean breeze;
	private boolean stench;

	public RoomGuess(int x, int y) {
		position = new Coordinate(x, y);
	}

	public void propagateDistance() {
		neighbouringRooms.values().forEach(n -> {
			if (n != null && n.isSafe()) {
				if (n.distanceToGoal < 0 || n.distanceToGoal > distanceToGoal) {
					n.distanceToGoal = distanceToGoal + 1;
					n.propagateDistance();
				}
			}
		});
	}

	public List<RoomGuess> getRouteToRoom(RoomGuess goalRoom) {
		List<RoomGuess> route = new ArrayList<>();
		route.add(this);
		if (neighbouringRooms.containsValue(goalRoom)) {
			route.add(goalRoom);
		} else {
			neighbouringRooms.values().stream()
				.filter(n -> n != null && n.distanceToGoal > 0 && n.distanceToGoal < distanceToGoal)
				.findFirst()
				.ifPresent(nextRoom -> route.addAll(nextRoom.getRouteToRoom(goalRoom)));
		}
		return route;
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
		if (neighbour == null) {
			neighbouringRooms.remove(0);
		}
		neighbouringRooms.put(0, neighbour);
	}

	public void setEasternNeighbour(RoomGuess neighbour) {
		if (neighbour == null) {
			neighbouringRooms.remove(1);
		}
		neighbouringRooms.put(1, neighbour);
	}

	public void setSouthernNeighbour(RoomGuess neighbour) {
		if (neighbour == null) {
			neighbouringRooms.remove(2);
		}
		neighbouringRooms.put(2, neighbour);
	}

	public void setWesternNeighbour(RoomGuess neighbour) {
		if (neighbour == null) {
			neighbouringRooms.remove(3);
		}
		neighbouringRooms.put(3, neighbour);
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
		this.breeze = !(empty || visited) && breeze;
	}

	public boolean isStench() {
		return !visited && stench;
	}

	public void setStench(boolean stench) {
		if (!empty) {
			this.stench = stench;
		}
	}

	public boolean isSafe() {
		return empty || !(breeze || stench);
	}

	public int getDistanceToGoal() {
		return distanceToGoal;
	}

	public void setDistanceToGoal(int distanceToGoal) {
		this.distanceToGoal = distanceToGoal;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
		if (visited) {
			breeze = false;
			stench = false;
		}
	}

	public int getDirectionTo(RoomGuess roomGuess) {
		return neighbouringRooms.entrySet().stream()
			.filter(e -> e.getValue() != null && e.getValue().equals(roomGuess))
			.findFirst()
			.map(Map.Entry::getKey)
			.orElse(-1);
	}
}
