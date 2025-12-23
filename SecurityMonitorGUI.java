import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

public class SecurityMonitorGUI extends JPanel {
    private JTree directoryTree;
    private JTable fileInfoTable;
    private JTextArea statusArea;
    private JTextField selectedPathField;
    private JButton scanButton, encryptButton, decryptButton, logButton;
    private JPasswordField passwordField;
    private FileScanner fileScanner;
    private SecurityEngine securityEngine;
    private SecurityLogger securityLogger;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;

    public SecurityMonitorGUI() {
        fileScanner = new FileScanner();
        securityEngine = new SecurityEngine();
        securityLogger = new SecurityLogger();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        rootNode = new DefaultMutableTreeNode("Root");
        treeModel = new DefaultTreeModel(rootNode);
        directoryTree = new JTree(treeModel);
        directoryTree.setShowsRootHandles(true);

        String[] columnNames = {"Name", "Size", "Modified", "Type"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        fileInfoTable = new JTable(tableModel);

        statusArea = new JTextArea(5, 50);
        statusArea.setEditable(false);
        JScrollPane statusScrollPane = new JScrollPane(statusArea);

        selectedPathField = new JTextField();
        selectedPathField.setEditable(false);

        scanButton = new JButton("Scan Directory");
        encryptButton = new JButton("Encrypt");
        decryptButton = new JButton("Decrypt");
        logButton = new JButton("View Logs");
        passwordField = new JPasswordField(15);

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Selected Path:"));
        topPanel.add(selectedPathField);
        topPanel.add(scanButton);
        topPanel.add(new JLabel("Password:"));
        topPanel.add(passwordField);
        topPanel.add(encryptButton);
        topPanel.add(decryptButton);
        topPanel.add(logButton);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(directoryTree));
        splitPane.setRightComponent(new JScrollPane(fileInfoTable));
        splitPane.setDividerLocation(300);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(new JScrollPane(statusArea), BorderLayout.SOUTH);
    }

    private void setupLayout() {
    }

    private void setupEventHandlers() {
        directoryTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getPath();
                if (path != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    Object obj = node.getUserObject();
                    if (obj instanceof File) {
                        File selectedFile = (File) obj;
                        selectedPathField.setText(selectedFile.getAbsolutePath());
                        updateFileInfoTable(selectedFile);
                    }
                }
            }
        });

        scanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performScan();
            }
        });

        encryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performEncryption();
            }
        });

        decryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performDecryption();
            }
        });

        logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewLogs();
            }
        });
    }

    private void performScan() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            updateStatus("Scanning directory: " + path);
            
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    buildDirectoryTree(path);
                    return null;
                }
                
                @Override
                protected void done() {
                    updateStatus("Directory scan completed: " + path);
                    securityLogger.logAction("SCAN", path, "SUCCESS");
                }
            };
            worker.execute();
        }
    }

    private void buildDirectoryTree(String rootPath) {
        SwingUtilities.invokeLater(() -> {
            rootNode.removeAllChildren();
            File rootFile = new File(rootPath);
            DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(rootFile.getName());
            rootTreeNode.setUserObject(rootFile);
            buildTreeRecursively(rootFile, rootTreeNode);
            rootNode.add(rootTreeNode);
            treeModel.reload();
        });
    }

    private void buildTreeRecursively(File file, DefaultMutableTreeNode node) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child.getName());
                    childNode.setUserObject(child);
                    node.add(childNode);
                    if (child.isDirectory()) {
                        buildTreeRecursively(child, childNode);
                    }
                }
            }
        }
    }

    private void updateFileInfoTable(File file) {
        DefaultTableModel model = (DefaultTableModel) fileInfoTable.getModel();
        model.setRowCount(0);
        
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    Object[] row = {
                        f.getName(),
                        f.length() + " bytes",
                        new java.util.Date(f.lastModified()).toString(),
                        f.isDirectory() ? "Directory" : "File"
                    };
                    model.addRow(row);
                }
            }
        } else {
            Object[] row = {
                file.getName(),
                file.length() + " bytes",
                new java.util.Date(file.lastModified()).toString(),
                "File"
            };
            model.addRow(row);
        }
    }

    private void performEncryption() {
        String path = selectedPathField.getText();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a file or directory to encrypt.");
            return;
        }

        char[] password = passwordField.getPassword();
        if (password.length == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a password for encryption.");
            return;
        }

        updateStatus("Encrypting: " + path);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                File file = new File(path);
                if (file.isDirectory()) {
                    securityEngine.encryptFolder(path, new String(password));
                } else {
                    securityEngine.encryptFile(path, new String(password));
                }
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    updateStatus("Encryption completed: " + path);
                    securityLogger.logAction("ENCRYPT", path, "SUCCESS");
                    JOptionPane.showMessageDialog(SecurityMonitorGUI.this, "Encryption completed successfully!");
                } catch (Exception e) {
                    updateStatus("Encryption failed: " + e.getMessage());
                    securityLogger.logAction("ENCRYPT", path, "FAILED: " + e.getMessage());
                    JOptionPane.showMessageDialog(SecurityMonitorGUI.this, "Encryption failed: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void performDecryption() {
        String path = selectedPathField.getText();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a file or directory to decrypt.");
            return;
        }

        char[] password = passwordField.getPassword();
        if (password.length == 0) {
            JOptionPane.showMessageDialog(this, "Please enter the password for decryption.");
            return;
        }

        updateStatus("Decrypting: " + path);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                File file = new File(path);
                if (file.isDirectory()) {
                    securityEngine.decryptFolder(path, new String(password));
                } else {
                    securityEngine.decryptFile(path, new String(password));
                }
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get(); 
                    updateStatus("Decryption completed: " + path);
                    securityLogger.logAction("DECRYPT", path, "SUCCESS");
                    JOptionPane.showMessageDialog(SecurityMonitorGUI.this, "Decryption completed successfully!");
                } catch (Exception e) {
                    updateStatus("Decryption failed: " + e.getMessage());
                    securityLogger.logAction("DECRYPT", path, "FAILED: " + e.getMessage());
                    JOptionPane.showMessageDialog(SecurityMonitorGUI.this, "Decryption failed: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void viewLogs() {
        try {
            List<String> logs = securityLogger.readLogs();
            StringBuilder logContent = new StringBuilder();
            for (String log : logs) {
                logContent.append(log).append("\n");
            }
            
            JTextArea logArea = new JTextArea(logContent.toString());
            logArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(logArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));
            
            JOptionPane.showMessageDialog(this, scrollPane, "Security Logs", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error reading logs: " + e.getMessage());
        }
    }

    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusArea.append(new java.util.Date() + " - " + message + "\n");
            statusArea.setCaretPosition(statusArea.getDocument().getLength());
        });
    }
}