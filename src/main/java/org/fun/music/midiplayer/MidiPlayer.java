package org.fun.music.midiplayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

/**
 * A MIDI player.
 * 
 * <p>
 * This class encapsulate all the methods necessary to open a MIDI Sequencer, play songs (one at a
 * time) and manage a playlist.
 * </p>
 * 
 * <p>
 * A thread is used to navigate through the playlist when reaching the end of a song or when user
 * requests a change.
 * </p>
 * 
 * @see MidiThread
 *
 * @see <a href=
 *      "https://www.midi.org/specifications/category/complete-midi-1-0-detailed-specification">The
 *      Official MIDI Specifications </a>
 * @see <a href=
 *      "http://www.java2s.com/Code/Java/Development-Class/AnexamplethatplaysaMidisequence.htm">An
 *      example that plays a MIDI sequence</a>
 *
 * @author Mathieu Brunot
 */
public class MidiPlayer implements MetaEventListener, AutoCloseable {

  /**
   * Logger.
   */
  private static final Logger LOGGER =
      Logger.getLogger(MidiPlayer.class.getName());

  /**
   * MIDI meta event for end of track.
   */
  protected static final int END_OF_TRACK_MESSAGE = 47;

  /**
   * MIDI player single instance.
   */
  private static MidiPlayer instance = null;

  /**
   * Get MIDI player single instance.
   * 
   * @return MIDI player single instance
   */
  public static MidiPlayer getInstance() {
    if (instance == null) {
      instance = new MidiPlayer();
    }
    return instance;
  }

  // #########################################################################
  /**
   * Song resource natural comparator.
   */
  public static final class NaturalComparator implements Comparator<Object> {

    @SuppressWarnings("unchecked")
    @Override
    public int compare(final Object o1, final Object o2) {
      if (o1 == null) {
        if (o2 == null) {
          return 0;
        } else {
          return 1;
        }
      } else if (o2 == null) {
        return -1;
      } else if (o1 instanceof Comparable) {
        return ((Comparable<Object>) o1).compareTo(o2);
      } else if (o1 instanceof Comparable) {
        return ((Comparable<Object>) o1).compareTo(o2);
      } else {
        return o1.toString().compareTo(o2.toString());
      }
    }

  }

  /**
   * Default comparator for playlist resources.
   */
  public static final NaturalComparator COMPARATOR = new NaturalComparator();

  /**
   * Reverse default comparator for playlist resources.
   */
  public static final Comparator<Object> REVERSE_COMPARATOR =
      Collections.reverseOrder(COMPARATOR);

  // #########################################################################
  /**
   * Print on the standard output a string describing a song's resource.
   * 
   * @param songResource the song resource
   * @param song the song's MIDI sequence
   */
  protected static void printSongInfo(final Object songResource,
      final Sequence song) {
    printSongInfo(System.out, songResource, song);
  }

  /**
   * Print on the specified print stream a string describing a song's resource.
   * 
   * @param ps the print stream
   * @param songResource the song resource
   * @param song the song's MIDI sequence
   */
  protected static void printSongInfo(final PrintStream ps,
      final Object songResource, final Sequence song) {
    String songInfo;
    if (songResource instanceof Path) {
      songInfo = getSongInfo((Path) songResource, song);
    } else if (songResource instanceof File) {
      songInfo = getSongInfo((File) songResource, song);
    } else if (songResource instanceof URL) {
      songInfo = getSongInfo((URL) songResource, song);
    } else if (songResource instanceof InputStream) {
      songInfo = getSongInfo((InputStream) songResource, song);
    } else {
      songInfo = getSongInfo(songResource, song);
    }
    ps.println(songInfo);
  }

  /**
   * Get a string describing a song path.
   * 
   * @param songResource the song's path
   * @param song the song's MIDI sequence
   * @return a string describing a song path.
   */
  public static String getSongInfo(final Path songResource,
      final Sequence song) {
    // Get file name
    String fileName = songResource.getFileName().toString();

    return getSongInfo(fileName, song);
  }

  /**
   * Get a string describing a song file.
   * 
   * @param songResource the song's file
   * @param song the song's MIDI sequence
   * @return a string describing a song file.
   */
  public static String getSongInfo(final File songResource,
      final Sequence song) {
    // Get file name
    String fileName = songResource.getName();

    return getSongInfo(fileName, song);
  }

  /**
   * Get a string describing a song URL.
   * 
   * @param songResource the song's URL
   * @param song the song's MIDI sequence
   * @return a string describing a song URL.
   */
  public static String getSongInfo(final URL songResource,
      final Sequence song) {
    // Get file name
    String url = songResource.toString();
    String fileName = url.substring(url.lastIndexOf('/') + 1);

    return getSongInfo(fileName, song);
  }

  /**
   * Get a string describing a song's resource.
   * 
   * @param songResource the song resource
   * @param song the song's MIDI sequence
   * @return a string describing a song's resource.
   */
  public static String getSongInfo(final InputStream songResource,
      final Sequence song) {
    // Get file name
    String fileName = songResource.toString();

    return getSongInfo(fileName, song);
  }

