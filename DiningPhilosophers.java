import java.util.HashMap;
import java.util.Random;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class DiningPhilosophers {
	private static final int GUI_SIZE = 800;

	public static void main(String[] args) {
		new GUIManager(GUI_SIZE);
		GUIManager.instance.add(new ForkManager());
		// This is going to be called once the user clicks the Start button.

	}
}

class Philosopher extends Thread {
	// THINK_TIME and EAT_TIME act as multipliers for how long a philosopher
	// will think/eat.
	// WAIT_TIME is a multiplier for how often the philosopher will check to see
	// if his/her
	// forks have become available through the Fork Manager.
	public static final float THINK_TIME = 2f;
	public static final float EAT_TIME = 2f;
	public static final float WAIT_TIME = .5f;

	// A philosopher may think, eat, or wait. The cycle is think -> wait -> eat.
	public enum State {
		THINKING, EATING, WAITING
	}

	public boolean isAlive;
	public static HashMap<Integer, Philosopher> philosophers = new HashMap<Integer, Philosopher>();
	private int id;
	private int xPos;
	private int yPos;
	private State currentState;
	private int leftFork;
	private int rightFork;
	public Color currentColor;
	// The random number generator will give our philosophers a bit of variation
	// in the time it takes them to do things.
	private Random rand = new Random();
	private ForkManager gui;

	// Each philosopher's left fork will have an id matching the philosopher's
	// id.
	// The right fork will have an id that is one more than the philosopher's
	// id.
	// Of course there is a special case where Philosopher 1 will have Fork 5 on
	// his/her left.
	public Philosopher(int id, int x, int y, ForkManager gui) {
		this.id = id;
		xPos = x;
		yPos = y;
		currentState = State.WAITING;
		leftFork = id == 1 ? 5 : id - 1;
		rightFork = id;
		philosophers.put(id, this);
		currentColor = Color.WHITE;
		this.gui = gui;
		isAlive = true;
	}

	public void draw(Graphics g) {
		g.setColor(currentColor);
		g.fillOval(xPos - 75 / 2, yPos - 75 / 2, 75, 75);
	}

	// The main loop for each philosopher. Their entire life cycle is simulated
	// here.
	@Override
	public void run() {

		while (isAlive) {

			currentColor = Color.RED;
			gui.repaint();
			waiting();
			currentColor = Color.GREEN;
			gui.repaint();
			eating();
			currentColor = Color.YELLOW;
			gui.repaint();
			thinking();
		}
	}

	// The philosopher will keep waiting until his/her forks become available.
	// If they are available, transition to eating.
	private void waiting() {
		currentState = State.WAITING;
		System.out.println("Philosopher " + id + " is " + currentState);
		while (currentState == State.WAITING) {
			// Check to see if this philosopher is still alive, meaning the simulation hasn't been restarted in the meantime.
			if (!isAlive) {
				return;
			} else if (ForkManager.acquireForks(leftFork, rightFork)) {
				// The philosopher has both forks, so it's time to eat!
				currentState = State.EATING;
				System.out.println("Philosopher " + id + " is " + currentState);
			} else {
				// Both forks are not available. Ask again shortly.
				sleepTime(WAIT_TIME);
			}
		}
	}

	// Eat for a certain amount of time, then return forks to the table.
	private void eating() {
		sleepTime(EAT_TIME);
		// We are only concerned about the actions of living philosophers (this is done because even though the simulation has restarted,
		// old threads are not immediately stopped and could impact the GUI before they fully terminate.
		if (isAlive)
			ForkManager.returnForks(leftFork, rightFork);
	}

	// The distinction between thinking and waiting is a thinking philosopher
	// will not pick up the forks even if they are available.
	// They are not yet hungry.
	private void thinking() {
		currentState = State.THINKING;
		System.out.println("Philosopher " + id + " is " + currentState);
		sleepTime(THINK_TIME);
	}

	// This thread will halt for some time to simulate thinking/eating/waiting
	// times.
	private void sleepTime(float multiplier) {
		while (true) {
			try {
				Thread.sleep((int) (multiplier * (rand.nextInt(2) + 1) * 1000));
			} catch (InterruptedException e) {
				System.out.println("Interrupted!");
			}
			return;
		}
	}

}

// ForkManager acts as the arbitrator of forks. Whenever a philosopher wishes to
// acquire a
// fork, the ForkManager will ensure that the philosopher only takes a fork if
// he/she can pick
// up both forks needed to eat.
class ForkManager extends JPanel {
	private static HashMap<Integer, Fork> forks;
	// The following four fields dictate the position and distance between
	// philosophers.
	private int xModifier = 800;
	private int yModifier = 800;
	private int xStretch = 600;
	private int yStretch = 600;
	JButton start, reset;

