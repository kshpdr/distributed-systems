package de.tuberlin.cit.vs;

import java.io.Serializable;
import java.util.Arrays;

public class Order implements Serializable {

    // TODO: understand, what valid is used for
    private String orderId;
    private String customerId;
    private String firstName;
    private String lastName;
    private String surfboardsNumber;
    private String suitsNumber;
    private String valid;
    private String validationResult = "false";

    public Order(String orderId, String customerId, String firstName, String lastName, String surfboardsNumber, String suitsNumber) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.surfboardsNumber = surfboardsNumber;
        this.suitsNumber = suitsNumber;
    }

    public Order(String customerId, String firstName, String lastName, String surfboardsNumber, String suitsNumber) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.surfboardsNumber = surfboardsNumber;
        this.suitsNumber = suitsNumber;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderForWeb(){
        return String.join(",", Arrays.asList(firstName, lastName, String.valueOf(surfboardsNumber), String.valueOf(suitsNumber), String.valueOf(customerId)));
    }

    public String getOrderForCall(){
        return String.join(",", Arrays.asList(String.valueOf(customerId), firstName + " " + lastName, String.valueOf(surfboardsNumber), String.valueOf(suitsNumber)));
    }

    public void validate(){
        this.validationResult = "true";
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                "customerId='" + String.valueOf(customerId) + '\'' +
                "full name='" + firstName + " " + lastName + '\'' +
                "surfboardsNumber='" + String.valueOf(surfboardsNumber) + '\'' +
                "suitsNumber='" + String.valueOf(suitsNumber) + '\'' +
                '}';
    }
}