  /**
   * Get a string describing a song's resource.
   * 
   * @param songResource the song resource
   * @param song the song's MIDI sequence
   * @return a string describing a song's resource.
   */
  public static String getSongInfo(final Object songResource,
      final Sequence song) {
    // Get file name
    String fileName = songResource.toString();

    return getSongInfo(fileName, song);
  }

  /**
   * Number of microseconds in a second.
   */
  private static final long MS_IN_MICROSECONDS = 1000;
  /**
   * Number of milliseconds in a second.
   */
  private static final long SECOND_IN_MS = 1000;
  /**
   * Number of seconds in a minute.
   */
  private static final long MINUTE_IN_S = 60;
  /**
   * Number of milliseconds in a minute.
   */
  private static final long MINUTE_IN_MS = SECOND_IN_MS * MINUTE_IN_S;

  /**
   * Get a string describing a song's resource.
   * 
   * @param fileName the song's file name
   * @param song the song's MIDI sequence
   * @return a string describing a song's resource.
   */
  private static String getSongInfo(final String fileName,
      final Sequence song) {
    if (song == null) {
      return fileName;
    }
    // Get song length
    long ms = song.getMicrosecondLength() / MS_IN_MICROSECONDS;
    long min = ms / MINUTE_IN_MS;
    long s = (ms / SECOND_IN_MS) - (min * MINUTE_IN_S);

    return String.format("%s (%d:%02d)", fileName, min, s);
  }

  // #########################################################################
  /**
   * The playlist.
   */
  private final LinkedList<Object> playlist = new LinkedList<>();
  /**
   * The MIDI sequencer.
   * 
   * @see #getSequencer()
   */
  private transient Sequencer sequencer = null;
  /**
   * The current song index.
   * 
   * @see #getCurrentSongIndex()
   * @see #setCurrentSongIndex(int)
   */
  private int currentSongIndex = 0;
  /**
   * The current song resource.
   * 
   * <p>
   * A {@code null} value means that no song has been identified as currently playing.
   * </p>
   * 
   * @see #getCurrentSongIndex()
   */
  private transient Object currentSongResource = null;
  /**
   * The MIDI player thread.
   */
  private transient MidiThread playingThread = null;
  /**
   * Is the MIDI player looping when reaching the end of a song?
   * 
   * @see #isLooping()
   * @see #setLooping(boolean)
   */
  private boolean songLooping = false;
  /**
   * Is the MIDI player looping when reaching the end of the playlist?
   * 
   * @see #isPlaylistLooping()
   * @see #setPlaylistLooping(boolean)
   */
  private boolean playlistLooping = false;

  /**
   * Hidden constructor.
   */
  protected MidiPlayer() {}

  // #########################################################################
  /**
   * Obtains the default MIDI {@code Sequencer}, connected to a default device.
   * 
   * @return the default MIDI sequencer
   */
  public final Sequencer getSequencer() {
    if (sequencer == null) {
      // Retrieve MIDI Sequencer
      try {
        sequencer = MidiSystem.getSequencer();
        System.out.println("MIDI Sequencer information:");
        System.out.println("\t" + sequencer.getDeviceInfo().getName());
        System.out.println("\t" + sequencer.getDeviceInfo().getDescription());
        System.out.println("\t" + sequencer.getDeviceInfo().getVendor());
        System.out.println("\t" + sequencer.getDeviceInfo().getVersion());
        System.out.println("\t" + sequencer.getDeviceInfo().getClass());

        // Add listener to detect end of song
        sequencer.addMetaEventListener(this);

        // Opens the device, indicating that it should now acquire any
        // system resources it requires and become operational.
        sequencer.open();
        System.out.println("MIDI Sequencer opened and ready for usage.");

        setupLoopCount();

      } catch (MidiUnavailableException ex) {
        if (sequencer != null) {
          sequencer.close();
          sequencer = null;
        }
        LOGGER.log(Level.SEVERE, null, ex);
      }
    }

    return sequencer;
  }

  /**
   * Reset sequencer to {@code null}.
   */
  protected final void resetSequencer() {
    this.sequencer = null;
  }

  /**
   * Get an unmodifiable view of the playlist.
   * 
   * @return an unmodifiable view of the playlist.
   */
  public final List<Object> getPlaylist() {
    return Collections.unmodifiableList(playlist);
  }

  /**
   * Get the MIDI player thread.
   * 
   * @return the MIDI player thread
   */
  protected final MidiThread getPlayingThread() {
    return playingThread;
  }

  /**
   * Get current song index in playlist.
   * 
   * @return current song index in playlist.
   */
  public final int getCurrentSongIndex() {
    return currentSongIndex;
  }

  /**
   * Set current song index in playlist.
   * 
   * @param songIndex new current song index in playlist
   */
  protected final void setCurrentSongIndex(final int songIndex) {
    this.currentSongIndex = songIndex;
  }

  /**
   * Is the MIDI player looping when reaching the end of a song?
   * 
   * @return {@code true} if the MIDI player will loop when reaching the end of a song
   */
  public final boolean isLooping() {
    return songLooping;
  }

