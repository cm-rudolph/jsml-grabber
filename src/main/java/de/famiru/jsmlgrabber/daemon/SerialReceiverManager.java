package de.famiru.jsmlgrabber.daemon;

import org.jetbrains.annotations.Nullable;
import org.openmuc.jrxtx.SerialPort;
import org.openmuc.jrxtx.SerialPortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class SerialReceiverManager implements IOExceptionHandler<Producer> {
    private static final Logger log = LoggerFactory.getLogger(SerialReceiverManager.class);
    private final String portName;
    private final ReattachStrategy reattachStrategy;
    @Nullable
    private SerialReceiver receiver;
    @Nullable
    private SerialPort serialPort;

    SerialReceiverManager(String portName, ReattachStrategy reattachStrategy) {
        this.portName = portName;
        this.reattachStrategy = reattachStrategy;
    }

    SerialReceiver createReceiver() throws OpenPortFailedException {
        if (receiver == null) {
            receiver = createReceiver(0);
        }

        return receiver;
    }

    private SerialReceiver createReceiver(int tryNo) throws OpenPortFailedException {
        try {
            serialPort = SerialPortBuilder.newBuilder(portName).build();
        } catch (IOException e) {
            log.warn("Failed to open serial port. Device not present. Trying to reattach.", e);
            reattachStrategy.reattachSerialPort();
            if (tryNo < 3) {
                return createReceiver(tryNo + 1);
            } else {
                throw new OpenPortFailedException("Giving up trying to reattach serial port.", e);
            }
        }

        try {
            receiver = new SerialReceiver(serialPort);
        } catch (IOException e) {
            log.warn("Failed to create receiver.", e);
            throw new OpenPortFailedException("Failed to open receiver.", e);
        }
        return receiver;
    }

    @Override
    public void handleIOException(IOException e, Producer producer) {
        if ("Input/output error in nativeavailable".equals(e.getMessage()) || "Timeout".equals(e.getMessage())) {
            log.info("Restart serial port.");
            closePort();
            producer.setReceiver(createReceiver());
        }
    }

    public void closePort() {
        if (serialPort != null) {
            try {
                serialPort.close();
            } catch (IOException e) {
                log.warn("Failed to close serial port. Hopefully everything is ok.", e);
            }
            serialPort = null;
            receiver = null;
        }
    }
}
