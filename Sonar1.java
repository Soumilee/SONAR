/*
 * AMQ Sonar CS656 V3.00
 * Sonar
 * Group: D11
 * Group Members: Ashray Kengunte Jayachandra(Ak379), Soumilee Ghosh(sg342), Sintu Boro(sb394), Noumala Hemanth Reddy(hn39), Sada Siva Tej Velalla(sv97)

 * Note: follow all instructions in this file
 * - 'ADC' means "add your code here"
 * - 'NOC' means don't change i.e. you should use as is
 * - Do not change any method signatures
 * - your own helper methods are OK
 */

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Topic;
import javax.jms.Destination;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Sonar1 {

    public static String getCurrentDateAndTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    public static void main(String[] args) throws Exception {

    	 if (args.length != 1) { 
	 	System.err.println( " Usage: java Sonar <shipname>  ## Consumer mode\n"); 
		System.exit(2);
			 }


        String pfile = "USS.properties";
        String bName = args[0];
        FileInputStream fis = null;
        Properties prop = null;
        try {
            fis = new FileInputStream(pfile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        prop = new Properties();
        try {
            prop.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String pbroker = prop.getProperty("broker");
        String pqueue = prop.getProperty("queue");
        String puser = prop.getProperty("username");
        String ppass = prop.getProperty("password");

                 
        Connection cx = null;
        Session sx = null; // NOC
        ConnectionFactory cf = null; // NOC
        // ADC additional vars here if needed
        TextMessage m = null;
        int totalMessages = 0;
        try {
            cf = new ActiveMQConnectionFactory(pbroker);
            cx = cf.createConnection(puser, ppass);
            sx = cx.createSession(false, Session.AUTO_ACKNOWLEDGE); // NOC
            // createSession should not change
            // make Destination, Consumer
            Destination destination = sx.createQueue(pqueue); // ADC, must use session.createXXXXXX
            // MessageConsumer // ADC, must use session.
            MessageConsumer messageConsumer = sx.createConsumer(destination);
            // start consumer
            cx.start();
            System.out.println("Connected to " + pbroker + ". Waiting for message...");
            System.out.println("*DRILL *DRILL *DRILL USS " + bName + " Sonar" + " connected to: " + pbroker + ".");
            System.out.println("*DRILL *DRILL *DRILL USS " + bName + " at " + getCurrentDateAndTime() + ": READY");

            while (true) { // NOC: while loop as is
                m = (TextMessage) messageConsumer.receive(70); // receive messages within 70 msec timeout

                if (m != null) {
                    if (m.getText().equalsIgnoreCase("end")) {
                        System.out.println(" " + getCurrentDateAndTime() + " " + bName + " " + "**TERMINATE DRILL");
                        break;
                        // break gracefully when done
                    } else {
                        System.out.println(" " + getCurrentDateAndTime() + " " + bName + " " + m.getText());
                        totalMessages++;
                    }
              }
            } // while
              // loop gracefully exited, print message
              // and fall thru, gracefully, to "finally"
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sx != null)
                sx.close();
            if (cx != null)
                cx.close();
            fis.close();
            // ADC - to close resources etc
        }
        System.out.println("Number of messages received = " + totalMessages);
    }

}