  /**
   * Set if the MIDI player should be looping when reaching the end of a song.
   * 
   * @param looping the playlist looping status
   */
  public void setLooping(final boolean looping) {
    this.songLooping = looping;

    if (this.sequencer != null) {
      setupLoopCount();
    }
  }

  /**
   * Sets the number of repetitions of the loop for playback.
   * 
   * @see #isLooping()
   * @see #setLooping(boolean)
   */
  private void setupLoopCount() {
    if (this.songLooping) {
      // Loop until interrupted
      sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
    } else {
      // repeat 0 times (play once)
      sequencer.setLoopCount(0);
    }
  }

  /**
   * Is the MIDI player looping when reaching the end of the playlist?
   * 
   * @return {@code true} if the MIDI player will loop when reaching the end of the playlist
   */
  public final boolean isPlaylistLooping() {
    return playlistLooping;
  }

  /**
   * Set if the MIDI player should be looping when reaching the end of the playlist.
   * 
   * @param looping the playlist looping status
   */
  public void setPlaylistLooping(final boolean looping) {
    this.playlistLooping = looping;
  }

  // #########################################################################
  /**
   * Start playing.
   * 
   * @return {@code true} if the MIDI player actually started playing
   */
  public synchronized boolean startPlaying() {
    // If nothing to play, quit
    if (this.playlist == null || this.playlist.isEmpty()) {
      return false;
    }

    // Retrieve MIDI Sequencer
    Sequencer player = this.getSequencer();
    // If no MIDI Sequencer, quit
    if (player == null) {
      return false;
    }

    // If there already is a thread playing songs
    if (playingThread != null && playingThread.isAlive() && player.isOpen()) {
      // Set sequencer at the start of the song
      player.setMicrosecondPosition(0);
      // If the sequencer is not playing, replay current song
      if (!player.isRunning() && player.getSequence() != null) {
        player.start();
      }
      return true;
    }

    // Create a thread to play songs
    this.playingThread = createNewMidiThread();
    this.playingThread.start();

    return true;
  }

  /**
   * Create a new MIDI player thread to manage playlist and song changes.
   * 
   * @return a new MIDI player thread
   */
  protected synchronized MidiThread createNewMidiThread() {
    return this.new MidiThread("MidiPlayerThread");
  }

  /**
   * Start playing song at specified index.
   * 
   * @see #startPlaying()
   * @see #moveToSong(int)
   * 
   * @param index the index in the playlist to move current song position to
   * 
   * @return {@code true} if the MIDI player actually started playing
   */
  public final boolean startPlaying(final int index) {
    if (this.moveToSong(index)) {
      return this.startPlaying();
    } else {
      return false;
    }
  }

  /**
   * Start playing song for the specified resource.
   * 
   * @see #startPlaying()
   * @see #moveToSong(Object)
   * 
   * @param resource the resource in the playlist to move current song position to
   * 
   * @return {@code true} if the MIDI player actually started playing
   */
  public final boolean startPlaying(final Object resource) {
    if (this.moveToSong(resource)) {
      return this.startPlaying();
    } else {
      return false;
    }
  }

  /**
   * Add resources to the playlist and Start playing song for the current position.
   * 
   * @see #startPlaying()
   * @see #addAll(Collection)
   * 
   * @param resources the resources to add to the playlist
   * 
   * @return {@code true} if the MIDI player actually started playing
   */
  public final boolean startPlaying(
      final Collection<? extends Object> resources) {
    if (this.addAll(resources)) {
      return this.startPlaying();
    } else {
      return false;
    }
  }

  /**
   * Pause playing.
   * 
   * @return {@code true} if the MIDI player actually paused playing
   */
  public synchronized boolean pausePlaying() {
    // If no MIDI Sequencer, quit
    if (this.sequencer == null) {
      return false;
    }
    // Retrieve MIDI Sequencer
    Sequencer player = this.getSequencer();

    // If there is a thread playing songs
    if (playingThread != null && playingThread.isAlive() && player.isOpen()) {
      // If the sequencer is playing, stop current song
      if (player.isRunning()) {
        player.stop();
      } else if (player.getSequence() != null) {
        // If it is paused, restart playing where it paused
        player.start();
      }
    }

    return true;
  }

  /**
   * Stop playing.
   * 
   * @return {@code true} if the MIDI player actually stopped playing
   */
  public synchronized boolean stopPlaying() {
    // If no MIDI Sequencer, quit
    if (this.sequencer == null) {
      return false;
    }
    // Retrieve MIDI Sequencer
    Sequencer player = this.getSequencer();

    // If there is a thread playing songs
    if (playingThread != null && playingThread.isAlive() && player.isOpen()) {
      // If the sequencer is playing, stop current song
      player.stop();
      player.close();

      this.resetSequencer();
      this.notifyAll();
    }

    return true;
  }

  /**
   * Move player current song position to the previous index.
   * 
   * @return {@code true} if the MIDI player current song position changed
   */
  public synchronized boolean moveToPreviousSong() {
    // Decrement position by 1 in playlist
    if (this.currentSongIndex > 0) {
      this.setCurrentSongIndex(currentSongIndex - 1);
      this.notifyAll();
      return true;
    }
    return false;
  }

