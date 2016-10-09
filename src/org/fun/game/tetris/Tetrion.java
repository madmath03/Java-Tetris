package org.fun.game.tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Closeable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import org.fun.music.midi_player.MidiPlayer;

/**
 * The Tetrion is the frame that surrounds the playfield and the "machine" that
 * plays Tetris.
 * 
 * <p>
 * This class is based on <em>Jan Bodnar</em>'s
 * <a href="http://zetcode.com/tutorials/javagamestutorial/tetris/">Tetris game
 * clone in Java Swing</a>.
 * </p>
 *
 * @author Jan Bodnar
 * @author Mathieu Brunot
 *
 * @see <a href="http://tetris.wikia.com/wiki/Tetrion">Tetrion</a>
 * @see <a href="http://tetris.wikia.com/wiki/Playfield">Playfield</a>
 * @see <a href="http://tetris.wikia.com/wiki/Tetris_Guideline">Tetris
 *      Guideline</a>
 * @see <a href="http://zetcode.com/tutorials/javagamestutorial/tetris/">Tetris
 *      game clone in Java Swing</a>
 */
public class Tetrion extends JPanel implements ActionListener, Closeable {

	/**
	 * Generated Serial Version ID.
	 */
	private static final long serialVersionUID = 3677056500763739209L;

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = 
			Logger.getLogger(Tetrion.class.getName());

	/**
	 * Fixed frame rate per second.
	 */
	private static final int FRAME_PER_SECOND = 60;
	/**
	 * Time (ms) between each frame.
	 */
	private static final int TIME_BETWEEN_FRAMES = 1000 / FRAME_PER_SECOND;

	/**
	 * Number of lines needed to upgrade level.
	 */
	private static final int LEVEL_RATIO = 10;
	/**
	 * Ratio applied to "gravity" on manual soft drop.
	 */
	private static final double SOFT_DROP_RATIO = 20;

	/**
	 * Tetrion width.
	 */
	private static final int BOARD_WIDTH = 10;
	/**
	 * Tetrion height.
	 *
	 * <p>
	 * The space between the ceiling and the board height is invisible yet
	 * usable.
	 * </p>
	 *
	 * @see #BOARD_CEILING
	 */
	private static final int BOARD_HEIGHT = 22;
	/**
	 * Tetrion ceiling height.
	 *
	 * <p>
	 * Rows above the ceiling are hidden or obstructed by the field frame.
	 * </p>
	 */
	private static final int BOARD_CEILING = 20;
	/**
	 * Tetrion danger zone.
	 *
	 * <p>
	 * Rows above the danger zone can trigger some alerts to the player.
	 * </p>
	 */
	private static final int BOARD_CEILING_DANGER_ZONE = BOARD_CEILING - 4;
	/**
	 * Speed ratio of music playing inside danger zone.
	 */
	private static final float DANGER_ZONE_MUSIC_RATIO = 1.4F;

	/**
	 * The shapes' Queue size.
	 */
	private static final int SHAPES_QUEUE_SIZE = 
			Tetromino.Shape.values().length;

	/**
	 * Delayed Auto Shift or autorepeat refers to the behavior of most falling
	 * block puzzle games when the player holds the left or right key.
	 *
	 * <p>
	 * The game will shift the falling piece sideways, wait, and then shift it
	 * repeatedly if the player continues to hold the key.
	 * </p>
	 *
	 * @see <a href="http://tetris.wikia.com/wiki/DAS">Delayed Auto Shift</a>
	 */
	private static final long AUTO_SHIFT_DELAY = 11;
	/**
	 * Delayed Auto Shift period.
	 *
	 * @see #AUTO_SHIFT_DELAY
	 *
	 * @see <a href="http://tetris.wikia.com/wiki/DAS">Delayed Auto Shift</a>
	 */
	private static final long AUTO_SHIFT_PERIOD = 6;

	/**
	 * Super Rotation System default lock delay.
	 *
	 * <p>
	 * Lock delay refers to how many frames a {@link Tetromino} waits while on
	 * the ground before locking.
	 * </p>
	 *
	 * @see <a href="http://tetris.wikia.com/wiki/Lock_delay">Lock delay</a>
	 * @see <a href="http://tetris.wikia.com/wiki/SRS">Super Rotation System</a>
	 */
	private static final long SRS_LOCK_DELAY = 30;
	/**
	 * Ratio applied to "gravity" on each level-up in modern mode.
	 */
	private static final double SRS_SPEED_UP_RATIO = 1.45;
	/**
	 * Classic Tetris lock delay.
	 *
	 * <p>
	 * Lock delay refers to how many frames a {@link Tetromino} waits while on
	 * the ground before locking.
	 * </p>
	 *
	 * @see <a href="http://tetris.wikia.com/wiki/Lock_delay">Lock delay</a>
	 * @see <a href="http://tetris.wikia.com/wiki/SRS">Super Rotation System</a>
	 */
	private static final long CLASSIC_LOCK_DELAY = 0;
	/**
	 * Ratio applied to "gravity" on each level-up in classic mode.
	 */
	private static final double CLASSIC_SPEED_UP_RATIO = 1.1625;
	/**
	 * Scores by number of lines cleared.
	 */
	private static final int[] SCORES = { 0, 40, 100, 300, 1200};
	/**
	 * A "<em>Tetris</em>" is when the player clears 4 lines at once.
	 */
	private static final int TETRIS = Tetromino.BLOCKS;

	/**
	 * Has the game started?
	 */
	private boolean started = false;
	/**
	 * Is the game paused?
	 */
	private boolean paused = false;

	// Refresh Scheduler
	/**
	 * The refresh thread.
	 * 
	 * @see #actionPerformed(ActionEvent)
	 */
	private Timer timer;
	/**
	 * Number of frames since the last drop occurred.
	 * 
	 * @see #actionPerformed(ActionEvent)
	 */
	private int framesSinceLastDrop = 0;
	/**
	 * Number of frames the user kept asking for soft drops.
	 * 
	 * @see #actionPerformed(ActionEvent)
	 */
	private int framesContinouslySoftDropped = -1;

	// Score, level and "speed"
	/**
	 * Number of lines cleared.
	 */
	private int lines = 0;
	/**
	 * Score.
	 */
	private long score = 0;
	/**
	 * Level.
	 */
	private int level = 0;
	/**
	 * Current Gravity force.
	 * 
	 * @see <a href="http://tetris.wikia.com/wiki/Gravity">Gravity</a>
	 * @see <a href="http://tetris.wikia.com/wiki/Drop#Gravity">Drop Gravity</a>
	 */
	private double gravity = 1 / (double) FRAME_PER_SECOND;

	// Pieces and position
	/**
	 * Falling {@link Tetromino} current {@code X} position.
	 * 
	 * @see #fallingPiece
	 */
	private int curX = 0;
	/**
	 * Falling {@link Tetromino} current {@code Y} position.
	 * 
	 * @see #fallingPiece
	 */
	private int curY = 0;
	/**
	 * Falling {@link Tetromino}.
	 */
	private Tetromino fallingPiece;
	/**
	 * The held piece.
	 * 
	 * @see #holdPiece()
	 * @see <a href="http://tetris.wikia.com/wiki/Hold_piece">Hold piece</a>
	 */
	private Tetromino holdPiece;
	/**
	 * Is hold piece available yet?
	 * 
	 * @see #holdPiece
	 * @see #holdPiece()
	 */
	private boolean holdPieceAvailable = false;
	/**
	 * Is the stack inside the danger zone?
	 * 
	 * @see #BOARD_CEILING
	 * @see #BOARD_CEILING_DANGER_ZONE
	 * @see #pieceDropped()
	 */
	private boolean insideDangerZone = false;
	/**
	 * This is where the action is.
	 *
	 * <p>
	 * The playfield is the grid into which {@link Tetromino}es fall, also
	 * called the "well" (common in older games) or the "matrix" (especially in
	 * more recent Tetris brand games). The playfield is surrounded by a frame
	 * called the {@link Tetrion}, which controls the overall behavior of
	 * {@link Tetromino}es.
	 * </p>
	 */
	private final Tetromino.Shape[] playfield;

	/**
	 * Shape generator thread.
	 * 
	 * <p>
	 * Shapes are generated in a separate thread to prevent delay in the
	 * gameplay.
	 * </p>
	 * 
	 * @see #nextShapes
	 * @see #newPiece()
	 */
	private final Thread shapeGenerator;
	/**
	 * Next shapes queue.
	 * 
	 * <p>
	 * The shapes are filled by the {@link #shapeGenerator} thread until the
	 * queue is full. The {@code BlockingQueue} will also make the thread asking
	 * to take a piece wait until the queue is not empty (should never
	 * happen...).
	 * </p>
	 */
	private final BlockingQueue<Tetromino.Shape> nextShapes;

	// Drop variables
	/**
	 * Lock delay count.
	 *
	 * <p>
	 * Counts the number of frames between drop and lock of the falling piece.
	 * </p>
	 *
	 * @see #fallingPiece
	 * @see #SRS_LOCK_DELAY
	 */
	private int lockDelayFrameCount = 0;
	/**
	 * Is hard drop in progress?
	 * 
	 * @see DropHardAction
	 * @see ReleaseDropHardAction
	 * @see #hardDrop()
	 * @see #isDroppingHard()
	 * @see #setDroppingHard(boolean)
	 * @see <a href="http://tetris.wikia.com/wiki/Drop">Drop</a>
	 */
	private boolean droppingHard;
	/**
	 * Is soft drop in progress?
	 * 
	 * @see DropSoftAction
	 * @see ReleaseDropSoftAction
	 * @see #softDrop()
	 * @see #isDroppingSoft()
	 * @see #setDroppingSoft(boolean)
	 * @see <a href="http://tetris.wikia.com/wiki/Drop">Drop</a>
	 */
	private boolean droppingSoft;

	// Shift variables
	/**
	 * Shift delay count.
	 *
	 * <p>
	 * Positive values counts frames between first shift and start of auto
	 * shift, negative value counts frames between each auto-shifts.
	 * </p>
	 *
	 * @see #AUTO_SHIFT_DELAY
	 * @see #AUTO_SHIFT_PERIOD
	 */
	private int shiftDelayCount = 0;
	/**
	 * Is left shift in progress?
	 * 
	 * @see ShiftLeftAction
	 * @see ReleaseShiftLeftAction
	 * @see #isShiftingLeft()
	 * @see #setShiftingLeft(boolean)
	 */
	private boolean shiftingLeft;
	/**
	 * Is right shift in progress?
	 * 
	 * @see ShiftRightAction
	 * @see ReleaseShiftRightAction
	 * @see #isShiftingRight()
	 * @see #setShiftingRight(boolean)
	 */
	private boolean shiftingRight;

