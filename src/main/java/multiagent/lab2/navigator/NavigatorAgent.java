package multiagent.lab2.navigator;

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

import java.util.Scanner;

public class NavigatorAgent extends Agent {
	private AID spelunker;

	@Override
	protected void setup() {
		DFAgentDescription description = new DFAgentDescription();
		description.setName(this.getAID());

		ServiceDescription sd = new ServiceDescription();
		sd.setName("wumpus-nav");
		sd.setType("nav");
		description.addServices(sd);

		try {
			DFService.register(this, description);
			System.out.println("Navigation service registered");
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		addBehaviour(new NavInitBehaviour());
	}

	class NavInitBehaviour extends ProcessDependentBehaviour {
		private MessageTemplate requestTemplate = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
			MessageTemplate.MatchContent("NavRequest")
		);

		@Override
		public void action() {
			BehaviourUtils.receiveMessage(this, requestTemplate, m -> {
				System.out.println("Request for navigation arrived.");
				spelunker = m.getSender();
				getAgent().addBehaviour(new NavigationBehaviour());
				ACLMessage ok = m.createReply();
				ok.setPerformative(ACLMessage.CONFIRM);
				ok.setContent("OK");
				getAgent().send(ok);

				done = true;
			});
		}
	}

	private class NavigationBehaviour extends ProcessDependentBehaviour {
		private MessageTemplate reqTemplate = MessageTemplate.and(
			MessageTemplate.MatchSender(spelunker),
			MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
		);

		private Scanner console = new Scanner(System.in);

		@Override
		public void action() {
			BehaviourUtils.receiveMessage(this, reqTemplate, m -> {
				System.out.println(m.getContent());
				if (m.getContent().contains("Current tick")) {
					System.out.println("Your move?");
					ACLMessage reply = m.createReply();
					reply.setPerformative(ACLMessage.PROPOSE);
					String command = console.nextLine();
					reply.setContent(command);
					getAgent().send(reply);
				} else {
					getAgent().doDelete();
				}
			});
		}
	}

	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
			System.out.println("Navigator service deregistered");
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
}
