/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zubarevam.filescopier;

import com.zubarevam.filescopier.gui.SystemTrayIconSingleton;
import java.awt.AWTException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.*;


/**
 *
 * @author ZubarevAM
 */
public class EntryPoint {

    private static final Logger log = Logger.getLogger(EntryPoint.class.getName());
    static {
        try {
            Path path = Paths.get(System.getenv("ProgramFiles").concat("\\zamSoft\\FilesCopier"));
            Files.createDirectories(path);
            System.out.println(path);
            FileHandler fh = new FileHandler(path.toString().concat("\\logs.log"), true);
            fh.setFormatter(new SimpleFormatter());
            log.addHandler(fh);
            log.info("all good");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        
        // trying to set normal looking for file choosers
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SystemTrayIconSingleton.INSTANCE.tryCreateTrayApp();
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex);
        }
    }
}
