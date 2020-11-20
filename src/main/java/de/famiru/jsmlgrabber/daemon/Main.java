package de.famiru.jsmlgrabber.daemon;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.mysql.jdbc.log.Slf4JLogger;
import de.famiru.jsmlgrabber.messaging.DelegatingMessageHandler;
import de.famiru.jsmlgrabber.messaging.JdbcMessageHandler;
import de.famiru.jsmlgrabber.messaging.LoggingMessageHandler;
import de.famiru.jsmlgrabber.messaging.MessageHandler;
import de.famiru.jsmlgrabber.model.Message;
import de.famiru.jsmlgrabber.stub.FileReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final int MAX_QUEUE_CAPACITY = 10000;
    private static final String DATABASE_SERVERNAME = "database.famiru.local";
    private static final String DATABASE_USER = "dbuser";
    private static final String DATABASE_PASSWORD = "dbpass";
    private static final String DATABASE_NAME = "dbname";
    private static final long METER_NUMBER = 1234567L;
    private static final long METER_NUMBER_FEEDIN = 1234568L;

    public static void main(String... args) {
        SerialReceiverManager manager = new SerialReceiverManager("/dev/ttyUSB0", new LinuxReattachStrategy("3-1.2"));

        SerialReceiver receiver = manager.createReceiver();
        /*FileReceiver receiver = null;
        try {
            receiver = new FileReceiver();
        } catch (FileNotFoundException e) {
            System.exit(0);
        }*/

        MessageHandler handler = new DelegatingMessageHandler()
                //.addHandler(new LoggingMessageHandler());
                .addHandler(new JdbcMessageHandler(createMysqlDataSource(), METER_NUMBER, METER_NUMBER_FEEDIN));

        BlockingQueue<Message> queue = new LinkedBlockingQueue<>(MAX_QUEUE_CAPACITY);
        Producer producer = new Producer(queue, receiver, manager);
        Consumer consumer = new Consumer(queue, handler);

        Thread producerThread = new Thread(producer);
        producerThread.setDaemon(true);
        producerThread.start();

        Thread consumerThread = new Thread(consumer);
        consumerThread.setDaemon(true);
        consumerThread.start();

        Thread shutdownHook = new Thread(() -> {
            log.debug("Executing shutdown hook.");
            producer.stop();
            consumer.stop();
            manager.closePort();
            log.debug("Shutdown hook executed successfully.");
        });

        Runtime.getRuntime().addShutdownHook(shutdownHook);

        try {
            producerThread.join();
        } catch (InterruptedException ignore) {
        }
        shutdownHook.run();
    }

    private static DataSource createMysqlDataSource() {
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setServerName(DATABASE_SERVERNAME);
        mysqlDataSource.setUser(DATABASE_USER);
        mysqlDataSource.setPassword(DATABASE_PASSWORD);
        mysqlDataSource.setDatabaseName(DATABASE_NAME);
        mysqlDataSource.setCharacterEncoding("utf8");
        mysqlDataSource.setLogger(Slf4JLogger.class.getName());
        return mysqlDataSource;
    }
}
