/*
Note: This will not run/compile in Cloud 9 unless we make a builder and runner file. Try running it on something else and see if you spot problems.
The state changes are logged to the console currently.
*/

import java.util.HashMap;
import java.util.Random;

public class DiningPhilosophers {
	public static void main(String[] args){
		// This is going to be called once the user clicks the Start button.
		ForkManager.initialize();
		for(int i = 1; i <= 5; i++){
			Philosopher.philosophers.get(i).start();
		}
	}
}

class Philosopher extends Thread{
	// THINK_TIME and EAT_TIME act as multipliers for how long a philosopher will think/eat.
	// WAIT_TIME is a multiplier for how often the philosopher will check to see if his/her
	// forks have become available through the Fork Manager.
	public static final float THINK_TIME = 3f;
	public static final float EAT_TIME = 2f;
	public static final float WAIT_TIME = .5f;
	// A philosopher may think, eat, or wait. The cycle is think -> wait -> eat.
	public enum State{
		THINKING,
		EATING,
		WAITING
	}
	public static HashMap<Integer, Philosopher> philosophers = new HashMap<Integer, Philosopher>();
	private int id;
	private State currentState;
	private int leftFork;
	private int rightFork;
	// The random number generator will give our philosophers a bit of variation in the time it takes them to do things.
	private Random rand = new Random();
	
	// Each philosopher's left fork will have an id matching the philosopher's id.
	// The right fork will have an id that is one more than the philosopher's id.
	// Of course there is a special case where Philosopher 1 will have Fork 5 on his/her left.
	public Philosopher(int id){
		this.id = id;
		currentState = State.WAITING;
		leftFork = id == 1 ? 5 : id - 1;
		rightFork = id;
		philosophers.put(id, this);
	}
	
	// The main loop for each philosopher. Their entire life cycle is simulated here.
	@Override
	public void run() {
		
		while(true){
			
			waiting();
			eating();
			thinking();
		}
	}
	
	// The philosopher will keep waiting until his/her forks become available. If they are available, transition to eating.
	private void waiting(){
		currentState = State.WAITING;
		System.out.println("Philosopher " + id + " is " + currentState);
		while(currentState == State.WAITING){
			if(ForkManager.acquireForks(leftFork, rightFork)){
				// The philosopher has both forks, so it's time to eat!
				currentState = State.EATING;
				System.out.println("Philosopher " + id + " is " + currentState);
			}else{
				// Both forks are not available. Ask again shortly.
				sleepTime(WAIT_TIME);
			}
		}
	}
	
	// Eat for a certain amount of time, then return forks to the table.
	private void eating(){
		sleepTime(EAT_TIME);
		ForkManager.returnForks(leftFork, rightFork);
	}
	
	// The distinction between thinking and waiting is a thinking philosopher will not pick up the forks even if they are available.
	// They are not yet hungry.
	private void thinking(){
		currentState = State.THINKING;
		System.out.println("Philosopher " + id + " is " + currentState);
		sleepTime(THINK_TIME);
	}
	
	// This thread will halt for some time to simulate thinking/eating/waiting times.
	private void sleepTime(float multiplier){
		while(true){
			try {
				Thread.sleep((int)(multiplier * (rand.nextInt(2) + 1) * 1000));
			} catch (InterruptedException e) {
				System.out.println("Interrupted!");
			}
			return;
	    }
	}
	
}

// ForkManager acts as the arbitrator of forks. Whenever a philosopher wishes to acquire a
// fork, the ForkManager will ensure that the philosopher only takes a fork if he/she can pick
// up both forks needed to eat.
class ForkManager {
	private static HashMap<Integer, Fork> forks;
 
	public static void initialize(){
		forks = new HashMap<Integer, Fork>();
		for(int i = 1; i <= 5; i++){
			ForkManager.forks.put(i, new Fork(i));
			new Philosopher(i);
		}
	}
	 
	// Only grants forks if both forks are available for pickup.
	public static boolean acquireForks(int left, int right){
		Fork leftFork = forks.get(left);
		Fork rightFork = forks.get(right);
		 
		if(leftFork.isAvailable && rightFork.isAvailable){
			leftFork.isAvailable = false;
			rightFork.isAvailable = false;
			return true;
		}else{
			return false;
		}
	}
	 
	// The forks are returned to the table and marked as available.
	public static void returnForks(int left, int right){
		forks.get(left).isAvailable = true;
		forks.get(right).isAvailable = true;
	}
}

// A simple class used to track the numeric id of a fork, along with its availability status.
class Fork {
	public int id;
	public boolean isAvailable;
	
	public Fork(int id){
		this.id = id;
		isAvailable = true;
	}
}