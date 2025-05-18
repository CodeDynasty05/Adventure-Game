package com.OOP;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

// Import your game model classes
import com.OOP.model.entities.Player;
import com.OOP.model.core.Room;
import com.OOP.model.core.World;

public class GameGUI extends JFrame {

    private JTextArea outputArea;
    private JTextField inputField;
    // Removed individual submitButton as Enter in inputField is primary

    // Panels
    private JPanel mainPanel;
    private JPanel statusPanel;
    private JPanel actionButtonPanel;
    private JPanel inputPanel;

    // Status Labels
    private JLabel roomLabel;
    private JLabel healthLabel;
    private JLabel goldLabel;
    private JLabel equippedWeaponLabel;
    private JLabel equippedShieldLabel;

    // Action Buttons
    private JButton northButton, southButton, eastButton, westButton;
    private JButton lookButton, inventoryButton, takeButton, useButton, equipButton, attackButton, talkButton;


    private World world;
    private Player player;
    private MainGameLogicController gameLogicController;

    public GameGUI() {
        try {
            // Apply a more modern Look and Feel
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.err.println("Nimbus L&F not found, using default.");
            // If Nimbus isn't available, it will fall back to the default Metal L&F
        }

        world = new World();
        world.setupWorld();
        player = world.getPlayer();

        gameLogicController = new MainGameLogicController(world, player, this::updateGameOutput, this::updatePlayerStatus);

        setTitle("Text Adventure Chronicles");
        setSize(900, 700); // Slightly larger
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        initComponents();
        redirectSystemStreams();

        gameLogicController.startGame();
        updatePlayerStatus();
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout(10, 10)); // Add some spacing
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Padding around the main panel
        setContentPane(mainPanel);

