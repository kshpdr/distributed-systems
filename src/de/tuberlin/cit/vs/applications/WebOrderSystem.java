package de.tuberlin.cit.vs.applications;
import com.github.javafaker.Faker;

import java.util.*;

public class WebOrderSystem {

    public static String generateOrder(){
        // generate fake names
        Faker faker = new Faker();
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();

        // generate random numbers from 1 to 8
        String surfboardsNumber = String.valueOf((int)((Math.random() * (8 - 1)) + 1));
        String suitsNumber = String.valueOf((int)((Math.random() * (8 - 1)) + 1));

        //generate random id from 1 to 100000
        String customerId = String.valueOf((int)((Math.random() * (100000 - 1)) + 1));

        String order = String.join(",", Arrays.asList(firstName, lastName, surfboardsNumber, suitsNumber, customerId));

        return order;
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
            timer.scheduleAtFixedRate(task, 20*1000, 20*1000);
        }
    }
}