package com.OOP.gui;

import com.OOP.MainGameLogicController;
import com.OOP.model.entities.Player;
import com.OOP.model.core.World;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


public class GameWindow extends JFrame {

    private RenderPanel renderPanel;
    private JTextArea messageArea; // To display text output from game logic
    private MainGameLogicController gameLogicController;
    private World world;
    private Player player;


    public GameWindow() {
        world = new World();
        world.setupWorld();
        player = world.getPlayer();

        // Setup MainGameLogicController to output to our JTextArea
        gameLogicController = new MainGameLogicController(world, player,
                this::appendMessage, // Consumer<String> for output
                this::updatePlayerStatus // Runnable for status (can be empty for now)
        );

        setTitle("Your Graphical Adventure");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        renderPanel = new RenderPanel(gameLogicController);
        add(renderPanel, BorderLayout.CENTER);

        messageArea = new JTextArea(5, 50); // 5 rows, 50 columns
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        add(scrollPane, BorderLayout.SOUTH);

        KeyInputHandler keyHandler = new KeyInputHandler(gameLogicController, renderPanel);
        renderPanel.addKeyListener(keyHandler); // Add key listener to the panel that has focus

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        renderPanel.startGameThread();
        gameLogicController.startGame(); // Initial game messages
        renderPanel.refreshRoomGraphics(); // Initial room draw
    }

    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(message + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength()); // Scroll to bottom
        });
    }

    private void updatePlayerStatus() {
        // This could update graphical health bars, etc., on the RenderPanel
        // For now, RenderPanel draws HP directly from player object.
        // We might call renderPanel.repaint() here if status changes require immediate redraw.
        SwingUtilities.invokeLater(() -> renderPanel.repaint());
    }


    public static void main(String[] args) {
        // Redirect System.out to avoid console pop-ups for existing System.out.println
        // but allow our GUI to capture and display it.
        // This is optional if your game logic exclusively uses the outputConsumer.
        OutputStream textAreaOutputStream = new OutputStream() {
            private JTextArea tempTextArea = new JTextArea(); // Temporary until GUI is up
            @Override
            public void write(int b) {
                // This ensures updates happen on the EDT
                // We need a reference to the actual messageArea, so this is tricky at static init
                // Best to have gameLogicController use its outputConsumer exclusively
                // System.out.print((char)b); // For debugging if needed
            }
        };
        // System.setOut(new PrintStream(textAreaOutputStream, true));
        // System.setErr(new PrintStream(textAreaOutputStream, true));


        SwingUtilities.invokeLater(GameWindow::new);
    }
}