        // --- Output Area (Center) ---
        outputArea = new JTextArea(20, 50); // Suggest initial size
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // A slightly nicer font if available
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Game Log"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Status Panel (North) ---
        statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS)); // Vertical layout
        statusPanel.setBorder(BorderFactory.createTitledBorder("Player Status"));

        roomLabel = createStatusLabel("Room: N/A");
        healthLabel = createStatusLabel("HP: N/A");
        goldLabel = createStatusLabel("Gold: N/A");
        equippedWeaponLabel = createStatusLabel("Weapon: None");
        equippedShieldLabel = createStatusLabel("Shield: None");

        statusPanel.add(roomLabel);
        statusPanel.add(healthLabel);
        statusPanel.add(goldLabel);
        statusPanel.add(equippedWeaponLabel);
        statusPanel.add(equippedShieldLabel);
        mainPanel.add(statusPanel, BorderLayout.WEST); // Move status to the West


        // --- Action Button Panel (East) ---
        actionButtonPanel = new JPanel(new GridLayout(0, 1, 5, 5)); // Single column of buttons
        actionButtonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        // Directional Buttons
        JPanel directionPanel = new JPanel(new GridBagLayout());
        directionPanel.setBorder(new TitledBorder("Movement"));
        GridBagConstraints gbc = new GridBagConstraints();
        northButton = createActionButton("Go North", "go north");
        southButton = createActionButton("Go South", "go south");
        eastButton = createActionButton("Go East", "go east");
        westButton = createActionButton("Go West", "go west");

        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; directionPanel.add(northButton, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; directionPanel.add(westButton, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; directionPanel.add(southButton, gbc);
        gbc.gridx = 2; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; directionPanel.add(eastButton, gbc);
        actionButtonPanel.add(directionPanel);

        // Common Action Buttons
        JPanel commonActionsPanel = new JPanel(new GridLayout(0, 2, 5, 5)); // Two buttons per row
        commonActionsPanel.setBorder(new TitledBorder("Commands"));
        lookButton = createActionButton("Look", "look");
        inventoryButton = createActionButton("Inventory", "inventory");
        takeButton = createActionButton("Take Item...", "take "); // Add space for prompting
        useButton = createActionButton("Use Item...", "use ");
        equipButton = createActionButton("Equip Item...", "equip ");
        attackButton = createActionButton("Attack...", "attack ");
        talkButton = createActionButton("Talk To...", "talk ");

        commonActionsPanel.add(lookButton);
        commonActionsPanel.add(inventoryButton);
        commonActionsPanel.add(takeButton);
        commonActionsPanel.add(useButton);
        commonActionsPanel.add(equipButton);
        commonActionsPanel.add(attackButton);
        commonActionsPanel.add(talkButton);
        actionButtonPanel.add(commonActionsPanel);

        mainPanel.add(actionButtonPanel, BorderLayout.EAST);


        // --- Input Panel (South) ---
        inputPanel = new JPanel(new BorderLayout(5,0));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Command Input"));
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton submitButton = new JButton("Send"); // Re-add submit button explicitly for clarity
        // submitButton.setIcon(new ImageIcon(getClass().getResource("/path/to/send_icon.png"))); // Example for icon

        inputPanel.add(new JLabel("Enter command: "), BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // Action Listeners for input field and submit button
        ActionListener inputProcessor = e -> processPlayerInput();
        inputField.addActionListener(inputProcessor);
        submitButton.addActionListener(inputProcessor);
    }

    private JLabel createStatusLabel(String initialText) {
        JLabel label = new JLabel(initialText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setBorder(new EmptyBorder(2, 5, 2, 5)); // Add some padding within the label
        return label;
    }

    private JButton createActionButton(String buttonText, String commandPrefix) {
        JButton button = new JButton(buttonText);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setToolTipText("Executes: " + commandPrefix + (commandPrefix.endsWith(" ") ? "<target_name>" : ""));
        // button.setIcon(new ImageIcon(getClass().getResource("/path/to/icon.png"))); // Example for icon
        button.addActionListener(e -> {
            if (commandPrefix.endsWith(" ")) { // For commands that need a target
                String target = JOptionPane.showInputDialog(this,
                        "What do you want to " + commandPrefix.trim() + "?",
                        "Specify Target", JOptionPane.PLAIN_MESSAGE);
                if (target != null && !target.trim().isEmpty()) {
                    gameLogicController.processInput(commandPrefix + target.trim());
                } else if (target != null) { // User pressed OK with empty input
                    outputArea.append("\n> " + commandPrefix + "\nNo target specified.\n");
                }
                // If target is null (Cancel pressed), do nothing
            } else { // For commands without arguments or specific target from button
                gameLogicController.processInput(commandPrefix);
            }
        });
        return button;
    }

    private void processPlayerInput() {
        String command = inputField.getText().trim();
        if (!command.isEmpty()) {
            outputArea.append("\n> " + command + "\n");
            gameLogicController.processInput(command);
            inputField.setText("");
        }
    }

    public void updateGameOutput(String text) {
        // Ensure updates are on the EDT
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text + "\n");
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    public void updatePlayerStatus() {
        SwingUtilities.invokeLater(() -> {
            if (player != null && player.getCurrentRoom() != null) {
                roomLabel.setText("Room: " + player.getCurrentRoom().getName());
                healthLabel.setText("HP: " + player.getHealthPoints() + "/" + player.getMaxHealthPoints());
                goldLabel.setText("Gold: " + player.getGoldQuantity());
                equippedWeaponLabel.setText("Weapon: " + (player.getEquippedWeapon() != null ? player.getEquippedWeapon().getName() : "None"));
                equippedShieldLabel.setText("Shield: " + (player.getEquippedShield() != null ? player.getEquippedShield().getName() : "None"));

                Room currentRoom = player.getCurrentRoom();
                northButton.setEnabled(currentRoom.getExit("north") != null);
                southButton.setEnabled(currentRoom.getExit("south") != null);
                eastButton.setEnabled(currentRoom.getExit("east") != null);
                westButton.setEnabled(currentRoom.getExit("west") != null);
            }
        });
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // Forward to the GUI update method, ensuring it's on EDT
                updateGUIOutputCharByChar(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                updateGUIOutputCharByChar(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    // Modified to handle char-by-char updates if necessary, or full strings
    private void updateGUIOutputCharByChar(final String text) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameGUI gui = new GameGUI();
            gui.setVisible(true);
        });
    }
}