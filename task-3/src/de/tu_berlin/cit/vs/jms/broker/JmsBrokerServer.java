package de.tu_berlin.cit.vs.jms.broker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import de.tu_berlin.cit.vs.jms.common.Stock;

public class JmsBrokerServer {
    public static void main(String[] args) {
        try {
            List<Stock> stocks = new ArrayList<>();
            stocks.add(new Stock("aldi", 10, 2.0));
            stocks.add(new Stock("lidl", 10, 1.0));
            stocks.add(new Stock("rewe", 10, 0.5));
            
            SimpleBroker broker = new SimpleBroker(stocks);
            System.in.read();
            broker.stop();
        } catch (JMSException ex) {
            Logger.getLogger(JmsBrokerServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JmsBrokerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
