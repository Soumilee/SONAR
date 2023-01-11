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

public class SonarB {

    public static String getCurrentDateAndTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    public static Boolean isMessageValid(String msg, String signature) {
        Auth mykey = new Auth("AABECZT");
        String message = msg;
        String sign = mykey.sign(message);

        if (sign.equals(signature)) {
            return true;
        }

        return false;
    }

    public static Boolean isAlphanumericSixChars(String str) {
        String pattern = "^[a-zA-Z0-9]{6}$";
        return str.matches(pattern);
    }

    public static Boolean isAlphanumericUptoTwentyChars(String str) {
        String pattern = "^[A-Za-z0-9-]{0,20}$";
        return str.matches(pattern);
    }

    public static Boolean isNumberUptoFifteen(String str) {
        String pattern = "^([0]?[1-9]|1[0-5])$";
        return str.matches(pattern);
    }

    public static Boolean isFormatCorrect(String msg) {
        
        Boolean validTargetPackageNumber = false;
        Boolean validMessageNumber = false;
        Boolean validMessageBody = false;
        String strArray1[] = msg.split(" ");
        String strArray2[] = strArray1[1].split("/");
        String strArray3[] = strArray2[0].split("-");

        
        validTargetPackageNumber = isAlphanumericSixChars(strArray3[3]);
        validMessageNumber = isNumberUptoFifteen(strArray2[1]);

        
        if (strArray1.length == 3) {
            validMessageBody = isAlphanumericUptoTwentyChars(strArray1[2]);
        }

        
        if (validTargetPackageNumber && validMessageNumber && validMessageBody) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(" Usage: java Sonar <shipname> <properties>  ## Consumer mode\n");
            System.exit(2);
        }

        String pfile = args[1];
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
        int totalGoodMessages = 0;
        int totalBadMessages = 0;
        try {
            cf = new ActiveMQConnectionFactory(pbroker);
            cx = cf.createConnection(puser, ppass);
            sx = cx.createSession(false, Session.AUTO_ACKNOWLEDGE); // NOC
            // createSession should not change
            // make Destination, Consumer
            Destination destination = sx.createQueue(pqueue); // ADC, must use session.createXXXXXX
            // MessageConsumer // ADC, must use session.

            String selector = "boat = " + "'" + bName + "'";
            MessageConsumer messageConsumer = sx.createConsumer(destination, selector);
            // start consumer
            cx.start();
            System.out.println("Connected to " + pbroker + ". Waiting for message...");
            System.out.println("*DRILL *DRILL *DRILL USS " + bName + " Sonar" + " connected to: " + pbroker + ".");
            System.out.println("*DRILL *DRILL *DRILL USS " + bName + " at " + getCurrentDateAndTime() + ": READY");

            while (true) { // NOC: while loop as is
                m = (TextMessage) messageConsumer.receive(100); // receive messages within 100 msec timeout

                if (m != null) {
                   
                    Boolean isMsgValid = isMessageValid(m.getText(), m.getStringProperty("signature"));
                    Boolean isTermMsg = m.getText().equals("END");
		    
					Boolean isCorrectFormat = null;
		
                    if (!isTermMsg) {
                        isCorrectFormat = isFormatCorrect(m.getText());
                    }

                    if (isMsgValid && m.getText().equals("END")) {
                        System.out.println(
                                " " + getCurrentDateAndTime() + " " + "**TERMINATE DRILL"
                                        + " (" + totalGoodMessages + " GOOD and " + totalBadMessages
                                        + " BAD MSG RECD)");
                        break;
                    } else if (isMsgValid && isCorrectFormat) {
                        System.out.println(" " + getCurrentDateAndTime() + " " + bName + ":" + " " + m.getText()
                                + " " + "(AUTH-OK, FMT-OK)");

                        totalMessages++;
                        totalGoodMessages++;
                    } else if (!isMsgValid && isCorrectFormat) {
                        System.out.println(" " + getCurrentDateAndTime() + " " + bName + ":" + " " + m.getText()
                                + " " + "(AUTH-BAD, FMT-OK)");

                        totalMessages++;
                        totalBadMessages++;
                    } else if (isMsgValid && !isCorrectFormat) {
                        System.out.println(" " + getCurrentDateAndTime() + " " + bName + ":" + " " + m.getText()
                                + " " + "(AUTH-OK, FMT-BAD)");

                        totalMessages++;
                        totalBadMessages++;
                    } else {
                        System.out.println(" " + getCurrentDateAndTime() + " " + bName + ":" + " " + m.getText()
                                + " " + "(AUTH-BAD, FMT-BAD)");

                        totalMessages++;
                        totalBadMessages++;
                    }
                }
            } // while
              // loop gracefully exited, print message
              // and fall thru, gracefully, to "finally"
        } catch (

        Exception e) {
            e.printStackTrace();
        } finally {
            if (sx != null)
                sx.close();
            if (cx != null)
                cx.close();
            fis.close();
            // ADC - to close resources etc
        }
        System.out.println("Total number of messages received = " + totalMessages);
    }
}