	// Rotation variables
	/**
	 * Count the rotations needed.
	 *
	 * <p>
	 * Negative value stands for left rotations, positive values stands for
	 * right rotations.
	 * </p>
	 */
	private int rotationsNeeded = 0;
	/**
	 * Is left rotation in progress?
	 * 
	 * @see RotateLeftAction
	 * @see ReleaseRotateLeftAction
	 * @see #isRotatingLeft()
	 * @see #setRotatingLeft(boolean)
	 * @see <a href="http://tetris.wikia.com/wiki/Rotate">Rotate</a>
	 */
	private boolean rotatingLeft;
	/**
	 * Is right rotation in progress?
	 * 
	 * @see RotateRightAction
	 * @see ReleaseRotateRightAction
	 * @see #isRotatingRight()
	 * @see #setRotatingRight(boolean)
	 * @see <a href="http://tetris.wikia.com/wiki/Rotate">Rotate</a>
	 */
	private boolean rotatingRight;

	// Behavior variables
	/**
	 * Class to store <em>standard</em> configurations for the Tetris game.
	 * 
	 * @author Mathieu Brunot
	 */
	public enum Mode {
		/**
		 * Classic Tetris rules.
		 */
		CLASSIC(CLASSIC_SPEED_UP_RATIO, false, false, 
				CLASSIC_LOCK_DELAY, false),
		/**
		 * Modern Tetris rule.
		 */
		MODERN(SRS_SPEED_UP_RATIO, true, true, SRS_LOCK_DELAY, true),
		/**
		 * A mix between {@link Mode#CLASSIC} rules and {@link Mode#MODERN}
		 * rules.
		 */
		MIX(CLASSIC_SPEED_UP_RATIO, true, false, SRS_LOCK_DELAY, false);

		/**
		 * Ratio applied to {@link Tetrion#gravity} on each
		 * {@link Tetrion#level} up.
		 * 
		 * @see Tetrion#levelUpSpeedUpRatio
		 */
		private final double levelUpSpeedUpRatio;
		/**
		 * Are wall kicks enabled?
		 *
		 * <p>
		 * A wall kick happens when a player rotates a piece when no space
		 * exists in the squares where that {@link Tetromino} would normally
		 * occupy after the rotation. To compensate, the game sets a certain
		 * number of alternative spaces for the {@link Tetromino} to look.
		 * </p>
		 * 
		 * @see Tetrion#wallKickEnabled
		 *
		 * @see <a href="http://tetris.wikia.com/wiki/Wall_kick">Wall kick</a>
		 */
		private final boolean wallKickEnabled;
		/**
		 * Are floor kicks enabled?
		 *
		 * <p>
		 * A floor kick, like a wall kick, happens when a player rotates a piece
		 * when no space exists in the squares where that {@link Tetromino}
		 * would normally occupy after the rotation when rotating against the
		 * floor opposed to a wall. To compensate, the game sets a certain
		 * number of alternative spaces for the {@link Tetromino} to look.
		 * </p>
		 * 
		 * @see Tetrion#floorKickEnabled
		 *
		 * @see #wallKickEnabled
		 * @see <a href="http://tetris.wikia.com/wiki/Floor_kick">Floor kick</a>
		 */
		private final boolean floorKickEnabled;
		/**
		 * Lock delay refers to how many frames a {@link Tetromino} waits while
		 * on the ground before locking.
		 *
		 * <p>
		 * Classic games lock {@link Tetromino}es immediately once it has fallen
		 * to the ground, while some newer games give the {@link Tetromino} some
		 * time before locking.
		 * </p>
		 * 
		 * @see Tetrion#lockDelay
		 *
		 * @see #SRS_LOCK_DELAY
		 * @see #CLASSIC_LOCK_DELAY
		 * @see <a href="http://tetris.wikia.com/wiki/Lock_delay">Lock delay</a>
		 * @see <a href="http://tetris.wikia.com/wiki/Infinity">Infinity</a>
		 */
		private long lockDelay;
		/**
		 * Is infinity mode enabled?
		 *
		 * <p>
		 * Infinity is The Tetris Company's term to refer to a characteristic in
		 * recent Tetris Guideline-compliant games where the lock delay of a
		 * {@link Tetromino} is reset whenever it is moved or rotated (even O,
		 * which ordinarily does not rotate).
		 * </p>
		 * 
		 * @see Tetrion#infiniteLockDelayEnabled
		 *
		 * @see #SRS_LOCK_DELAY
		 * @see <a href="http://tetris.wikia.com/wiki/Lock_delay">Lock delay</a>
		 * @see <a href="http://tetris.wikia.com/wiki/Infinity">Infinity</a>
		 */
		private boolean infiniteLockDelayEnabled;

		/**
		 * Tetris game configuration constructor.
		 * 
		 * @param speedUpRatio
		 *            the level up speed up ratio
		 * @param isWallKickEnabled
		 *            Are wall kicks enabled?
		 * @param isFloorKickEnabled
		 *            Are floor kicks enabled?
		 * @param delay
		 *            the lock delay
		 * @param isInfiniteLockDelayEnabled
		 *            Is infinity mode enabled?
		 */
		Mode(final double speedUpRatio, 
				final boolean isWallKickEnabled, 
				final boolean isFloorKickEnabled, 
				final long delay,
				final boolean isInfiniteLockDelayEnabled) {
			this.levelUpSpeedUpRatio = speedUpRatio;
			this.wallKickEnabled = isWallKickEnabled;
			this.floorKickEnabled = isFloorKickEnabled;
			this.lockDelay = delay;
			this.infiniteLockDelayEnabled = isInfiniteLockDelayEnabled;
		}

		/**
		 * Ratio applied to {@link Tetrion#gravity} on each
		 * {@link Tetrion#level} up.
		 * 
		 * @return the level up speed up ration
		 */
		public double getLevelUpSpeedUpRatio() {
			return levelUpSpeedUpRatio;
		}

		/**
		 * Are wall kicks enabled?
		 * 
		 * @return {@code true} if wall kicks enabled
		 */
		public boolean isWallKickEnabled() {
			return wallKickEnabled;
		}

		/**
		 * Are floor kicks enabled?
		 * 
		 * @return {@code true} if floor kicks enabled
		 */
		public boolean isFloorKickEnabled() {
			return floorKickEnabled;
		}

		/**
		 * Lock delay refers to how many frames a {@link Tetromino} waits while
		 * on the ground before locking.
		 * 
		 * @return the lock delay
		 */
		public long getLockDelay() {
			return lockDelay;
		}

		/**
		 * Is infinity mode enabled?
		 * 
		 * @return {@code true} if infinity mode enabled
		 */
		public boolean isInfiniteLockDelayEnabled() {
			return infiniteLockDelayEnabled;
		}
	}

	/**
	 * Ratio applied to {@link #gravity} on each {@link #level} up.
	 */
	private double levelUpSpeedUpRatio = SRS_SPEED_UP_RATIO;
	/**
	 * Are wall kicks enabled?
	 *
	 * <p>
	 * A wall kick happens when a player rotates a piece when no space exists in
	 * the squares where that {@link Tetromino} would normally occupy after the
	 * rotation. To compensate, the game sets a certain number of alternative
	 * spaces for the {@link Tetromino} to look.
	 * </p>
	 *
	 * @see <a href="http://tetris.wikia.com/wiki/Wall_kick">Wall kick</a>
	 */
	private boolean wallKickEnabled = true;
	/**
	 * Are floor kicks enabled?
	 *
	 * <p>
	 * A floor kick, like a wall kick, happens when a player rotates a piece
	 * when no space exists in the squares where that {@link Tetromino} would
	 * normally occupy after the rotation when rotating against the floor
	 * opposed to a wall. To compensate, the game sets a certain number of
	 * alternative spaces for the {@link Tetromino} to look.
	 * </p>
	 *
	 * @see #wallKickEnabled
	 * @see <a href="http://tetris.wikia.com/wiki/Floor_kick">Floor kick</a>
	 */
	private boolean floorKickEnabled = true;
	/**
	 * Lock delay refers to how many frames a {@link Tetromino} waits while on
	 * the ground before locking.
	 *
	 * <p>
	 * Classic games lock {@link Tetromino}es immediately once it has fallen to
	 * the ground, while some newer games give the {@link Tetromino} some time
	 * before locking.
	 * </p>
	 *
	 * @see #SRS_LOCK_DELAY
	 * @see #CLASSIC_LOCK_DELAY
	 * @see <a href="http://tetris.wikia.com/wiki/Lock_delay">Lock delay</a>
	 * @see <a href="http://tetris.wikia.com/wiki/Infinity">Infinity</a>
	 */
	private long lockDelay = SRS_LOCK_DELAY;
	/**
	 * Is infinity mode enabled?
	 *
	 * <p>
	 * Infinity is The Tetris Company's term to refer to a characteristic in
	 * recent Tetris Guideline-compliant games where the lock delay of a
	 * {@link Tetromino} is reset whenever it is moved or rotated (even O, which
	 * ordinarily does not rotate).
	 * </p>
	 *
	 * @see #SRS_LOCK_DELAY
	 * @see <a href="http://tetris.wikia.com/wiki/Lock_delay">Lock delay</a>
	 * @see <a href="http://tetris.wikia.com/wiki/Infinity">Infinity</a>
	 */
	private boolean infiniteLockDelayEnabled = true;

	// Graphical components
	/**
	 * Is a refresh of the hold space needed?
	 * 
	 * @see #drawHold()
	 */
	private boolean refreshHoldPanelNeeded;
	/**
	 * Hold space display panel.
	 * 
	 * @see #holdPiece
	 */
	private JPanel holdPanel;
	/**
	 * Is a refresh of the preview panels needed?
	 * 
	 * @see #drawNextShapes()
	 */
	private boolean refreshPreviewPanelsNeeded;
	/**
	 * Preview panels.
	 * 
	 * @see #nextShapes
	 */
	private JPanel[] previewPanels;
	/**
	 * Status Bar.
	 */
	private JLabel statusbar;

