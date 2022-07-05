package de.tuberlin.cit.vs.applications;

import de.tuberlin.cit.vs.Order;
import de.tuberlin.cit.vs.Vote;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.IOException;

public class BillingSystem {

    public static void main(String[] args) throws JMSException {
        try {
            ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            conFactory.setTrustAllPackages(true);
            Connection con = conFactory.createConnection();

            // created two queues for getting and sending messages back
            final Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue inQueue = session.createQueue("billingIn");
            Queue outQueue = session.createQueue("billingOut");
            MessageConsumer consumer = session.createConsumer(inQueue);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        Order order = (Order)((ObjectMessage)message).getObject();

                        System.out.println("Billing system got the following order: " + order.toString());
                        order.validate();

                        // randomly decide whether customer has enough money
                        ObjectMessage answer = session.createObjectMessage(order);
                        boolean payed = Math.random() > 0.5;
                        answer.setBooleanProperty("payed", payed);

                        MessageProducer producer = session.createProducer(outQueue);
                        producer.send(answer);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });

            con.start();
            System.in.read();
            con.close();
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
