package de.tu_berlin.cit.vs.jms.broker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;

import de.tu_berlin.cit.vs.jms.common.BrokerMessage;
import de.tu_berlin.cit.vs.jms.common.RegisterMessage;
import de.tu_berlin.cit.vs.jms.common.Stock;
import de.tu_berlin.cit.vs.jms.common.UnregisterMessage;
import org.apache.activemq.ActiveMQConnectionFactory;


public class SimpleBroker {
    /* TODO: variables as needed */
    private Session session;
    private Connection connection;
    private MessageConsumer serverConsumer;
    private MessageProducer serverProducer;


    
    private final MessageListener listener = new MessageListener() {
        @Override
        public void onMessage(Message msg) {
            try{
                TextMessage response = session.createTextMessage();
                if(msg instanceof ObjectMessage) {
                    //TODO
                    TextMessage txtMsg = (TextMessage) msg;
                    String messageText = txtMsg.getText();
                    System.out.println(messageText);
                }
            }
            catch (JMSException e){
            }
        }
    };
    
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
        
        for(Stock stock : stockList) {
            /* TODO: prepare stocks as topics */
        }
    }
    
    public void stop() throws JMSException {
        //TODO
    }
    
    public synchronized int buy(String stockName, int amount) throws JMSException {
        //TODO
        return -1;
    }
    
    public synchronized int sell(String stockName, int amount) throws JMSException {
        //TODO
        return -1;
    }
    
    public synchronized List<Stock> getStockList() {
        List<Stock> stockList = new ArrayList<>();

        /* TODO: populate stockList */

        return stockList;
    }
}
