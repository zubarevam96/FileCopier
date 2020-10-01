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
import java.util.Date;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import com.zubarevam.filescopier.model.PropertiesHolderSingleton;
import java.util.LinkedList;

/**
 * Class that does the logic of copying files in this app
 * @author ZubarevAM
 */
public enum ProgramLogicSingleton {

    INSTANCE;
    
    /** hour   == 60 minutes,
     *  minute == 60 seconds, 
     *  second == 1000 mills,
     *  total  == 60*60*1000 */
    private static final int MILLIS_IN_HOUR = 3600000;

    private static final Logger log = LoggerDispatcherSingleton.INSTANCE.getLoggerWithHandler(ProgramLogicSingleton.class);

    /**
     * Contains files that must be copied
     */
    private final List<File> filesToCopy = new LinkedList();

    /**
     * Executes copying of files; this method does all preparations and confirms, then calls
     * {@link ProgramLogicSingleton#executeCopying}(
     * {@link PropertiesHolderSingleton#INPUT_DIRECTORY_PATH},
     * {@link PropertiesHolderSingleton#OUTPUT_DIRECTORY_PATH} ).
     * @return {@code False} if there was no files to copy; {@code True} otherwise.
     */
    public boolean copyFiles() throws CancellationException, IOException {
        
        try {
            String inputRootPath = getInputPath();
            String outputRootPath = getOutputPath();
            
            resetRecentlyChangedFiles(new File(inputRootPath));
            
            if (filesToCopy.isEmpty()) {
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
                    log.info(new Date() + ": files was copied successfully");
                    return true;
                } else {
                    throw new CancellationException();
                }
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, "IOException was occurred while copying files", ex);
            throw ex;
        }
    }

    /**
     * copies {@code filesToCopy}; if namecollision occures, backups previous files
     * by calling {@link ProgramLogicSingleton#backupIt}.
     * @param inputFilesPath root directory of files in {@link ProgramLogicSingleton#filesToCopy}
     * @param outputFilesPath root directory of files in {@link ProgramLogicSingleton#filesToCopy} must be replaced with that path
     * @throws IOException
     */
    private void executeCopying(String inputFilesPath, String outputFilesPath) throws IOException {
        
        for (File tmp : filesToCopy) {
            String outDestination = outputFilesPath.concat(tmp.getAbsolutePath().substring(inputFilesPath.length()));
            
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

    /**
     * adds to {@link ProgramLogicSingleton#filesToCopy} name ".backyyyymmdd_hhhmmmsss".
     * For example, if {@link ProgramLogicSingleton#filesToCopy} has name foo.bar,
     * this method will rename it to foo.bar.back20151231_23h59m59s
     * (if this method was called "31.12.2015 at 23:59:59").
     * @param fileToBackup file that must be backuped
     */
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

    /**
     * clears {@link ProgramLogicSingleton#filesToCopy}, than fills it by calling method
     * {@link ProgramLogicSingleton#addRecentlyChangedFiles}.
     * @param rootDirectory files in that directory will be filled if they are young enough.
     * @throws IOException
     */
    private void resetRecentlyChangedFiles(File rootDirectory) throws IOException {
        
        filesToCopy.clear();
        addRecentlyChangedFiles(rootDirectory);
    }

    /**
     * Adds all files contained in directory {@code nodeFile} and it's subdirectories if they are young enough.
     * Youthfulness of file are checked with {@link ProgramLogicSingleton#isDateOldEnough}.
     * @param nodeFile
     * @throws IOException
     */
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

    /**
     * Checks if date (in mills) are young enough; to do that,
     * {@code timeFileWasCreated} are compares with (this momnet in mills -
     * hours contained in {@link PropertiesHolderSingleton} converted to mills).
     * @param timeFileWasCreated age of file in mills; can be obtained by method {@link File#lastModified()}
     * @return {@code True} if {@code timeFileWasCreated} is old enough; {@code False} otherwise
     * @throws IOException
     */
    boolean isDateOldEnough(long timeFileWasCreated) throws IOException {

        long secondValue = new Date().getTime()
                - Long.valueOf(PropertiesHolderSingleton.INSTANCE.getProperty(
                        PropertiesHolderSingleton.MIN_TIME_TO_REWRITE_IN_HOURS))
                * MILLIS_IN_HOUR;
        
        return (timeFileWasCreated > secondValue);
    }

    /**
     * gets root directory where contained files that must be copied
     * @return inputPath that contained in {@link PropertiesHolderSingleton}
     * @throws IOException if IOException occurs or if fetched String == ""
     */
    private String getInputPath() throws IOException {
        
        String path = PropertiesHolderSingleton.INSTANCE
                .getProperty(PropertiesHolderSingleton.INPUT_DIRECTORY_PATH);
        
        if (path.equals("")) {
            throw new IOException("inputPath is undefined");
        }
        
        return path;
    }

    /**
     * gets root directory of directories where files must be copied to
     * @return outputPath that contained in {@link PropertiesHolderSingleton}
     * @throws IOException if IOException occurs or if fetched String == ""
     */
    private String getOutputPath() throws IOException {
        
        String path =  PropertiesHolderSingleton.INSTANCE
                .getProperty(PropertiesHolderSingleton.OUTPUT_DIRECTORY_PATH);
        
        if (path.equals("")) {
            throw new IOException("outputPath is undefined");
        }
        
        return path;
    }
}
