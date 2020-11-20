package de.famiru.jsmlgrabber.daemon;

import de.famiru.jsmlgrabber.messaging.MessageHandler;
import de.famiru.jsmlgrabber.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

class Consumer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Consumer.class);
    private final BlockingQueue<Message> queue;
    private final MessageHandler messageHandler;
    private final CountDownLatch isStopped = new CountDownLatch(1);
    private Thread consumerThread = null;

    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    Consumer(BlockingQueue<Message> queue, MessageHandler messageHandler) {
        this.queue = queue;
        this.messageHandler = messageHandler;
        this.messageHandler.initialize();
    }

    void stop() {
        log.debug("Stopping consumer.");
        consumerThread.interrupt();
        try {
            isStopped.await();
        } catch (InterruptedException e) {
            log.warn("Interrupted consumer while waiting for consumer thread to terminate.");
            Thread.currentThread().interrupt();
        }
        log.debug("Executing message handler cleanup code.");
        messageHandler.cleanup();
        log.debug("Consumer stopped.");
    }

    @Override
    public void run() {
        consumerThread = Thread.currentThread();
        while (!Thread.currentThread().isInterrupted()) {
            Message message;
            try {
                message = queue.take();
            } catch (InterruptedException e) {
                log.debug("Consumer interrupted.");
                Thread.currentThread().interrupt();
                break;
            }
            messageHandler.handleMessage(message);
        }
        isStopped.countDown();
    }
}
