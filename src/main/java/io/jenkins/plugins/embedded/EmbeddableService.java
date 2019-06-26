package io.jenkins.plugins.embedded;

import io.vavr.control.Try;
import java.util.logging.Level;
import java.util.logging.Logger;


public interface EmbeddableService extends AutoCloseable {
    //EmbeddableService start();
    //default EmbeddableService start() { stop(); throw new UnsupportedOperationException(); }

    default EmbeddableService stop() {
        Try.of(()-> {close(); return this;})
                //.onFailure(e -> log.log(Level.FINE, "error while stopping " + toString(), e))
                .onFailure(e ->
                        Logger.getLogger(this.getClass().getName())
                                .log(Level.WARNING, "error while stopping " + toString(), e))
        ;
        return this;
    }
}
