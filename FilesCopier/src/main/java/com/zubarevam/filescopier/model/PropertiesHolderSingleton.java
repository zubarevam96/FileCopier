/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zubarevam.filescopier.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Logger;

/**
 *
 * @author ZubarevAM
 */
public enum PropertiesHolderSingleton {
    
    INSTANCE;
    
    public static final String  INPUT_DIRECTORY_PATH =  "inputDirectoryPath";
    public static final String OUTPUT_DIRECTORY_PATH = "outputDirectoryPath";
    public static final String MIN_TIME_TO_REWRITE_IN_HOURS = "ageOfFilesToRewriteInHours";
    
    static final Logger log = Logger.getLogger(PropertiesHolderSingleton.class.getName());
    
    private File file;
    private final Properties properties = new Properties();
    
    PropertiesHolderSingleton() {

        try {
            initializePropertiesFile();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    public static PropertiesHolderSingleton getInstance() throws IllegalAccessException, IOException {
        
        return INSTANCE;
    }
    
    public String getProperty(String key) throws IOException {
        
        try {
            properties.load(new FileInputStream(file));
        } catch (IOException e) {
            initializePropertiesFile();
        }
        return properties.getProperty(key);
    }
    
    public Object setProperty(String key, String value) throws IOException {

        properties.load(new FileInputStream(file));
        Object result = properties.setProperty(key, value);
        properties.store(new FileOutputStream(file), null);
        return result;
    }
    
    private void initializePropertiesFile() throws IOException {
        
        Path path = Paths.get(System.getenv("ProgramFiles").concat("\\zamSoft\\").concat("FilesCopier"));
        Files.createDirectories(path);
        file = new File(path.toString().concat("\\FilesCopier.properties"));
        if (!file.isFile()) {
            file.createNewFile();
        }
    }
    
    public String getPropertiesPath() {
        
        return System.getenv("ProgramFiles").concat("\\zamSoft\\FilesCopier\\FilesCopier.properties");
    }
}
