package multiagent.lab2.navigator.guess;

import multiagent.lab2.environment.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CaveGuess {
	private int currentDirection;
	private Coordinate currentPosition;
	private List<RoomGuess> roomGuesses;

	public CaveGuess() {
		currentPosition = new Coordinate(3, 3);
		this.roomGuesses = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				roomGuesses.add(new RoomGuess(i, j));
			}
		}
		roomGuesses.forEach(g -> {
			Coordinate gCoord = g.getPosition().getClone();
			roomGuesses.forEach(og -> {
				Coordinate ogCoord = og.getPosition();
				if (ogCoord.isNextTo(gCoord)) {
					if (ogCoord.getX() < gCoord.getX()) {
						g.setWesternNeighbour(og);
					} else if (ogCoord.getX() > gCoord.getX()) {
						g.setEasternNeighbour(og);
					} else if (gCoord.getY() > ogCoord.getY()) {
						g.setNorthernNeighbour(og);
					} else {
						g.setSouthernNeighbour(og);
					}
				}
			});
		});
	}

	public List<RoomGuess> getAllRooms() {
		return new ArrayList<>(roomGuesses);
	}

	public List<RoomGuess> getUnknownRooms() {
		return roomGuesses.stream()
			.filter(RoomGuess::isUnknown)
			.collect(Collectors.toList());
	}

	public List<RoomGuess> getDangerousRooms() {
		return roomGuesses.stream()
			.filter(g -> g.isBreeze() || g.isStench())
			.collect(Collectors.toList());
	}

	public RoomGuess getCurrentRoom() {
		return getRoomAt(currentPosition);
	}

	public void cutTopGuesses(Coordinate topBorder, boolean cutBottom) {
		roomGuesses.removeIf(g -> g.getPosition().getY() < topBorder.getY());
		roomGuesses.forEach(g -> {
			if (topBorder.getY() == g.getPosition().getY()) {
				g.setNorthernNeighbour(null);
			}
			g.getPosition().setY(g.getPosition().getY() - topBorder.getY());
		});
		currentPosition.setY(currentPosition.getY() - topBorder.getY());
		if (cutBottom) {
			Coordinate bottomBorder = topBorder.getClone();
			bottomBorder.setY(3);
			cutBottomGuesses(bottomBorder, false);
		}
	}

	public void cutBottomGuesses(Coordinate bottomBorder, boolean cutTop) {
		roomGuesses.removeIf(g -> g.getPosition().getY() > bottomBorder.getY());
		roomGuesses.forEach(g -> {
			if (bottomBorder.getY() == g.getPosition().getY()) {
				g.setSouthernNeighbour(null);
			}
		});
		if (cutTop) {
			Coordinate topBorder = bottomBorder.getClone();
			topBorder.setY(topBorder.getY() - 3);
			cutTopGuesses(topBorder, false);
		}
	}

	public void cutLeftGuesses(Coordinate leftBorder, boolean cutRight) {
		roomGuesses.removeIf(g -> g.getPosition().getX() < leftBorder.getX());
		roomGuesses.forEach(g -> {
			if (leftBorder.getX() == g.getPosition().getX()) {
				g.setWesternNeighbour(null);
			}
			g.getPosition().setX(g.getPosition().getX() - leftBorder.getX());
		});
		currentPosition.setX(currentPosition.getX() - leftBorder.getX());
		if (cutRight) {
			Coordinate rightBorder = leftBorder.getClone();
			rightBorder.setX(3);
			cutRightGuesses(rightBorder, false);
		}
	}

	public void cutRightGuesses(Coordinate rightBorder, boolean cutLeft) {
		roomGuesses.removeIf(g -> g.getPosition().getX() > rightBorder.getX());
		roomGuesses.forEach(g -> {
			if (rightBorder.getX() == g.getPosition().getX()) {
				g.setEasternNeighbour(null);
			}
		});
		if (cutLeft) {
			Coordinate leftBorder = rightBorder.getClone();
			leftBorder.setY(leftBorder.getX() - 3);
			cutLeftGuesses(leftBorder, false);
		}
	}

	public RoomGuess getRoomAt(Coordinate coordinate) {
		return roomGuesses.stream().filter(g -> g.getPosition().equals(coordinate)).findFirst().orElse(null);
	}

	public Coordinate getCurrentPosition() {
		return currentPosition;
	}

	public int getCurrentDirection() {
		return currentDirection;
	}

	public void setCurrentDirection(int currentDirection) {
		this.currentDirection = currentDirection;
	}

	public List<RoomGuess> getBreezyRooms() {
		return roomGuesses.stream()
			.filter(RoomGuess::isBreeze)
			.collect(Collectors.toList());
	}

	public List<RoomGuess> getStinkyRooms() {
		return roomGuesses.stream()
			.filter(RoomGuess::isStench)
			.collect(Collectors.toList());
	}

	@Override
	public String toString() {
		roomGuesses.sort((a, b) -> {
			if (a.getPosition().getY() > b.getPosition().getY()) {
				return 1;
			} else if (a.getPosition().getY() < b.getPosition().getY()) {
				return -1;
			} else {
				if (a.getPosition().getX() > b.getPosition().getX()) {
					return 1;
				} else if (a.getPosition().getX() < b.getPosition().getX()) {
					return -1;
				}
				return 0;
			}
		});
		int maxX = roomGuesses.stream().mapToInt(g -> g.getPosition().getX()).max().orElse(7);
		int maxY = roomGuesses.stream().mapToInt(g -> g.getPosition().getY()).max().orElse(7);
		StringBuilder builder = new StringBuilder(currentPosition.toString()).append("(").append(currentDirection).append(")\n");
		for (int i = 0; i <= maxY; i++) {
			for (int j = 0; j <= maxX; j++) {
				if (currentPosition.equals(new Coordinate(j, i))) {
					builder.append("*--");
				} else {
					builder.append("+--");
				}
			}
			builder.append("+\n");
			for (int j = 0; j <= maxX; j++) {
				RoomGuess room = getRoomAt(new Coordinate(j, i));
				builder.append("|");
				builder.append(room.isVisited()? "V" :" ");
				builder.append(room.isEmpty()? "E" :" ");
			}
			builder.append("|\n");
			for (int j = 0; j <= maxX; j++) {
				RoomGuess room = getRoomAt(new Coordinate(j, i));
				builder.append("|");
				builder.append(room.isBreeze()? "B" :" ");
				builder.append(room.isStench()? "S" :" ");
			}
			builder.append("|\n");
		}
		for (int j = 0; j <= maxX; j++) {
			builder.append("+--");
		}
		builder.append("+\n");
		return builder.toString();
	}
}
