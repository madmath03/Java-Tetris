
import java.util.logging.Logger;
import org.fun.game.tetris.TetrisFrame;

/**
 * The Tetris Game main entry point.
 *
 * <p>
 * The Tetris game is one of the most popular computer games ever created.
 * </p>
 *
 * <p>
 * The original game was designed and programmed by a Russian programmer Alexey
 * Pajitnov in 1985. Since then, Tetris is available on almost every computer
 * platform in lots of variations.
 * </p>
 *
 * <p>
 * Tetris is called a falling block puzzle game. In this game, we have seven
 * different shapes called tetrominoes. The shapes are falling down the board.
 * </p>
 * <p>
 * The object of the Tetris game is to move and rotate the shapes, so that they
 * fit as much as possible. If we manage to form a row, the row is destroyed and
 * we score. We play the tetris game until we top out.
 * </p>
 *
 * @author Jan Bodnar
 * @author Mathieu Brunot
 *
 * @see <a href="http://zetcode.com/tutorials/javagamestutorial/tetris/">Tetris
 *      game clone in Java Swing</a>
 * @see <a href="http://tetris.wikia.com/wiki/">Tetris Wiki</a>
 */
public final class TetrisMain {

    /**
     * Logger.
     */
    private static final Logger LOGGER = 
    		Logger.getLogger(TetrisMain.class.getName());
    
    /**
     * Hidden constructor.
     */
    private TetrisMain() {
    }

    /**
     * Set Java Swing Look and Feel.
     * 
     * @param lookAndFeelName
     *            Look and feel name
     */
    private static void setLookAndFeel(final String lookAndFeelName) {
	if (lookAndFeelName == null) {
	    return;
	}

	/* Set look and feel */
	// <editor-fold defaultstate="collapsed" desc=" Look and feel setting
	// code (optional) ">
	/*
	 * If Nimbus (introduced in Java SE 6) is not available, stay with the
	 * default look and feel. For details see
	 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.
	 * html
	 */
	try {
		javax.swing.UIManager.LookAndFeelInfo[] lookAndFeels = 
				javax.swing.UIManager.getInstalledLookAndFeels();
	    for (javax.swing.UIManager.LookAndFeelInfo info : lookAndFeels) {
		if (lookAndFeelName.equals(info.getName())) {
		    javax.swing.UIManager.setLookAndFeel(info.getClassName());
		    break;
		}
	    }
	} catch (ClassNotFoundException | InstantiationException 
			| IllegalAccessException
			| javax.swing.UnsupportedLookAndFeelException ex) {
	    LOGGER.log(java.util.logging.Level.SEVERE, null, ex);
	}
	// </editor-fold>
    }

    /**
     * Main entry point.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(final String[] args) {
	System.out.println("TETRIS");

	/* Set the Nimbus look and feel */
	setLookAndFeel("Nimbus");

	/* Create and display the form */
	java.awt.EventQueue.invokeLater(() -> {
	    System.out.println("Initialization in progress...");

	    final TetrisFrame gameFrame = new TetrisFrame("Tetris");

	    gameFrame.setLocationRelativeTo(null);

	    // Make frame visible
	    gameFrame.setVisible(true);
	});
    }

}
