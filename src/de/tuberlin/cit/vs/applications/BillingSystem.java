package de.tuberlin.cit.vs.applications;

import de.tuberlin.cit.vs.Order;
import de.tuberlin.cit.vs.Vote;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.FileUtil;


import javax.jms.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class BillingSystem {


    public static final String billingPath = "data/billing_validated_orders/billing_validation.txt";
    public static final String validatedPath = "data/validated_orders";
    public static final String ackPath = "data/ACK.txt";
    public static int count = 0;


    static {
        //delete billingOrders before start
        File file1 = new File(billingPath);
        file1.delete();

        //delete every validated order before start
        File file2 = new File(validatedPath);
        for(File file: file2.listFiles())
            if (!file.isDirectory())
                file.delete();


    }

    public static void writeToFile(String path, String content, Boolean append) throws IOException {
        FileWriter fw = new FileWriter(path, append);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.close();
    }

    private static void updateFile(int count, String validatedOrder) throws IOException {
        //wait till billingOrder has finished
        //improvised synchronisation between inventorySystem and billingSystem
        while (true) {
            //read ACK.txt file until InventorySystem sends an ACK: OKFromInventory
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
        //sending own ACK: OKFromBilling
        writeToFile(ackPath, "OKFromBilling", false);


    }

    public static void main(String[] args) throws JMSException {

        try {
            ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            conFactory.setTrustAllPackages(true);
            Connection con = conFactory.createConnection();

            // listen to the topic: billingIn
            final Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic billingIn = session.createTopic("billingIn");
            MessageConsumer consumer = session.createConsumer(billingIn);
            consumer.setMessageListener(new MessageListener() {

                @Override
                public void onMessage(Message message) {
                    try {
                        Order order = (Order)((ObjectMessage)message).getObject();

                        System.out.println("Billing system got the following order: " + order.toString());
                        order.validate();

                        // randomly decide whether customer has enough money
                        boolean payed = Math.random() > 0.5;
                        if(payed) order.setValid("1");

                        //writes the order to billing_validation.txt
                        String validatedOrder = order.getOrderForCall() + "," + order.getValid() + ",0\n";

                        writeToFile(billingPath, validatedOrder, true);
                        //synchronizes with inventorySystem
                        updateFile(BillingSystem.count, validatedOrder);
                        BillingSystem.count++;



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
