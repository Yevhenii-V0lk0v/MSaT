package multiagent.lab2;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.function.Consumer;

public class ActionUtils {

	/**
	 * A method, that encompasses basic stages of receiving a message of some template
	 * @param behaviour A behaviour that needs to receive a message
	 * @param mt A certain pattern of message that is required. Can be null
	 * @param messageProcessor An action to be performed, if the message is received
	 */
	public static void receiveMessage(Behaviour behaviour, MessageTemplate mt, Consumer<ACLMessage> messageProcessor) {
		ACLMessage message = behaviour.getAgent().receive(mt);
		if (message != null) {
			messageProcessor.accept(message);
		} else {
			behaviour.block();
		}
	}
}
