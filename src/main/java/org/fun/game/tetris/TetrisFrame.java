package org.fun.game.tetris;

import javax.swing.JPanel;

/**
 * The Tetris frame.
 * 
 * 
 * <p>
 * This class is based on <em>Jan Bodnar</em>'s
 * <a href="http://zetcode.com/tutorials/javagamestutorial/tetris/">Tetris game clone in Java
 * Swing</a>.
 * </p>
 *
 * @author Mathieu Brunot
 */
public class TetrisFrame extends javax.swing.JFrame {

  /**
   * Generated Serial Version ID.
   */
  private static final long serialVersionUID = 6583684518756647127L;

  /**
   * Creates new form TetrisFrame.
   */
  public TetrisFrame() {
    initComponents();
    this.boardPanel.setStatusbar(statusBarLabel);
    this.boardPanel.setHoldPanel(holdPanel);
    this.boardPanel.setPreviewPanels(
        new JPanel[] {nextPanel1, nextPanel2, nextPanel3, nextPanel4, nextPanel5, nextPanel6});
    initCommandsPanel();
  }

  /**
   * Creates new form TetrisFrame.
   * 
   * @param title the title for the frame
   */
  public TetrisFrame(final String title) {
    super(title);
    initComponents();
    this.boardPanel.setStatusbar(statusBarLabel);
    this.boardPanel.setHoldPanel(holdPanel);
    this.boardPanel.setPreviewPanels(
        new JPanel[] {nextPanel1, nextPanel2, nextPanel3, nextPanel4, nextPanel5, nextPanel6});
    initCommandsPanel();
  }

