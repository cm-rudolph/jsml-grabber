package de.famiru.jsmlgrabber.daemon;

import java.io.IOException;

interface IOExceptionHandler <T> {
    void handleIOException(IOException e, T subject);
}
