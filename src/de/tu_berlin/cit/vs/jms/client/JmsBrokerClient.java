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
import org.apache.activemq.command.ActiveMQBlobMessage;
import org.w3c.dom.Text;


public class JmsBrokerClient {
    private final String clientName;
    private Session session;
    private Connection connection;
    private MessageProducer clientProducer;
    private MessageConsumer clientConsumer;

    public JmsBrokerClient(String clientName) throws JMSException {
        this.clientName = clientName;
        
        /* TODO: initialize connection, sessions, consumer, producer, etc. */
        // initialize connection factory with corresponding connection
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        this.connection = connectionFactory.createConnection();
        connection.start();

        // create session and queue for the messages
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("queue");
        this.clientProducer = session.createProducer(queue);
        this.clientConsumer = session.createConsumer(queue);
        clientConsumer.setMessageListener(listener);
    }

    private final MessageListener listener = new MessageListener() {
        @Override
        public void onMessage(Message msg) {
            try{
                if(msg instanceof ObjectMessage) {
                    //TODO
                    String content = ((String) ((ObjectMessage) msg).getObject());
                    System.out.println(content);
                }
            }
            catch (JMSException e){
            }
        }
    };
    
    public void requestList() throws JMSException {
        //Create a message
        String content = "list";
        ObjectMessage msg = session.createObjectMessage(content);

        clientProducer.send(msg);
        System.out.println("Command 'list' was sent");
    }
    
    public void buy(String stockName, int amount) throws JMSException {

        String content = "buy," + stockName + "," + amount;
        ObjectMessage msg = session.createObjectMessage(content);

        clientProducer.send(msg);
        System.out.println("Command 'buy " + stockName + " " + amount + "' was sent");
    }
    
    public void sell(String stockName, int amount) throws JMSException {

        String content = stockName + "," + amount;
        ObjectMessage msg = session.createObjectMessage(content);

        clientProducer.send(msg);
        System.out.println("Command 'sell " + stockName + " " + amount + "' was sent");
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
