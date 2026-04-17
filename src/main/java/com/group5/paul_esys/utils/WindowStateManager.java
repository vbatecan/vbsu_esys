package com.group5.paul_esys.utils;

import java.awt.Frame;
import java.util.prefs.Preferences;

/**
 * Manages window state persistence (position, size, and maximize state).
 * Saves window state across sessions and restores it on application start.
 */
public final class WindowStateManager {

	private static final Preferences PREFS = Preferences.userNodeForPackage(WindowStateManager.class);
	private static final String PREF_WINDOW_X = "window.x";
	private static final String PREF_WINDOW_Y = "window.y";
	private static final String PREF_WINDOW_WIDTH = "window.width";
	private static final String PREF_WINDOW_HEIGHT = "window.height";
	private static final String PREF_WINDOW_MAXIMIZED = "window.maximized";
	
	private static final int DEFAULT_X = 100;
	private static final int DEFAULT_Y = 100;
	private static final int DEFAULT_WIDTH = 1280;
	private static final int DEFAULT_HEIGHT = 720;

	private WindowStateManager() {
	}

	/**
	 * Restores the window to its previously saved state (position, size, and maximized state).
	 * If no saved state exists, uses default dimensions.
	 *
	 * @param frame the window frame to restore
	 */
	public static void restoreWindowState(Frame frame) {
		int x = PREFS.getInt(PREF_WINDOW_X, DEFAULT_X);
		int y = PREFS.getInt(PREF_WINDOW_Y, DEFAULT_Y);
		int width = PREFS.getInt(PREF_WINDOW_WIDTH, DEFAULT_WIDTH);
		int height = PREFS.getInt(PREF_WINDOW_HEIGHT, DEFAULT_HEIGHT);
		boolean wasMaximized = PREFS.getBoolean(PREF_WINDOW_MAXIMIZED, false);

		frame.setBounds(x, y, width, height);

		if (wasMaximized) {
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		}
	}

	/**
	 * Saves the current window state (position, size, and maximized state) to preferences.
	 *
	 * @param frame the window frame to save
	 */
	public static void saveWindowState(Frame frame) {
		int extendedState = frame.getExtendedState();
		boolean isMaximized = (extendedState & Frame.MAXIMIZED_BOTH) != 0;

		if (!isMaximized) {
			PREFS.putInt(PREF_WINDOW_X, frame.getX());
			PREFS.putInt(PREF_WINDOW_Y, frame.getY());
			PREFS.putInt(PREF_WINDOW_WIDTH, frame.getWidth());
			PREFS.putInt(PREF_WINDOW_HEIGHT, frame.getHeight());
		}

		PREFS.putBoolean(PREF_WINDOW_MAXIMIZED, isMaximized);

		try {
			PREFS.flush();
		} catch (Exception e) {
			System.err.println("Failed to save window state: " + e.getMessage());
		}
	}

	/**
	 * Clears saved window state, resetting to default dimensions.
	 */
	public static void clearWindowState() {
		PREFS.remove(PREF_WINDOW_X);
		PREFS.remove(PREF_WINDOW_Y);
		PREFS.remove(PREF_WINDOW_WIDTH);
		PREFS.remove(PREF_WINDOW_HEIGHT);
		PREFS.remove(PREF_WINDOW_MAXIMIZED);

		try {
			PREFS.flush();
		} catch (Exception e) {
			System.err.println("Failed to clear window state: " + e.getMessage());
		}
	}
}