  /**
   * Initialize command panels with Tetrion key strokes.
   */
  private void initCommandsPanel() {
    this.commandTextArea.setText("TETRIS\n\nCommands:\n\n" + "Hard: " + boardPanel.getHardDropKey()
        + "\n" + "Soft: " + boardPanel.getSoftDropKey() + "\n" + "Left: " + boardPanel.getLeftKey()
        + "\n" + "Right: " + boardPanel.getRightKey() + "\n" + "Rotate: "
        + boardPanel.getRotateLeftKey() + ", " + boardPanel.getRotateRightKey() + "\n" + "Hold: "
        + boardPanel.getHoldKey() + "\n" + "Pause: " + boardPanel.getPauseKey());
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT
   * modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated
  // Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    statusBarLabel = new javax.swing.JLabel();
    boardPanel = new org.fun.game.tetris.Tetrion();
    infoPanel = new javax.swing.JPanel();
    nextLabel = new javax.swing.JLabel();
    nextPanel1 = new javax.swing.JPanel();
    nextPanel2 = new javax.swing.JPanel();
    nextPanel3 = new javax.swing.JPanel();
    nextPanel4 = new javax.swing.JPanel();
    nextPanel5 = new javax.swing.JPanel();
    nextPanel6 = new javax.swing.JPanel();
    scorePanel = new javax.swing.JPanel();
    holdLabel = new javax.swing.JLabel();
    holdPanel = new javax.swing.JPanel();
    commandScrollPane = new javax.swing.JScrollPane();
    commandTextArea = new javax.swing.JTextArea();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setResizable(false);

    statusBarLabel.setText("Status bar");
    getContentPane().add(statusBarLabel, java.awt.BorderLayout.PAGE_END);

    boardPanel.setPreferredSize(new java.awt.Dimension(250, 500));

    javax.swing.GroupLayout boardPanelLayout = new javax.swing.GroupLayout(boardPanel);
    boardPanel.setLayout(boardPanelLayout);
    boardPanelLayout.setHorizontalGroup(
        boardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0,
            290, Short.MAX_VALUE));
    boardPanelLayout.setVerticalGroup(
        boardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0,
            520, Short.MAX_VALUE));

    getContentPane().add(boardPanel, java.awt.BorderLayout.CENTER);

    infoPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    nextLabel.setText("Next");

    nextPanel1.setBackground(new java.awt.Color(40, 40, 40));
    nextPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    nextPanel1.setPreferredSize(new java.awt.Dimension(80, 80));

    javax.swing.GroupLayout nextPanel1Layout = new javax.swing.GroupLayout(nextPanel1);
    nextPanel1.setLayout(nextPanel1Layout);
    nextPanel1Layout.setHorizontalGroup(
        nextPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0,
            76, Short.MAX_VALUE));
    nextPanel1Layout.setVerticalGroup(
        nextPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0,
            76, Short.MAX_VALUE));

    nextPanel2.setBackground(new java.awt.Color(40, 40, 40));
    nextPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    nextPanel2.setPreferredSize(new java.awt.Dimension(80, 80));

    javax.swing.GroupLayout nextPanel2Layout = new javax.swing.GroupLayout(nextPanel2);
    nextPanel2.setLayout(nextPanel2Layout);
    nextPanel2Layout.setHorizontalGroup(
        nextPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0,
            76, Short.MAX_VALUE));
    nextPanel2Layout.setVerticalGroup(
        nextPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0,
            76, Short.MAX_VALUE));

    nextPanel3.setBackground(new java.awt.Color(40, 40, 40));
    nextPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    nextPanel3.setPreferredSize(new java.awt.Dimension(80, 80));

    javax.swing.GroupLayout nextPanel3Layout = new javax.swing.GroupLayout(nextPanel3);
    nextPanel3.setLayout(nextPanel3Layout);
    nextPanel3Layout.setHorizontalGroup(
        nextPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0,
            76, Short.MAX_VALUE));
    nextPanel3Layout.setVerticalGroup(
        nextPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0,
            76, Short.MAX_VALUE));

    nextPanel4.setBackground(new java.awt.Color(40, 40, 40));
    nextPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    nextPanel4.setPreferredSize(new java.awt.Dimension(80, 80));

    javax.swing.GroupLayout nextPanel4Layout = new javax.swing.GroupLayout(nextPanel4);
    nextPanel4.setLayout(nextPanel4Layout);
    nextPanel4Layout.setHorizontalGroup(
        nextPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0,
            76, Short.MAX_VALUE));
    nextPanel4Layout.setVerticalGroup(
        nextPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0,
            76, Short.MAX_VALUE));

    nextPanel5.setBackground(new java.awt.Color(40, 40, 40));
    nextPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    nextPanel5.setPreferredSize(new java.awt.Dimension(80, 80));

    javax.swing.GroupLayout nextPanel5Layout = new javax.swing.GroupLayout(nextPanel5);
    nextPanel5.setLayout(nextPanel5Layout);
    nextPanel5Layout.setHorizontalGroup(
        nextPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0,
            76, Short.MAX_VALUE));
    nextPanel5Layout.setVerticalGroup(
        nextPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0,
            76, Short.MAX_VALUE));

    nextPanel6.setBackground(new java.awt.Color(40, 40, 40));
    nextPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    nextPanel6.setPreferredSize(new java.awt.Dimension(80, 80));

    javax.swing.GroupLayout nextPanel6Layout = new javax.swing.GroupLayout(nextPanel6);
    nextPanel6.setLayout(nextPanel6Layout);
    nextPanel6Layout.setHorizontalGroup(
        nextPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0,
            76, Short.MAX_VALUE));
    nextPanel6Layout.setVerticalGroup(
        nextPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0,
            76, Short.MAX_VALUE));

    javax.swing.GroupLayout infoPanelLayout = new javax.swing.GroupLayout(infoPanel);
    infoPanel.setLayout(infoPanelLayout);
    infoPanelLayout.setHorizontalGroup(infoPanelLayout
        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(infoPanelLayout.createSequentialGroup().addContainerGap().addComponent(nextLabel)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, infoPanelLayout
            .createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
            .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(nextPanel1, javax.swing.GroupLayout.Alignment.TRAILING,
                    javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(nextPanel2, javax.swing.GroupLayout.Alignment.TRAILING,
                    javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(nextPanel3, javax.swing.GroupLayout.Alignment.TRAILING,
                    javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(nextPanel4, javax.swing.GroupLayout.Alignment.TRAILING,
                    javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(nextPanel5, javax.swing.GroupLayout.Alignment.TRAILING,
                    javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(nextPanel6, javax.swing.GroupLayout.Alignment.TRAILING,
                    javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE))));
    infoPanelLayout.setVerticalGroup(infoPanelLayout
        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(infoPanelLayout.createSequentialGroup().addComponent(nextLabel).addGap(5, 5, 5)
            .addComponent(nextPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(1, 1, 1)
            .addComponent(nextPanel2, javax.swing.GroupLayout.PREFERRED_SIZE,
                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(1, 1, 1)
            .addComponent(nextPanel3, javax.swing.GroupLayout.PREFERRED_SIZE,
                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(1, 1, 1)
            .addComponent(nextPanel4, javax.swing.GroupLayout.PREFERRED_SIZE,
                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(1, 1, 1)
            .addComponent(nextPanel5, javax.swing.GroupLayout.PREFERRED_SIZE,
                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(1, 1, 1)
            .addComponent(nextPanel6, javax.swing.GroupLayout.PREFERRED_SIZE,
                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(8, Short.MAX_VALUE)));

    getContentPane().add(infoPanel, java.awt.BorderLayout.LINE_END);

    scorePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    holdLabel.setText("Hold");

    holdPanel.setBackground(new java.awt.Color(40, 40, 40));
    holdPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    holdPanel.setPreferredSize(new java.awt.Dimension(80, 80));

    javax.swing.GroupLayout holdPanelLayout = new javax.swing.GroupLayout(holdPanel);
    holdPanel.setLayout(holdPanelLayout);
    holdPanelLayout.setHorizontalGroup(
        holdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 76,
            Short.MAX_VALUE));
    holdPanelLayout.setVerticalGroup(
        holdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 76,
            Short.MAX_VALUE));

    commandScrollPane.setPreferredSize(new java.awt.Dimension(80, 80));

    commandTextArea.setColumns(8);
    commandTextArea.setFont(new java.awt.Font("Monospaced", 0, 8)); // NOI18N
    commandTextArea.setRows(5);
    commandTextArea.setTabSize(4);
    commandTextArea.setText(
        "TETRIS\n\nCommands:\n\nHard: UP\nSoft: DOWN\nLeft: LEFT\nRight: RIGHT\nRotate: A, Z\nHold: D\nStart: ENTER");
    commandTextArea.setWrapStyleWord(true);
    commandTextArea.setEnabled(false);
    commandScrollPane.setViewportView(commandTextArea);

    javax.swing.GroupLayout scorePanelLayout = new javax.swing.GroupLayout(scorePanel);
    scorePanel.setLayout(scorePanelLayout);
    scorePanelLayout.setHorizontalGroup(scorePanelLayout
        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(scorePanelLayout.createSequentialGroup().addContainerGap().addComponent(holdLabel)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scorePanelLayout
            .createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
            .addGroup(scorePanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(holdPanel, javax.swing.GroupLayout.Alignment.TRAILING,
                    javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(commandScrollPane, javax.swing.GroupLayout.Alignment.TRAILING,
                    javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE))));
    scorePanelLayout.setVerticalGroup(scorePanelLayout
        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(scorePanelLayout.createSequentialGroup().addComponent(holdLabel).addGap(5, 5, 5)
            .addComponent(holdPanel, javax.swing.GroupLayout.PREFERRED_SIZE,
                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(commandScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 360,
                javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(51, Short.MAX_VALUE)));

    getContentPane().add(scorePanel, java.awt.BorderLayout.LINE_START);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private org.fun.game.tetris.Tetrion boardPanel;
  private javax.swing.JScrollPane commandScrollPane;
  private javax.swing.JTextArea commandTextArea;
  private javax.swing.JLabel holdLabel;
  private javax.swing.JPanel holdPanel;
  private javax.swing.JPanel infoPanel;
  private javax.swing.JLabel nextLabel;
  private javax.swing.JPanel nextPanel1;
  private javax.swing.JPanel nextPanel2;
  private javax.swing.JPanel nextPanel3;
  private javax.swing.JPanel nextPanel4;
  private javax.swing.JPanel nextPanel5;
  private javax.swing.JPanel nextPanel6;
  private javax.swing.JPanel scorePanel;
  private javax.swing.JLabel statusBarLabel;
  // End of variables declaration//GEN-END:variables

}
