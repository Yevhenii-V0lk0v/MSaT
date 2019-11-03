package multiagent.lab2.spelunker;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import multiagent.lab2.BehaviourUtils;
import multiagent.lab2.ProcessDependentBehaviour;
import multiagent.lab2.spelunker.behaviour.GameCycleBehaviour;

public class SpelunkerAgent extends Agent {
	private AID environment;
	private AID navigator;

	@Override
	protected void setup() {
		addBehaviour(new EnvironmentSearchBehaviour());
		addBehaviour(new NavigatorSearchBehaviour());
	}

	class EnvironmentSearchBehaviour extends ProcessDependentBehaviour {
		@Override
		public void action() {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("env");
			template.addServices(sd);
			try {
				DFAgentDescription[] envs = DFService.search(getAgent(), template);
				if (envs.length > 0) {
					done = true;
					ACLMessage gameReq = new ACLMessage(ACLMessage.REQUEST);
					gameReq.setConversationId("GameRequest");
					gameReq.setContent("GameRequest");
					gameReq.addReceiver(envs[0].getName());
					getAgent().send(gameReq);
					getAgent().addBehaviour(new ProcessDependentBehaviour() {
						private MessageTemplate mt = MessageTemplate.and(
							MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
							MessageTemplate.and(
								MessageTemplate.MatchContent("OK"),
								MessageTemplate.MatchConversationId("GameRequest")
							)
						);

						@Override
						public void action() {
							BehaviourUtils.receiveMessage(this, mt, m -> {
								System.out.println("Game environment found");
								environment = m.getSender();
								if (environment != null && navigator != null) {
									addBehaviour(new GameCycleBehaviour(environment, navigator));
								}
								done = true;
							});
						}
					});
				}
			} catch (FIPAException e) {
				e.printStackTrace();
			}
		}
	}

	class NavigatorSearchBehaviour extends ProcessDependentBehaviour {
		@Override
		public void action() {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("nav");
			template.addServices(sd);
			try {
				DFAgentDescription[] envs = DFService.search(getAgent(), template);
				if (envs.length > 0) {
					done = true;
					ACLMessage navigReq = new ACLMessage(ACLMessage.REQUEST);
					navigReq.setConversationId("NavRequest");
					navigReq.setContent("NavRequest");
					navigReq.addReceiver(envs[0].getName());
					getAgent().send(navigReq);
					getAgent().addBehaviour(new ProcessDependentBehaviour() {
						private MessageTemplate mt = MessageTemplate.and(
							MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
							MessageTemplate.and(
								MessageTemplate.MatchContent("OK"),
								MessageTemplate.MatchConversationId("NavRequest")
							)
						);

						@Override
						public void action() {
							BehaviourUtils.receiveMessage(this, mt, m -> {
								System.out.println("Game navigator found");
								navigator = m.getSender();
								if (environment != null && navigator != null) {
									addBehaviour(new GameCycleBehaviour(environment, navigator));
								}
								done = true;
							});
						}
					});
				}
			} catch (FIPAException e) {
				e.printStackTrace();
			}
		}
	}
}
