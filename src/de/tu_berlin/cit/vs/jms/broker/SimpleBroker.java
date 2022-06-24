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
    private ArrayList<Topic> topics = new ArrayList<>();    //list of topics

    private final MessageListener listener = new MessageListener() {
        @Override
        public void onMessage(Message msg) {
            try{
                if(msg instanceof ObjectMessage) {
                    String request = ((String) ((ObjectMessage) msg).getObject());
                    ArrayList<String> message = separateMessage(request);
                    System.out.println("Got a new message: " + request);


                    if (request.startsWith("list")){
                        String text = "";
                        for (Stock stock : stockList){
                            text += stock.toString();
                            text += "\n";
                        }
                        String clientName = message.get(0);

                        Queue queue = session.createQueue(clientName);
                        MessageProducer queueProducer = session.createProducer(queue);

                        //text += "this shit was printed within 'list' ";
                        ObjectMessage response = session.createObjectMessage(text);
                        queueProducer.send(response);
                        System.out.println("Response was sent");
                    }
                    if(request.startsWith("buy")){
                        String stockName = message.get(0);
                        String clientName = message.get(2);
                        Queue queue = session.createQueue(clientName);
                        MessageProducer queueProducer = session.createProducer(queue);

                        if (buy(request) == -1) {
                            String text = "-1: Not enough stocks available." + "\n";
                            ObjectMessage response = session.createObjectMessage(text);
                            queueProducer.send(response);
                        } else {
                            String text = "0: Buying stocks now." + "\n";
                            ObjectMessage response = session.createObjectMessage(text);
                            queueProducer.send(response);


                            String information = "";
                            Stock st = null;
                            //find stock
                            for(Stock stock : stockList){
                                if(stock.getName().equals(stockName)){
                                    st = stock;
                                    break;
                                }
                            }
                            information += st.toString();
                            information += ", -" + message.get(1); //amount

                            Topic topic = null;
                            for (Topic t : topics){
                                if (t.getTopicName().equals(stockName)){
                                    topic = t;
                                    break;
                                }
                            }

                            //broadcast information of stock amount to clients which subscribed
                            MessageProducer topicProducer = session.createProducer(topic);
                            ObjectMessage info = session.createObjectMessage(information);
                            topicProducer.send(info);


                        }
                    }
                    if(request.startsWith("sell")){
                        String stockName = message.get(0);
                        String clientName = message.get(2);
                        Queue queue = session.createQueue(clientName);
                        MessageProducer queueProducer = session.createProducer(queue);

                        if(sell(request) == -1){
                            System.out.println("Something went wrong");
                        }else{
                            String text = ": Selling stocks now." + "\n";
                            ObjectMessage response = session.createObjectMessage(text);
                            queueProducer.send(response);


                            String information = "";
                            Stock st = null;
                            //find stock
                            for(Stock stock : stockList){
                                if(stock.getName().equals(stockName)){
                                    st = stock;
                                    break;
                                }
                            }
                            information += st.toString();
                            information += ", +" + message.get(1); //amount

                            Topic topic = null;
                            for (Topic t : topics){
                                if (t.getTopicName().equals(stockName)){
                                    topic = t;
                                    break;
                                }
                            }

                            //broadcast information of stock amount to clients which subscribed
                            MessageProducer topicProducer = session.createProducer(topic);
                            ObjectMessage info = session.createObjectMessage(information);
                            topicProducer.send(info);

                        }


                    }
                    if(request.startsWith("quit")){
                        String clientName = message.get(0);
                        System.out.println("Client " + clientName + " is quitting..");
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
        if(splitted.length > 2){
            message.add(splitted[2]); //amount
            message.add(splitted[3]); //clientName
        }


        return message;
    }

    public SimpleBroker(List<Stock> stockList) throws JMSException {
        // initialize connection factory with corresponding connection and session
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        this.connection = connectionFactory.createConnection();
        connection.start();

        // create session and queue for the messages
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //Queue queue = session.createQueue("queue");
        Topic topic = session.createTopic("Server Topic");

        //create consumer and start a message listener
        this.serverConsumer = session.createConsumer(topic);
        this.serverProducer = session.createProducer(topic);
        serverConsumer.setMessageListener(listener);

        this.stockList = stockList;
        for(Stock stock : stockList) {
            Topic stockTopic = session.createTopic(stock.getName());
            // createConsumer() is necessary because AcitveMQ is not for creation but administration for existing topics
            MessageConsumer topicConsumer = session.createConsumer(stockTopic);
            topics.add(stockTopic);    //internal list of topics

        }
    }
    public void stop() throws JMSException {

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
