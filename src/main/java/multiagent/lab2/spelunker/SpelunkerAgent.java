package multiagent.lab2.spelunker;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import multiagent.lab2.ProcessDependentBehaviour;

public class SpelunkerAgent extends Agent {
	private AID environment;

	@Override
	protected void setup() {
		System.out.println("Spelunker agent created");
		addBehaviour(new ProcessDependentBehaviour() {
			@Override
			public void action() {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setName("wumpus-env");
				sd.setType("env");
				template.addServices(sd);
				try {
					DFAgentDescription[] envs = DFService.search(getAgent(), template);
					if (envs.length > 0) {
						done = true;
						ACLMessage gameReq = new ACLMessage(ACLMessage.REQUEST);
						gameReq.setContent("GameRequest");
						gameReq.addReceiver(envs[0].getName());
						getAgent().send(gameReq);
						getAgent().addBehaviour(new ProcessDependentBehaviour() {
							private MessageTemplate mt = MessageTemplate.and(
								MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
								MessageTemplate.MatchContent("OK")
							);

							@Override
							public void action() {
								ACLMessage message = getAgent().receive(mt);
								if (message != null) {
									System.out.println("Game environment found");
									environment = message.getSender();
									addBehaviour(new GameCycleBehaviour());
									done = true;
								} else {
									block();
								}
							}
						});
					}
				} catch (FIPAException e) {
					e.printStackTrace();
				}
			}
		});
	}

	class GameCycleBehaviour extends ProcessDependentBehaviour {
		private MessageTemplate mt;
		private int state = 0;
		private String gameId = "game" + System.currentTimeMillis();

		@Override
		public void action() {
			switch (state) {
				case 0:
					requestGameState();
					break;
				case 1:
					processGameState();
					break;
				case 2:
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
			state = 1;
		}

		private void processGameState() {

		}

		private void passControlCommand() {

		}
	}
}