  /**
   * Move player current song position to the next index.
   * 
   * @return {@code true} if the MIDI player current song position changed
   */
  public final boolean moveToNextSong() {
    return moveToNextSong(false);
  }

  /**
   * Move player current song position to the next index.
   * 
   * @param force force the current song index increase, even if at the edge of playlist?
   * @return {@code true} if the MIDI player current song position changed
   */
  protected synchronized boolean moveToNextSong(final boolean force) {
    boolean moved = false;
    if (isPlaylistLooping()) {
      // Increment position by 1 (loop back to start if needed)
      this.setCurrentSongIndex((currentSongIndex + 1) % this.playlist.size());
      moved = true;
    } else if (force || this.currentSongIndex < this.playlist.size() - 1) {
      // Increment position by 1 in playlist (if still inside playlist)
      this.setCurrentSongIndex(currentSongIndex + 1);
      moved = true;
    }
    this.notifyAll();
    return moved;
  }

  /**
   * Move player current song position to specified index.
   * 
   * @param index the index in the playlist to move current song position to
   * @return {@code true} if the MIDI player current song position changed
   */
  public synchronized boolean moveToSong(final int index) {
    // Set position to index in playlist (if still inside playlist)
    if (index >= 0 && index < this.playlist.size()) {
      this.setCurrentSongIndex(index);
      this.notifyAll();
      return true;
    }
    return false;
  }

  /**
   * Move player current song position to specified resource.
   * 
   * @param resource the resource in the playlist to move current song position to
   * @return {@code true} if the MIDI player current song position changed
   */
  public final boolean moveToSong(final Object resource) {
    return moveToSong(this.playlist.indexOf(resource));
  }

  // #########################################################################
  /**
   * Is the MIDI player currently playing?
   * 
   * @return {@code true} if the MIDI player thread is alive and the sequence currently playing
   */
  public final boolean isPlaying() {
    // If no MIDI Sequencer, quit
    if (this.sequencer == null) {
      return false;
    }
    // Retrieve MIDI Sequencer
    Sequencer player = this.getSequencer();

    return playingThread != null && playingThread.isAlive()
        && player.getSequence() != null && player.isRunning();
  }

  /**
   * Is the MIDI player currently paused?
   * 
   * @return {@code true} if the MIDI player thread is alive and the sequencer currently open but
   *         <strong>not</strong> playing
   */
  public final boolean isPaused() {
    // If no MIDI Sequencer, quit
    if (this.sequencer == null) {
      return false;
    }
    // Retrieve MIDI Sequencer
    Sequencer player = this.getSequencer();

    return playingThread != null && playingThread.isAlive() && player.isOpen()
        && player.getSequence() != null && !player.isRunning();
  }

  /**
   * Is the MIDI player currently playing?
   * 
   * @return {@code true} if the MIDI player thread is not alive and the sequencer currently not
   *         open
   */
  public final boolean isStopped() {
    // If no MIDI Sequencer, quit
    if (this.sequencer == null || playingThread == null) {
      return true;
    }
    // Retrieve MIDI Sequencer
    Sequencer player = this.getSequencer();

    return !playingThread.isAlive() && !player.isOpen();
  }

  /**
   * Returns the current tempo factor for the sequencer.
   * 
   * <p>
   * The default is 1.0.
   * </p>
   *
   * @return tempo factor.
   * @see #setTempoFactor(float)
   */
  public final Float getTempoFactor() {
    // If no MIDI Sequencer, quit
    if (this.sequencer == null) {
      return null;
    }

    return this.sequencer.getTempoFactor();
  }

  /**
   * Scales the sequencer's actual playback tempo by the factor provided.
   * 
   * <p>
   * The default is 1.0. A value of 1.0 represents the natural rate (the tempo specified in the
   * sequence), 2.0 means twice as fast, etc.
   * </p>
   * 
   * <p>
   * Note that the tempo factor cannot be adjusted when external synchronization is used. In that
   * situation, <code>setTempoFactor</code> always sets the tempo factor to 1.0.
   * </p>
   *
   * @param tempoFactor the requested tempo scalar
   * @see #getTempoFactor
   */
  public void setTempoFactor(final float tempoFactor) {
    // If no MIDI Sequencer, quit
    if (this.sequencer == null) {
      return;
    }

    this.sequencer.setTempoFactor(tempoFactor);
  }

  /**
   * Obtains the current position in the sequence, expressed in microseconds.
   * 
   * @return the current position in microseconds
   * @see #setMicrosecondPosition
   */
  public Long getMicrosecondPosition() {
    // If no MIDI Sequencer, quit
    if (this.sequencer == null) {
      return null;
    }

    return this.sequencer.getMicrosecondPosition();
  }

  /**
   * Sets the current position in the sequence, expressed in microseconds.
   * 
   * @param microseconds desired position in microseconds
   * @see #getMicrosecondPosition
   */
  public final void setMicrosecondPosition(final long microseconds) {
    // If no MIDI Sequencer, quit
    if (this.sequencer == null) {
      return;
    }

    this.sequencer.setMicrosecondPosition(microseconds);
  }

