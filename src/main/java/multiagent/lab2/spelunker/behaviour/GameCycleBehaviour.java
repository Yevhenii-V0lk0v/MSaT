package multiagent.lab2.spelunker.behaviour;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import multiagent.lab2.ActionUtils;
import multiagent.lab2.ProcessDependentBehaviour;
import multiagent.lab2.environment.EnvironmentState;
import multiagent.lab2.spelunker.NaturalLanguageUtils;
import multiagent.lab2.spelunker.StatePercept;

import static multiagent.lab2.spelunker.behaviour.GameCycleBehaviour.GameCycleState.*;

public class GameCycleBehaviour extends ProcessDependentBehaviour {
	private String gameId = "game" + System.currentTimeMillis();
	private MessageTemplate mt;
	private GameCycleState state = REQUESTING_STATE;
	private AID environment;
	private AID navigator;

	public GameCycleBehaviour(AID environment, AID navigator) {
		this.environment = environment;
		this.navigator = navigator;
	}

	@Override
	public void action() {
		switch (state) {
			case REQUESTING_STATE:
				requestGameState();
				break;
			case PROCESSING_STATE:
				processGameState();
				break;
			case PROCESSING_ACTION:
				passControlCommand();
				break;
		}
	}

	private void requestGameState() {
		ACLMessage stateReq = new ACLMessage(ACLMessage.REQUEST);
		stateReq.setContent("StateRequest");
		stateReq.addReceiver(environment);
		stateReq.setConversationId(gameId);

		getAgent().send(stateReq);
		mt = MessageTemplate.and(
			MessageTemplate.MatchSender(environment),
			MessageTemplate.MatchConversationId(gameId)
		);
		state = PROCESSING_STATE;
	}

	private void processGameState() {
		ActionUtils.receiveMessage(this, mt, m -> {
			String predicate = m.getContent();
			if (predicate.startsWith("Percept")) {
				StatePercept percept = new StatePercept(predicate);
				ACLMessage naturalLangMessage = new ACLMessage(ACLMessage.REQUEST);
				naturalLangMessage.addReceiver(navigator);
				naturalLangMessage.setConversationId(m.getConversationId());
				naturalLangMessage.setContent(NaturalLanguageUtils.transformPerceptToNaturalLanguage(percept));
				mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
					MessageTemplate.MatchConversationId(naturalLangMessage.getConversationId())
				);
				state = PROCESSING_ACTION;
				getAgent().send(naturalLangMessage);
			}
		});
	}

	private void passControlCommand() {
		ActionUtils.receiveMessage(this, mt, m -> {
			EnvironmentState.GameAction parsedAction = NaturalLanguageUtils.transformPhraseIntoAction(m.getContent());
			if (parsedAction != null) {
				ACLMessage actMessage = new ACLMessage(ACLMessage.PROPOSE);
				actMessage.addReceiver(environment);
				actMessage.setConversationId(m.getConversationId());
				actMessage.setContent(parsedAction.getPredicateValue());
				getAgent().send(actMessage);
				getAgent().blockingReceive(MessageTemplate.and(
					MessageTemplate.MatchSender(environment),
					MessageTemplate.and(
						MessageTemplate.MatchConversationId(m.getConversationId()),
						MessageTemplate.or(
							MessageTemplate.and(
								MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL),
								MessageTemplate.MatchContent("NO")
							),
							MessageTemplate.and(
								MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
								MessageTemplate.MatchContent("OK")
							)
						)
					)
				));
			}
			state = REQUESTING_STATE;
		});
	}

	enum GameCycleState {
		REQUESTING_STATE,
		PROCESSING_STATE,
		PROCESSING_ACTION;
	}
}
