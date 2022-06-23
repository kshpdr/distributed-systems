package de.tu_berlin.cit.vs.jms.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;
import javax.jms.Queue;
import java.util.concurrent.TimeUnit;

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
    private MessageProducer clientProducer;   //wenn wir nachrichten an den SimpleBroker schreiben
    private MessageConsumer clientConsumer;     //wenn wir Nachrichten vom SimpleBroker erhalten
    private MessageConsumer queueConsumer;
    private MessageProducer queueProducer;
    private List<String> clientStocks = new ArrayList<>(); // save stocks of client
    private List<List<Object>> topicConsumers = new ArrayList<>();
    private String tmpStockName;
    private int tmpAmount;

    public JmsBrokerClient(String clientName) throws JMSException {
        this.clientName = clientName;

        /* TODO: initialize connection, sessions, consumer, producer, etc. */
        // initialize connection factory with corresponding connection
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        this.connection = connectionFactory.createConnection();
        connection.start();

        // create session and queue for the messages
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic topic = session.createTopic("Server Topic");
        this.clientProducer = session.createProducer(topic);
        //this.clientConsumer = session.createConsumer(topic);
        //clientConsumer.setMessageListener(listener);

        //for 1:1 connection
        Queue queue = session.createQueue(clientName);
        this.queueConsumer = session.createConsumer(queue);
        this.queueProducer = session.createProducer(queue);
        queueConsumer.setMessageListener(listener);
    }

    private final MessageListener listener = new MessageListener() {
        @Override
        public void onMessage(Message msg) {
            try{
                if(msg instanceof ObjectMessage) {
                    //TODO
                    String content = ((String) ((ObjectMessage) msg).getObject());
                    System.out.println(content);
                    System.out.println("received content from broker");

                    if (content.startsWith("0")){
                        increaseClientStocks(tmpStockName, tmpAmount);
                    }
                }
            }
            catch (JMSException e){
            }
        }
    };

    private final MessageListener topicListener = new MessageListener() {
        @Override
        public void onMessage(Message msg) {
            try{
                if(msg instanceof ObjectMessage) {
                    //TODO
                    String content = ((String) ((ObjectMessage) msg).getObject());
                    System.out.println(content);
                    System.out.println("received content from stock");

                    if (content.startsWith("0")){
                        increaseClientStocks(tmpStockName, tmpAmount);
                    }
                }
            }
            catch (JMSException e){
            }
        }
    };

    public void requestList() throws JMSException {
        //Create a message
        String content = "list," + clientName;
        ObjectMessage msg = session.createObjectMessage(content);
        clientProducer.send(msg);
        System.out.println("Command 'list' was sent");
    }

    public void buy(String stockName, int amount) throws JMSException {
        this.tmpAmount = amount;
        this.tmpStockName = stockName;
        String content = "buy," + stockName + "," + amount + "," + clientName;
        ObjectMessage msg = session.createObjectMessage(content);
        clientProducer.send(msg);
        System.out.println("Command 'buy " + stockName + " " + amount + "' was sent");

    }

    public void sell(String stockName, int amount) throws JMSException {
        int count = 0; // count number of stocks in our list
        for (String currentStock: clientStocks) {
            if (stockName.equals(currentStock)) {
                count++;
            }
        }
        if (count < amount) {
            System.out.println("You only have "+ count + " of " + stockName + " stocks to sell");
            return; // we dont have enough stocks
        }
        String content = "sell," + stockName + "," + amount + "," + clientName;
        ObjectMessage msg = session.createObjectMessage(content);
        clientProducer.send(msg);
        System.out.println("Command 'sell " + stockName + " " + amount + "' was sent");

        // remove sold stocks from list
        ListIterator<String> iter = clientStocks.listIterator();
        while(iter.hasNext() && count != 0){
            if(iter.next().equals(stockName)){
                iter.remove();
                count--;
            }
        }
        System.out.println("Your stocks: ");
        System.out.println(clientStocks);
    }

    public void increaseClientStocks(String stockName, int amount) throws JMSException {
        while (amount != 0){
            clientStocks.add(stockName);    //add amount of bought stocks to list
            amount--;
        }
        System.out.println("Your stocks: ");
        System.out.println(clientStocks);
    }

    public void watch(String stockName) throws JMSException {
        //TODO
        //subscribe to existing topic
        Topic topic = session.createTopic(stockName);
        MessageConsumer topicConsumer = session.createConsumer(topic);
        topicConsumer.setMessageListener(topicListener);

        List topicConsumerAndName = new ArrayList();
        topicConsumerAndName.add(stockName);
        topicConsumerAndName.add(topicConsumer);
        topicConsumers.add(topicConsumerAndName);
        System.out.println("Command 'watch " + stockName + "' was sent");

    }

    public void unwatch(String stockName) throws JMSException {
        //TODO
        for (List topicConsumerAndName : topicConsumers){
            if (((String)topicConsumerAndName.get(0)).startsWith(stockName)){
                ((MessageConsumer)topicConsumerAndName.get(1)).close();
                System.out.println("Stock " + stockName + " is now not being watched.");
            }
        }
    }

    public void quit() throws JMSException {
        if (clientStocks.size() == 0) {
            return;
        }
        int cntA = 0;
        int cntL = 0;
        int cntR = 0;

        for (int i = 0; i < clientStocks.size(); i++){
            if (clientStocks.get(i).equals("aldi")) {
                cntA++;
            }
            if (clientStocks.get(i).equals("lidl")) {
                cntL++;
            }
            if (clientStocks.get(i).equals("rewe")) {
                cntR++;
            }
        }
        sell("aldi", cntA);
        sell("lidl", cntL);
        sell("rewe", cntR);

        String content = "quit," + clientName;
        ObjectMessage msg = session.createObjectMessage(content);
        clientProducer.send(msg);
        System.out.println("Command 'quit' was sent");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter the client name: ");
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
