package de.famiru.jsmlgrabber.daemon;

import de.famiru.jsmlgrabber.model.Message;
import de.famiru.jsmlgrabber.model.MessageBuilder;
import de.famiru.jsmlgrabber.stub.FileReceiver;
import org.openmuc.jsml.structures.*;
import org.openmuc.jsml.structures.responses.SmlGetListRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;

class Producer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Producer.class);
    private static final int MAX_SUBSEQUENT_READ_ERRORS = 10;

    private final BlockingQueue<Message> queue;
    private final AtomicBoolean stopReading = new AtomicBoolean(false);
    private final CountDownLatch isStopped = new CountDownLatch(1);
    private final IOExceptionHandler<Producer> ioExceptionHandler;
    private SerialReceiver receiver;

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    Producer(BlockingQueue<Message> queue, SerialReceiver receiver, IOExceptionHandler<Producer> exceptionHandler) {
        this.queue = requireNonNull(queue);
        this.receiver = requireNonNull(receiver);
        this.ioExceptionHandler = exceptionHandler;
    }

    void setReceiver(SerialReceiver receiver) {
        this.receiver = requireNonNull(receiver);
    }

    void stop() {
        log.debug("Stopping producer.");
        stopReading.set(true);
        try {
            isStopped.await();
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for producer to stop.");
            Thread.currentThread().interrupt();
        }
        log.debug("Producer stopped.");
    }

    @Override
    public void run() {
        int subsequentReadErrors = 0;
        while (!stopReading.get()) {
            SmlFile smlFile;
            try {
                smlFile = receiver.getSMLFile();
                subsequentReadErrors = 0;
            } catch (IOException e) {
                log.warn("Failed to get SML-File.", e);
                subsequentReadErrors++;
                if (subsequentReadErrors == MAX_SUBSEQUENT_READ_ERRORS) {
                    log.warn("Too many subsequent failures. Exiting producer.");
                    break;
                }

                if (ioExceptionHandler != null)
                    ioExceptionHandler.handleIOException(e, this);

                continue;
            }

            List<SmlMessage> smlMessages = smlFile.getMessages();

            for (SmlMessage smlMessage : smlMessages) {
                EMessageBody tag = smlMessage.getMessageBody().getTag();

                if (tag.equals(EMessageBody.GET_LIST_RESPONSE)) {
                    SmlGetListRes resp = (SmlGetListRes) smlMessage.getMessageBody().getChoice();
                    SmlList smlList = resp.getValList();

                    MessageBuilder messageBuilder = new MessageBuilder();
                    messageBuilder.timestamp(LocalDateTime.now());
                    messageBuilder.sensorTime(((Unsigned32) resp.getActSensorTime().getChoice()).getVal());

                    for (SmlListEntry entry : smlList.getValListEntry()) {
                        //byte[] objName = entry.getObjName().getOctetString();
                        byte[] objName = entry.getObjName().getValue();

                        if (objName.length == 6 && objName[0] == (byte) 0x01 && objName[1] == (byte) 0x00 && objName[5] == (byte) 0xFF) {
                            if (objName[2] == (byte) 0x10 && objName[3] == (byte) 0x07 && objName[4] == (byte) 0x00) {
                                // Momentane Leistung in Zehntelwatt
                                messageBuilder.currentPower(((Integer32) entry.getValue().getChoice()).getVal(), entry.getScaler().getVal());
                            } else if (objName[2] == (byte) 0x01 && objName[3] == (byte) 0x08 && objName[4] == (byte) 0x00) {
                                // Wirkarbeit Zählerstand total in Zehntel-Wattstunden
                                messageBuilder.totalEnergy(((Integer64) entry.getValue().getChoice()).getVal(), entry.getScaler().getVal());
                            } else if (objName[2] == (byte) 0x02 && objName[3] == (byte) 0x08 && objName[4] == (byte) 0x00) {
                                // Wirkarbeit Einspeisezählerstand total in Zehntel-Wattstunden
                                messageBuilder.totalFeedInEnergy(((Integer64) entry.getValue().getChoice()).getVal(), entry.getScaler().getVal());
                            }
                        }
                    }
                    Message message = messageBuilder.build();
                    if (!queue.offer(message)) {
                        log.warn("Failed to write message to queue. Capacity limit reached. Throwing message away.");
                    }
                }
            }
        }
        log.debug("Terminated producer loop.");
        isStopped.countDown();
    }
}
