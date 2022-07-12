package com.cherokeelessons.raven;

import com.cherokeelessons.gui.MainWindow;
import com.cherokeelessons.gui.MainWindow.Config;

import java.awt.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Config config = new Config() {
            @Override
            public String getApptitle() {
                return "Raven Dictionary Rebuilder";
            }

            @Override
            public Runnable getApp(String... args) throws Exception {
                return new App(this, args);
            }
        };
        config.setAutoExit(true);
        config.setAutoExitOnError(true);
        EventQueue.invokeLater(new MainWindow(config, args));
    }
}