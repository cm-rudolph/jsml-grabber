package de.famiru.jsmlgrabber.daemon;

class OpenPortFailedException extends RuntimeException {
    OpenPortFailedException(Throwable cause) {
        super(cause);
    }

    OpenPortFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
