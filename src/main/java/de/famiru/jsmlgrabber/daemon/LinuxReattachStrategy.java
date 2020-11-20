package de.famiru.jsmlgrabber.daemon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;

public class LinuxReattachStrategy implements ReattachStrategy {
    private static final Logger log = LoggerFactory.getLogger(LinuxReattachStrategy.class);
    private static final int WAIT_TIME_IN_MS = 3000;
    private final String deviceId;

    LinuxReattachStrategy(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void reattachSerialPort() {
        try {
            writeIdToFile("/sys/bus/usb/drivers/usb/unbind");
            Thread.sleep(WAIT_TIME_IN_MS);
            writeIdToFile("/sys/bus/usb/drivers/usb/bind");
        } catch (IOException | InterruptedException e) {
            log.warn("Failed to reattach serial port.", e);
        }

        try {
            Thread.sleep(WAIT_TIME_IN_MS);
        } catch (InterruptedException ignore) {
        }
    }

    private void writeIdToFile(String fileName) throws IOException {
        log.debug("trying to write " + deviceId + " to " + fileName);
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(deviceId);
        }
    }
}
