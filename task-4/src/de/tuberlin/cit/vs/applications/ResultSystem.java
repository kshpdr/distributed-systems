package de.tuberlin.cit.vs.applications;

import de.tuberlin.cit.vs.Order;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import javax.jms.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ResultSystem {

    public static void main(String[] args) throws JMSException {
        try {
            ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            conFactory.setTrustAllPackages(true);
            Connection con = conFactory.createConnection();

            // listen to the topic: billingIn
            final Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue validate = session.createQueue("validation2");
            MessageConsumer consumer = session.createConsumer(validate);

            consumer.setMessageListener(new MessageListener() {

                @Override
                public void onMessage(Message message) {
                    try {
                        Order order = (Order)((ObjectMessage)message).getObject();

                        if(order.getFullValidity().equals("1")){
                            System.out.println("Order successfull: " + order);
                        }else{
                            System.err.println("Order failed: " + order);
                        }


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


