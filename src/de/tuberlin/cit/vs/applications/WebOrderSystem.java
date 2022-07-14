package de.tuberlin.cit.vs.applications;
import com.github.javafaker.Faker;
import de.tuberlin.cit.vs.Order;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.Producer;
import org.apache.camel.ProducerTemplate;

import javax.jms.*;
import javax.jms.Queue;
import java.io.IOException;
import java.util.*;

// TODO: send the messages directly to the CamelMain, probably with Java Beans




public class WebOrderSystem {

    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RESET = "\u001B[0m";

    private static int orderCounter = 0;

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
/*
    public static TimerTask task = new TimerTask() {
        @Override
        public void run() {
            String order = generateOrder() + "\n";


        }
    };
*/
    public static void main(String[] args) {

        try {
            ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            conFactory.setTrustAllPackages(true);
            Connection con = conFactory.createConnection();
            con.start();

            final Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue orderQueue = session.createQueue("order");
            MessageProducer prod = session.createProducer(orderQueue);


            while(true){

                Thread.sleep(5000);

                String order = generateOrder() + "\n";
/*
                order += generateOrder() + "\n";
                order += generateOrder() + "\n";
                order += generateOrder() + "\n";
                order += generateOrder() + "\n";

 */

                TextMessage textMessage = session.createTextMessage(order);
                prod.send(textMessage);
                System.out.println(ANSI_YELLOW + "New order created and sent: " + order + ANSI_RESET);
            }





        }catch (JMSException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*
        while(true){

            Timer timer = new Timer();

            // generates new order every 20 seconds
            timer.scheduleAtFixedRate(task, 5 * 1000, 5 * 1000);

        }

         */
    }
}