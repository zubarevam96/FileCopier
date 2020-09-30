/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zubarevam.filescopier.gui;

import com.zubarevam.filescopier.control.ProgramLogicSingleton;
import com.zubarevam.filescopier.model.PropertiesHolderSingleton;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author ZubarevAM
 */
public enum SystemTrayIconSingleton {
    
    INSTANCE;
    
    TrayIcon trayIcon;
    
    static final Logger log = Logger.getLogger(SystemTrayIconSingleton.class.getName());
    
    public SystemTrayIconSingleton getInstance() throws AWTException {
        return INSTANCE;
    }
    
    private void fillPopupMenu(PopupMenu popup) {
        
        MenuItemsGetter get = new MenuItemsGetter();
        
        Menu configureMenu = new Menu("configure");
        configureMenu.add(get. inputDirectorySetter());
        configureMenu.add(get.outputDirectorySetter());
        configureMenu.add(get.minAgeSetter());
        configureMenu.add(get.configOpener());
        
        popup.add(get.copyingExecutor());
        popup.add(configureMenu);
        popup.add(get.exiter());
    }
    
    public void tryCreateTrayApp() throws AWTException, IOException, URISyntaxException {
        
        if (trayIcon == null) {
        
            URL url = SystemTrayIconSingleton.class.getClassLoader().getResource("resources/copyPaste.png");
            
            Image image = Toolkit.getDefaultToolkit().getImage(url);

            PopupMenu popup = new PopupMenu("popup menu");

            fillPopupMenu(popup);
            trayIcon = new TrayIcon(image, "FilesCopier", popup);
            SystemTray.getSystemTray().add(trayIcon);
        }
    }
    private class MenuItemsGetter {
    
        private MenuItem copyingExecutor() {
            
            MenuItem item = new MenuItem("execute");
            item.addActionListener(e -> {
                boolean isSucceeded = false;
                try {
                    isSucceeded = ProgramLogicSingleton.INSTANCE.copyFiles();
                } catch (IOException ex) {
                    log.log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(null, "Copying was failed! Watch log for details");
                }
                
                if (isSucceeded) {
                    JOptionPane.showMessageDialog(null, "copying was done successfully!");
                } 
            });
            return item;
        }

        private MenuItem inputDirectorySetter() {
            
            MenuItem item = new MenuItem("set input directory");
            item.addActionListener(e -> {
                try {
                    
                    // Trying to fetch initial path from properties
                    String initialPath = PropertiesHolderSingleton.INSTANCE
                            .getProperty(PropertiesHolderSingleton.INPUT_DIRECTORY_PATH);
                    
                    // Setting inputDirectory property 
                    try {
                        String chosenPath = chooseDirectoryPath(initialPath);
                        PropertiesHolderSingleton.INSTANCE
                                .setProperty(PropertiesHolderSingleton.INPUT_DIRECTORY_PATH,
                                        chosenPath);
                    } catch (IOException ex) {
                        //log.log(Level.WARNING, "Failed to set input directory");
                    }
                } catch (IOException ex ) {
                    log.log(Level.INFO, null, ex);
                } 
            });
            return item;
        }

        private MenuItem outputDirectorySetter() {
            
            MenuItem item = new MenuItem("set output directory");
            item.addActionListener(e -> {
                try {
                    
                    // Trying to fetch initial path from properties
                    String initialPath = PropertiesHolderSingleton.INSTANCE
                            .getProperty(PropertiesHolderSingleton.OUTPUT_DIRECTORY_PATH);
                    
                    // Setting outputDirectory property 
                    try {
                        String chosenPath = chooseDirectoryPath(initialPath);
                        PropertiesHolderSingleton.INSTANCE
                                .setProperty(PropertiesHolderSingleton.OUTPUT_DIRECTORY_PATH,
                                        chosenPath);
                    } catch (IOException ex) {
                        //log.log(Level.WARNING, "Failed to set output directory");
                    }
                } catch (IOException ex ) {
                    log.log(Level.INFO, null, ex);
                } 
            });
            return item;
        }
        
        private MenuItem minAgeSetter() {
            
            MenuItem item = new MenuItem("set min. file's age");
            item.addActionListener(e -> {
                PropertiesHolderSingleton props = PropertiesHolderSingleton.INSTANCE;
                //open JOptionPane with current
                String previousValue;
                try {
                    previousValue = props.getProperty(
                            PropertiesHolderSingleton.MIN_TIME_TO_REWRITE_IN_HOURS);
                } catch (IOException ioe) {
                    previousValue = "";
                }
                
                Integer currentValue = getIntOrNullFromUser(previousValue);
                if (currentValue != null) {
                    try {
                        props.setProperty(PropertiesHolderSingleton.MIN_TIME_TO_REWRITE_IN_HOURS,
                                currentValue.toString());
                    } catch (IOException ex) {
                        log.log(Level.SEVERE, null, ex);
                    }
                }
                
            });
            return item;
        }
        
        private MenuItem configOpener() {
            
            MenuItem item = new MenuItem("open in explorer");
            item.addActionListener(e -> {
                try {
                    Runtime.getRuntime().exec("explorer.exe /select," + PropertiesHolderSingleton.INSTANCE.getPropertiesPath());
                } catch (IOException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });
            return item;
        }

        private MenuItem exiter() {
            MenuItem item = new MenuItem("exit");
            item.addActionListener(e -> {
                SystemTray.getSystemTray().remove(trayIcon);
                System.exit(0);
            });
            return item;
        }
        
        private String chooseDirectoryPath(String initialPath) throws IOException {
            
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            
            // Checking if initial path property is not initiated
            if (initialPath != null) {
                File directoryChecker = new File(initialPath);
                if (directoryChecker.isDirectory()) {
                    chooser.setCurrentDirectory(directoryChecker);
                }
            }
            
            if (chooser.showOpenDialog(new JPanel()) == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile().getAbsolutePath();
            } else {
                throw new IOException("File wasn't chosen");
            }
        }
        
        private Integer getIntOrNullFromUser(String initialValue) {
            
            return getIntOrNullFromUser(initialValue, "Set minimal age of file to copy (in hours)");
        }
        
        private Integer getIntOrNullFromUser(String initialValue, String initialMessage) {
            
            String userInput = JOptionPane.showInputDialog(initialMessage,
                    initialValue);
            if (userInput == null) return null;
            
            try {
                Integer result = Integer.parseInt(userInput);
                return result;
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Please only use Digits for this field");
                return getIntOrNullFromUser(initialValue, initialMessage);
            }
        }
    }
}
