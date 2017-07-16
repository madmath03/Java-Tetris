package org.fun.game.tetris;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.fun.game.tetris.Tetromino.Shape.RandomGenerator;

/**
 * A Tetromino for the Tetris game.
 *
 * <p>
 * <strong>Tetrominoes</strong>, occasionally known alternately as Tetrads, Blocks, or Tetriminoes,
 * are the blocks used in every known Tetris game. They come in seven shapes, all of which can be
 * rotated and then dropped.
 * </p>
 *
 * <p>
 * A tetromino, is a polyomino made of four square blocks. The seven one-sided tetrominoes are:
 * </p>
 * <ul>
 * <li>{@link Shape#I}: I-shape or Line-shape;</li>
 * <li>{@link Shape#J}: J-shape or MirroredL-shape;</li>
 * <li>{@link Shape#L}: L-shape;</li>
 * <li>{@link Shape#O}: O-shape or Square-shape;</li>
 * <li>{@link Shape#S}: S-shape;</li>
 * <li>{@link Shape#T}: T-shape;</li>
 * <li>{@link Shape#Z}: Z-shape.</li>
 * </ul>
 * 
 * <p>
 * This class is based on <em>Jan Bodnar</em>'s
 * <a href="http://zetcode.com/tutorials/javagamestutorial/tetris/">Tetris game clone in Java
 * Swing</a>.
 * </p>
 *
 * @author Jan Bodnar
 * @author Mathieu Brunot
 *
 * @see <a href="http://tetris.wikia.com/wiki/Tetromino">Tetromino</a>
 * @see <a href="http://tetris.wikia.com/wiki/TGM_legend">TGM legend</a>
 * @see <a href="http://zetcode.com/tutorials/javagamestutorial/tetris/">Tetris game clone in Java
 *      Swing</a>
 */
public class Tetromino {

  /**
   * Number of blocks by Tetromino.
   */
  static final int BLOCKS = 4;
  /**
   * Number of dimensions for Tetromino.
   */
  static final int DIMENSIONS = 2;

  /**
   * Tetromino shape.
   *
   * @see <a href="http://tetris.wikia.com/wiki/SRS">Super Rotation System</a>
   * @see <a href="http://tetris.wikia.com/wiki/Tetris_Guideline">Tetris Guideline</a>
   */
  public enum Shape {
    /**
     * I-shape or Line-shape.
     */
    I(new Color(102, 204, 204), new int[][] {{2, 0}, {1, 0}, {0, 0}, {-1, 0}}),
    /**
     * J-shape or MirroredL-shape.
     */
    J(new Color(218, 170, 0), new int[][] {{1, -1}, {1, 0}, {0, 0}, {-1, 0}}),
    /**
     * L-shape.
     */
    L(new Color(102, 102, 204), new int[][] {{1, 0}, {0, 0}, {-1, 0}, {-1, -1}}),
    /**
     * O-shape or Square-shape.
     */
    O(new Color(204, 204, 102), new int[][] {{0, 0}, {1, 0}, {1, 1}, {0, 1}}),
    /**
     * S-shape.
     */
    S(new Color(102, 204, 102), new int[][] {{1, -1}, {0, -1}, {0, 0}, {-1, 0}}),
    /**
     * T-shape.
     */
    T(new Color(204, 102, 204), new int[][] {{1, 0}, {0, 0}, {-1, 0}, {0, -1}}),
    /**
     * Z-shape.
     */
    Z(new Color(204, 102, 102), new int[][] {{1, 0}, {0, 0}, {0, -1}, {-1, -1}});

    /**
     * The <em>Random Generator</em> is the algorithm used to generate the sequence of tetrominoes
     * in Tetris brand games that follow the Tetris Guideline.
     *
     * <p>
     * Random Generator generates a sequence of all seven one-sided tetrominoes ({@link Shape#I},
     * {@link Shape#J}, {@link Shape#L}, {@link Shape#O}, {@link Shape#S}, {@link Shape#T},
     * {@link Shape#Z}) permuted randomly, as if they were drawn from a bag. Then it deals all seven
     * tetrominoes to the piece sequence before generating another bag. There are 7!, or 5,040,
     * permutations of seven elements.
     * </p>
     *
     * @see <a href="http://tetris.wikia.com/wiki/Random_Generator">Random Generator</a>
     */
    public static class RandomGenerator {

