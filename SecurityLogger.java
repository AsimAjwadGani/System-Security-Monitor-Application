import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SecurityLogger {
    private static final String LOG_FILE = "security_log.txt";
    private static final String ENCRYPTED_LOG_FILE = "security_log.encrypted";
    private static final String LOG_PASSWORD = "default_log_password"; 
    
    public void logAction(String action, String target, String result) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new Date());
        String logEntry = String.format("[%s] ACTION: %s | TARGET: %s | RESULT: %s%n", 
                                      timestamp, action, target, result);
        
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            writer.write(logEntry);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
    
    public List<String> readLogs() throws IOException {
        List<String> logs = new ArrayList<>();
        
        File logFile = new File(LOG_FILE);
        if (!logFile.exists()) {
            return logs;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logs.add(line);
            }
        }
        
        return logs;
    }
    
    public void encryptLog() throws Exception {
        SecurityEngine securityEngine = new SecurityEngine();
        securityEngine.encryptFile(LOG_FILE, LOG_PASSWORD);
        
        new File(LOG_FILE).delete();
    }
    
    public void decryptLog() throws Exception {
        SecurityEngine securityEngine = new SecurityEngine();
        securityEngine.decryptFile(ENCRYPTED_LOG_FILE, LOG_PASSWORD);
    }
    
    public boolean hasEncryptedLog() {
        return new File(ENCRYPTED_LOG_FILE).exists();
    }
}