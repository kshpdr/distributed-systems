package de.tuberlin.cit.vs;

import java.io.Serializable;
import java.util.Arrays;

public class Order implements Serializable {

    private String orderId;
    private int customerId;
    private String firstName;
    private String lastName;
    private int surfboardsNumber;
    private int suitsNumber;
    private Boolean order;

    public Order(String orderId, int customerId, String firstName, String lastName, int surfboardsNumber, int suitsNumber, Boolean order) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.surfboardsNumber = surfboardsNumber;
        this.suitsNumber = suitsNumber;
        this.order = order;
    }

    public String getOrderId() {
        return orderId;
    }

    public Boolean getOrder() {
        return order;
    }

    public String getOrderForWeb(){
        return String.join(",", Arrays.asList(firstName, lastName, String.valueOf(surfboardsNumber), String.valueOf(suitsNumber), String.valueOf(customerId)));
    }

    public String getOrderForCall(){
        return String.join(",", Arrays.asList(String.valueOf(customerId), firstName + " " + lastName, String.valueOf(surfboardsNumber), String.valueOf(suitsNumber)));
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                "customerId='" + String.valueOf(customerId) + '\'' +
                "full name='" + firstName + " " + lastName + '\'' +
                "surfboardsNumber='" + String.valueOf(surfboardsNumber) + '\'' +
                "suitsNumber='" + String.valueOf(suitsNumber) + '\'' +
                ", order=" + order +
                '}';
    }
}
