package multiagent.lab2.spelunker.behaviour;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import multiagent.lab2.BehaviourUtils;
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
			MessageTemplate.MatchPerformative(ACLMessage.INFORM)
		);
		state = PROCESSING_STATE;
	}

	private void processGameState() {
		BehaviourUtils.receiveMessage(this, mt, m -> {
			String predicate = m.getContent();
			GamePerceptType gameStage = GamePerceptType.getByPredicate(predicate.substring(0, predicate.indexOf('(')));
			if (gameStage != null) {
				ACLMessage naturalLangMessage = new ACLMessage(ACLMessage.REQUEST);
				naturalLangMessage.addReceiver(navigator);
				naturalLangMessage.setConversationId(m.getConversationId());
				switch (gameStage) {
					case PERCEPT:
						StatePercept percept = new StatePercept(predicate);
						naturalLangMessage.setContent(NaturalLanguageUtils.transformPerceptToNaturalLanguage(percept));
						state = PROCESSING_ACTION;
						mt = MessageTemplate.and(
							MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
							MessageTemplate.MatchConversationId(naturalLangMessage.getConversationId())
						);
						getAgent().send(naturalLangMessage);
						break;
					case WIN:
						naturalLangMessage.setContent(NaturalLanguageUtils.transformWinToNaturalLanguage(predicate));
						getAgent().send(naturalLangMessage);
						getAgent().doDelete();
						break;
					case LOSS:
						naturalLangMessage.setContent(NaturalLanguageUtils.transformLossToNaturalLanguage(predicate));
						getAgent().send(naturalLangMessage);
						getAgent().doDelete();
						break;
				}

			}
		});
	}

	private void passControlCommand() {
		BehaviourUtils.receiveMessage(this, mt, m -> {
			EnvironmentState.GameAction parsedAction = NaturalLanguageUtils.transformPhraseIntoAction(m.getContent());
			if (parsedAction != null) {
				ACLMessage actMessage = new ACLMessage(ACLMessage.PROPOSE);
				actMessage.addReceiver(environment);
				actMessage.setConversationId(m.getConversationId());
				actMessage.setContent(String.format("Action(%s)", parsedAction.getPredicateValue()));
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

	public enum GamePerceptType {
		PERCEPT("Percept"),
		WIN("Win"),
		LOSS("Loss");

		public static GamePerceptType getByPredicate(String predicate) {
			for (GamePerceptType type : values()) {
				if (type.textFormat.equals(predicate)) {
					return type;
				}
			}
			return null;
		}

		private final String textFormat;

		GamePerceptType(String textFormat) {
			this.textFormat = textFormat;
		}

		public String getTextFormat() {
			return textFormat;
		}
	}
}
