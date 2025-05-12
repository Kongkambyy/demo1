package com.example.demo.data.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

// LoggerUtility-klassen håndterer logging af events, fejl og advarsler
public class LoggerUtility {
    private static final Logger logger = Logger.getLogger(LoggerUtility.class.getName()); // Initialiserer Logger
    private static final String LOG_FILE = "logs/app.log"; // Sti til logfilen

    static {
        try {
            Files.createDirectories(Paths.get("logs")); // Sikrer, at log-mappen findes
            FileHandler fileHandler = new FileHandler(LOG_FILE, true); // Opretter en file handler til logging
            fileHandler.setFormatter(new SimpleFormatter()); // Sætter format for logging
            logger.addHandler(fileHandler); // Tilføjer file handler til logger
            logger.setUseParentHandlers(false); // Deaktiverer standard console logging
        } catch (IOException e) {
            System.err.println("Kunne ikke initialisere logfil: " + e.getMessage()); // Fejlhåndtering ved problemer med logfil
        }
    }

    // Metode til at logge en event (fx login-handlinger)
    public static void logEvent(String message) {
        String logMessage = formatMessage("EVENT", message); // Formatterer beskeden
        logger.info(logMessage); // Logger informationen
        writeToLogFile(logMessage); // Skriver beskeden til logfilen
    }

    // Metode til at logge en fejl (Exception vises også i UI via SceneBuilder)
    public static void logError(String message) {
        String logMessage = formatMessage("ERROR", message); // Formatterer fejlbeskeden
        logger.severe(logMessage); // Logger fejlen som 'severe'
        writeToLogFile(logMessage); // Skriver fejlen til logfilen
    }

    // Metode til at logge en advarsel
    public static void logWarning(String message) {
        String logMessage = formatMessage("WARNING", message); // Formatterer advarslen
        logger.warning(logMessage); // Logger advarslen som 'warning'
        writeToLogFile(logMessage); // Skriver advarslen til logfilen
    }

    // Formatterer logmeddelelser med timestamp
    private static String formatMessage(String type, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); // Henter timestamp
        return String.format("[%s] %s: %s", timestamp, type, message); // Returnerer formatteret besked
    }

    // Skriver log til fil
    private static void writeToLogFile(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) { // Åbner logfil til skrivning
            writer.write(message); // Skriver logmeddelelsen
            writer.newLine(); // Tilføjer ny linje
        } catch (IOException e) {
            System.err.println("Kunne ikke skrive til logfil: " + e.getMessage()); // Fejlhåndtering ved skrivefejl
        }
    }
}