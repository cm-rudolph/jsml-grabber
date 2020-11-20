package de.famiru.jsmlgrabber.messaging;

import de.famiru.jsmlgrabber.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

public class LoggingMessageHandler implements MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(LoggingMessageHandler.class);

    @Override
    public void handleMessage(Message message) {
        log.info("Datensatz von: {}", DateTimeFormatter.ISO_DATE_TIME.format(message.getTimestamp()));
        log.info("Sensorzeit: {}", message.getSensorTime());
        log.info("Leistung in Zehntelwatt: {}", message.getCurrentPower());
        log.info("Zählerstand in Zehntel-Wattstunden: {}", message.getTotalEnergy());
        log.info("Einspeisezählerstand in Zehntel-Wattstunden: {}", message.getTotalFeedInEnergy());
    }
}
