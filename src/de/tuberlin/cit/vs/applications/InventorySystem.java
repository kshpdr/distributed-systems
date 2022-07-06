package de.tuberlin.cit.vs.applications;

import de.tuberlin.cit.vs.Order;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.IOException;

public class InventorySystem {

    public static void main(String[] args) throws JMSException {
        try {
            ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            conFactory.setTrustAllPackages(true);
            Connection con = conFactory.createConnection();

            // created two queues for getting and sending messages back
            final Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue inQueue = session.createQueue("inventoryIn");
            Queue outQueue = session.createQueue("inventoryOut");
            MessageConsumer consumer = session.createConsumer(inQueue);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        Order order = (Order)((ObjectMessage)message).getObject();

                        System.out.println("Inventory system got the following order: " + order.toString());
                        order.validate();

                        // randomly decide whether item is there or not
                        ObjectMessage answer = session.createObjectMessage(order);
                        boolean available = Math.random() > 0.5;
                        answer.setBooleanProperty("item available", available);

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
