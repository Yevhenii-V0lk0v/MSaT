package multiagent.lab2.environment;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import multiagent.lab2.ProcessDependentBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvironmentAgent extends Agent {
	private int tick;
	private Coordinate gold;
	private List<Coordinate> pits;
	private Coordinate wumpus;

	private AID spelunker;
	private Coordinate spelunkerPosition;

	/**
	 * Shows spelunkers current direction (0 - N, 1 - E, 2 - S, 3 - W)
	 */
	private int spelunkerRotation;

	@Override
	protected void setup() {
		DFAgentDescription description = new DFAgentDescription();
		description.setName(this.getAID());

		ServiceDescription sd = new ServiceDescription();
		sd.setName("wumpus-env");
		sd.setType("env");
		description.addServices(sd);

		try {
			DFService.register(this, description);
			System.out.println("Environment service registered");
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		addBehaviour(new GameInitBehaviour());
		addBehaviour(new StateProviderBehaviour());
		addBehaviour(new StateEditorBehaviour());
	}

	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
			System.out.println("Environment service deregistered");
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}

	class GameInitBehaviour extends ProcessDependentBehaviour {
		private MessageTemplate requestTemplate = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
			MessageTemplate.MatchContent("GameRequest")
		);

		@Override
		public void action() {
			ACLMessage gameReq = getAgent().receive(requestTemplate);
			if (gameReq != null) {
				System.out.println("Request for a game arrived.");

				spelunker = gameReq.getSender();

				Random rand = new Random();

				wumpus = Coordinate.asRandom(rand, 4);

				pits = new ArrayList<>();
				for (int i = 0; i < 3; i++) {
					pits.add(Coordinate.asRandom(rand, 4));
				}

				while (gold == null) {
					Coordinate newGold = Coordinate.asRandom(rand, 4);
					if (!pits.contains(newGold) && !wumpus.equals(newGold)) {
						gold = newGold;
					}
				}

				do {
					spelunkerPosition = Coordinate.asRandom(rand, 4);
				} while (pits.contains(spelunkerPosition) || wumpus.equals(spelunkerPosition));
				spelunkerRotation = 0;

				tick = 0;

				ACLMessage ok = gameReq.createReply();
				ok.setPerformative(ACLMessage.CONFIRM);
				ok.setContent("OK");
				getAgent().send(ok);

				done = true;
			} else {
				block();
			}
		}
	}

	class StateProviderBehaviour extends ProcessDependentBehaviour {
		private MessageTemplate mt = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
			MessageTemplate.and(
				MessageTemplate.MatchContent("StateRequest"),
				MessageTemplate.MatchSender(spelunker)
			)
		);

		@Override
		public void action() {
			ACLMessage req = getAgent().receive(mt);
			if (req != null) {
				ACLMessage resp = req.createReply();
				resp.setPerformative(ACLMessage.INFORM);
				resp.setContent(getStatePredicate());
				getAgent().send(resp);
			} else {
				block();
			}
		}
	}

	private String getStatePredicate() {
		StringBuilder builder = new StringBuilder("Percept(");
		if (wumpus == null) {
			builder.append("roar,");
		} else if (spelunkerPosition.isNextTo(wumpus)) {
			builder.append("stench,");
		} else if (spelunkerPosition.equals(wumpus)) {
			builder.append("scream,");
		}
		for (Coordinate pit : pits) {
			if (spelunkerPosition.equals(pit) && !builder.toString().contains("scream")) {
				builder.append("scream,");
				break;
			} else if (spelunkerPosition.isNextTo(pit)) {
				builder.append("breeze,");
				break;
			}
		}
		if (spelunkerPosition.equals(gold)) {
			builder.append("glitter,");
		}
		builder.append(tick).append(")");
		return builder.toString();
	}

	class StateEditorBehaviour extends ProcessDependentBehaviour {
		private MessageTemplate mt = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
			MessageTemplate.MatchSender(spelunker)
		);

		@Override
		public void action() {
			ACLMessage action = getAgent().receive(mt);
			if (action != null) {
				ACLMessage actionReply = action.createReply();
				if (processAction(action.getContent())) {
					actionReply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					actionReply.setContent("OK");
				} else {
					actionReply.setPerformative(ACLMessage.REJECT_PROPOSAL);
					actionReply.setContent("NO");
				}
				getAgent().send(actionReply);
			}
		}
	}

	private Pattern actionPattern = Pattern.compile("^Action\\((\\S+)\\)$");

	private boolean processAction(String actionPredicate) {
		Matcher actionMatcher = actionPattern.matcher(actionPredicate);
		if (actionMatcher.matches()) {
			String action = actionMatcher.group(1);
			/*
			 * Our action cases:
			 * Turn(left)
			 * Turn(right)
			 * Forward
			 * Shoot
			 * Grab
			 * Climb
			 * */
			if ("Climb".equals(action)) {
				performLeave();
				tick++;
				return true;
			} else if ("Shoot".equals(action)) {
				performShot();
				tick++;
				return true;
			} else if ("Grab".equals(action)) {
				if (spelunkerPosition.equals(gold)) {
					gold = null;
				}
				tick++;
				return true;
			} else if ("Forward".equals(action)) {
				moveCoordinate(spelunkerPosition, spelunkerRotation);
				tick++;
				return true;
			} else if (action.startsWith("Turn")) {
				performTurn(action.substring(action.indexOf("("), action.length() - 1));
				tick++;
				return true;
			}
		}
		return false;
	}

	private void performTurn(String direction) {
		if ("left".equals(direction)) {
			if (spelunkerRotation == 0) {
				spelunkerRotation = 3;
			} else {
				spelunkerRotation--;
			}
		} else if ("right".equals(direction)) {
			spelunkerRotation = (spelunkerRotation + 1) % 4;
		}
	}

	private void performShot() {
		if (spelunkerRotation % 2 == 0 &&
			spelunkerPosition.getX() == wumpus.getX()) {
			if ((
				spelunkerRotation == 0 &&
					spelunkerPosition.getY() > wumpus.getY()
			) || (
				spelunkerRotation == 2 &&
					spelunkerPosition.getY() < wumpus.getY()
			)) {
				wumpus = null;
			}
		} else if (spelunkerRotation % 2 == 1 &&
			spelunkerPosition.getY() == wumpus.getY()) {
			if ((
				spelunkerRotation == 1 &&
					spelunkerPosition.getX() > wumpus.getX()
			) || (
				spelunkerRotation == 3 &&
					spelunkerPosition.getX() < wumpus.getX()
			)) {
				wumpus = null;
			}
		}
		if (wumpus != null) {
			moveCoordinate(wumpus, new Random().nextInt(4));
		}
	}

	private void moveCoordinate(Coordinate coordinate, int direction) {
		if (direction % 2 == 0) {
			int y = coordinate.getY() + direction - 1;
			if (y >= 0 && y < 4) {
				coordinate.setY(y);
			}
		} else {
			int x = coordinate.getX() + direction - 2;
			if (x >= 0 && x < 4) {
				coordinate.setX(x);
			}
		}
	}

	private void performLeave() {
		// TODO: 21.10.2019 Check for possible bugs. This method should finish the game
		doDelete();
	}

}