  /**
   * Get the current song's resource.
   * 
   * @return the current song's MIDI resource, {@code null} if no song currently playing
   */
  protected final Object getCurrentSongResource() {
    if (this.currentSongIndex < 0
        || this.currentSongIndex >= this.playlist.size()) {
      return null;
    }
    this.currentSongResource = this.playlist.get(this.currentSongIndex);
    return this.currentSongResource;
  }

  /**
   * Is the given resource the current song's resource?
   * 
   * @param resource the given resource to compare with current song's resource
   * 
   * @return {@code true} if the given resource matches the current song
   */
  public final boolean isCurrentSong(final Object resource) {
    Object currentResource = getCurrentSongResource();
    if (currentResource != null) {
      return currentResource.equals(resource);
    } else {
      return resource == null;
    }
  }

  /**
   * Get the current song's MIDI sequence.
   * 
   * @return the current song's MIDI sequence, {@code null} if no song currently playing
   */
  protected final Sequence getCurrentSong() {
    if (isStopped()) {
      return null;
    }

    return getSequencer().getSequence();
  }

  /**
   * Print on the standard output a string describing the current song's resource.
   */
  public final void printCurrentSongInfo() {
    Sequence song = getCurrentSong();
    Object songResource = getCurrentSongResource();
    if (songResource == null || song == null) {
      return;
    }

    printSongInfo(songResource, song);
  }

  /**
   * Get a string describing the current song's resource.
   * 
   * @return a string describing the current song's resource, {@code null} if no current song
   */
  public final String getCurrentSongInfo() {
    Sequence song = getCurrentSong();
    Object songResource = getCurrentSongResource();
    if (songResource == null || song == null) {
      return null;
    }

    return getSongInfo(songResource, song);
  }

  // #########################################################################
  /**
   * Rotates the resources in the playlist between {@code start} and {@code end} indexes to the
   * specified {@code position} index.
   * 
   * @param start start index (inclusive) of the sublist of elements in the playlist to move
   * @param end end index (inclusive) of the sublist of elements in the playlist to move
   * @param position index of the destination for the sublist
   * @throws IllegalArgumentException if the {@code start} is greater than the {@code end} index
   * @return {@code true} if the elements were moved, {@code false} if the {@code position} is
   *         between {@code start} and {@code end}
   */
  public boolean moveSongsTo(final int start, final int end,
      final int position) {
    if (start > end) {
      throw new IllegalArgumentException("Start (" + start
          + ") must be lesser or equal to end (" + end + ") index.");
    }
    if (position >= start && position <= end) {
      return false;
    }

    // Rotation algorithm
    int distance = (1 + end - start);
    int rotationStart, rotationEnd;
    if (start >= position) {
      rotationStart = position;
      rotationEnd = end + 1;
    } else {
      distance *= -1;
      rotationStart = start;
      rotationEnd = position;
    }
    List<Object> subList = this.playlist.subList(rotationStart, rotationEnd);
    Collections.rotate(subList, distance);

    // Update current song index
    if (this.currentSongIndex >= rotationStart
        && this.currentSongIndex <= rotationEnd) {
      int offset;
      if (this.currentSongIndex >= start && this.currentSongIndex <= end) {
        int direction;
        if (this.currentSongIndex >= position) {
          direction = -1;
        } else {
          direction = 1;
        }
        offset = (rotationEnd - rotationStart - Math.abs(distance)) * direction;
      } else {
        offset = distance;
      }
      this.setCurrentSongIndex(this.currentSongIndex + offset);
    }

    return true;
  }

  /**
   * Randomly permutes the specified playlist using a default source of randomness.
   * 
   * <p>
   * All permutations occur with approximately equal likelihood.
   * </p>
   *
   * @throws UnsupportedOperationException if the specified list or its list-iterator does not
   *         support the <tt>set</tt> operation.
   * @return {@code true} if the playlist was shuffled
   */
  public boolean shufflePlaylist() {
    if (playlist == null || playlist.isEmpty()) {
      return false;
    }
    Object currentSong = getCurrentSongResource();
    Collections.shuffle(playlist);
    if (currentSong != null) {
      setCurrentSongIndex(playlist.indexOf(currentSong));
    }
    return true;
  }

  /**
   * Randomly permute the playlist using the specified source of randomness.
   * 
   * <p>
   * All permutations occur with equal likelihood assuming that the source of randomness is fair.
   * </p>
   *
   * @param random the source of randomness to use to shuffle the playlist.
   * @throws UnsupportedOperationException if the specified list or its list-iterator does not
   *         support the {@code set} operation.
   * @return {@code true} if the playlist was shuffled
   */
  public boolean shufflePlaylist(final Random random) {
    if (playlist == null || playlist.isEmpty()) {
      return false;
    }
    Object currentSong = getCurrentSongResource();
    Collections.shuffle(playlist, random);
    if (currentSong != null) {
      setCurrentSongIndex(playlist.indexOf(currentSong));
    }
    return true;
  }

