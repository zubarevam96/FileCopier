/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zubarevam.filescopier.model;

import com.zubarevam.filescopier.control.LoggerDispatcherSingleton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Logger;

/**
 * Class that Holds logic of properties, including export to file and importing from it.
 * It reads data from file every time getters or setters are called so
 * "dirty read" is excepted, and user can change properties file when app
 * is working.
 * @author ZubarevAM
 */
public enum PropertiesHolderSingleton {
    
    INSTANCE;
    /**
     * Name of property that contains root directory for files that must be copied.
     */
    public static final String  INPUT_DIRECTORY_PATH =  "inputDirectoryPath";
    /**
     * Name of property that contains root directory where files must be copied to.
     */
    public static final String OUTPUT_DIRECTORY_PATH = "outputDirectoryPath";
    /**
     * Name of property that contains age of files;
     * if (this moment - hours, equal to that value) < age of file,
     * that file must be copied.
     */
    public static final String MIN_TIME_TO_REWRITE_IN_HOURS = "ageOfFilesToRewriteInHours";

    private static final Logger log = LoggerDispatcherSingleton.INSTANCE.getLoggerWithHandler(PropertiesHolderSingleton.class);

    /**
     * holder for our properties; every time app calls
     * {@link PropertiesHolderSingleton#getProperty} or
     * {@link PropertiesHolderSingleton#setProperty}, at first
     * {@link PropertiesHolderSingleton#properties} is refreshed from this file.
     */
    private File file;

    /**
     * every time app calls
     * {@link PropertiesHolderSingleton#getProperty} or
     * {@link PropertiesHolderSingleton#setProperty}, this methods works with that variable.
     * Remark: user can change {@link PropertiesHolderSingleton#file}
     * while app is working and be sure than data will not be deleted
     */
    private final Properties properties = new Properties();

    /**
     * Tries to call {@link PropertiesHolderSingleton#initializePropertiesFile()};
     * if it fails with {@link IOException}, crushes App with {@link ExceptionInInitializerError}.
     */
    PropertiesHolderSingleton() {

        try {
            initializePropertiesFile();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Reads data from {@link PropertiesHolderSingleton#file} into
     * {@link PropertiesHolderSingleton#properties}
     * and reads property {@code key} from it.
     * @param key - property for which value must be returned
     * @return value, associated with {@code key}
     * @throws IOException
     */
    public String getProperty(String key) throws IOException {
        
        try {
            properties.load(new FileInputStream(file));
        } catch (IOException e) {
            initializePropertiesFile();
            properties.load(new FileInputStream(file));
        }
        return properties.getProperty(key);
    }

    /**
     * Reads data from {@link PropertiesHolderSingleton#file} into
     * {@link PropertiesHolderSingleton#properties} than
     * sets {@code key}'s value with new one, sent in params,
     * and rewrites {@link PropertiesHolderSingleton#file}.
     * @param key property for which value must be reset
     * @param value
     * @return old value or null if it was null
     * @throws IOException
     */
    public Object setProperty(String key, String value) throws IOException {

        properties.load(new FileInputStream(file));
        Object result = properties.setProperty(key, value);
        properties.store(new FileOutputStream(file), null);
        return result;
    }

    /**
     * Tries to create properties file in file system.
     * @throws IOException
     */
    private void initializePropertiesFile() throws IOException {
        
        Path path = Paths.get(System.getenv("ProgramFiles") + "\\zamSoft\\FilesCopier");
        Files.createDirectories(path);
        file = new File(path.toString().concat("\\FilesCopier.properties"));
        file.createNewFile();
    }

    /**
     * @return path to properties file
     */
    public String getPropertiesPath() throws IOException {
        if (file != null) {
            return file.getAbsolutePath();
        } else {
            throw new IOException();
        }
    }
}
