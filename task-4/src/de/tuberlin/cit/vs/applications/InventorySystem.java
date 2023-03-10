package de.tuberlin.cit.vs.applications;

import de.tuberlin.cit.vs.Order;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.*;
import java.util.Scanner;

public class InventorySystem {

    public static final String inventoryPath = "data/inventory_validated_orders/inventory_validation.txt";
    public static final String validatedPath = "data/validated_orders/validated_orders.txt";
    public static final String ackPath = "data/ACK.txt";
    public static int count = 0;

    static {
        File file = new File(inventoryPath);
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
        while (true){
            int condition = 0;
            File file = new File(ackPath);
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()){
                String data = sc.nextLine();
                if(data.equals("OKFromBilling")) condition = 1;
            }
            sc.close();

            if(condition == 1) break;

        }

        writeToFile("data/validated_orders/validated_orders_" + count + ".txt", validatedOrder, true);
        writeToFile(ackPath, "OKFromInventory", false);

    }

    public static void main(String[] args) throws JMSException {
        try {
            ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            conFactory.setTrustAllPackages(true);
            Connection con = conFactory.createConnection();

            final Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic billingIn = session.createTopic("billingIn");
            MessageConsumer consumer = session.createConsumer(billingIn);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        Order order = (Order)((ObjectMessage)message).getObject();

                        System.out.println("Inventory system got the following order: " + order.toString());
                        order.validate();

                        // randomly decide whether item is there or not
                        boolean available = Math.random() > 0.5;
                        if(available) order.setValid("1");


                        String validatedOrder = order.getOrderForCall() + ",0," + order.getValid() + "\n";

                        writeToFile(inventoryPath, validatedOrder, true);
                        updateFile(InventorySystem.count, validatedOrder);
                        InventorySystem.count++;

                    } catch (JMSException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
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
