package de.famiru.jsmlgrabber.stub;

import org.openmuc.jsml.structures.SmlFile;
import org.openmuc.jsml.structures.SmlMessage;
import org.openmuc.jsml.transport.MessageExtractor;

import java.io.*;

public class FileReceiver {
    private DataInputStream is;

    public FileReceiver() throws FileNotFoundException {
        is = new DataInputStream(new BufferedInputStream(new FileInputStream("smartmeter.dat")));
    }

    public SmlFile getSMLFile() throws IOException {
        MessageExtractor extractor = new MessageExtractor(is, 3000);
        return handleSMLStream(extractor.getSmlMessage());
    }

    SmlFile handleSMLStream(byte[] smlPacket) throws IOException {
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(smlPacket));

        SmlFile smlFile = new SmlFile();

        while (is.available() > 0) {
            SmlMessage message = new SmlMessage();

            if (!message.decode(is)) {
                throw new IOException("Could not decode message");
            } else {

                smlFile.add(message);
            }

        }
        return smlFile;
    }

    public void close() throws IOException {
        is.close();
    }
}
