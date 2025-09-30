import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;

public class ToDoApp extends JFrame {
    private DefaultListModel<String> listModel;
    private JList<String> taskList;
    private JTextField taskField;
    private JLabel statusLabel;
    private static final String DATA_FILE = "tasks.txt";

    public ToDoApp() {
        super("To-Do App (Java Swing)");
        initComponents();
        loadTasksOnStart();
    }

    private void initComponents() {
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        taskList.setVisibleRowCount(10);

        JScrollPane listScroll = new JScrollPane(taskList);

        taskField = new JTextField(20);
        JButton addBtn = new JButton("Add");
        JButton deleteBtn = new JButton("Delete");
        JButton editBtn = new JButton("Edit");
        JButton saveBtn = new JButton("Save");
        JButton loadBtn = new JButton("Load");
        JButton clearBtn = new JButton("Clear All");

        statusLabel = new JLabel("Tasks: 0");

        // Top panel: input + add
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("New task:"));
        topPanel.add(taskField);
        topPanel.add(addBtn);

        // Bottom panel: controls
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(deleteBtn);
        bottomPanel.add(editBtn);
        bottomPanel.add(saveBtn);
        bottomPanel.add(loadBtn);
        bottomPanel.add(clearBtn);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(statusLabel);

        // Layout
        this.setLayout(new BorderLayout(8, 8));
        this.add(topPanel, BorderLayout.NORTH);
        this.add(listScroll, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        // Actions
        addBtn.addActionListener(e -> addTask());
        taskField.addActionListener(e -> addTask());

        deleteBtn.addActionListener(e -> deleteSelected());
        editBtn.addActionListener(e -> editSelected());
        saveBtn.addActionListener(e -> saveTasks());
        loadBtn.addActionListener(e -> loadTasks());
        clearBtn.addActionListener(e -> clearAllTasks());

        // Double click to edit
        taskList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int idx = taskList.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        editTaskAt(idx);
                    }
                }
            }
        });

        // Delete key to remove
        taskList.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteSelected();
                }
            }
        });

        // Window close -> ask to save?
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                int opt = JOptionPane.showConfirmDialog(
                        ToDoApp.this,
                        "Do you want to save tasks before exit?",
                        "Exit",
                        JOptionPane.YES_NO_CANCEL_OPTION
                );
                if (opt == JOptionPane.CANCEL_OPTION) {
                    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                } else {
                    if (opt == JOptionPane.YES_OPTION) saveTasks();
                    setDefaultCloseOperation(EXIT_ON_CLOSE);
                }
            }
        });

        updateStatus();
        this.setSize(550, 420);
        this.setLocationRelativeTo(null); // center
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    private void addTask() {
        String text = taskField.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a non-empty task.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        listModel.addElement(text);
        taskField.setText("");
        updateStatus();
    }

    private void deleteSelected() {
        int[] sel = taskList.getSelectedIndices();
        if (sel.length == 0) {
            JOptionPane.showMessageDialog(this, "Select at least one task to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected task(s)?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        for (int i = sel.length - 1; i >= 0; i--) {
            listModel.remove(sel[i]);
        }
        updateStatus();
    }

    private void editSelected() {
        int idx = taskList.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Select a single task to edit (double-click also works).", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        editTaskAt(idx);
    }

    private void editTaskAt(int idx) {
        String oldVal = listModel.getElementAt(idx);
        String newVal = JOptionPane.showInputDialog(this, "Edit task:", oldVal);
        if (newVal != null) {
            newVal = newVal.trim();
            if (!newVal.isEmpty()) {
                listModel.set(idx, newVal);
                updateStatus();
            } else {
                JOptionPane.showMessageDialog(this, "Task cannot be empty.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void clearAllTasks() {
        if (listModel.isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Clear ALL tasks?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            listModel.clear();
            updateStatus();
        }
    }

    private void saveTasks() {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(DATA_FILE))) {
            for (int i = 0; i < listModel.size(); i++) {
                bw.write(listModel.get(i));
                bw.newLine();
            }
            JOptionPane.showMessageDialog(this, "Tasks saved to " + DATA_FILE, "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving tasks: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasks() {
        Path p = Paths.get(DATA_FILE);
        if (!Files.exists(p)) {
            JOptionPane.showMessageDialog(this, DATA_FILE + " not found. Nothing loaded.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            List<String> lines = Files.readAllLines(p);
            listModel.clear();
            for (String l : lines) {
                if (l != null && !l.trim().isEmpty()) listModel.addElement(l);
            }
            updateStatus();
            JOptionPane.showMessageDialog(this, "Tasks loaded from " + DATA_FILE, "Loaded", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasksOnStart() {
        // try to auto-load if file exists (silent)
        Path p = Paths.get(DATA_FILE);
        if (Files.exists(p)) {
            try {
                List<String> lines = Files.readAllLines(p);
                for (String l : lines) if (l != null && !l.trim().isEmpty()) listModel.addElement(l);
                updateStatus();
            } catch (IOException ignored) {}
        }
    }

    private void updateStatus() {
        statusLabel.setText("Tasks: " + listModel.getSize());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Optional: set system look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            ToDoApp app = new ToDoApp();
            app.setVisible(true);
        });
    }
}
