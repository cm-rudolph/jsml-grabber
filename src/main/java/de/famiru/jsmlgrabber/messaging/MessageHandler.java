package de.famiru.jsmlgrabber.messaging;

import de.famiru.jsmlgrabber.model.Message;

public interface MessageHandler {
    void handleMessage(Message message);
    default void initialize() {}
    default void cleanup() {}
}
