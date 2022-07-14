package de.tuberlin.cit.vs.applications;

import com.github.javafaker.Faker;
import de.tuberlin.cit.vs.Order;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class CallCenterOrderSystem {

    // to track list of saved orders
    private static int orderListId = 0;
    private static int orderCounter = 0;


    static {
        File file2 = new File("data/call_center_orders");
        for(File file: file2.listFiles())
            if (!file.isDirectory())
                file.delete();
    }


    public static String generateOrder(){
        // TODO: delete orderId from here, necessary in OrderFactory, not in Call Center
        // generate fake names
        Faker faker = new Faker();
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();

        // generate random numbers from 1 to 8
        String surfboardsNumber = String.valueOf((int)((Math.random() * (8 - 1)) + 1));
        String suitsNumber = String.valueOf((int)((Math.random() * (8 - 1)) + 1));

        //generate random id from 1 to 100000
        String customerId = String.valueOf((int)((Math.random() * (100000 - 1)) + 1));
        String orderId = "call" + String.valueOf(orderCounter);

        // instantiate order
        Order order = new Order(orderId, customerId, firstName, lastName, surfboardsNumber, suitsNumber);

        orderCounter++;
        return order.getOrderForCall();
    }

    public static TimerTask task = new TimerTask() {
        @Override
        public void run() {
            try {
                // create new file to store the orders
                String orderPath = "data/call_center_orders/call_center_order" + String.valueOf(orderListId) + ".txt";
                File orderFile = new File(orderPath);
                // delete file if exists for debugging purposes
                Files.deleteIfExists(orderFile.toPath());
                orderFile.getParentFile().mkdirs();
                orderFile.createNewFile();

                // generate random amount of orders from 1 to 20 to one file
                int ordersNumber = (int)((Math.random() * (20 - 1)) + 1);
                for (int i = 0; i < ordersNumber; i++) {
                    // write the orders in the file
                    String order = generateOrder() + "\n";
                    FileWriter fw = new FileWriter(orderPath, true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(order);
                    bw.close();
                }
                orderListId++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public static void main(String[] args) throws IOException, InterruptedException {
        /*
        while(true){
            Timer timer = new Timer();
            // generates new order every 2 minutes (for debug 10 seconds)
            timer.scheduleAtFixedRate(task, 10*1000, 10*1000);

        }

         */


        try {
            ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            conFactory.setTrustAllPackages(true);
            Connection con = conFactory.createConnection();
            con.start();

            final Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue orderQueue = session.createQueue("order");
            MessageConsumer consumer = session.createConsumer(orderQueue);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {

                        String orderPath = "data/call_center_orders/call_center_order" + String.valueOf(orderListId) + ".txt";
                        File orderFile = new File(orderPath);
                        // delete file if exists for debugging purposes
                        Files.deleteIfExists(orderFile.toPath());
                        orderFile.getParentFile().mkdirs();
                        orderFile.createNewFile();
                        String order = ((TextMessage)message).getText();





                        FileWriter fw = new FileWriter(orderPath, true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(order);
                        bw.close();
                        orderListId++;
                    } catch (JMSException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });






        }catch (JMSException e){
            e.printStackTrace();
        }

    }
}
