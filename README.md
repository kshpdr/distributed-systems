# Distributed Systems
This README provides an overview of the homeworks done by our group for the Distributed Systems course. The tasks were implemented using Java and different technologies such as Java NIO, Message Service (JMS) API (ActiveMQ), and Apache Camel.

## Task 1: SMTP Email Server
In this task, we implemented a reduced version of the SMTP protocol using Java NIO. The server supports the following commands:

- HELO: identifies the client and initiates a connection.
- MAIL FROM: specifies the sender email address.
- RCPT TO: specifies the recipient email address.
- DATA: starts the email message data transmission.
- HELP: provides help information about the server commands.
- QUIT: terminates the connection with the server.

## Task 2: Message Sequencer
For this task, we implemented a message sequencer that acts as a broadcasting service, allowing multiple threads to broadcast messages over it. The sequencer also supports Lamport timestamps to maintain the order of messages.

## Task 3: Simple Stock Exchange Broker
In this task, we implemented a simple stock exchange broker and a corresponding client using the Message Service (JMS) API (ActiveMQ). The broker receives buy/sell orders from clients and forwards them to the stock market. The client can also subscribe to a specific stock and receive updates on the price changes.

## Task 4: Dive-Surf Inc. Shop
For this task, we implemented a shop "Dive-Surf Inc." using Enterprise Application Integration with integration of Apache Camel. The shop provides a web interface for customers to browse and purchase products. The system integrates with external systems to manage inventory and payment processing.

These homeworks were made by Denis Koshelev, David Hörmann, Maxim Wölk, Georgiy Rudnev, and Hanna. If you have any questions or feedback, please do not hesitate to contact us.
