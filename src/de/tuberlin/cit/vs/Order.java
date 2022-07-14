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
    private String valid = "0";
    private String validationResult = "false";
    private String fullValidity = "0";

    public String getFullValidity() {
        return fullValidity;
    }

    public void setFullValidity(String fullValidity) {
        this.fullValidity = fullValidity;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSurfboardsNumber() {
        return surfboardsNumber;
    }

    public void setSurfboardsNumber(String surfboardsNumber) {
        this.surfboardsNumber = surfboardsNumber;
    }

    public String getSuitsNumber() {
        return suitsNumber;
    }

    public void setSuitsNumber(String suitsNumber) {
        this.suitsNumber = suitsNumber;
    }

    public String getValid() {
        return valid;
    }

    public void setValid(String valid) {
        this.valid = valid;
    }

    public String getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(String validationResult) {
        this.validationResult = validationResult;
    }

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
