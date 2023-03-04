package de.tuberlin.cit.vs;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.aggregate.AggregationStrategy;

//simply combines Exchange String body values using '+' as a delimiter
class StringAggregationStrategy implements AggregationStrategy {

    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            return newExchange;
        }
        if(!(Integer.parseInt(oldExchange.getIn().getBody(Order.class).getOrderId()) % 2 == 0)){
            return newExchange;
        }
        Boolean available = (Boolean) newExchange.getIn().getHeader("available");
        Boolean payed = (Boolean) oldExchange.getIn().getHeader("payed");

        if(payed && available){
            newExchange.getIn().setHeader("valid", true);
            return newExchange;
        }else{
            newExchange.getIn().setHeader("valid", false);
            return newExchange;
        }


    }
}


public class CamelMain {

    private static Processor validationFactory2 = new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
            Order order = exchange.getIn().getBody(Order.class);

            order.setFullValidity("1");
            exchange.getIn().setBody(order);
        }
    };

    private static Processor validationFactory = new Processor() {
        private int orderId = 0;
        private int switcher = 0;
        @Override
        public void process(Exchange exchange) throws Exception {
            //System.out.println(exchange.getIn());

            String[] parts = exchange.getIn().getBody(String.class).split(",");
            String customerId = parts[0];

            String[] fullName = parts[1].split(" ");
            String firstName = fullName[0];
            String lastName = fullName[1];

            String surfboardsNumber = parts[2];
            String suitsNumber = parts[3];



            boolean payed = false;
            boolean available = false;
            String billingValidation = parts[4];
            String inventoryValidation = parts[5];
            if(billingValidation.equals("1")) payed = true;
            if(inventoryValidation.equals("1")) available = true;

            String orderId = String.valueOf(this.orderId);
            this.switcher++;
            if(this.switcher == 2) {
                this.switcher = 0;
                this.orderId++;
            }

            Order order = new Order(orderId, customerId, firstName, lastName, surfboardsNumber, suitsNumber);
            exchange.getIn().setBody(order);
            exchange.getIn().setHeader("payed", payed);
            exchange.getIn().setHeader("available", available);
        }
    };

    private static Processor orderFactory = new Processor() {
        private int orderId = 0;

        @Override
        public void process(Exchange exchange) throws Exception {
            String[] parts = exchange.getIn().getBody(String.class).split(",");
            String customerId = parts[0];

            String[] fullName = parts[1].split(" ");
            String firstName = fullName[0];
            String lastName = fullName[1];

            String surfboardsNumber = parts[2];
            String suitsNumber = parts[3];

            String orderId = String.valueOf(this.orderId);
            this.orderId++;

            Order order = new Order(orderId, customerId, firstName, lastName, surfboardsNumber, suitsNumber);
            exchange.getIn().setBody(order);
            // for debug
            //System.out.println(order.toString());
        }
    };


    public static void main(String[] args) throws Exception {
        DefaultCamelContext ctxt = new DefaultCamelContext();
        ActiveMQComponent activeMQComponent = ActiveMQComponent.activeMQComponent();
        activeMQComponent.setTrustAllPackages(true);
        ctxt.addComponent("activemq", activeMQComponent);

        RouteBuilder route = new RouteBuilder() {
            @Override
            public void configure() throws Exception {


                //Point-To-Point-Channel from CallCenterOderSystem to Publish-Subscribe-Channel:
                //
                //     [Msg Translator]   [P2P]           [PubSub]  /-----> BillingSystem    -----> ...
                //  DATA --------> order -----> billingIn --------><
                //                                                  \-----> InventorySystem  -----> ...
                //
                //Get orders from data file, convert to orders and put in topic 'billingIn'
                from("file:data/call_center_orders?noop=true")
                        .split(body().tokenize("\n"))
                        .process(orderFactory)
                        .to("activemq:topic:billingIn");
                //
                // billingSystem    ----->\
                //                         > ----> validated_orders.txt
                // inventorySystem  ----->/
                //



                //
                // validated_orders[i]   ----->\
                //                              > [Aggregator] -----> validation
                // validated_orders[i+1] ----->/
                //
                from("file:data/validated_orders?noop=true")
                        .split(body().tokenize("\n"))
                        .process(validationFactory)
                        .aggregate(constant(0), new StringAggregationStrategy()).completionSize(2)
                        .to("activemq:queue:validation");


                //
                // validation ----> [Content Enricher + Content based Router] ----> validation2
                //
                from("activemq:queue:validation").choice()
                        .when(header("valid")).process(validationFactory2).to("activemq:queue:validation2")
                        .otherwise().to("activemq:queue:validation2");


            }
        };

        ctxt.addRoutes(route);

        ctxt.start();
        System.in.read();
        ctxt.stop();
    }
}