      /**
       * All shapes available to the generator.
       */
      private static final List<Shape> ALL_SHAPES =
          Arrays.asList(Shape.values());

      /**
       * Random Generator's current bag of shapes.
       */
      private final LinkedList<Shape> bag = new LinkedList<>();

      /**
       * Private constructor.
       */
      public RandomGenerator() {}

      /**
       * Fill the Random Generator's current bag with shapes.
       *
       * @return Random Generator's current bag of shapes
       */
      private LinkedList<Shape> fillBag() {
        bag.addAll(ALL_SHAPES);
        Collections.shuffle(bag);
        return bag;
      }

      /**
       * Get next shape from Random Generator.
       *
       * @return a random shape
       */
      public final Shape nextShape() {
        if (bag.isEmpty()) {
          fillBag();
        }
        return bag.removeFirst();
      }
    }

    /**
     * Random Tetromino index generator.
     */
    private static final RandomGenerator RANDOM_GENERATOR =
        new RandomGenerator();

    /**
     * Get next shape from <em>Random Generator</em>.
     *
     * @return a random shape
     * @see Shape
     * @see RandomGenerator
     */
    public static Shape getRandomShape() {
      return RANDOM_GENERATOR.nextShape();
    }

    /**
     * A Tetromino default color.
     */
    private final Color color;
    /**
     * A Tetromino default color when active (i.e. currently falling).
     */
    private final Color activeColor;
    /**
     * A Tetromino default shadow color (i.e. its Ghost piece).
     * 
     * @see <a href="http://tetris.wikia.com/wiki/Ghost_piece">Ghost piece</a>
     */
    private final Color shadowColor;
    /**
     * A Tetromino default coordinates.
     */
    private final int[][] defaultCoords;

    /**
     * Construct a Tetromino.
     * 
     * @param mainColor default color
     * @param initialCoords default coordinates
     */
    Shape(final Color mainColor, final int[][] initialCoords) {
      this.color = mainColor;
      this.activeColor = mainColor.brighter();
      this.shadowColor = mainColor.darker().darker().darker();
      this.defaultCoords = initialCoords;
    }

    /**
     * A Tetromino default color.
     * 
     * @return default color
     */
    public Color getColor() {
      return color;
    }

    /**
     * A Tetromino default color when active (i.e. currently falling).
     * 
     * @return default color when active (i.e. currently falling)
     */
    public Color getActiveColor() {
      return activeColor;
    }

    /**
     * A Tetromino default shadow color (i.e. its Ghost piece).
     * 
     * @see <a href="http://tetris.wikia.com/wiki/Ghost_piece">Ghost piece</a>
     * 
     * @return default shadow color (i.e. its Ghost piece)
     */
    public Color getShadowColor() {
      return shadowColor;
    }

    /**
     * A Tetromino default coordinates.
     * 
     * @return default coordinates
     */
    public int[][] getDefaultCoords() {
      return defaultCoords;
    }

    /**
     * Get <em>x</em> coordinate of the specified block.
     * 
     * @param block the block index
     * @return <em>x</em> coordinate of the specified block
     */
    public final int x(final int block) {
      return defaultCoords[block][0];
    }

    /**
     * Get <em>y</em> coordinate of the specified block.
     * 
     * @param block the block index
     * @return <em>y</em> coordinate of the specified block
     */
    public final int y(final int block) {
      return defaultCoords[block][1];
    }
  };

  /**
   * The Tetromino's shape.
   */
  private Shape pieceShape;
  /**
   * The blocks coordinates.
   */
  private final int[][] blocks;

  /**
   * Default constructor.
   */
  Tetromino() {
    this.blocks = new int[BLOCKS][DIMENSIONS];
    this.pieceShape = null;
  }

