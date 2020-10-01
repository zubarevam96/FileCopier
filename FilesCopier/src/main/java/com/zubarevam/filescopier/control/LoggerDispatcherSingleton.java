package com.zubarevam.filescopier.control;

import com.zubarevam.filescopier.EntryPoint;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Class that is created to dispatch Logger with fileHandler in it
 */
public enum LoggerDispatcherSingleton {

    INSTANCE;

    /**
     * Effectively final;
     * can remain null-value in case of {@link IOException} in constructor occurs,
     * so keyword "final" can't be placed here
     */
    private FileHandler fileHandler;

    /**
     * Constructor initializes and configures {@code fileHandler} for logs
     * @throws IOException
     */
    LoggerDispatcherSingleton() {
        try {
            Path path = Paths.get(System.getenv("ProgramFiles") + "\\zamSoft\\FilesCopier");
            Files.createDirectories(path);
            fileHandler = new FileHandler(path.toString().concat("\\logs.log"), true);
            fileHandler.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "couldn't set file for logs; logFile will not be writed");
        }
    }

    /**
     * returns Logger with {@code fileHandler} in it.
     * @param clazz class that calls this method
     * @return
     */
    public Logger getLoggerWithHandler(Class clazz) {

        if (clazz == null) {
            clazz = EntryPoint.class;
        }
        Logger result = Logger.getLogger(clazz.getName());
        result.addHandler(fileHandler);

        return result;
    }
}