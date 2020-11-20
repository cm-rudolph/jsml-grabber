package de.famiru.jsmlgrabber.daemon;

import org.openmuc.jrxtx.SerialPort;
import org.openmuc.jsml.structures.SmlFile;
import org.openmuc.jsml.transport.Transport;

import java.io.DataInputStream;
import java.io.IOException;

public class SerialReceiver {

    private final DataInputStream is;

    public SerialReceiver(SerialPort serialPort) throws IOException {
        this.is = new DataInputStream(serialPort.getInputStream());
    }

    public SmlFile getSMLFile() throws IOException {
        Transport transport = new Transport();
        return transport.getSMLFile(is);
    }

    public void closeStream() throws IOException {
        is.close();
    }
}
