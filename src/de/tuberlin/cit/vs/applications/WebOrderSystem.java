package de.tuberlin.cit.vs.applications;
import com.github.javafaker.Faker;
import de.tuberlin.cit.vs.Order;
import org.apache.camel.Producer;
import org.apache.camel.ProducerTemplate;

import java.util.*;

// TODO: send the messages directly to the CamelMain, probably with Java Beans

public class WebOrderSystem {

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

        // generate random id from 1 to 100000
        String customerId = String.valueOf((int)((Math.random() * (100000 - 1)) + 1));
        String orderId = "web" + String.valueOf(orderCounter);

        // instantiate order
        Order order = new Order(orderId, customerId, firstName, lastName, surfboardsNumber, suitsNumber);

        orderCounter++;
        return order.getOrderForWeb();
    }

    public static TimerTask task = new TimerTask() {
        @Override
        public void run() {
            String order = generateOrder();
            System.out.println(order);

        }
    };

    public static void main(String[] args) {
        while(true){
            Timer timer = new Timer();

            // generates new order every 20 seconds
            timer.scheduleAtFixedRate(task, 5*1000, 5*1000);
        }
    }
}