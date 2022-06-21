package de.tu_berlin.cit.vs.jms.broker;

import java.util.*;
import javax.jms.*;
import javax.jms.Queue;

import de.tu_berlin.cit.vs.jms.common.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class SimpleBroker {

    private Session session;
    private Connection connection;
    private MessageConsumer serverConsumer;
    private MessageProducer serverProducer;
    private List<Stock> stockList;
    private ArrayList<Topic> topics = new ArrayList<>();

    private final MessageListener listener = new MessageListener() {
        @Override
        public void onMessage(Message msg) {
            try{
                if(msg instanceof ObjectMessage) {
                    String request = ((String) ((ObjectMessage) msg).getObject());
                    System.out.println("Got a new message: " + request);

                    if (request.startsWith("list")){
                        String text = "";
                        for (Stock stock : stockList){
                             text += stock.toString();
                             text += "\n";
                        }
                        //text += "this shit was printed within 'list' ";
                        ObjectMessage response = session.createObjectMessage(text);
                        serverProducer.send(response);
                        System.out.println("Response was sent");
                    }
                    if(request.startsWith("buy")){
                        if (buy(request) == -1) {
                            String text = "-1: Not enough stocks available." + "\n";
                            ObjectMessage response = session.createObjectMessage(text);
                            serverProducer.send(response);
                        } else {
                            String text = "0: Buying stocks now." + "\n";
                            ObjectMessage response = session.createObjectMessage(text);
                            serverProducer.send(response);
                        }
                    }
                    if(request.startsWith("sell")){
                        sell(request);
                    }
                }
            }
            catch (JMSException e){
            }
        }
    };

    private ArrayList<String> separateMessage(String request) {
        ArrayList<String> message = new ArrayList<>();
        String[] splitted = request.split(",");
        message.add(splitted[1]); //stockName
        message.add(splitted[2]); //amount

        return message;
    }

    public SimpleBroker(List<Stock> stockList) throws JMSException {
        /* TODO: initialize connection, sessions, etc. */
        // initialize connection factory with corresponding connection and session
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        this.connection = connectionFactory.createConnection();
        connection.start();

        // create session and queue for the messages
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("queue");

        //create consumer and start a message listener
        this.serverConsumer = session.createConsumer(queue);
        this.serverProducer = session.createProducer(queue);
        serverConsumer.setMessageListener(listener);

        this.stockList = stockList;
        for(Stock stock : stockList) {
            /* TODO: prepare stocks as topics */
            Topic stockTopic = session.createTopic(stock.getName());
            topics.add(stockTopic);
        }
    }
    
    public void stop() throws JMSException {
        //TODO

    }

    public synchronized int buy(String request) throws JMSException {
        ArrayList<String> message = separateMessage(request);
        String stockName = message.get(0);
        int amount = Integer.parseInt(message.get(1));

        //search stock
        Stock myStock = null;
        for(Stock stock : stockList){
            if (stock.getName().equals(stockName)){
                myStock = stock;
            }
        }
        //decrease stock
        if(myStock != null){
            if (amount > myStock.getAvailableCount()) {
                return -1;
            } else {
                myStock.setAvailableCount(myStock.getAvailableCount() - amount);
            }
        }
        return 0;
    }
    
    public synchronized int sell(String request) throws JMSException {
        ArrayList<String> message = separateMessage(request);
        String stockName = message.get(0);
        int amount = Integer.parseInt(message.get(1));

        //search stock
        Stock myStock = null;
        for(Stock stock : stockList){
            if (stock.getName().equals(stockName)){
                myStock = stock;
            }
        }

        //increase stock
        if(myStock != null){
            myStock.setAvailableCount(myStock.getAvailableCount() + amount);
        }
        return 0;
    }
}