  /**
   * Copy constructor.
   * 
   * @param another another Tetromino to copy
   * @throws IllegalArgumentException if the other Tetromino is {@code null}
   */
  Tetromino(final Tetromino another) {
    if (another == null) {
      throw new IllegalArgumentException("Tetromino to copy cannot be null");
    }
    this.blocks = new int[BLOCKS][DIMENSIONS];
    this.pieceShape = another.getShape();
  }

  /**
   * Set the Tetromino's shape.
   * 
   * <p>
   * The Tetromino's blocks coordinates are set according the shape's default coordinates.
   * </p>
   * 
   * @see #blocks
   * @see #pieceShape
   * @see Shape#getDefaultCoords()
   * 
   * @param shape the new Tetromino's shape. If {@code null}, then all blocks are set to origin.
   */
  public final void setShape(final Shape shape) {
    if (shape != null) {
      for (int i = 0; i < BLOCKS; i++) {
        for (int j = 0; j < DIMENSIONS; ++j) {
          blocks[i][j] = shape.getDefaultCoords()[i][j];
        }
      }
    } else {
      for (int i = 0; i < BLOCKS; i++) {
        for (int j = 0; j < DIMENSIONS; ++j) {
          blocks[i][j] = 0;
        }
      }
    }
    this.pieceShape = shape;
  }

  /**
   * Set {@code X} coordinate of given block index.
   * 
   * @param block a given block index
   * @param x the new {@code X} coordinate of given block index
   */
  protected final void setX(final int block, final int x) {
    setCoord(block, 0, x);
  }

  /**
   * Set {@code Y} coordinate of given block index.
   * 
   * @param block a given block index
   * @param y the new {@code Y} coordinate of given block index
   */
  protected final void setY(final int block, final int y) {
    setCoord(block, 1, y);
  }

  /**
   * Set a given dimension's coordinate for a given block index.
   * 
   * @param block a given block index
   * @param dimension a given dimension's index
   * @param position the new dimension's coordinate of given block index
   */
  private void setCoord(final int block, final int dimension,
      final int position) {
    blocks[block][dimension] = position;
  }

  /**
   * Get {@code X} coordinate of given block index.
   * 
   * @param block a given block index
   * @return {@code X} coordinate of given block index
   */
  public final int x(final int block) {
    return coord(block, 0);
  }

  /**
   * Get {@code Y} coordinate of given block index.
   * 
   * @param block a given block index
   * @return {@code Y} coordinate of given block index
   */
  public final int y(final int block) {
    return coord(block, 1);
  }

  /**
   * Get a given dimension's coordinate for a given block index.
   * 
   * @param block a given block index
   * @param dimension a given dimension's index
   * @return a given dimension's coordinate for a given block index
   */
  private int coord(final int block, final int dimension) {
    return blocks[block][dimension];
  }

  /**
   * Get the Tetromino's shape.
   * 
   * @return the Tetromino's shape.
   */
  public final Shape getShape() {
    return pieceShape;
  }

  /**
   * Change the Tetromino's shape by a random shape.
   * 
   * @see Shape#getRandomShape()
   * @see RandomGenerator
   */
  public final void setRandomShape() {
    setShape(Shape.getRandomShape());
  }

  /**
   * Get array of minimum coordinate for each dimension.
   * 
   * @return an array of minimum coordinate for each dimension
   */
  public final int[] getMin() {
    int[] mins = new int[DIMENSIONS];
    Arrays.fill(mins, Integer.MAX_VALUE);
    for (int[] block : blocks) {
      for (int dimension = 0; dimension < DIMENSIONS; dimension++) {
        mins[dimension] = Math.min(mins[dimension], block[dimension]);
      }
    }
    return mins;
  }

  /**
   * Get minimum {@code X} coordinate.
   * 
   * @return {@code X} minimum coordinate
   */
  public final int minX() {
    int m = Integer.MAX_VALUE;
    for (int[] block : blocks) {
      m = Math.min(m, block[0]);
    }
    return m;
  }

