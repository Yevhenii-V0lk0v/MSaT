package multiagent.lab2.environment;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import multiagent.lab2.ActionUtils;
import multiagent.lab2.ProcessDependentBehaviour;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvironmentAgent extends Agent {
	private EnvironmentState gameState;
	private AID spelunker;

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
				gameState = new EnvironmentState();
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
				resp.setContent(gameState.getStatePercept());
				getAgent().send(resp);
			} else {
				block();
			}
		}
	}

	class StateEditorBehaviour extends ProcessDependentBehaviour {
		private MessageTemplate mt = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
			MessageTemplate.MatchSender(spelunker)
		);

		@Override
		public void action() {
			ActionUtils.receiveMessage(this, mt, m -> {
				ACLMessage actionReply = m.createReply();
				if (processAction(m.getContent())) {
					actionReply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					actionReply.setContent("OK");
				} else {
					actionReply.setPerformative(ACLMessage.REJECT_PROPOSAL);
					actionReply.setContent("NO");
				}
				getAgent().send(actionReply);
			});
		}
	}

	private Pattern actionPattern = Pattern.compile("^Action\\((\\S+)\\)$");

	private boolean processAction(String actionPredicate) {
		Matcher actionMatcher = actionPattern.matcher(actionPredicate);
		if (actionMatcher.matches()) {
			EnvironmentState.GameAction actionObject = EnvironmentState.GameAction.getByPredicateValue(actionMatcher.group(1));
			if (actionObject != null) {
				switch (actionObject) {
					case CLIMB:
						// TODO: 02.11.2019 Add finishing behaviour
						return true;
					case SHOOT:
						gameState.performShot();
						return true;
					case GRAB:
						gameState.performGrab();
						return true;
					case FORWARD:
						gameState.performForward();
						return true;
					case TURN_LEFT:
					case TURN_RIGHT:
						gameState.performTurn(actionObject.getNatLangValue());
						return true;
				}
			}
		}
		return false;
	}

}
