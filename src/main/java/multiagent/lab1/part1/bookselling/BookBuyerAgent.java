package multiagent.lab1.part1.bookselling;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BookBuyerAgent extends Agent {
	private static final String BOOK_TRADE = "book-trade";

	private String targetBookTitle;

	private AID[] sellers = new AID[]{
		new AID("seller1", AID.ISLOCALNAME),
		new AID("seller2", AID.ISLOCALNAME)
	};

	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! Buyer - agent " + getAID().getName() + " is ready");

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			targetBookTitle = (String) args[0];
			System.out.println("Trying to by " + targetBookTitle);
			addBehaviour(new TickerBehaviour(this, 6000) {
				@Override
				protected void onTick() {
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("book-selling");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(getAgent(), template);
						sellers = new AID[result.length];
						for (int i = 0; i < result.length; i++) {
							sellers[i] = result[i].getName();
						}
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}
					getAgent().addBehaviour(new RequestPerformer());
				}
			});
		} else {
			System.out.println("No book title specified");
			doDelete();
		}
	}

	@Override
	protected void takeDown() {
		System.out.println("Buyer-agent " + getAID().getName() + " terminating.");
	}

	class RequestPerformer extends Behaviour {
		private AID bestSeller;
		private int bestPrice;
		private int repliesCnt = 0;
		private MessageTemplate mt;
		private int step = 0;

		@Override
		public void action() {
			switch (step) {
				case 0:
					sendTradeRequest();
					break;
				case 1:
					processOffer();
					break;
				case 2:
					acceptBestProposal();
					break;
				case 3:
					processResult();
					break;
			}
		}

		private void sendTradeRequest() {
			ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
			for (AID seller : sellers) {
				System.out.println("Sending trade request to " + seller.getName());
				cfp.addReceiver(seller);
			}
			cfp.setContent(targetBookTitle);
			cfp.setConversationId("book-trade");
			cfp.setReplyWith("cfp" + System.currentTimeMillis());
			myAgent.send(cfp);

			mt = MessageTemplate.and(
				MessageTemplate.MatchConversationId(BOOK_TRADE),
				MessageTemplate.MatchInReplyTo(cfp.getReplyWith())
			);
			step = 1;
		}

		private void processOffer() {
			ACLMessage reply = getAgent().receive(mt);
			if (reply != null) {
				System.out.println("Got a reply from " + reply.getSender().getName());
				if (reply.getPerformative() == ACLMessage.PROPOSE) {
					int price = Integer.parseInt(reply.getContent());
					if (bestSeller == null || price < bestPrice) {
						bestPrice = price;
						bestSeller = reply.getSender();
					}
				}
				repliesCnt++;
				if (repliesCnt >= sellers.length) {
					step = 2;
				}
			} else {
				block();
			}
		}

		private void acceptBestProposal() {
			System.out.println("Best proposal is from " + bestSeller.getName());
			ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			order.addReceiver(bestSeller);
			order.setContent(targetBookTitle);
			order.setConversationId(BOOK_TRADE);
			order.setReplyWith("cfp" + System.currentTimeMillis());
			getAgent().send(order);
			mt = MessageTemplate.and(
				MessageTemplate.MatchConversationId(BOOK_TRADE),
				MessageTemplate.MatchInReplyTo(order.getReplyWith())
			);
			step = 3;
		}

		private void processResult() {
			ACLMessage reply = getAgent().receive(mt);
			if (reply != null) {
				if (reply.getPerformative() == ACLMessage.INFORM) {
					System.out.println(targetBookTitle + " successfully purchased.");
					System.out.println("Price = " + bestPrice);
					getAgent().doDelete();
				}
				step = 4;
			} else {
				block();
			}
		}

		@Override
		public boolean done() {
			return (step == 2 && bestSeller == null) || step == 4;
		}
	}
}

