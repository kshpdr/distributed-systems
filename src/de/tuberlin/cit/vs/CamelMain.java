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

    private static Processor voteFactory = new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
            String[] parts = exchange.getIn().getBody(String.class).split("\\-");
            String voterId = parts[0];
            Boolean vote = parts[1].equalsIgnoreCase("yes");
            exchange.getIn().setBody(new Vote(voterId, vote));

        }
    };

    private static Processor billingValidationFactory = new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
            System.out.println(exchange.getIn());
        }
    };

    private static Processor inventoryValidationFactory = new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
            System.out.println(exchange.getIn());
        }
    };

    private static Processor validationFactory = new Processor() {
        private int orderId = 0;
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
            this.orderId++;

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


    public static class VoteFilter {
        public boolean isYesVote(Vote vote) {
            return vote.getVote();
        }
    }

    // not necessary yet, might be later
    public static class OrderFilter {
        public boolean isYesOrder(Order order) {return true;}
    }

    public static class CountingAggregation implements AggregationStrategy {
        private int count = 0;

        @Override
        public Exchange aggregate(Exchange exchange, Exchange exchange1) {
            count++;
            exchange1.getIn().setBody(count);
            return exchange1;
        }
    }

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
                //                        [P2P]           [PubSub]  /-----> BillingSystem    -----> ...
                //  DATA --------> order -----> billingIn --------><
                //                                                  \-----> InventorySystem  -----> ...
                //
                //Get orders from data file, convert to orders and put in topic 'billingIn'
                from("file:data/call_center_orders?noop=true")
                        .split(body().tokenize("\n"))
                        .process(orderFactory)
                        .to("activemq:topic:billingIn");


                from("file:data/validated_orders?noop=true")
                        .split(body().tokenize("\n"))
                        .process(validationFactory)
                        .aggregate(constant(0), new StringAggregationStrategy()).completionSize(2)
                        .to("activemq:queue:validation");

                from("activemq:queue:validation").choice()
                        .when(header("valid")).to("stream:out")
                        .otherwise().to("stream:err");


                /*
                //               [P2P]
                // billingOut    ---->\
                //                     >------>billingValidationIn
                // inventoryOut  ---->/
                //               [P2P]

                from("activemq:queue:billingOut")
                        .to("activemq:queue:billingValidationIn");


                from("activemq:queue:inventoryOut")
                        .to("activemq:queue:billingValidationIn");


                //
                //  billingValidationIn[billingOut]   -----> \
                //                                            > [Aggregator] ----> billingValidationOut
                //  billingValidationIn[inventoryOut] -----> /
                //

                from("activemq:queue:billingValidationIn")
                        .aggregate(constant(0), new StringAggregationStrategy()).completionSize(2)
                        .to("activemq:queue:billingValidationOut");


                from("activemq:queue:billingValidationOut").choice()
                        .when(header("valid")).to("stream:out")
                        //.filter(method(orderFilter, "isYesOrder"))
                        //.aggregate(constant(0), new StringAggregationStrategy()).completionInterval(2)
                        .otherwise().to("stream:err");

                 */
/*
                //here change to your absolute path to votes, its for test purposes anyway
                from("file:src/votes?noop=true")
                    .split(body().tokenize("\n"))
                    .process(voteFactory)
                    .to("activemq:queue:validationIn");

                VoteFilter voteFilter = new VoteFilter();

                from("activemq:queue:validationOut")
                    .choice()
                        .when(header("validated"))
                            .filter(method(voteFilter, "isYesVote"))
                            .aggregate(constant(0), new CountingAggregation()).completionInterval(5)
                            .to("stream:out")
                            .end()
                        .endChoice().otherwise()
                            .to("stream:err");

 */
            }
        };

        ctxt.addRoutes(route);

        ctxt.start();
        System.in.read();
        ctxt.stop();
    }
}
