package com.sellivu.backend.settlement.service;

import org.postgresql.copy.CopyManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

final class CopyStreamSupport {

    private static final int PIPE_BUFFER_SIZE = 64 * 1024;

    private CopyStreamSupport() {
    }

    static void copyIn(CopyManager copyManager, String copySql, ThrowingWriterConsumer writerConsumer) throws Exception {
        PipedInputStream inputStream = new PipedInputStream(PIPE_BUFFER_SIZE);
        PipedOutputStream outputStream = new PipedOutputStream(inputStream);
        AtomicReference<Throwable> writerFailure = new AtomicReference<>();

        Thread writerThread = new Thread(() -> {
            try (PipedOutputStream closeableOutput = outputStream;
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(closeableOutput, StandardCharsets.UTF_8), PIPE_BUFFER_SIZE)) {
                writerConsumer.accept(writer);
                writer.flush();
            } catch (Throwable throwable) {
                writerFailure.set(throwable);
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }, "pg-copy-stream-writer");
        writerThread.start();

        Throwable copyFailure = null;
        try (InputStream copyInput = inputStream) {
            copyManager.copyIn(copySql, copyInput);
        } catch (Throwable throwable) {
            copyFailure = throwable;
            throw throwable;
        } finally {
            try {
                writerThread.join();
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                if (copyFailure == null) {
                    throw interruptedException;
                }
            }
        }

        Throwable writerThrowable = writerFailure.get();
        if (writerThrowable == null) {
            return;
        }
        if (writerThrowable instanceof Exception exception) {
            throw exception;
        }
        if (writerThrowable instanceof Error error) {
            throw error;
        }
        throw new RuntimeException(writerThrowable);
    }

    @FunctionalInterface
    interface ThrowingWriterConsumer {
        void accept(BufferedWriter writer) throws Exception;
    }
}