	public ForkManager() {
		forks = new HashMap<Integer, Fork>();
		this.setBackground(Color.GRAY);
		start = new JButton("Start");
		reset = new JButton("Reset");
		ListenForButton lForButton = new ListenForButton();
		start.addActionListener(lForButton);
		reset.addActionListener(lForButton);
		this.add(start);
		this.add(reset);

		for (int i = 1; i <= 5; i++) {
			double angle = Math.PI / 2 + 2 * Math.PI / 5 * i;
			ForkManager.forks.put(
					i,
					new Fork(i, (int) (xModifier / 2.0 + xStretch / 5.0
							* Math.cos(angle + 120)),
							(int) (yModifier / 2.0 - yStretch / 5.0
									* Math.sin(angle + 120))));
			// Each philosopher is given an id, and an x and y position to
			// dictate where they will be drawn on the GUI.
			new Philosopher(i, (int) (xModifier / 2.0 + xStretch / 3.0
					* Math.cos(angle)), (int) (yModifier / 2.0 - yStretch / 3.0
					* Math.sin(angle)), this);
		}

	}

	// The table, philosophers, and TODO forks are drawn here.
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// Table is painted.
		g.setColor(Color.BLACK);
		g.fillOval(325, 325, 150, 150);
		// The color key is shown as strings.
		g.setFont(new Font("Arial", Font.BOLD, 16));
		g.setColor(Color.RED);
		g.drawString("Red = waiting philosopher", 0, 12);
		g.setColor(Color.GREEN);
		g.drawString("Green = eating philosopher", 0, 28);
		g.setColor(Color.YELLOW);
		g.drawString("Yellow = thinking philosopher", 0, 44);
		g.setColor(Color.DARK_GRAY);
		g.drawString("Dark gray = unused fork", 0, 76);
		g.setColor(Color.CYAN);
		g.drawString("Cyan = fork in use", 0, 94);
		// Philosophers are each painted according to their draw methods.
		for (int i = 1; i <= 5; i++) {
			Philosopher.philosophers.get(i).draw(g);
			forks.get(i).draw(g);

		}
	}

	// Only grants forks if both forks are available for pickup.
	public static synchronized boolean acquireForks(int left, int right) {
		Fork leftFork = forks.get(left);
		Fork rightFork = forks.get(right);

		if (leftFork.isAvailable && rightFork.isAvailable) {
			leftFork.currentColor = Color.CYAN;
			rightFork.currentColor = Color.CYAN;
			leftFork.isAvailable = false;
			rightFork.isAvailable = false;

			return true;
		} else {
			return false;
		}
	}

	// The forks are returned to the table and marked as available.
	public static void returnForks(int left, int right) {
		forks.get(left).currentColor = Color.DARK_GRAY;
		forks.get(right).currentColor = Color.DARK_GRAY;
		forks.get(left).isAvailable = true;
		forks.get(right).isAvailable = true;

	}

	
	// Implementation of listeners for events
	class ListenForButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// Start all of the philosopher threads.
			if (e.getSource() == start) {
				for (int j = 1; j <= 5; j++) {
					Philosopher.philosophers.get(j).isAlive = true;
					Philosopher.philosophers.get(j).start();

				}
				start.setEnabled(false);
				reset.setEnabled(true);
			}
			// Signal the current threads to stop when they reach the end of their run cycle, reset the 
			// JPanel, and initialize to prepare for next simulation run.
			if (e.getSource() == reset) {
				for (int j = 1; j <= 5; j++) {
					Philosopher.philosophers.get(j).isAlive = false;
				}
				GUIManager.instance.getContentPane().removeAll();
				GUIManager.instance.getContentPane().revalidate();
				GUIManager.instance.getContentPane().repaint();

				GUIManager.instance.add(new ForkManager());

				start.setEnabled(true);
			}
		}

	}
}

// A simple class used to track the numeric id of a fork, along with its
// availability status.
class Fork {
	public int id;
	public boolean isAvailable;
	public Color currentColor;
	private int xPos;
	private int yPos;

	public Fork(int id, int x, int y) {
		this.id = id;
		isAvailable = true;
		xPos = x;
		yPos = y;
		currentColor = Color.DARK_GRAY;
	}

	public void draw(Graphics g) {
		g.setColor(currentColor);
		g.fillOval(xPos - 30 / 2, yPos - 30 / 2, 30, 30);
	}
}

// Acts as the JFrame used to support the GUI window. Various settings of the
// window are established here.
class GUIManager extends JFrame {
	public static GUIManager instance;

	public GUIManager(int size) {
		instance = this;
		this.setSize(size, size);
		this.setTitle("Dining Philosophers");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
}
