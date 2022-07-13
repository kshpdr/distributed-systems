package de.tuberlin.cit.vs.applications;

import de.tuberlin.cit.vs.Order;
import de.tuberlin.cit.vs.Vote;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import javax.jms.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class BillingSystem {


    public static final String billingPath = "data/billing_validated_orders/billing_validation.txt";
    public static final String validatedPath = "data/validated_orders/validated_orders.txt";
    public static final String ackPath = "data/ACK.txt";
    public static int count = 0;


    static {
        File file = new File(billingPath);
        file.delete();

        File file2 = new File(validatedPath);
        file2.delete();
    }

    public static void writeToFile(String path, String content, Boolean append) throws IOException {
        FileWriter fw = new FileWriter(path, append);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.close();
    }

    private static void updateFile(int count, String validatedOrder) throws IOException {
        //wait till billingOrder has finished
        while (true) {
            int condition = 0;
            File file = new File(ackPath);
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String data = sc.nextLine();
                if (data.equals("OKFromInventory")) condition = 1;
            }
            sc.close();

            if (condition == 1) break;

        }

        writeToFile("data/validated_orders/validated_orders_" + count + ".txt", validatedOrder, true);
        writeToFile(ackPath, "OKFromBilling", false);


    }

    public static void main(String[] args) throws JMSException {

        try {
            ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            conFactory.setTrustAllPackages(true);
            Connection con = conFactory.createConnection();

            // created two queues for getting and sending messages back
            final Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic billingIn = session.createTopic("billingIn");
            Queue billingOut = session.createQueue("billingOut");
            MessageConsumer consumer = session.createConsumer(billingIn);
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
                        if(payed) order.setValid("1");
                        answer.setBooleanProperty("available", false);



                        String validatedOrder = order.getOrderForCall() + "," + order.getValid() + ",0\n";

                        writeToFile(billingPath, validatedOrder, true);
                        updateFile(BillingSystem.count, validatedOrder);
                        BillingSystem.count++;

                        //MessageProducer producer = session.createProducer(billingOut);
                        //producer.send(answer);
                    } catch (JMSException | IOException e) {
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
