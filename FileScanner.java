import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileScanner {
    
    public List<FileInfo> scanDirectory(String path) {
        List<FileInfo> fileInfoList = new ArrayList<>();
        scanDirectoryRecursively(new File(path), fileInfoList);
        return fileInfoList;
    }
    
    private void scanDirectoryRecursively(File directory, List<FileInfo> fileInfoList) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    FileInfo fileInfo = new FileInfo(
                        file.getAbsolutePath(),
                        file.length(),
                        new Date(file.lastModified()),
                        new Date(file.lastModified()),
                        getPermissions(file)
                    );
                    fileInfoList.add(fileInfo);
                    
                    if (file.isDirectory()) {
                        scanDirectoryRecursively(file, fileInfoList);
                    }
                }
            }
        }
    }
    
    private String getPermissions(File file) {
        StringBuilder permissions = new StringBuilder();
        permissions.append(file.canRead() ? "r" : "-");
        permissions.append(file.canWrite() ? "w" : "-");
        permissions.append(file.canExecute() ? "x" : "-");
        return permissions.toString();
    }
    
    public static class FileInfo {
        private String path;
        private long size;
        private Date modifiedTime;
        private Date createdTime;
        private String permissions;
        
        public FileInfo(String path, long size, Date modifiedTime, Date createdTime, String permissions) {
            this.path = path;
            this.size = size;
            this.modifiedTime = modifiedTime;
            this.createdTime = createdTime;
            this.permissions = permissions;
        }
        
        public String getPath() { return path; }
        public long getSize() { return size; }
        public Date getModifiedTime() { return modifiedTime; }
        public Date getCreatedTime() { return createdTime; }
        public String getPermissions() { return permissions; }
        
        @Override
        public String toString() {
            return "FileInfo{" +
                    "path='" + path + '\'' +
                    ", size=" + size +
                    ", modifiedTime=" + modifiedTime +
                    ", createdTime=" + createdTime +
                    ", permissions='" + permissions + '\'' +
                    '}';
        }
    }
}