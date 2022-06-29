package de.tuberlin.cit.vs.applications;
import com.github.javafaker.Faker;
import de.tuberlin.cit.vs.Order;

import java.util.*;

public class WebOrderSystem {

    private static int orderCounter = 0;

    public static String generateOrder(){
        // generate fake names
        Faker faker = new Faker();
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();

        // generate random numbers from 1 to 8
        int surfboardsNumber = (int)((Math.random() * (8 - 1)) + 1);
        int suitsNumber = (int)((Math.random() * (8 - 1)) + 1);

        //generate random id from 1 to 100000
        int customerId = (int)((Math.random() * (100000 - 1)) + 1);
        String orderId = "web" + String.valueOf(orderCounter);
        Boolean checked = false;

        // instantiate order
        Order order = new Order(orderId, customerId, firstName, lastName, surfboardsNumber, suitsNumber, checked);

        orderCounter++;
        return order.getOrderForWeb();
    }

    public static TimerTask task = new TimerTask() {
        @Override
        public void run() {
            System.out.println(generateOrder());
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