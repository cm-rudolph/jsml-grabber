package de.famiru.jsmlgrabber.messaging;

import de.famiru.jsmlgrabber.model.Message;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DelegatingMessageHandler implements MessageHandler {
    private final List<MessageHandler> delegates = new CopyOnWriteArrayList<>();

    @Override
    public void handleMessage(Message message) {
        for (MessageHandler delegate : delegates) {
            delegate.handleMessage(message);
        }
    }

    public DelegatingMessageHandler addHandler(MessageHandler delegate) {
        delegates.add(delegate);
        return this;
    }

    @Override
    public void initialize() {
        delegates.forEach(MessageHandler::initialize);
    }

    @Override
    public void cleanup() {
        delegates.forEach(MessageHandler::cleanup);
    }
}