  /**
   * Get minimum {@code Y} coordinate.
   * 
   * @return {@code Y} minimum coordinate
   */
  public final int minY() {
    int m = Integer.MAX_VALUE;
    for (int[] block : blocks) {
      m = Math.min(m, block[1]);
    }
    return m;
  }

  /**
   * Get array of maximum coordinate for each dimension.
   * 
   * @return an array of maximum coordinate for each dimension
   */
  public final int[] getMax() {
    int[] maxs = new int[DIMENSIONS];
    Arrays.fill(maxs, Integer.MIN_VALUE);
    for (int[] block : blocks) {
      for (int dimension = 0; dimension < DIMENSIONS; dimension++) {
        maxs[dimension] = Math.max(maxs[dimension], block[dimension]);
      }
    }
    return maxs;
  }

  /**
   * Get maximum {@code X} coordinate.
   * 
   * @return {@code X} maximum coordinate
   */
  public final int maxX() {
    int m = Integer.MIN_VALUE;
    for (int[] block : blocks) {
      m = Math.max(m, block[0]);
    }
    return m;
  }

  /**
   * Get maximum {@code Y} coordinate.
   * 
   * @return {@code Y} maximum coordinate
   */
  public final int maxY() {
    int m = Integer.MIN_VALUE;
    for (int[] block : blocks) {
      m = Math.max(m, block[1]);
    }
    return m;
  }

  /**
   * Get array of sizes for each dimension.
   * 
   * @return an array of sizes for each dimension
   */
  public final int[] getSizes() {
    int[] mins = new int[DIMENSIONS];
    int[] maxs = new int[DIMENSIONS];
    Arrays.fill(mins, Integer.MAX_VALUE);
    Arrays.fill(maxs, Integer.MIN_VALUE);
    for (int[] block : blocks) {
      for (int dimension = 0; dimension < DIMENSIONS; dimension++) {
        mins[dimension] = Math.min(mins[dimension], block[dimension]);
        maxs[dimension] = Math.max(maxs[dimension], block[dimension]);
      }
    }

    int[] sizes = new int[DIMENSIONS];
    for (int dimension = 0; dimension < DIMENSIONS; dimension++) {
      sizes[dimension] = maxs[dimension] - (mins[dimension] - 1);
    }
    return sizes;
  }

  /**
   * Get Tetromino's width.
   * 
   * @return Tetromino's width.
   */
  public final int getWidth() {
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    for (int[] block : blocks) {
      min = Math.min(min, block[0]);
      max = Math.max(max, block[0]);
    }
    return max - (min - 1);
  }

  /**
   * Get Tetromino's height.
   * 
   * @return Tetromino's height.
   */
  public final int getHeight() {
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    for (int[] block : blocks) {
      min = Math.min(min, block[1]);
      max = Math.max(max, block[1]);
    }
    return max - (min - 1);
  }

  /**
   * Rotate the Tetromino 90° counter clockwise.
   * 
   * @return the Tetromino rotated
   */
  public final Tetromino rotateLeft() {
    if (this.pieceShape == Shape.O) {
      return this;
    }

    Tetromino result = new Tetromino();
    result.pieceShape = this.pieceShape;

    for (int i = 0; i < BLOCKS; ++i) {
      result.setX(i, y(i));
      result.setY(i, -x(i));
    }

    return result;
  }

  /**
   * Rotate the Tetromino 90° clockwise.
   * 
   * @return the Tetromino rotated
   */
  public final Tetromino rotateRight() {
    if (this.pieceShape == Shape.O) {
      return this;
    }

    Tetromino result = new Tetromino();
    result.pieceShape = this.pieceShape;

    for (int i = 0; i < BLOCKS; ++i) {
      result.setX(i, -y(i));
      result.setY(i, x(i));
    }

    return result;
  }

  @Override
  public final String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{").append(pieceShape).append(",");
    sb.append("[");
    for (int[] block : blocks) {
      sb.append(Arrays.toString(block));
    }
    sb.append("]");
    sb.append("}");
    return sb.toString();
  }

}
