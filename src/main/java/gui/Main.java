package gui;

import core.EquationSystem;
import core.Logger;
import core.Prover;
import core.io.BufferedOutputWriter;
import core.io.TextAreaOutputWriter;
import core.parsing.InputParser;
import core.parsing.ParserException;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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

class ProofPanel extends JPanel {

    private JTextArea inputArea;
    private JTextArea outputArea;
    private JButton proofButton;
    private JTextField searchSteps;
    private JToggleButton rewriteLeft;

    ProofPanel() {
        this.setLayout(new GridLayout(0, 2));

        inputArea = new JTextArea();
        JScrollPane sp = new JScrollPane(inputArea);
        this.add(sp);

        outputArea = new JTextArea();
        JScrollPane sp2 = new JScrollPane(outputArea);
        this.add(sp2);

        searchSteps = new JTextField("8");
        add(searchSteps);

        rewriteLeft = new JToggleButton("Rewrite both directions");
        add(rewriteLeft);

        proofButton = new JButton("Start");
        proofButton.addActionListener(actionEvent -> {

            String[] split = inputArea.getText().split("\\n");
            List<String> input = new ArrayList<>(Arrays.asList(split));

            EquationSystem system = null;
            try {
                system = InputParser.parseSystem(input);
            } catch(ParserException e) {
                Logger.e("Exception while parsing, quitting program");
            }
            system.print();

            outputArea.setText("");
            TextAreaOutputWriter writer = new TextAreaOutputWriter(outputArea);
            int searchDepth = Integer.parseInt(searchSteps.getText());

            try {
                Prover.induction(system, writer, searchDepth, rewriteLeft.isSelected(),0, null);
                writer.close();
            } catch (IOException e) {
                Logger.e("IOException: " + e.getMessage());
            }
        });
        this.add(proofButton);
    }

    void setInput(String text) {
        this.inputArea.setText(text);
    }

    void setInput(List<String> lines) {
        this.inputArea.setText("");

        for (String line : lines) {
            this.inputArea.append(line);
            this.inputArea.append("\n");
        }
    }

    String getInput() {
        return this.inputArea.getText();
    }
}