  /**
   * Sorts the playlist according to the order induced by the specified comparator.
   * 
   * @see #COMPARATOR
   *
   * @see List#sort(Comparator)
   * @return {@code true} if the playlist was sorted
   */
  public boolean sortPlaylist() {
    return sortPlaylist(COMPARATOR);
  }

  /**
   * Sorts the playlist according to the order induced by the specified comparator.
   *
   * @param comparator the comparator to determine the order of the list. A {@code null} value
   *        indicates that the elements' <i>natural ordering</i> should be used.
   * @see List#sort(Comparator)
   * @return {@code true} if the playlist was sorted
   */
  public boolean sortPlaylist(final Comparator<Object> comparator) {
    if (playlist == null || playlist.isEmpty()) {
      return false;
    }
    Object currentSong = getCurrentSongResource();
    Collections.sort(playlist, comparator);
    if (currentSong != null) {
      setCurrentSongIndex(playlist.indexOf(currentSong));
    }
    return true;
  }

  // #########################################################################
  /**
   * Returns {@code true} if this MIDI player's playlist contains no elements.
   *
   * @return {@code true} if this MIDI player's playlist contains no elements
   */
  public final boolean isEmpty() {
    return this.playlist.isEmpty();
  }

  /**
   * Returns the number of elements in this player's playlist.
   *
   * @return the number of elements in this player's playlist
   */
  public final int size() {
    return this.playlist.size();
  }

  /**
   * Appends the specified resource to the end of this list.
   *
   * @param resource resource to be appended to the playlist
   * @return {@code true} if the playlist changed as a result of the call
   */
  public boolean add(final Object resource) {
    if (resource == null) {
      return false;
    }
    boolean added = this.playlist.add(resource);
    return added;
  }

  /**
   * Inserts the specified element at the specified position in this list.
   * 
   * <p>
   * Shifts the element currently at that position (if any) and any subsequent elements to the right
   * (adds one to their indices).
   * </p>
   *
   * @param index index at which the specified element is to be inserted
   * @param resource resource to be inserted
   * @return {@code true} if the playlist changed as a result of the call
   * @throws IndexOutOfBoundsException if the index is out of range
   *         (<tt>index &lt; 0 || index &gt; size()</tt>)
   * 
   */
  public boolean add(final int index, final Object resource) {
    if (resource == null) {
      return false;
    }
    Object currentSong = getCurrentSongResource();
    this.playlist.add(index, resource);
    // Update current song index
    if (currentSong != null) {
      setCurrentSongIndex(playlist.indexOf(currentSong));
    }
    return true;
  }

  /**
   * Appends all of the resources in the specified collection to the end of this player's resource,
   * in the order that they are returned by the specified collection's iterator.
   * 
   * <p>
   * The behavior of this operation is undefined if the specified collection is modified while the
   * operation is in progress. (Note that this will occur if the specified collection is this list,
   * and it's nonempty.)
   * </p>
   *
   * @param resources the resources to add to playlist
   * @return {@code true} if the playlist changed as a result of the call
   */
  public boolean addAll(final Collection<? extends Object> resources) {
    if (resources == null || resources.isEmpty()) {
      return false;
    }
    boolean added = this.playlist.addAll(resources);
    return added;
  }

  /**
   * Inserts all of the resources in the specified collection into this player's playlist, starting
   * at the specified position.
   * 
   * <p>
   * Shifts the element currently at that position (if any) and any subsequent elements to the right
   * (increases their indices). The new elements will appear in the list in the order that they are
   * returned by the specified collection's iterator.
   * </p>
   *
   * @param index index at which to insert the first element from the specified collection
   * @param resources the resources to add to playlist
   * @return {@code true} if the playlist changed as a result of the call
   * @throws IndexOutOfBoundsException if the index is out of range
   *         (<tt>index &lt; 0 || index &gt; size()</tt>)
   */
  public boolean addAll(final int index,
      final Collection<? extends Object> resources) {
    if (resources == null || resources.isEmpty()) {
      return false;
    }
    Object currentSong = getCurrentSongResource();
    boolean added = this.playlist.addAll(index, resources);
    // Update current song index
    if (added && currentSong != null) {
      setCurrentSongIndex(playlist.indexOf(currentSong));
    }
    return added;
  }

  /**
   * Removes the resource at the specified index in this player's playlist.
   * 
   * @param index the index of the resource to remove from playlist
   * @return {@code true} if this player's playlist changed as a result of the call, {@code false}
   *         if no song exists at the given index or if the playlist did not change
   */
  private boolean removeSongAtIndex(final int index) {
    if (index < 0 || index >= this.playlist.size()) {
      return false;
    }
    Object removedResource = this.playlist.remove(index);
    boolean removed = removedResource != null;
    removed &= afterRemove(index, removed);
    return removed;
  }

  /**
   * Removes the resource at the specified index in this player's playlist.
   * 
   * @param index the index of the resource to remove from playlist
   * @return {@code true} if this player's playlist changed as a result of the call, {@code false}
   *         if no song exists at the given index or if the playlist did not change
   */
  public boolean remove(final int index) {
    return removeSongAtIndex(index);
  }

