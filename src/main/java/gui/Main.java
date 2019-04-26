package gui;

import core.EquationSystem;
import core.Logger;
import core.Prover;
import core.io.TextAreaOutputWriter;
import core.parsing.InputParser;
import core.parsing.ParserException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
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

    private JTextArea inputArea;
    private JTextArea outputArea;
    private JButton proofButton;
    private JTextField searchSteps;

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
                Prover.induction(system, writer, searchDepth, 0, null);
                writer.close();
            } catch (IOException e) {
                Logger.e("IOException: " + e.getMessage());
            }
        });
        this.add(proofButton);
    }
}
