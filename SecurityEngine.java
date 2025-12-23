import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SecurityEngine {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int KEY_LENGTH = 256;
    private static final int PBKDF2_ITERATIONS = 100000;
    private static final int SALT_LENGTH = 16;
    
    public void encryptFile(String filePath, String password) throws Exception {
        File inputFile = new File(filePath);
        File outputFile = new File(filePath + ".encrypted");
        
        byte[] salt = generateSalt();
        SecretKey key = generateKeyFromPassword(password, salt);
        byte[] iv = generateIV();
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile);
             DataOutputStream dos = new DataOutputStream(fos)) {
            
            dos.write(salt);
            dos.write(iv);
            
            try (CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    cos.write(buffer, 0, bytesRead);
                }
            }
        }
    }
    
    public void decryptFile(String filePath, String password) throws Exception {
        File inputFile = new File(filePath);
        String outputFilePath = filePath.replace(".encrypted", "");
        File outputFile = new File(outputFilePath);
        
        try (FileInputStream fis = new FileInputStream(inputFile);
             DataInputStream dis = new DataInputStream(fis)) {
            
            byte[] salt = new byte[SALT_LENGTH];
            dis.readFully(salt);
            
            byte[] iv = new byte[GCM_IV_LENGTH];
            dis.readFully(iv);
            
            SecretKey key = generateKeyFromPassword(password, salt);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            
            try (FileOutputStream fos = new FileOutputStream(outputFile);
                 CipherInputStream cis = new CipherInputStream(dis, cipher)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = cis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
        } catch (javax.crypto.AEADBadTagException e) {
            outputFile.delete();
            throw new Exception("Incorrect password or corrupted file", e);
        } catch (Exception e) {
            outputFile.delete();
            throw e;
        }
    }
    
    public void encryptFolder(String folderPath, String password) throws Exception {
        File folder = new File(folderPath);
        String zipPath = folderPath + ".zip";
        String encryptedZipPath = zipPath + ".encrypted";
  
        zipFolder(folderPath, zipPath);
        
        encryptFile(zipPath, password);
        
        new File(zipPath).delete();
    }
    
    public void decryptFolder(String encryptedFolderPath, String password) throws Exception {
        String zipPath = encryptedFolderPath.replace(".encrypted", "");
        String folderPath = zipPath.replace(".zip", "");
        
        decryptFile(encryptedFolderPath, password);
        
        unzipFolder(zipPath, folderPath);
        
        new File(zipPath).delete();
    }
    
    private void zipFolder(String folderPath, String zipPath) throws IOException {
        File folder = new File(folderPath);
        try (FileOutputStream fos = new FileOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            zipDirectory(folder, folder.getName(), zos);
        }
    }
    
    private void zipDirectory(File folder, String parentPath, ZipOutputStream zos) throws IOException {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    zipDirectory(file, parentPath + "/" + file.getName(), zos);
                } else {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        ZipEntry zipEntry = new ZipEntry(parentPath + "/" + file.getName());
                        zos.putNextEntry(zipEntry);
                        
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                        zos.closeEntry();
                    }
                }
            }
        }
    }
    
    private void unzipFolder(String zipPath, String extractPath) throws IOException {
        File destDir = new File(extractPath);
        destDir.mkdirs();
        
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(destDir, zipEntry.getName());
                
                String destDirPath = destDir.getCanonicalPath();
                String destFilePath = newFile.getCanonicalPath();
                if (!destFilePath.startsWith(destDirPath + File.separator)) {
                    throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
                }
                
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    File parent = newFile.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }
                    
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }
    
    private SecretKey generateKeyFromPassword(String password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
    
    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
    
    private byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}