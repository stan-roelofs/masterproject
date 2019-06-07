package gui;

import core.io.BufferedOutputWriter;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

class GUI extends JFrame {

    private ProofPanel proofPanel;

    GUI() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300, 300);

        setupMenuBar();

        proofPanel = new ProofPanel();
        this.add(proofPanel);

        this.setVisible(true);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");

        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(actionEvent -> {
            FileDialog fd = new FileDialog(this, "Save to file", FileDialog.SAVE);
            fd.setVisible(true);
            String filename = fd.getFile();
            if (filename == null) {
                System.out.println("You cancelled the choice");
            } else {
                System.out.println("You chose " + filename);
                String path = fd.getDirectory() + fd.getFile();
                File outputFile = new File(path);
                try {
                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    BufferedOutputWriter writer = new BufferedOutputWriter(new OutputStreamWriter(outputStream));
                    writer.write(proofPanel.getInput());
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
        menu.add(save);

        JMenuItem load = new JMenuItem("Load");
        load.addActionListener(actionEvent -> {
            FileDialog fd = new FileDialog(this, "Choose a file", FileDialog.LOAD);
            fd.setVisible(true);
            String filename = fd.getFile();
            if (filename == null) {
                System.out.println("You cancelled the choice");
            } else {
                System.out.println("You chose " + filename);
                // Read file
                String path = fd.getDirectory() + fd.getFile();
                try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                    List<String> input = new ArrayList<>();

                    String line = br.readLine();
                    while (line != null) {
                        input.add(line);
                        line = br.readLine();
                    }

                    proofPanel.setInput(input);
                } catch (IOException e) {

                }
            }
        });
        menu.add(load);

        menuBar.add(menu);
        this.setJMenuBar(menuBar);
    }

    public static void main(String[] args) {
        new GUI();
    }
}
