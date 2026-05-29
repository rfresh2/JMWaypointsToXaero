package com.github.rfresh2;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JourneyMapWaypointsToXaeroGui extends JFrame {
    private final JTextField inputFolderField = new JTextField(32);
    private final JTextField outputFolderField = new JTextField(32);
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton convertButton = new JButton("Convert");

    private JourneyMapWaypointsToXaeroGui() {
        super("JMWaypointsToXaero");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(620, 220));
        setSize(new Dimension(620, 220));
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setContentPane(content);

        JPanel form = new JPanel(new GridBagLayout());
        content.add(form, BorderLayout.NORTH);

        addPathRow(form, 0, "JourneyMap World Folder", inputFolderField);
        addPathRow(form, 1, "Xaero Waypoints Folder", outputFolderField);

        convertButton.addActionListener(event -> convert());

        JPanel actions = new JPanel(new BorderLayout(0, 10));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.add(convertButton);
        actions.add(buttonPanel, BorderLayout.NORTH);

        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        actions.add(statusLabel, BorderLayout.CENTER);
        content.add(actions, BorderLayout.CENTER);

        pack();
        setSize(new Dimension(620, 220));
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatDarculaLaf());
            } catch (Exception ignored) {
            }
            new JourneyMapWaypointsToXaeroGui().setVisible(true);
        });
    }

    private void addPathRow(JPanel content, int row, String label, JTextField field) {
        JLabel pathLabel = new JLabel(label);
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(0, 0, 8, 8);
        content.add(pathLabel, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(0, 0, 8, 8);
        content.add(field, fieldConstraints);

        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(event -> chooseFolder(field));
        GridBagConstraints browseConstraints = new GridBagConstraints();
        browseConstraints.gridx = 2;
        browseConstraints.gridy = row;
        browseConstraints.insets = new Insets(0, 0, 8, 0);
        content.add(browseButton, browseConstraints);
    }

    private void chooseFolder(JTextField field) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (!field.getText().trim().isEmpty()) {
            fileChooser.setSelectedFile(Paths.get(field.getText().trim()).toFile());
        }

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void convert() {
        String input = inputFolderField.getText().trim();
        String output = outputFolderField.getText().trim();
        String validationError = validateInputs(input, output);
        if (validationError != null) {
            showError(validationError);
            return;
        }

        convertButton.setEnabled(false);
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setText("Converting...");

        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return JourneyMapWaypointsToXaero.convert(input, output);
            }

            @Override
            protected void done() {
                convertButton.setEnabled(true);
                try {
                    int count = get();
                    statusLabel.setForeground(new Color(0, 128, 0));
                    statusLabel.setText("Conversion complete. Converted " + count + " waypoint" + (count == 1 ? "." : "s."));
                } catch (Exception e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    showError(cause.getMessage() == null ? "Conversion failed." : cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private String validateInputs(String input, String output) {
        if (input.isEmpty()) {
            return "JourneyMap world folder required.";
        }
        if (output.isEmpty()) {
            return "Xaero waypoints folder required.";
        }

        Path inputWaypoints = Paths.get(input, "waypoints");
        if (!Files.isDirectory(inputWaypoints)) {
            return "JourneyMap world folder does not exist: " + inputWaypoints;
        }

        Path outputPath = Paths.get(output);
        if (Files.exists(outputPath) && !Files.isDirectory(outputPath)) {
            return "Xaero waypoints path must be a folder: " + outputPath;
        }
        Path outputParent = outputPath.toAbsolutePath().getParent();
        if (!Files.exists(outputPath) && outputParent != null && !Files.isDirectory(outputParent)) {
            return "Xaero waypoints parent folder does not exist: " + outputParent;
        }

        return null;
    }

    private void showError(String message) {
        statusLabel.setForeground(Color.RED);
        statusLabel.setText(message);
    }
}
