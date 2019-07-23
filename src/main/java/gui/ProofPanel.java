package gui;

import core.EquationSystem;
import core.Prover;
import core.io.TextAreaOutputWriter;
import core.logging.Logger;
import core.parsing.InputParser;
import core.parsing.ParserException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ProofPanel extends JPanel {

    private JTextArea inputArea;
    private JTextArea outputArea;
    private JButton proofButton;
    private JTextField searchSteps;
    private JToggleButton rewriteLeft;
    private JButton proofLemmaButton;
    private JTextField maxTermDepth;
    private JTextField combineTerms;
    private JTextField maxLemmas;
    private JLabel maxTermDepthLabel;
    private JLabel searchStepsLabel;
    private JLabel combineTermsLabel;
    private JLabel maxLemmasLabel;
    private JLabel progressLabel;

    ProofPanel() {
        this.setLayout(new BorderLayout());

        inputArea = new JTextArea();
        JScrollPane sp = new JScrollPane(inputArea);
        sp.setPreferredSize(new Dimension(300, 400));
        this.add(sp, BorderLayout.LINE_START);

        outputArea = new JTextArea();
        JScrollPane sp2 = new JScrollPane(outputArea);
        sp2.setPreferredSize(new Dimension(300, 400));
        this.add(sp2, BorderLayout.LINE_END);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(2, 1));

        JPanel proofControlPanel = new JPanel();
        proofControlPanel.setLayout(new FlowLayout());

        searchStepsLabel = new JLabel("Search steps: ");
        proofControlPanel.add(searchStepsLabel);

        searchSteps = new JTextField("8");
        searchSteps.setPreferredSize(new Dimension(50, 20));
        proofControlPanel.add(searchSteps);

        rewriteLeft = new JCheckBox("Rewrite both directions");
        proofControlPanel.add(rewriteLeft);

        proofButton = new JButton("Prove");
        proofButton.addActionListener(actionEvent -> {

            String[] split = inputArea.getText().split("\\n");
            java.util.List<String> input = new ArrayList<>(Arrays.asList(split));

            EquationSystem system;
            try {
                system = InputParser.parseSystem(input);
            } catch(ParserException e) {
                Logger.e("Exception while parsing, quitting program");
                return;
            }
            system.print();

            outputArea.setText("");
            TextAreaOutputWriter writer = new TextAreaOutputWriter(outputArea);
            int searchDepth = Integer.parseInt(searchSteps.getText());
            if (searchDepth < 0) {
                Logger.e("Search depth < 0, using 0 instead");
                searchDepth = 0;
            }

            progressLabel.setVisible(true);
            proofButton.setEnabled(false);
            proofLemmaButton.setEnabled(false);
            int finalSearchDepth = searchDepth;
            new Thread(() -> {
                try {
                    Prover.induction(system, writer, finalSearchDepth, rewriteLeft.isSelected(),0, null);
                    progressLabel.setVisible(false);
                    proofButton.setEnabled(true);
                    proofLemmaButton.setEnabled(true);
                } catch (IOException e) {
                    Logger.e("IOException: " + e.getMessage());
                }
                writer.close();
            }).start();
        });

        ImageIcon loading = new ImageIcon("res/loading.gif");
        progressLabel = new JLabel("Processing... ", loading, JLabel.CENTER);
        progressLabel.setVisible(false);
        proofControlPanel.add(progressLabel);

        proofControlPanel.add(proofButton);
        controlPanel.add(proofControlPanel);

        JPanel lemmaControlPanel = new JPanel();
        lemmaControlPanel.setLayout(new FlowLayout());
        maxTermDepthLabel = new JLabel("Max term depth: ");
        lemmaControlPanel.add(maxTermDepthLabel);

        maxTermDepth = new JTextField("5");
        maxTermDepth.setPreferredSize(new Dimension(50, 20));
        lemmaControlPanel.add(maxTermDepth);

        combineTermsLabel = new JLabel("Combine terms: ");
        lemmaControlPanel.add(combineTermsLabel);

        combineTerms = new JTextField("2");
        combineTerms.setPreferredSize(new Dimension(50, 20));
        lemmaControlPanel.add(combineTerms);

        maxLemmasLabel = new JLabel("Max lemmas: ");
        lemmaControlPanel.add(maxLemmasLabel);

        maxLemmas = new JTextField("100");
        maxLemmas.setPreferredSize(new Dimension(50, 20));
        lemmaControlPanel.add(maxLemmas);

        proofLemmaButton = new JButton("Search for lemmas and prove");
        proofLemmaButton.addActionListener(actionEvent -> {
            String[] split = inputArea.getText().split("\\n");
            java.util.List<String> input = new ArrayList<>(Arrays.asList(split));

            EquationSystem system;
            try {
                system = InputParser.parseSystem(input);
            } catch(ParserException e) {
                Logger.e("Exception while parsing, quitting program");
                return;
            }
            system.print();

            outputArea.setText("");
            TextAreaOutputWriter writer = new TextAreaOutputWriter(outputArea);
            int searchDepth = Integer.parseInt(searchSteps.getText());
            if (searchDepth < 0) {
                Logger.e("Search depth < 0, using 0 instead");
                searchDepth = 0;
            }

            int maxTerm = Integer.parseInt(maxTermDepth.getText());
            if (maxTerm < 0) {
                Logger.e("Max term depth < 0, using 0 instead");
                maxTerm = 0;
            }

            int combine = Integer.parseInt(combineTerms.getText());
            if (combine < 0) {
                Logger.e("Combine terms < 0, using 0 instead");
                combine = 0;
            }

            int maxLemma = Integer.parseInt(maxLemmas.getText());
            if (maxLemma < 0) {
                Logger.e("Max lemmas < 0, using 0 instead");
                maxLemma = 0;
            }

            int finalSearchDepth = searchDepth;
            int finalMaxTerm = maxTerm;
            int finalCombine = combine;
            int finalMaxLemma = maxLemma;

            progressLabel.setVisible(true);
            proofButton.setEnabled(false);
            proofLemmaButton.setEnabled(false);
            new Thread(() -> {
                try {
                    Prover.inductionLemmaSearch(system, writer, finalSearchDepth, rewriteLeft.isSelected(), finalMaxTerm, finalCombine, finalMaxLemma);
                    progressLabel.setVisible(false);
                    proofButton.setEnabled(true);
                    proofLemmaButton.setEnabled(true);
                } catch (IOException e) {
                    Logger.e("IOException: " + e.getMessage());
                }
                writer.close();
            }).start();

        });
        lemmaControlPanel.add(proofLemmaButton);
        controlPanel.add(lemmaControlPanel);

        add(controlPanel, BorderLayout.PAGE_END);
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