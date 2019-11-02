package multiagent.lab2.spelunker;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import multiagent.lab2.ProcessDependentBehaviour;
import multiagent.lab2.spelunker.behaviour.GameCycleBehaviour;

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
					// TODO: 02.11.2019 Add search for a navigator
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
									addBehaviour(new GameCycleBehaviour(environment, navigator));
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
}