	// Music components
	/**
	 * MIDI player for background music.
	 */
	private static final MidiPlayer MIDI_PLAYER = MidiPlayer.getInstance();

	static {
		if (MIDI_PLAYER != null) {
			ClassLoader classLoader = MidiPlayer.class.getClassLoader();
			MIDI_PLAYER.add(
					classLoader.getResource("music/Tetris-Title_Screen.mid"));
			MIDI_PLAYER.add(
					classLoader.getResource("music/Tetris-Theme_A.mid"));
			MIDI_PLAYER.add(
					classLoader.getResource("music/Tetris-Theme_B.mid"));
			MIDI_PLAYER.add(
					classLoader.getResource("music/Tetris-Game_Over.mid"));
			MIDI_PLAYER.setLooping(true);
		}
	}
	
	// User interactions
	/**
	 * Key stroke release keyword.
	 * 
	 * @see #initInputs()
	 */
	private static final String RELEASE_INPUT = " released ";
	/**
	 * Key stroke to switch to Theme A song.
	 * 
	 * @see #initInputs()
	 */
	private final String themeA = "ctrl 1";
	/**
	 * Key stroke to switch to Theme B song.
	 * 
	 * @see #initInputs()
	 */
	private final String themeB = "ctrl 2";
	/**
	 * Key stroke to pause game.
	 * 
	 * @see #initInputs()
	 * @see PauseAction
	 */
	private String pause = "ENTER";
	/**
	 * Key stroke to hold a piece.
	 * 
	 * @see #initInputs()
	 * @see HoldAction
	 */
	private String hold = "D";
	/**
	 * Key stroke to shift the falling piece to the left.
	 * 
	 * @see #initInputs()
	 * @see ShiftLeftAction
	 * @see ReleaseShiftLeftAction
	 */
	private String left = "LEFT";
	/**
	 * Key stroke to shift the falling piece to the right.
	 * 
	 * @see #initInputs()
	 * @see ShiftRightAction
	 * @see ReleaseShiftRightAction
	 */
	private String right = "RIGHT";
	/**
	 * Key stroke to soft drop the falling piece.
	 * 
	 * @see #initInputs()
	 * @see DropSoftAction
	 * @see ReleaseDropSoftAction
	 */
	private String softDrop = "DOWN";
	/**
	 * Key stroke to hard drop the falling piece.
	 * 
	 * @see #initInputs()
	 * @see DropHardAction
	 * @see ReleaseDropHardAction
	 */
	private String hardDrop = "UP";
	/**
	 * Key stroke to rotate the falling piece to the left.
	 * 
	 * @see #initInputs()
	 * @see RotateLeftAction
	 * @see ReleaseRotateLeftAction
	 */
	private String rotateLeft = "A";
	/**
	 * Key stroke to rotate the falling piece to the right.
	 * 
	 * @see #initInputs()
	 * @see RotateRightAction
	 * @see ReleaseRotateRightAction
	 */
	private String rotateRight = "Z";

	/**
	 * Default constructor.
	 */
	public Tetrion() {
		this(null, null);
	}

	/**
	 * Constructor of a Tetrion with a status bar, an hold space and preview
	 * panels.
	 * 
	 * @param label
	 *            the status bar
	 * @param holdSpace
	 *            the panel in which to draw the {@link #holdPiece}
	 * @param previews
	 *            the panels in which to draw the {@link #nextShapes}
	 */
	public Tetrion(final JLabel label, final JPanel holdSpace, 
			final JPanel... previews) {
		this.setFocusable(true);
		this.setBackground(Color.BLACK.brighter());
		// this.setBorder(new B);
		this.fallingPiece = new Tetromino();
		this.holdPiece = new Tetromino();
		this.timer = new Timer(TIME_BETWEEN_FRAMES, this);

		this.statusbar = label;
		this.playfield = new Tetromino.Shape[BOARD_WIDTH * BOARD_HEIGHT];

		// Thread to fill the next shapes with Random Generator
		this.nextShapes = new ArrayBlockingQueue<>(
				Math.max(SHAPES_QUEUE_SIZE, previews.length), true);
		this.shapeGenerator = new Thread(() -> {
			try {
				while (true) {
					this.nextShapes.put(Tetromino.Shape.getRandomShape());
					this.refreshPreviewPanelsNeeded = 
							this.previewPanels != null 
									&& this.previewPanels.length > 0;
				}
			} catch (InterruptedException ex) {
				LOGGER.log(Level.SEVERE, null, ex);
			}
		}, "Shape Generator");

		this.holdPanel = holdSpace;
		this.previewPanels = previews;

		this.initInputs();
		this.setMode();

		this.clear();

		if (MIDI_PLAYER != null) {
			MIDI_PLAYER.startPlaying();
		}
	}