  /**
   * After removing a resource from the player's playlist.
   * 
   * @param removedIndex the index of the resource removed from playlist
   * @param removed was the resources removed?
   * @return {@code true} if the post remove actions were done
   */
  protected boolean afterRemove(final int removedIndex, final boolean removed) {
    if (!removed) {
      return false;
    }
    if (this.isPlaying() && this.currentSongIndex == removedIndex) {
      this.stopPlaying();
    } else if (removedIndex < this.currentSongIndex) {
      this.setCurrentSongIndex(this.currentSongIndex - 1);
    }
    return true;
  }

  /**
   * Removes the first occurrence of the specified resource in this player's playlist.
   * 
   * @param resource the resource to remove from playlist
   * @return {@code true} if this player's playlist changed as a result of the call
   */
  protected boolean remove(final Object resource) {
    if (resource == null) {
      return false;
    }
    boolean removed = this.playlist.remove(resource);
    removed &= afterRemove(resource, removed);
    return removed;
  }

  /**
   * After removing a resource from the player's playlist.
   * 
   * @param removedResource the resource removed from playlist
   * @param removed was the resources removed?
   * @return {@code true} if the post remove actions were done
   */
  protected boolean afterRemove(final Object removedResource,
      final boolean removed) {
    if (!removed) {
      return false;
    }
    Object songResource = getCurrentSongResource();
    if (this.isPlaying() && songResource == removedResource) {
      this.stopPlaying();
    } else {
      this.setCurrentSongIndex(this.playlist.indexOf(songResource));
    }
    return true;
  }

  /**
   * Removes all of this player's playlist resources at the specified indexes.
   * 
   * @param indexes array of playlist resources' indexes to remove
   * @return {@code true} if this player's playlist changed as a result of the call
   */
  public boolean removeAll(final int[] indexes) {
    if (indexes == null || indexes.length < 0) {
      return false;
    }

    // Create a copy of array
    int[] sortedIndexes = Arrays.copyOf(indexes, indexes.length);
    // Sort it in ascending order
    Arrays.sort(sortedIndexes);
    // Then reverse array
    for (int i = 0, n = sortedIndexes.length / 2; i < n; i++) {
      int temp = sortedIndexes[i];
      sortedIndexes[i] = sortedIndexes[sortedIndexes.length - i - 1];
      sortedIndexes[sortedIndexes.length - i - 1] = temp;
    }

    boolean removed = true;
    for (int index : sortedIndexes) {
      removed &= removeSongAtIndex(index);
    }
    return removed;
  }

  /**
   * Removes all of this player's playlist resources that are also contained in the specified
   * collection (optional operation).
   * 
   * <p>
   * After this call returns, this collection will contain no elements in common with the specified
   * collection.
   * </p>
   * 
   * @param resources the resources to remove from playlist
   * @return {@code true} if this player's playlist changed as a result of the call
   */
  protected boolean removeAll(final Collection<? extends Object> resources) {
    if (resources == null || resources.isEmpty()) {
      return false;
    }
    boolean removed = this.playlist.removeAll(resources);
    removed &= afterRemoveAll(resources, removed);
    return removed;
  }

  /**
   * After removing collection of resources from the player's playlist.
   * 
   * @param resources the resources removed from playlist
   * @param removed were the resources removed?
   * @return {@code true} if the post remove actions were done
   */
  protected boolean afterRemoveAll(final Collection<? extends Object> resources,
      final boolean removed) {
    Object songResource = getCurrentSongResource();
    if (this.isPlaying() && resources.contains(songResource)) {
      this.stopPlaying();
    } else {
      this.setCurrentSongIndex(this.playlist.indexOf(songResource));
    }
    return true;
  }

  /**
   * Removes all of the elements from the player's playlist.
   * 
   * <p>
   * The playlist will be empty after this call returns.
   * </p>
   * 
   * @return {@code true} if playlist was cleared.
   */
  public boolean clear() {
    this.playlist.clear();
    afterClear();
    return true;
  }

  /**
   * After clearing the player's playlist.
   */
  protected void afterClear() {
    if (this.isPlaying()) {
      this.stopPlaying();
    }
  }

  // #########################################################################
  /**
   * Report a throwable catch by the {@link MidiThread}.
   * 
   * @param msg Message sent by the thread
   * @param thrown Throwable sent by the thread
   */
  protected void reportThrowable(final String msg, final Throwable thrown) {}

  // #########################################################################
  @Override
  public final void meta(final MetaMessage meta) {
    switch (meta.getType()) {
      case MidiPlayer.END_OF_TRACK_MESSAGE:
        // Notify player to force move to next song
        MidiPlayer.this.moveToNextSong(true);
        break;
      default:

    }
  }

  // #########################################################################
  @Override
  public void close() throws Exception {
    System.out.println("Closing allocated resources");
    if (playingThread != null) {
      playingThread.interrupt();
      playingThread = null;
    }
    if (sequencer != null) {
      sequencer.stop();
      sequencer.close();
      this.resetSequencer();
    }
  }

