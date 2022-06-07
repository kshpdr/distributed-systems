package de.tu_berlin.cit.vs.jms.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;

import de.tu_berlin.cit.vs.jms.common.BuyMessage;
import de.tu_berlin.cit.vs.jms.common.ListMessage;
import de.tu_berlin.cit.vs.jms.common.RegisterMessage;
import de.tu_berlin.cit.vs.jms.common.RequestListMessage;
import de.tu_berlin.cit.vs.jms.common.SellMessage;
import de.tu_berlin.cit.vs.jms.common.Stock;
import de.tu_berlin.cit.vs.jms.common.UnregisterMessage;
import org.apache.activemq.ActiveMQConnectionFactory;


public class JmsBrokerClient {
    private final String clientName;

    public JmsBrokerClient(String clientName) throws JMSException {
        this.clientName = clientName;
        
        /* TODO: initialize connection, sessions, consumer, producer, etc. */
        // initialize connection factory with corresponding connection
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:8161");
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // create session and queue for the messages
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("clientQueue");
        MessageProducer producer = session.createProducer(queue);
    }
    
    public void requestList() throws JMSException {
        //TODO
    }
    
    public void buy(String stockName, int amount) throws JMSException {
        //TODO
    }
    
    public void sell(String stockName, int amount) throws JMSException {
        //TODO
    }
    
    public void watch(String stockName) throws JMSException {
        //TODO
    }
    
    public void unwatch(String stockName) throws JMSException {
        //TODO
    }
    
    public void quit() throws JMSException {
        //TODO
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter the client name:");
            String clientName = reader.readLine();
            
            JmsBrokerClient client = new JmsBrokerClient(clientName);
            
            boolean running = true;
            while(running) {
                System.out.println("Enter command:");
                String[] task = reader.readLine().split(" ");
                
                synchronized(client) {
                    switch(task[0].toLowerCase()) {
                        case "quit":
                            client.quit();
                            System.out.println("Bye bye");
                            running = false;
                            break;
                        case "list":
                            client.requestList();
                            break;
                        case "buy":
                            if(task.length == 3) {
                                client.buy(task[1], Integer.parseInt(task[2]));
                            } else {
                                System.out.println("Correct usage: buy [stock] [amount]");
                            }
                            break;
                        case "sell":
                            if(task.length == 3) {
                                client.sell(task[1], Integer.parseInt(task[2]));
                            } else {
                                System.out.println("Correct usage: sell [stock] [amount]");
                            }
                            break;
                        case "watch":
                            if(task.length == 2) {
                                client.watch(task[1]);
                            } else {
                                System.out.println("Correct usage: watch [stock]");
                            }
                            break;
                        case "unwatch":
                            if(task.length == 2) {
                                client.unwatch(task[1]);
                            } else {
                                System.out.println("Correct usage: watch [stock]");
                            }
                            break;
                        default:
                            System.out.println("Unknown command. Try one of:");
                            System.out.println("quit, list, buy, sell, watch, unwatch");
                    }
                }
            }
            
        } catch (JMSException | IOException ex) {
            Logger.getLogger(JmsBrokerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