	/**
	 * Initialize controller inputs mapping to actions.
	 */
	private void initInputs() {
		InputMap inputMap = this.getInputMap();
		inputMap.clear();
		ActionMap actionMap = this.getActionMap();
		actionMap.clear();
		
		// Change song
		inputMap.put(KeyStroke.getKeyStroke(themeA), "Theme A");
		actionMap.put("Theme A", new AbstractAction("Theme A") {
			/**
			 * Default Serial Version ID.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (MIDI_PLAYER != null && !paused) {
					MIDI_PLAYER.moveToSong(1);
				}
			}
		});
		// Change song
		inputMap.put(KeyStroke.getKeyStroke(themeB), "Theme B");
		actionMap.put("Theme B", new AbstractAction("Theme B") {
			/**
			 * Default Serial Version ID.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (MIDI_PLAYER != null && !paused) {
					MIDI_PLAYER.moveToSong(2);
				}
			}
		});
		
		// Pause
		if (pause != null && !pause.isEmpty()) {
			inputMap.put(KeyStroke.getKeyStroke(pause), "PAUSE");
		}
		actionMap.put("PAUSE", new PauseAction("Pause"));
		
		// Hold
		if (hold != null && !hold.isEmpty()) {
			inputMap.put(KeyStroke.getKeyStroke(hold), "HOLD");
		}
		actionMap.put("HOLD", new HoldAction("Hold"));
		
		// Left
		if (left != null && !left.isEmpty()) {
			inputMap.put(KeyStroke.getKeyStroke(left), "LEFT");
			inputMap.put(KeyStroke.getKeyStroke(RELEASE_INPUT + left), 
					"RELEASE_LEFT");
		}
		actionMap.put("LEFT", new ShiftLeftAction("Left"));
		actionMap.put("RELEASE_LEFT", 
				new ReleaseShiftLeftAction("Release Left"));
		// Right
		if (right != null && !right.isEmpty()) {
			inputMap.put(KeyStroke.getKeyStroke(right), "RIGHT");
			inputMap.put(KeyStroke.getKeyStroke(RELEASE_INPUT + right), 
					"RELEASE_RIGHT");
		}
		actionMap.put("RIGHT", new ShiftRightAction("Right"));
		actionMap.put("RELEASE_RIGHT", 
				new ReleaseShiftRightAction("Release Right"));
		
		// Down
		if (softDrop != null && !softDrop.isEmpty()) {
			inputMap.put(KeyStroke.getKeyStroke(softDrop), "DOWN");
			inputMap.put(KeyStroke.getKeyStroke(RELEASE_INPUT + softDrop), 
					"RELEASE_DOWN");
		}
		actionMap.put("DOWN", new DropSoftAction("Down"));
		actionMap.put("RELEASE_DOWN", 
				new ReleaseDropSoftAction("Release Down"));
		// Drop down
		if (hardDrop != null && !hardDrop.isEmpty()) {
			inputMap.put(KeyStroke.getKeyStroke(hardDrop), "DROP");
			inputMap.put(KeyStroke.getKeyStroke(RELEASE_INPUT + hardDrop), 
					"RELEASE_DROP");
		}
		actionMap.put("DROP", new DropHardAction("Drop"));
		actionMap.put("RELEASE_DROP", 
				new ReleaseDropHardAction("Release Drop"));
		
		// Rotate left
		if (rotateLeft != null && !rotateLeft.isEmpty()) {
			inputMap.put(KeyStroke.getKeyStroke(rotateLeft), "ROTATE_LEFT");
			inputMap.put(KeyStroke.getKeyStroke(RELEASE_INPUT + rotateLeft), 
					"RELEASE_ROTATE_LEFT");
		}
		actionMap.put("ROTATE_LEFT", new RotateLeftAction("Rotate Left"));
		actionMap.put("RELEASE_ROTATE_LEFT", 
				new ReleaseRotateLeftAction("Release Rotate Left"));
		// Rotate right
		if (rotateRight != null && !rotateRight.isEmpty()) {
			inputMap.put(KeyStroke.getKeyStroke(rotateRight), "ROTATE_RIGHT");
			inputMap.put(KeyStroke.getKeyStroke(RELEASE_INPUT + rotateRight), 
					"RELEASE_ROTATE_RIGHT");
		}
		actionMap.put("ROTATE_RIGHT", new RotateRightAction("Rotate Right"));
		actionMap.put("RELEASE_ROTATE_RIGHT", 
				new ReleaseRotateRightAction("Release Rotate Right"));
	}

	/**
	 * Set the Tetris game to its default configuration.
	 */
	public final void setMode() {
		setMode(null);
	}

	/**
	 * Set the Tetris game configuration based on the given {@link Mode}.
	 * 
	 * @param mode
	 *            <em>standard</em> configurations for the Tetris game
	 */
	public final void setMode(final Mode mode) {
		if (mode == null) {
			setMode(Mode.MIX);
			return;
		}
		switch (mode) {
		case CLASSIC:
		case MODERN:
		case MIX:
			this.levelUpSpeedUpRatio = mode.getLevelUpSpeedUpRatio();
			this.wallKickEnabled = mode.isWallKickEnabled();
			this.floorKickEnabled = mode.isFloorKickEnabled();
			this.lockDelay = mode.getLockDelay();
			this.infiniteLockDelayEnabled = mode.isInfiniteLockDelayEnabled();
			break;
		default:
			setMode(Mode.MIX);
			break;
		}
	}

	// #########################################################################
	/**
	 * Get the status bar.
	 * 
	 * @return the status bar
	 */
	public final JLabel getStatusbar() {
		return statusbar;
	}

	/**
	 * Set the status bar.
	 * 
	 * @param label
	 *            the status bar
	 */
	public void setStatusbar(final JLabel label) {
		this.statusbar = label;
	}

	/**
	 * Get the preview panels.
	 * 
	 * @return the preview panels
	 */
	public final JPanel[] getPreviewPanels() {
		return previewPanels;
	}

	/**
	 * Set the preview panels.
	 * 
	 * @param newPreviewPanels
	 *            the preview panels
	 */
	public void setPreviewPanels(final JPanel[] newPreviewPanels) {
		this.previewPanels = newPreviewPanels;
	}

	/**
	 * Get the hold space panel.
	 * 
	 * @return the hold space panel
	 */
	public final JPanel getHoldPanel() {
		return holdPanel;
	}

	/**
	 * Set the hold space panel.
	 * 
	 * @param panel
	 *            the hold space panel
	 */
	public void setHoldPanel(final JPanel panel) {
		this.holdPanel = panel;
	}

	// #########################################################################
	/**
	 * @return the key stroke to pause game
	 */
	public final synchronized String getPause() {
		return pause;
	}

	/**
	 * @param pauseKeyStroke the key stroke to pause game
	 */
	public final synchronized void setPause(final String pauseKeyStroke) {
		this.pause = pauseKeyStroke;
	}

	/**
	 * @return the key stroke to hold a piece
	 */
	public final synchronized String getHold() {
		return hold;
	}

	/**
	 * @param holdKeyStroke the key stroke to hold a piece
	 */
	public final synchronized void setHold(final String holdKeyStroke) {
		this.hold = holdKeyStroke;
	}

	/**
	 * @return the key stroke to shift the falling piece to the left
	 */
	public final synchronized String getLeft() {
		return left;
	}

	/**
	 * @param leftKeyStroke
	 *            the key stroke to shift the falling piece to the left
	 */
	public final synchronized void setLeft(final String leftKeyStroke) {
		this.left = leftKeyStroke;
	}

	/**
	 * @return the key stroke to shift the falling piece to the right
	 */
	public final synchronized String getRight() {
		return right;
	}

	/**
	 * @param rightKeyStroke the right to set
	 */
	public final synchronized void setRight(final String rightKeyStroke) {
		this.right = rightKeyStroke;
	}

	/**
	 * @return the key stroke to soft drop the falling piece
	 */
	public final synchronized String getSoftDrop() {
		return softDrop;
	}

	/**
	 * @param softDropKeyStroke the key stroke to soft drop the falling piece
	 */
	public final synchronized void setSoftDrop(
			final String softDropKeyStroke) {
		this.softDrop = softDropKeyStroke;
	}

	/**
	 * @return the key stroke to hard drop the falling piece
	 */
	public final synchronized String getHardDrop() {
		return hardDrop;
	}

	/**
	 * @param hardDropKeyStroke the key stroke to hard drop the falling piece
	 */
	public final synchronized void setHardDrop(
			final String hardDropKeyStroke) {
		this.hardDrop = hardDropKeyStroke;
	}

	/**
	 * @return the key stroke to rotate the falling piece to the left
	 */
	public final synchronized String getRotateLeft() {
		return rotateLeft;
	}

	/**
	 * @param rotateLeftKeyStroke
	 *            the key stroke to rotate the falling piece to the left
	 */
	public final synchronized void setRotateLeft(
			final String rotateLeftKeyStroke) {
		this.rotateLeft = rotateLeftKeyStroke;
	}

	/**
	 * @return the key stroke to rotate the falling piece to the right
	 */
	public final synchronized String getRotateRight() {
		return rotateRight;
	}

	/**
	 * @param rotateRightKeyStroke
	 *            the key stroke to rotate the falling piece to the right
	 */
	public final synchronized void setRotateRight(
			final String rotateRightKeyStroke) {
		this.rotateRight = rotateRightKeyStroke;
	}

	// #########################################################################
	/**
	 * Get the number of lines cleared.
	 * 
	 * @return the number of lines cleared
	 */
	public final synchronized int getLines() {
		return lines;
	}

	/**
	 * Set the number of lines cleared.
	 * 
	 * @param newLines
	 *            the new number of lines cleared.
	 */
	protected final synchronized void setLines(final int newLines) {
		this.lines = newLines;
	}

	/**
	 * Get level.
	 * 
	 * @return the level
	 */
	public final synchronized int getLevel() {
		return level;
	}

	/**
	 * Set the level.
	 * 
	 * <p>
	 * Setting the level will automatically update the {@link #gravity}.
	 * </p>
	 * 
	 * @see <a href="http://tetris.wikia.com/wiki/Gravity">Gravity</a>
	 * @see <a href="http://tetris.wikia.com/wiki/Drop#Gravity">Drop Gravity</a>
	 * 
	 * @param newLevel
	 *            the new level
	 */
	protected synchronized void setLevel(final int newLevel) {
		this.level = newLevel;
		// Set up gravity (number of cell per frames)
		this.gravity = Math.min(BOARD_CEILING,
				Math.pow(this.levelUpSpeedUpRatio, this.level) 
						/ (double) FRAME_PER_SECOND);
		System.out.println("Level " + this.level + " (" + this.gravity + "G)");
	}

	/**
	 * Get the score.
	 * 
	 * @return the score
	 */
	public final synchronized long getScore() {
		return score;
	}

	/**
	 * Set the score.
	 * 
	 * @param newScore
	 *            the new score
	 */
	protected final synchronized void setScore(final long newScore) {
		this.score = newScore;
	}

	/**
	 * Is the stack inside the danger zone?
	 * 
	 * @see #BOARD_CEILING
	 * @see #BOARD_CEILING_DANGER_ZONE
	 * @see #pieceDropped()
	 * 
	 * @return {@code true} if the stack inside the danger zone
	 */
	public final synchronized boolean isInsideDangerZone() {
		return insideDangerZone;
	}

	/**
	 * Set if the stack is inside the danger zone.
	 * 
	 * @param isInsideDangerZone
	 *            the new <em>insideDangerZone</em> status
	 */
	protected final synchronized void setInsideDangerZone(
			final boolean isInsideDangerZone) {
		this.insideDangerZone = isInsideDangerZone;
	}

	/**
	 * Has the game started?
	 * 
	 * @return {@code true} if the game has started
	 */
	public final synchronized boolean isStarted() {
		return started;
	}

	/**
	 * Set game's <em>started</em> status.
	 * 
	 * @see #start()
	 * @see #stop()
	 * 
	 * @param isStarted
	 *            the new <em>started</em> status
	 */
	protected final synchronized void setStarted(
			final boolean isStarted) {
		this.started = isStarted;
	}

	/**
	 * Is the game paused?
	 * 
	 * @return {@code true} if the game is paused
	 */
	public final synchronized boolean isPaused() {
		return paused;
	}

	/**
	 * Set game's <em>paused</em> status.
	 * 
	 * @see #pause()
	 * 
	 * @param isPaused
	 *            the new <em>paused</em> status
	 */
	protected final synchronized void setPaused(
			final boolean isPaused) {
		this.paused = isPaused;
	}

	/**
	 * Is hard drop in progress?
	 * 
	 * @return {@code true} if hard drop in progress
	 */
	public final synchronized boolean isDroppingHard() {
		return droppingHard;
	}

	/**
	 * Set hard drop progress.
	 * 
	 * @param isHardDropping
	 *            Is hard drop in progress?
	 */
	protected final synchronized void setDroppingHard(
			final boolean isHardDropping) {
		this.droppingHard = isHardDropping;
	}

	/**
	 * Is soft drop in progress?
	 * 
	 * @return {@code true} if soft drop in progress
	 */
	public final synchronized boolean isDroppingSoft() {
		return droppingSoft;
	}

	/**
	 * Set soft drop progress.
	 * 
	 * @param isSoftDropping
	 *            Is soft drop in progress?
	 */
	protected final synchronized void setDroppingSoft(
			final boolean isSoftDropping) {
		this.droppingSoft = isSoftDropping;
	}

	/**
	 * Is left shift in progress?
	 * 
	 * @return {@code true} if left shift in progress
	 */
	public final synchronized boolean isShiftingLeft() {
		return shiftingLeft;
	}

	/**
	 * Set left shift progress.
	 * 
	 * <p>
	 * Resets {@link #shiftDelayCount} to {@code 0}.
	 * </p>
	 * 
	 * @param leftShifting
	 *            Is left shift in progress?
	 */
	protected final synchronized void setShiftingLeft(
			final boolean leftShifting) {
		this.shiftingLeft = leftShifting;
		this.shiftDelayCount = 0;
	}

	/**
	 * Is right shift in progress?
	 * 
	 * @return {@code true} if right shift in progress
	 */
	public final synchronized boolean isShiftingRight() {
		return shiftingRight;
	}

	/**
	 * Set right shift progress.
	 * 
	 * <p>
	 * Resets {@link #shiftDelayCount} to {@code 0}.
	 * </p>
	 * 
	 * @param rightShifting
	 *            Is right shift in progress?
	 */
	protected final synchronized void setShiftingRight(
			final boolean rightShifting) {
		this.shiftingRight = rightShifting;
		this.shiftDelayCount = 0;
	}

	/**
	 * Is left rotation in progress?
	 * 
	 * @return {@code true} if left rotation in progress
	 */
	public final synchronized boolean isRotatingLeft() {
		return rotatingLeft;
	}

	/**
	 * Set left rotation progress.
	 * 
	 * <p>
	 * Decrease {@link #rotationsNeeded} if {@code true}, resets to {@code 0}
	 * otherwise.
	 * </p>
	 * 
	 * @see #rotationsNeeded
	 * 
	 * @param isRotatingLeft
	 *            Is left rotation in progress?
	 */
	protected final synchronized void setRotatingLeft(
			final boolean isRotatingLeft) {
		this.rotatingLeft = isRotatingLeft;
		if (rotatingLeft) {
			rotationsNeeded--;
		} else {
			rotationsNeeded = 0;
		}
	}

	/**
	 * Is right rotation in progress?
	 * 
	 * @return {@code true} if right rotation in progress
	 */
	public final synchronized boolean isRotatingRight() {
		return rotatingRight;
	}

	/**
	 * Set right rotation progress.
	 * 
	 * <p>
	 * Increase {@link #rotationsNeeded} if {@code true}, resets to {@code 0}
	 * otherwise.
	 * </p>
	 * 
	 * @see #rotationsNeeded
	 * 
	 * @param isRotatingRight
	 *            Is right rotation in progress?
	 */
	protected final synchronized void setRotatingRight(
			final boolean isRotatingRight) {
		this.rotatingRight = isRotatingRight;
		if (rotatingRight) {
			rotationsNeeded++;
		} else {
			rotationsNeeded = 0;
		}
	}

	// #########################################################################
	@Override
	public void close() {
		this.stop();
		if (MIDI_PLAYER != null) {
			try {
				MIDI_PLAYER.close();
			} catch (Exception ex) {
				LOGGER.log(Level.SEVERE, null, ex);
			}
		}
		if (shapeGenerator != null && shapeGenerator.isAlive()) {
			shapeGenerator.interrupt();
		}
		nextShapes.clear();
		timer = null;
	}

	// #########################################################################
	@Override
	public void actionPerformed(final ActionEvent e) {
		if (fallingPiece.getShape() == null) {
			newPiece();
		} else {
			// Rotation
			if (rotatingLeft && rotationsNeeded < 0) {
				if (Tetrion.this.tryMove(
						Tetrion.this.fallingPiece.rotateLeft(), 
						Tetrion.this.curX, Tetrion.this.curY)) {
					rotationsNeeded = 0;
				}
			} else if (rotatingRight && rotationsNeeded > 0) {
				if (Tetrion.this.tryMove(
						Tetrion.this.fallingPiece.rotateRight(), 
						Tetrion.this.curX, Tetrion.this.curY)) {
					rotationsNeeded = 0;
				}
			}

			// Shift
			if ((shiftingLeft || shiftingRight) 
					&& ((shiftDelayCount >= 0 && shiftDelayCount++ >= AUTO_SHIFT_DELAY) 
							|| (shiftDelayCount < 0 && shiftDelayCount-- >= -AUTO_SHIFT_PERIOD))) {
				if (shiftingLeft
						&& Tetrion.this.tryMove(Tetrion.this.fallingPiece, 
								Tetrion.this.curX - 1, Tetrion.this.curY)) {
					shiftDelayCount = -1;
				} else if (shiftingRight
						&& Tetrion.this.tryMove(Tetrion.this.fallingPiece, 
								Tetrion.this.curX + 1, Tetrion.this.curY)) {
					shiftDelayCount = -1;
				} else {
					shiftDelayCount = 0;
				}
			}

			// Gravity
			this.framesSinceLastDrop++;
			double cellsDown = this.gravity * this.framesSinceLastDrop;
			if (droppingSoft) {
				cellsDown *= SOFT_DROP_RATIO;
			}
			boolean softDroppingWithoutLock = true;
			while (cellsDown > 1 && softDroppingWithoutLock) {
				cellsDown--;
				softDroppingWithoutLock = softDrop();
			}

			// Lock
			if (!softDroppingWithoutLock 
					&& lockDelayFrameCount++ >= lockDelay) {
				pieceDropped();
			}
		}
	}

	// #########################################################################
	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		updateStatusBar();
		// Force refresh of hold panel
		if (refreshHoldPanelNeeded 
				&& holdPanel != null 
				&& holdPanel.getGraphics() != null) {
			holdPanel.getGraphics().fillRect(0, 0, 
					holdPanel.getWidth(), 
					holdPanel.getHeight());
		}
		// Force refresh of each preview panel
		if (refreshPreviewPanelsNeeded && previewPanels != null) {
			for (JPanel previewPanel : previewPanels) {
				if (previewPanel != null 
						&& previewPanel.getGraphics() != null) {
					previewPanel.getGraphics().fillRect(0, 0, 
							previewPanel.getWidth(), 
							previewPanel.getHeight());
				}
			}
		}
		if (paused) {
			return;
		}

		Dimension size = getSize();
		int squareWidth = squareWidth();
		int squareHeight = squareHeight();
		int boardTop = (int) size.getHeight() - BOARD_CEILING * squareHeight;

		// Draw stack
		drawStack(g, squareWidth, squareHeight, boardTop);

		// Draw current piece
		drawFallingPiece(g, squareWidth, squareHeight, boardTop);

		// TODO Update hold only if changes
		// Draw hold
		drawHold();

		// TODO Update next only if changes
		// Draw previews
		drawNextShapes();
	}

	/**
	 * Draw the stack in the {@link #playfield}.
	 * 
	 * @param g
	 *            the graphics used for drawing
	 * @param squareWidth
	 *            the blocks width
	 * @param squareHeight
	 *            the blocks height
	 * @param boardTop
	 *            ceiling position
	 */
	private void drawStack(final Graphics g, 
			final int squareWidth, final int squareHeight, 
			final int boardTop) {
		for (int y = 0; y < BOARD_CEILING; ++y) {
			for (int x = 0; x < BOARD_WIDTH; ++x) {
				Tetromino.Shape shape = shapeAt(x, BOARD_CEILING - y - 1);
				if (shape != null) {
					drawSquare(g, 
							0 + x * squareWidth, 
							boardTop + y * squareHeight, 
							shape, squareWidth, squareHeight);
				}
			}
		}
	}

	/**
	 * Draw the falling piece in the {@link #playfield}.
	 * 
	 * <p>
	 * Defines the estimated drop target destination and draws the <em>Ghost
	 * piece</em> as well.
	 * </p>
	 * 
	 * @see #fallingPiece
	 * @see <a href="http://tetris.wikia.com/wiki/Ghost_piece">Ghost piece</a>
	 * 
	 * @param g
	 *            the graphics used for drawing
	 * @param squareWidth
	 *            the blocks width
	 * @param squareHeight
	 *            the blocks height
	 * @param boardTop
	 *            ceiling position
	 */
	private void drawFallingPiece(final Graphics g, 
			final int squareWidth, final int squareHeight, 
			final int boardTop) {
		if (fallingPiece.getShape() != null) {
			Tetromino.Shape fallingShape = fallingPiece.getShape();
			// Define drop estimated target
			int dropY = curY;
			while (dropY > 0) {
				if (!isMoveable(fallingPiece, curX, dropY - 1)) {
					break;
				}
				--dropY;
			}

			// Draw ghost
			Color shadowColor = fallingShape.getShadowColor();
			for (int i = 0; i < Tetromino.BLOCKS; ++i) {
				int blockX = curX + fallingPiece.x(i);
				int blockDropY = dropY - fallingPiece.y(i);
				drawSquare(g, 
						0 + blockX * squareWidth, 
						boardTop + (BOARD_CEILING - blockDropY - 1) * squareHeight,
						shadowColor, squareWidth, squareHeight);
			}

			// Draw piece
			Color fallingColor = fallingShape.getActiveColor();
			for (int i = 0; i < Tetromino.BLOCKS; ++i) {
				int blockX = curX + fallingPiece.x(i);
				int blockY = curY - fallingPiece.y(i);
				drawSquare(g, 
						0 + blockX * squareWidth, 
						boardTop + (BOARD_CEILING - blockY - 1) * squareHeight,
						fallingColor, squareWidth, squareHeight);
			}
		}
	}

	/**
	 * Draw the hold piece.
	 * 
	 * @see #holdPiece
	 * @see #refreshHoldPanelNeeded
	 */
	private void drawHold() {
		if (!refreshHoldPanelNeeded 
				|| holdPanel == null 
				|| holdPiece.getShape() == null) {
			return;
		}
		Tetromino.Shape holdShape = holdPiece.getShape();
		int panelSquareWidth = squareWidth(holdPanel, Tetromino.BLOCKS);
		int panelSquareHeight = squareHeight(holdPanel, Tetromino.BLOCKS);
		for (int j = 0; j < Tetromino.BLOCKS; ++j) {
			Color color;
			if (holdPieceAvailable) {
				color = holdShape.getColor();
			} else {
				color = holdShape.getShadowColor();
			}
			drawSquare(holdPanel.getGraphics(), 
					(1 + holdShape.x(j)) * panelSquareWidth,
					(1 + holdShape.y(j)) * panelSquareHeight,
					color, 
					panelSquareWidth, panelSquareHeight);
		}
		refreshHoldPanelNeeded = false;
	}

	/**
	 * Draw the next shapes preview.
	 * 
	 * @see #nextShapes
	 * @see #previewPanels
	 * @see #refreshPreviewPanelsNeeded
	 */
	private void drawNextShapes() {
		if (!refreshPreviewPanelsNeeded || previewPanels == null) {
			return;
		}
		int s = 0;
		int n = Math.min(previewPanels.length, nextShapes.size());
		for (Tetromino.Shape shape : nextShapes) {
			if (s >= n) {
				break;
			}
			JPanel previewPanel = previewPanels[s++];
			int panelSquareWidth = squareWidth(previewPanel, Tetromino.BLOCKS);
			int panelSquareHeight = 
					squareHeight(previewPanel, Tetromino.BLOCKS);
			for (int j = 0; j < Tetromino.BLOCKS; ++j) {
				drawSquare(previewPanel.getGraphics(), 
						(1 + shape.x(j)) * panelSquareWidth,
						(1 + shape.y(j)) * panelSquareHeight, 
						shape, 
						panelSquareWidth, panelSquareHeight);
			}
		}
		refreshPreviewPanelsNeeded = false;
	}

	/**
	 * Draw a square for the given shape at the given position.
	 * 
	 * @param g
	 *            the graphics used for drawing
	 * @param x
	 *            the {@code X} position
	 * @param y
	 *            the {@code Y} position
	 * @param shape
	 *            the shape
	 * @param squareWidth
	 *            the blocks width in pixels
	 * @param squareHeight
	 *            the blocks height in pixels
	 */
	protected static void drawSquare(final Graphics g, 
			final int x, final int y, final Tetromino.Shape shape, 
			final int squareWidth, final int squareHeight) {
		drawSquare(g, x, y, shape.getColor(), squareWidth, squareHeight);
	}

	/**
	 * Draw a square with the given color at the given position.
	 * 
	 * @param g
	 *            the graphics used for drawing
	 * @param x
	 *            the {@code X} position
	 * @param y
	 *            the {@code Y} position
	 * @param color
	 *            the square's color
	 * @param squareWidth
	 *            the blocks width in pixels
	 * @param squareHeight
	 *            the blocks height in pixels
	 */
	protected static void drawSquare(final Graphics g, 
			final int x, final int y, final Color color, 
			final int squareWidth, final int squareHeight) {
		g.setColor(color);
		g.fillRect(x + 1, y + 1, squareWidth - 2, squareHeight - 2);

		g.setColor(color.brighter());
		g.drawLine(x, y + squareHeight - 1, x, y);
		g.drawLine(x, y, x + squareWidth - 1, y);

		g.setColor(color.darker());
		g.drawLine(x + 1, y + squareHeight - 1, 
				x + squareWidth - 1, y + squareHeight - 1);
		g.drawLine(x + squareWidth - 1, y + squareHeight - 1, 
				x + squareWidth - 1, y + 1);
	}

	/**
	 * Compute the blocks width for a given panel and a given panel width in
	 * blocks.
	 * 
	 * @param panel
	 *            the panel
	 * @param panelSquareWidth
	 *            the panel's width in blocks
	 * @return the blocks width in pixels
	 */
	private static int squareWidth(final JPanel panel, 
			final int panelSquareWidth) {
		return (int) panel.getSize().getWidth() / panelSquareWidth;
	}

	/**
	 * Compute the blocks width.
	 * 
	 * @return the blocks width in pixels
	 */
	private int squareWidth() {
		return squareWidth(this, BOARD_WIDTH);
	}

	/**
	 * Compute the blocks height for a given panel and a given panel height in
	 * blocks.
	 * 
	 * @param panel
	 *            the panel
	 * @param panelSquareHeight
	 *            the panel's height in blocks
	 * @return the blocks height in pixels
	 */
	private static int squareHeight(final JPanel panel, 
			final int panelSquareHeight) {
		return (int) panel.getSize().getHeight() / panelSquareHeight;
	}

	/**
	 * Compute the blocks height.
	 * 
	 * @return the blocks height in pixels
	 */
	private int squareHeight() {
		return squareHeight(this, BOARD_CEILING);
	}

	// #########################################################################
	/**
	 * Get the shape at the given position in the {@link #playfield}.
	 * 
	 * @param x
	 *            the {@code X} position
	 * @param y
	 *            the {@code Y} position
	 * @return the shape at the given position in the {@link #playfield},
	 *         {@code null} if none.
	 */
	private Tetromino.Shape shapeAt(final int x, final int y) {
		return playfield[(y * BOARD_WIDTH) + x];
	}

	/**
	 * Start the game at first level.
	 * 
	 * @see #start(int)
	 */
	public final void start() {
		start(0);
	}

	/**
	 * Start the game at given level.
	 * 
	 * @param startLevel
	 *            the level of the game
	 */
	public void start(final int startLevel) {
		System.out.println("Starting new game");
		if (paused) {
			return;
		}
		if (!shapeGenerator.isAlive()) {
			shapeGenerator.start();
		}

		framesContinouslySoftDropped = -1;
		fallingPiece.setShape(null);
		holdPiece.setShape(null);
		lines = 0;
		score = 0;
		this.setLevel(startLevel);
		this.clear();

		if (MIDI_PLAYER != null && (MIDI_PLAYER.isStopped() 
				|| MIDI_PLAYER.isPaused()
				|| MIDI_PLAYER.getCurrentSongIndex() == 0 
				|| MIDI_PLAYER.getCurrentSongIndex() > 2)) {
			MIDI_PLAYER.startPlaying(1);
			MIDI_PLAYER.setLooping(true);
		}
		started = true;
		timer.start();
	}

	/**
	 * Stop the game.
	 */
	private void stop() {
		timer.stop();
		fallingPiece.setShape(null);
		holdPiece.setShape(null);
		started = false;
		if (MIDI_PLAYER != null) {
			MIDI_PLAYER.setTempoFactor(1F);
			MIDI_PLAYER.moveToSong(MIDI_PLAYER.size() - 1);
			MIDI_PLAYER.setLooping(false);
		}
	}

	/**
	 * Pause the game.
	 */
	private void pause() {
		if (!started) {
			return;
		}

		paused = !paused;
		if (paused) {
			timer.stop();
		} else {
			timer.start();
		}
		// Setup refresh status
		refreshHoldPanelNeeded = holdPanel != null;
		refreshPreviewPanelsNeeded = this.previewPanels != null 
				&& this.previewPanels.length > 0;
		if (MIDI_PLAYER != null) {
			if (!MIDI_PLAYER.isStopped()) {
				MIDI_PLAYER.pausePlaying();
			} else {
				MIDI_PLAYER.startPlaying();
			}
		}
		repaint();
	}

	/**
	 * Clear the {@link #playfield}.
	 */
	private void clear() {
		for (int i = 0, n = BOARD_HEIGHT * BOARD_WIDTH; i < n; ++i) {
			playfield[i] = null;
		}
	}

	/**
	 * Hard drop.
	 * 
	 * <p>
	 * Drop the falling piece until it reaches the stack or bottom of
	 * {@link #playfield}.
	 * </p>
	 * 
	 * @see DropHardAction
	 * @see ReleaseDropHardAction
	 * @see #isDroppingHard()
	 * @see #setDroppingHard(boolean)
	 * @see <a href="http://tetris.wikia.com/wiki/Drop">Drop</a>
	 */
	private void hardDrop() {
		int newY = curY;
		int iniY = curY;
		while (newY > 0) {
			if (!tryMove(fallingPiece, curX, newY - 1)) {
				break;
			}
			--newY;
		}
		// Count lines continuously hard dropped and add twice to score
		if (droppingHard) {
			this.score += 2 * (iniY - newY);
		}

		pieceDropped();
	}

	/**
	 * Soft drop.
	 * 
	 * @see DropSoftAction
	 * @see ReleaseDropSoftAction
	 * @see #isDroppingSoft()
	 * @see #setDroppingSoft(boolean)
	 * @see <a href="http://tetris.wikia.com/wiki/Drop">Drop</a>
	 * 
	 * @return {@code true} if soft dropped occurred
	 */
	private boolean softDrop() {
		// Count lines continuously soft dropped and add to score
		if (droppingSoft && framesContinouslySoftDropped >= 0) {
			this.framesContinouslySoftDropped++;
		}
		if (!tryMove(fallingPiece, curX, curY - 1)) {
			if (droppingSoft && framesContinouslySoftDropped >= 0) {
				this.score += framesContinouslySoftDropped;
			}
			if (droppingSoft) {
				this.framesContinouslySoftDropped = 0;
			} else {
				this.framesContinouslySoftDropped = -1;
			}
			return false;
		}

		this.framesSinceLastDrop = 0;
		return true;
	}

	/**
	 * Lock the falling piece and add to the stack.
	 * 
	 * <p>
	 * When a falling piece reaches the stack or bottom of the
	 * {@link #playfield}, the piece is itself added to the stack.
	 * </p>
	 * 
	 * @return the number of full lines removed
	 */
	private int pieceDropped() {
		// Add piece to stack
		for (int i = 0; i < Tetromino.BLOCKS; ++i) {
			int x = curX + fallingPiece.x(i);
			int y = curY - fallingPiece.y(i);
			playfield[(y * BOARD_WIDTH) + x] = fallingPiece.getShape();
		}
		lockDelayFrameCount = 0;
		framesSinceLastDrop = 0;
		fallingPiece.setShape(null);

		refreshHoldPanelNeeded = holdPanel != null 
				&& (holdPieceAvailable != (holdPiece.getShape() != null));
		holdPieceAvailable = holdPiece.getShape() != null;

		int removedLines = removeFullLines();

		if (MIDI_PLAYER != null) {
			insideDangerZone = false;
			for (int i = (BOARD_HEIGHT * BOARD_WIDTH) - 1, 
					n = BOARD_CEILING_DANGER_ZONE * BOARD_WIDTH; 
					i > n && !insideDangerZone; 
					--i) {
				insideDangerZone = playfield[i] != null;
			}
			if (insideDangerZone) {
				MIDI_PLAYER.setTempoFactor(DANGER_ZONE_MUSIC_RATIO);
			} else {
				MIDI_PLAYER.setTempoFactor(1F);
			}
		}

		return removedLines;
	}

	/**
	 * Remove full lines from stack.
	 * 
	 * @return the number of lines removed from stack
	 */
	private int removeFullLines() {
		int numFullLines = 0;

		for (int i = BOARD_HEIGHT - 1; i >= 0; --i) {
			boolean lineIsFull = true;

			for (int j = 0; j < BOARD_WIDTH; ++j) {
				if (shapeAt(j, i) == null) {
					lineIsFull = false;
					break;
				}
			}

			if (lineIsFull) {
				++numFullLines;
				for (int k = i; k < BOARD_HEIGHT - 1; ++k) {
					for (int j = 0; j < BOARD_WIDTH; ++j) {
						playfield[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1);
					}
				}
			}
		}

		if (numFullLines > 0) {
			lines += numFullLines;
			updateScoreFromFullLines(numFullLines);

			fallingPiece.setShape(null);
			repaint();
		}

		return numFullLines;
	}

	/**
	 * Update score for a given number of lines removed from stack.
	 * 
	 * @param numFullLines
	 *            the number of lines removed from stack.
	 */
	private void updateScoreFromFullLines(final int numFullLines) {
		/*
		 * Increase score based on lines cleared for level.
		 *
		 * @see <a href="http://tetris.wikia.com/wiki/Scoring">Scoring</a>
		 */
		int scoreToAdd = SCORES[numFullLines] * (level + 1);
		if (numFullLines == TETRIS) {
			System.out.println("TETRIS!!!");
		}
		score += scoreToAdd * (level + 1);

		// Update speed based on level
		int newLevel = lines / LEVEL_RATIO;
		if (newLevel > (lines - numFullLines) / LEVEL_RATIO) {
			setLevel(level + 1);
		}
	}

	/**
	 * Hold piece.
	 * 
	 * <p>
	 * At any time starting when a {@link Tetromino} enters the
	 * {@link #playfield} until it locks, the player can press the Hold button
	 * on the controller to move the active {@link Tetromino} into the hold
	 * space and move the {@link Tetromino} that was in the hold space to the
	 * top of the {@link #playfield}. A {@link Tetromino} moved into the hold
	 * space is unavailable for switching out until the {@link Tetromino} that
	 * was moved out of the hold space locks.
	 * </p>
	 * 
	 * @see <a href="http://tetris.wikia.com/wiki/Hold_piece">Hold piece</a>
	 */
	private void holdPiece() {
		if (!started || (holdPiece.getShape() != null && !holdPieceAvailable)) {
			return;
		}

		newPiece(true);
	}

	/**
	 * Generate a new piece without using the hold piece.
	 */
	private void newPiece() {
		newPiece(false);
	}

	/**
	 * Generate a new piece at the top of the {@link #playfield}.
	 * 
	 * <p>
	 * If the new piece cannot be moved to the top of the {@link #playfield},
	 * the game stops.
	 * </p>
	 * 
	 * @see #holdPiece
	 * @see #holdPiece()
	 * @see #nextShapes
	 * @see #shapeGenerator
	 * @see #tryMove(Tetromino, int, int)
	 * @see #stop()
	 * 
	 * @param switchWithHoldPiece
	 *            switch current piece with {@link #holdPiece}
	 */
	private void newPiece(final boolean switchWithHoldPiece) {
		if (switchWithHoldPiece && holdPiece.getShape() != null) {
			Tetromino.Shape tempFallingPieceShape = fallingPiece.getShape();
			fallingPiece.setShape(holdPiece.getShape());
			holdPiece.setShape(tempFallingPieceShape);
			holdPieceAvailable = false;
			refreshHoldPanelNeeded = holdPanel != null;
		} else {
			// If no piece currently held but switch asked
			if (switchWithHoldPiece) {
				holdPiece.setShape(fallingPiece.getShape());
				holdPieceAvailable = false;
				refreshHoldPanelNeeded = holdPanel != null;
			}

			try {
				fallingPiece.setShape(this.nextShapes.take());
				System.out.println("Next pieces: " + this.nextShapes);
			} catch (InterruptedException ex) {
				LOGGER.log(Level.SEVERE, null, ex);
				fallingPiece.setRandomShape();
			}
		}
		curX = BOARD_WIDTH / 2 - 1;
		curY = BOARD_CEILING - 1 + fallingPiece.minY();

		if (!tryMove(fallingPiece, curX, curY)) {
			this.stop();
		}
		repaint();
	}

	/**
	 * Can the given piece be moved at the given position.
	 * 
	 * @param piece
	 *            the piece to test
	 * @param newX
	 *            the new {@code X} position for the given piece
	 * @param newY
	 *            the new {@code Y} position for the given piece
	 * @return {@code true} if the given piece can be moved at the given
	 *         position
	 */
	private boolean isMoveable(final Tetromino piece, final int newX, 
			final int newY) {
		for (int i = 0; i < Tetromino.BLOCKS; ++i) {
			int x = newX + piece.x(i);
			int y = newY - piece.y(i);
			if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
				return false;
			}
			if (shapeAt(x, y) != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Try to move the given piece at the given position.
	 * 
	 * <p>
	 * If the piece can be moved, then the given piece will <em>replace</em> the
	 * {@link #fallingPiece} and be set at the given position.
	 * </p>
	 * 
	 * <p>
	 * If the piece cannot be moved, the game will test if floor kicks and wall
	 * kicks are possible:
	 * </p>
	 * <ul>
	 * <li>A wall kick happens when a player rotates a piece when no space
	 * exists in the squares where that {@link Tetromino} would normally occupy
	 * after the rotation.</li>
	 * <li>A floor kick, like a wall kick, happens when a player rotates a piece
	 * when no space exists in the squares where that {@link Tetromino} would
	 * normally occupy after the rotation <em>when rotating against the floor
	 * opposed to a wall</em>.</li>
	 * </ul>
	 * 
	 * @see #isMoveable(Tetromino, int, int)
	 * @see <a href="http://tetris.wikia.com/wiki/Wall_kick">Wall kick</a>
	 * @see <a href="http://tetris.wikia.com/wiki/Floor_kick">Floor kick</a>
	 * 
	 * @param piece
	 *            the piece to test
	 * @param newX
	 *            the new {@code X} position for the given piece
	 * @param newY
	 *            the new {@code Y} position for the given piece
	 * @return {@code true} if the given piece was moved at the given position
	 */
	private boolean tryMove(final Tetromino piece, final int newX, 
			final int newY) {
		int finalNewX = newX;
		int finalNewY = newY;

		boolean isMoveable = isMoveable(piece, finalNewX, finalNewY);

		if (!isMoveable && wallKickEnabled && (rotatingLeft || rotatingRight)) {
			// Handle wall kick
			int newWidth = piece.getWidth();

			if (newX >= 0 && newX <= BOARD_WIDTH - newWidth - 1) {
				for (int x = newX + 1, l = newX + newWidth - 1; 
						x < l && !isMoveable; x++) {
					if (isMoveable(piece, x, newY)) {
						finalNewX = x;
						isMoveable = true;
					}
				}
			} else if (newX >= newWidth - 1 && newX <= BOARD_WIDTH - 1) {
				for (int x = newX - 1, l = newX - newWidth - 1; 
						x > l && !isMoveable; x--) {
					if (isMoveable(piece, x, newY)) {
						finalNewX = x;
						isMoveable = true;
					}
				}
			}
		}

		if (!isMoveable && floorKickEnabled 
				&& (rotatingLeft || rotatingRight)) {
			// Handle floor kick
			int newHeight = piece.getHeight();
			if (newY >= 0 && newY <= BOARD_HEIGHT - newHeight - 1) {
				for (int y = newY + 1, l = newY + newHeight; 
						y < l && !isMoveable; y++) {
					if (isMoveable(piece, newX, y)) {
						finalNewY = y;
						isMoveable = true;
					}
				}
			}
		}

		// Move piece and return status
		if (isMoveable) {
			// Infinity
			if (infiniteLockDelayEnabled) {
				lockDelayFrameCount = 0;
			}

			fallingPiece = piece;
			curX = finalNewX;
			curY = finalNewY;
			repaint();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Update the status bar.
	 */
	private void updateStatusBar() {
		if (statusbar == null) {
			return;
		}
		StringBuilder statusBuilder = new StringBuilder();
		if (!started) {
			statusBuilder.append("GAME OVER \t ");
		} else if (paused) {
			statusBuilder.append("Paused \t ");
		}
		statusBuilder.append("Level: ").append(level);
		statusBuilder.append(" \t Lines: ").append(lines);
		statusBuilder.append(" \t Score: ").append(score);
		// Debug
		// statusBuilder.append(" (").append(curX).append(",")
		// .append(curY).append(",")
		// .append(fallingPiece).append(")");

		statusbar.setText(statusBuilder.toString());
	}

	// #########################################################################
	/**
	 * Action to pause the {@link Tetrion}.
	 * 
	 * @author Mathieu Brunot
	 */
	protected class PauseAction extends AbstractAction {

		/**
		 * Default Serial Version ID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates an {@code PauseAction}.
		 */
		public PauseAction() {
		}

		/**
		 * Creates an {@code PauseAction} with the specified name.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 */
		public PauseAction(final String name) {
			super(name);
		}

		/**
		 * Creates an {@code PauseAction} with the specified name and small
		 * icon.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 * @param icon
		 *            the small icon ({@code Action.SMALL_ICON}) for the action;
		 *            a value of {@code null} is ignored
		 */
		public PauseAction(final String name, final Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (!started) {
				Tetrion.this.start();
			} else {
				Tetrion.this.pause();
			}
		}
	}

	/**
	 * Action to hold a piece.
	 * 
	 * @see Tetrion#holdPiece()
	 * @author Mathieu Brunot
	 */
	protected class HoldAction extends AbstractAction {

		/**
		 * Default Serial Version ID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates an {@code HoldAction}.
		 */
		public HoldAction() {
		}

		/**
		 * Creates an {@code HoldAction} with the specified name.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 */
		public HoldAction(final String name) {
			super(name);
		}

		/**
		 * Creates an {@code HoldAction} with the specified name and small icon.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 * @param icon
		 *            the small icon ({@code Action.SMALL_ICON}) for the action;
		 *            a value of {@code null} is ignored
		 */
		public HoldAction(final String name, final Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (isStarted() && !isPaused()) {
				Tetrion.this.holdPiece();
			}
		}
	}

	/**
	 * Action to shift the falling piece to the left.
	 * 
	 * @see Tetrion#setShiftingLeft(boolean)
	 * @see Tetrion#isShiftingLeft()
	 * @see Tetrion#tryMove(Tetromino, int, int)
	 * @author Mathieu Brunot
	 */
	protected class ShiftLeftAction extends AbstractAction {

		/**
		 * Default Serial Version ID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates an {@code ShiftLeftAction}.
		 */
		public ShiftLeftAction() {
		}

		/**
		 * Creates an {@code ShiftLeftAction} with the specified name.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 */
		public ShiftLeftAction(final String name) {
			super(name);
		}

		/**
		 * Creates an {@code ShiftLeftAction} with the specified name and small
		 * icon.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 * @param icon
		 *            the small icon ({@code Action.SMALL_ICON}) for the action;
		 *            a value of {@code null} is ignored
		 */
		public ShiftLeftAction(final String name, final Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (isShiftingLeft()) {
				return;
			}
			setShiftingLeft(true);
			if (!started || fallingPiece.getShape() == null || paused) {
				return;
			}
			Tetrion.this.tryMove(Tetrion.this.fallingPiece, 
					Tetrion.this.curX - 1, Tetrion.this.curY);
		}
	}

	/**
	 * Action to stop shifting the falling piece to the left.
	 * 
	 * @see Tetrion#setShiftingLeft(boolean)
	 * @see Tetrion#isShiftingLeft()
	 * @see Tetrion#tryMove(Tetromino, int, int)
	 * @see ShiftLeftAction
	 * @author Mathieu Brunot
	 */
	protected class ReleaseShiftLeftAction extends AbstractAction {

		/**
		 * Default Serial Version ID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates an {@code ReleaseShiftLeftAction}.
		 */
		public ReleaseShiftLeftAction() {
		}

		/**
		 * Creates an {@code ReleaseShiftLeftAction} with the specified name.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 */
		public ReleaseShiftLeftAction(final String name) {
			super(name);
		}

		/**
		 * Creates an {@code ReleaseShiftLeftAction} with the specified name and
		 * small icon.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 * @param icon
		 *            the small icon ({@code Action.SMALL_ICON}) for the action;
		 *            a value of {@code null} is ignored
		 */
		public ReleaseShiftLeftAction(final String name, final Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			setShiftingLeft(false);
		}
	}

	/**
	 * Action to shift the falling piece to the right.
	 * 
	 * @see Tetrion#setShiftingRight(boolean)
	 * @see Tetrion#isShiftingRight()
	 * @see Tetrion#tryMove(Tetromino, int, int)
	 * @author Mathieu Brunot
	 */
	protected class ShiftRightAction extends AbstractAction {

		/**
		 * Default Serial Version ID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates an {@code ShiftRightAction}.
		 */
		public ShiftRightAction() {
		}

		/**
		 * Creates an {@code ShiftRightAction} with the specified name.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 */
		public ShiftRightAction(final String name) {
			super(name);
		}

		/**
		 * Creates an {@code ShiftRightAction} with the specified name and small
		 * icon.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 * @param icon
		 *            the small icon ({@code Action.SMALL_ICON}) for the action;
		 *            a value of {@code null} is ignored
		 */
		public ShiftRightAction(final String name, final Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (isShiftingRight()) {
				return;
			}
			setShiftingRight(true);
			if (!started || fallingPiece.getShape() == null || paused) {
				return;
			}
			Tetrion.this.tryMove(Tetrion.this.fallingPiece, 
					Tetrion.this.curX + 1, Tetrion.this.curY);
		}
	}

	/**
	 * Action to stop shifting the falling piece to the right.
	 * 
	 * @see Tetrion#setShiftingRight(boolean)
	 * @see Tetrion#isShiftingRight()
	 * @see Tetrion#tryMove(Tetromino, int, int)
	 * @see ShiftRightAction
	 * @author Mathieu Brunot
	 */
	protected class ReleaseShiftRightAction extends AbstractAction {

		/**
		 * Default Serial Version ID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates an {@code ReleaseShiftRightAction}.
		 */
		public ReleaseShiftRightAction() {
		}

		/**
		 * Creates an {@code ReleaseShiftRightAction} with the specified name.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 */
		public ReleaseShiftRightAction(final String name) {
			super(name);
		}

		/**
		 * Creates an {@code ReleaseShiftRightAction} with the specified name
		 * and small icon.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 * @param icon
		 *            the small icon ({@code Action.SMALL_ICON}) for the action;
		 *            a value of {@code null} is ignored
		 */
		public ReleaseShiftRightAction(final String name, final Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			setShiftingRight(false);
		}
	}

	/**
	 * Action to softly drop the falling piece.
	 * 
	 * @see Tetrion#setDroppingSoft(boolean)
	 * @see Tetrion#isDroppingSoft()
	 * @author Mathieu Brunot
	 */
	protected class DropSoftAction extends AbstractAction {

		/**
		 * Default Serial Version ID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates an {@code DropSoftAction}.
		 */
		public DropSoftAction() {
		}

		/**
		 * Creates an {@code DropSoftAction} with the specified name.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 */
		public DropSoftAction(final String name) {
			super(name);
		}

		/**
		 * Creates an {@code DropSoftAction} with the specified name and small
		 * icon.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 * @param icon
		 *            the small icon ({@code Action.SMALL_ICON}) for the action;
		 *            a value of {@code null} is ignored
		 */
		public DropSoftAction(final String name, final Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (isDroppingSoft()) {
				return;
			}
			setDroppingSoft(true);
			if (!started || fallingPiece.getShape() == null || paused) {
				return;
			}
			Tetrion.this.framesSinceLastDrop = 0;
			Tetrion.this.framesContinouslySoftDropped = 0;
		}
	}

	/**
	 * Action to stop softly dropping the falling piece.
	 * 
	 * @see Tetrion#setDroppingSoft(boolean)
	 * @see DropSoftAction
	 * @author Mathieu Brunot
	 */
	protected class ReleaseDropSoftAction extends AbstractAction {

		/**
		 * Default Serial Version ID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates an {@code ReleaseDropSoftAction}.
		 */
		public ReleaseDropSoftAction() {
		}

		/**
		 * Creates an {@code ReleaseDropSoftAction} with the specified name.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 */
		public ReleaseDropSoftAction(final String name) {
			super(name);
		}

		/**
		 * Creates an {@code ReleaseDropSoftAction} with the specified name and
		 * small icon.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 * @param icon
		 *            the small icon ({@code Action.SMALL_ICON}) for the action;
		 *            a value of {@code null} is ignored
		 */
		public ReleaseDropSoftAction(final String name, final Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			Tetrion.this.setDroppingSoft(false);
		}
	}

	/**
	 * Action to hard drop the falling piece.
	 * 
	 * @see Tetrion#setDroppingHard(boolean)
	 * @see Tetrion#isDroppingHard()
	 * @see Tetrion#hardDrop()
	 * @author Mathieu Brunot
	 */
	protected class DropHardAction extends AbstractAction {

		/**
		 * Default Serial Version ID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates an {@code DropHardAction}.
		 */
		public DropHardAction() {
		}

		/**
		 * Creates an {@code DropHardAction} with the specified name.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 */
		public DropHardAction(final String name) {
			super(name);
		}

		/**
		 * Creates an {@code DropHardAction} with the specified name and small
		 * icon.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 * @param icon
		 *            the small icon ({@code Action.SMALL_ICON}) for the action;
		 *            a value of {@code null} is ignored
		 */
		public DropHardAction(final String name, final Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (isDroppingHard()) {
				return;
			}
			setDroppingHard(true);
			if (!started || fallingPiece.getShape() == null || paused) {
				return;
			}
			Tetrion.this.hardDrop();
		}
	}

	/**
	 * Action to stop hard dropping the falling piece.
	 * 
	 * @see Tetrion#setDroppingHard(boolean)
	 * @see DropSoftAction
	 * @author Mathieu Brunot
	 */
	protected class ReleaseDropHardAction extends AbstractAction {

		/**
		 * Default Serial Version ID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates an {@code ReleaseDropHardAction}.
		 */
		public ReleaseDropHardAction() {
		}

		/**
		 * Creates an {@code ReleaseDropHardAction} with the specified name.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 */
		public ReleaseDropHardAction(final String name) {
			super(name);
		}

		/**
		 * Creates an {@code ReleaseDropHardAction} with the specified name and
		 * small icon.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 * @param icon
		 *            the small icon ({@code Action.SMALL_ICON}) for the action;
		 *            a value of {@code null} is ignored
		 */
		public ReleaseDropHardAction(final String name, final Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			setDroppingHard(false);
		}
	}

	/**
	 * Action to rotate the falling piece to the left.
	 * 
	 * @see Tetrion#setRotatingLeft(boolean)
	 * @see Tetrion#isRotatingLeft()
	 * @author Mathieu Brunot
	 */
	protected class RotateLeftAction extends AbstractAction {

		/**
		 * Default Serial Version ID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates an {@code RotateLeftAction}.
		 */
		public RotateLeftAction() {
		}

		/**
		 * Creates an {@code RotateLeftAction} with the specified name.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 */
		public RotateLeftAction(final String name) {
			super(name);
		}

		/**
		 * Creates an {@code RotateLeftAction} with the specified name and small
		 * icon.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 * @param icon
		 *            the small icon ({@code Action.SMALL_ICON}) for the action;
		 *            a value of {@code null} is ignored
		 */
		public RotateLeftAction(final String name, final Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (isRotatingLeft()) {
				return;
			}
			setRotatingLeft(true);
		}
	}

	/**
	 * Action to stop rotating the falling piece to the left.
	 * 
	 * @see Tetrion#setRotatingLeft(boolean)
	 * @see RotateLeftAction
	 * @author Mathieu Brunot
	 */
	protected class ReleaseRotateLeftAction extends AbstractAction {

		/**
		 * Default Serial Version ID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates an {@code ReleaseRotateLeftAction}.
		 */
		public ReleaseRotateLeftAction() {
		}

		/**
		 * Creates an {@code ReleaseRotateLeftAction} with the specified name.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 */
		public ReleaseRotateLeftAction(final String name) {
			super(name);
		}

		/**
		 * Creates an {@code ReleaseRotateLeftAction} with the specified name
		 * and small icon.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 * @param icon
		 *            the small icon ({@code Action.SMALL_ICON}) for the action;
		 *            a value of {@code null} is ignored
		 */
		public ReleaseRotateLeftAction(final String name, final Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			setRotatingLeft(false);
		}
	}

	/**
	 * Action to rotate the falling piece to the right.
	 * 
	 * @see Tetrion#setRotatingRight(boolean)
	 * @see Tetrion#isRotatingRight()
	 * @author Mathieu Brunot
	 */
	protected class RotateRightAction extends AbstractAction {

		/**
		 * Default Serial Version ID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates an {@code RotateRightAction}.
		 */
		public RotateRightAction() {
		}

		/**
		 * Creates an {@code RotateRightAction} with the specified name.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 */
		public RotateRightAction(final String name) {
			super(name);
		}

		/**
		 * Creates an {@code RotateRightAction} with the specified name and
		 * small icon.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 * @param icon
		 *            the small icon ({@code Action.SMALL_ICON}) for the action;
		 *            a value of {@code null} is ignored
		 */
		public RotateRightAction(final String name, final Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (isRotatingRight()) {
				return;
			}
			setRotatingRight(true);
		}
	}

	/**
	 * Action to stop rotating the falling piece to the right.
	 * 
	 * @see Tetrion#setRotatingRight(boolean)
	 * @see RotateRightAction
	 * @author Mathieu Brunot
	 */
	protected class ReleaseRotateRightAction extends AbstractAction {

		/**
		 * Default Serial Version ID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates an {@code ReleaseRotateRightAction}.
		 */
		public ReleaseRotateRightAction() {
		}

		/**
		 * Creates an {@code ReleaseRotateRightAction} with the specified name.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 */
		public ReleaseRotateRightAction(final String name) {
			super(name);
		}

		/**
		 * Creates an {@code ReleaseRotateRightAction} with the specified name
		 * and small icon.
		 *
		 * @param name
		 *            the name ({@code Action.NAME}) for the action; a value of
		 *            {@code null} is ignored
		 * @param icon
		 *            the small icon ({@code Action.SMALL_ICON}) for the action;
		 *            a value of {@code null} is ignored
		 */
		public ReleaseRotateRightAction(final String name, final Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			setRotatingRight(false);
		}
	}
}
