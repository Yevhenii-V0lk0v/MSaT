package multiagent.lab1.part1.bookselling;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;
import java.util.Random;

public class BookSellerAgent extends Agent {
	private Hashtable<String, Integer> catalogue;

	@Override
	protected void setup() {
		catalogue = new Hashtable<>();
		Random rd = new Random();
		for (Object argument : getArguments()) {
			String bookInStore = (String) argument;
			catalogue.put(bookInStore, rd.nextInt(100));
		}

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("book-selling");
		sd.setName("JADE-book-trading");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		addBehaviour(new OfferRequestsServer());
		addBehaviour(new PurchaseOrdersServer());
		System.out.println("Seller started, awaiting requests");
	}

	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("Seller-agent " + getAID().getName() + " terminating.");
	}

	public void updateCatalogue(final String title, final int price) {
		addBehaviour(new OneShotBehaviour() {
			@Override
			public void action() {
				catalogue.put(title, price);
			}
		});
	}

	class OfferRequestsServer extends CyclicBehaviour {
		private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);

		@Override
		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();
				Integer price = catalogue.get(title);
				if (price != null) {
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(price.toString());
				} else {
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("non-available");
				}
				send(reply);
			} else {
				block();
			}
		}
	}

	class PurchaseOrdersServer extends CyclicBehaviour {
		private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);

		@Override
		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				System.out.println("Sold " + msg.getContent() + " to " + msg.getSender());
				catalogue.remove(msg.getContent());
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				getAgent().send(reply);
				if (catalogue.isEmpty()) {
					getAgent().doDelete();
				}
			} else {
				block();
			}
		}
	}
}
