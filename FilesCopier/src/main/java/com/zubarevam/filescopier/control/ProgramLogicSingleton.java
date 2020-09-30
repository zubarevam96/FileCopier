/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zubarevam.filescopier.control;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import com.zubarevam.filescopier.model.PropertiesHolderSingleton;
import java.util.LinkedList;

/**
 *
 * @author ZubarevAM
 */
public enum ProgramLogicSingleton {

    INSTANCE;
    
    /** hour   == 60 minutes,
     *  minute == 60 seconds, 
     *  second == 1000 millis, 
     *  total  == 60*60*1000 */
    private static final int MILLIS_IN_HOUR = 3600000; 
    
    private static final Logger log = Logger.getLogger(ProgramLogicSingleton.class.getName());
    
    List<File> filesToCopy = new LinkedList();

    ProgramLogicSingleton() {
    
    }
    
    public ProgramLogicSingleton getInstance() {
        
        return INSTANCE;
    }
    
    public boolean copyFiles() throws IOException {
        
        return copyFiles(true);
    }
    
    public boolean copyFiles(boolean withBackup) throws IOException {
        
        try {
            String inputRootPath = getInputPath();
            String outputRootPath = getOutputPath();
            
            resetRecentlyChangedFiles(new File(inputRootPath));
            
            if (filesToCopy.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There is no files to update");
                return false;
            } else {
                StringBuilder message = new StringBuilder("Next files will be copied from directory \"")
                        .append(inputRootPath)
                        .append("\" to directory \"")
                        .append(outputRootPath)
                        .append("\":");

                for (File tmp : filesToCopy) {
                    StringBuilder outputFilePath = new StringBuilder(getOutputPath());

                    // if outputFilePath is not ends with "\\", add it
                    if (!outputFilePath.substring(outputFilePath.length() - 2).equals("\\")) {
                        outputFilePath.append("\\");
                    }
                    message.append("\n".concat(tmp.getAbsolutePath()).concat(";"));
                }
                message.append(".\nAre you sure?");

                int dialogResult = JOptionPane.showConfirmDialog(null, message, "Confirmation", JOptionPane.YES_NO_OPTION);
                if(dialogResult == 0) {
                    executeCopying(inputRootPath, outputRootPath);
                    return true;
                } else {
                    throw new CancellationException();
                }
            }
        } catch (CancellationException ce) {
            return false;
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    private void executeCopying(String from, String to) throws IOException { 
        
        for (File tmp : filesToCopy) {
            String outDestination = to.concat(tmp.getAbsolutePath().substring(from.length()));
            
            Files.createDirectories(Paths.get(outDestination).getParent());
            
            File outFile = new File(outDestination);
            
            if (!outFile.exists() || tmp.lastModified() != outFile.lastModified()) {
                if (outFile.exists()) {
                    backupIt(outFile);
                }
                Files.copy(tmp.toPath(), outFile.toPath());
            }
        }
    }
    
    
    
    
    private void backupIt(File fileToBackup) {
        
        StringBuilder fileAbsolutePath = new StringBuilder(fileToBackup.getAbsolutePath());
        fileAbsolutePath.append(".back");
        
        // required format: 20151231_23h59m59s for "31.12.2015 23:59:59"
        StringBuilder dateString = new StringBuilder(new SimpleDateFormat("yyyymmdd_hhmmss").format(new Date()));
        dateString.insert(dateString.length() - 4, 'h');
        dateString.insert(dateString.length() - 2, 'm');
        dateString.insert(dateString.length(), 's');
        
        fileAbsolutePath.append(dateString);
        
        fileToBackup.renameTo(new File(fileAbsolutePath.toString()));
    }
    
    private void resetRecentlyChangedFiles(File rootFile) throws IOException {
        
        filesToCopy.clear();
        addRecentlyChangedFiles(rootFile);
    }
    
    private void addRecentlyChangedFiles(File nodeFile) throws IOException {
        
        String inputRootPath = getInputPath();
        String outputRootPath = getOutputPath();
        
        for (File tmp : nodeFile.listFiles()) {
            if (tmp.isDirectory()) {
                addRecentlyChangedFiles(tmp);
            } else {
                String outDestination = outputRootPath.concat(tmp.getAbsolutePath().substring(inputRootPath.length()));
                File outFile = new File(outDestination);
                
                if (isDateOldEnough(tmp.lastModified())
                        && (!outFile.exists()
                                || tmp.lastModified() != outFile.lastModified())) {
                    filesToCopy.add(tmp);
                }
            }
        }
    }
    
    boolean isDateOldEnough(long timeFileWasCreated) throws IOException {
        
        
        long secondValue = new Date().getTime()
                - Long.valueOf(PropertiesHolderSingleton.INSTANCE.getProperty(
                        PropertiesHolderSingleton.MIN_TIME_TO_REWRITE_IN_HOURS))
                * MILLIS_IN_HOUR;
        
        return (timeFileWasCreated > secondValue);
    }
    
    
    private String getInputPath() throws IOException {
        
        String path = PropertiesHolderSingleton.INSTANCE
                .getProperty(PropertiesHolderSingleton.INPUT_DIRECTORY_PATH);
        
        if (path.equals("")) {
            throw new IOException("inputPath is undefined");
        }
        
        return path;
    }
    
    private String getOutputPath() throws IOException{
        
        String path =  PropertiesHolderSingleton.INSTANCE
                .getProperty(PropertiesHolderSingleton.OUTPUT_DIRECTORY_PATH);
        
        if (path.equals("")) {
            throw new IOException("outputPath is undefined");
        }
        
        return path;
    }
}