  // #########################################################################
  /**
   * MIDI player thread.
   * 
   * <p>
   * The thread is responsible for playing a song, waiting until its end or a request for change,
   * and then move to the next one. Once there is no more songs to play, the thread stops the MIDI
   * player.
   * </p>
   * 
   * @see MidiPlayer#getSequencer()
   * @see MidiPlayer#getCurrentSongResource()
   * @see MidiPlayer#stopPlaying()
   * 
   * @author Mathieu Brunot
   */
  public class MidiThread extends Thread {

    /**
     * Resource of the song currently played.
     */
    private transient Object currentResource = null;

    /**
     * Allocates a new {@code MidiThread} object.
     */
    public MidiThread() {}

    /**
     * Allocates a new {@code MidiThread} object.
     *
     * @param name the name of the new thread
     */
    public MidiThread(final String name) {
      super(name);
    }

    /**
     * Get the resource of the song currently played.
     * 
     * @return the resource of the song currently played
     */
    public final Object getCurrentResource() {
      return currentResource;
    }

    /**
     * Play a song.
     * 
     * @param resource the resource of the song to play
     */
    protected final void playSequence(final Object resource) {
      if (resource instanceof Path) {
        playSequence(((Path) resource).toFile());
      } else if (resource instanceof File) {
        playSequence((File) resource);
      } else if (resource instanceof URL) {
        playSequence((URL) resource);
      } else if (resource instanceof InputStream) {
        playSequence((InputStream) resource);
      } else {
        MidiPlayer.this.stopPlaying();
        MidiPlayer.LOGGER.log(Level.SEVERE, "Impossible to read resource: {0}",
            resource);
        MidiPlayer.this
            .reportThrowable("Impossible to read resource: " + resource, null);
      }
    }

    /**
     * Play a MIDI URL.
     * 
     * @param url the URL of the MIDI song
     */
    private void playSequence(final URL url) {
      try {
        // Sets the current sequence on which the MidiPlayer operates.
        // The URL must point to MIDI file data.
        Sequence song = MidiSystem.getSequence(url);
        playSequence(song);
      } catch (IOException | InvalidMidiDataException ex) {
        MidiPlayer.this.stopPlaying();
        MidiPlayer.LOGGER.log(Level.SEVERE, "Impossible to read URL: " + url,
            ex);
        MidiPlayer.this.reportThrowable("Impossible to read URL: " + url, ex);
      }
    }

    /**
     * Play a MIDI File.
     * 
     * @param file the File of the MIDI song
     */
    private void playSequence(final File file) {
      try {
        // Sets the current sequence on which the MidiPlayer operates.
        // The file must point to MIDI file data.
        Sequence song = MidiSystem.getSequence(file);
        playSequence(song);
      } catch (IOException | InvalidMidiDataException ex) {
        MidiPlayer.this.stopPlaying();
        MidiPlayer.LOGGER.log(Level.SEVERE, "Impossible to read file: " + file,
            ex);
        MidiPlayer.this.reportThrowable("Impossible to read file: " + file, ex);
      }
    }

    /**
     * Play a MIDI input stream.
     * 
     * @param is the input stream of the MIDI song
     */
    private void playSequence(final InputStream is) {
      try {
        // Sets the current sequence on which the MidiPlayer operates.
        // The stream must point to MIDI file data.
        Sequence song = MidiSystem.getSequence(is);
        playSequence(song);
      } catch (IOException | InvalidMidiDataException ex) {
        MidiPlayer.this.stopPlaying();
        MidiPlayer.LOGGER.log(Level.SEVERE,
            "Impossible to read InputStream: " + is, ex);
        MidiPlayer.this.reportThrowable("Impossible to read InputStream: " + is,
            ex);
      }
    }

    /**
     * Play a sequence.
     * 
     * @param song the sequence of the MIDI song
     * @throws InvalidMidiDataException if the sequence contains invalid MIDI data, or is not
     *         supported.
     */
    private void playSequence(final Sequence song)
        throws InvalidMidiDataException {
      sequencer.setSequence(song);

      printSongInfo(currentResource, song);

      /*
       * Starts playback of the MIDI data in the currently loaded sequence.
       */
      sequencer.start();
    }

    @Override
    public void run() {
      synchronized (MidiPlayer.this) {
        try {
          // Retrieve or create a MIDI sequencer
          Sequencer songSequencer = MidiPlayer.this.getSequencer();
          while (songSequencer != null && songSequencer.isOpen()) {
            // Get current song to play
            this.currentResource = MidiPlayer.this.getCurrentSongResource();
            if (this.currentResource == null) {
              System.out
                  .println("No songs to play. Stopping thread and player...");
              return;
            }

            playSequence(this.currentResource);

            // Wait until the song ends and must be changed
            MidiPlayer.this.wait();
          }
        } catch (InterruptedException ex) {
          MidiPlayer.LOGGER.log(Level.INFO, null, ex);
        } finally {
          // Whatever happens, make sure to close resources
          System.out.println("Closing player");
          MidiPlayer.this.stopPlaying();
        }
      }
    }
  }

}
