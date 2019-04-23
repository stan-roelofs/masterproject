package gui;

import core.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        new GUI();
    }
}

class GUI extends JFrame {
    GUI() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300, 300);

        this.add(new ProofPanel());


        this.setVisible(true);
    }
}

class ProofPanel extends JPanel {

    private TextArea inputArea;
    private TextArea outputArea;
    private JButton proofButton;
    private JTextField searchSteps;

    ProofPanel() {
        this.setLayout(new GridLayout(0, 2));

        this.inputArea = new TextArea();
        this.add(inputArea);

        this.outputArea = new TextArea();
        this.add(outputArea);

        this.searchSteps = new JTextField();
        this.add(searchSteps);

        this.proofButton = new JButton("Start");
        this.proofButton.addActionListener(actionEvent -> {

            String[] split = inputArea.getText().split("\\n");
            List<String> input = new ArrayList<>(Arrays.asList(split));

            EquationSystem system = null;
            try {
                system = InputParser.parseSystem(input);
            } catch(ParserException e) {
                Logger.e("Exception while parsing, quitting program");
            }
            system.print();

            // If no output file specified, use System.out
            OutputStream output = System.out;

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));

            int searchDepth = Integer.parseInt(this.searchSteps.getText());

            try {
                Prover.induction(system, writer, searchDepth, 0, null);
            } catch (IOException e) {
                Logger.e("IOException: " + e.getMessage());
            }

            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        this.add(proofButton);
    }
}
