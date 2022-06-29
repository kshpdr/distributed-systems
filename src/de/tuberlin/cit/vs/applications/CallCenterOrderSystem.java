package de.tuberlin.cit.vs.applications;

import com.github.javafaker.Faker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class CallCenterOrderSystem {

    // to track list of saved orders
    public static int orderListId = 0;

    public static String generateOrder(){
        // generate fake names
        Faker faker = new Faker();
        String fullName = faker.name().fullName();

        // generate random numbers from 1 to 8
        String surfboardsNumber = String.valueOf((int)((Math.random() * (8 - 1)) + 1));
        String suitsNumber = String.valueOf((int)((Math.random() * (8 - 1)) + 1));

        //generate random id from 1 to 100000
        String customerId = String.valueOf((int)((Math.random() * (100000 - 1)) + 1));

        String order = String.join(",", Arrays.asList(customerId, fullName, surfboardsNumber, suitsNumber));

        return order;
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

    public static void main(String[] args) {
        while(true){
            Timer timer = new Timer();

            // generates new order every 2 minutes (for debug 10 seconds)
            timer.scheduleAtFixedRate(task, 10*1000, 10*1000);
        }
    }
}
