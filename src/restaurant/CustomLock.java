package restaurant;

public class CustomLock {
	private int waitersCount;
	private Restaurant restaurantManager;
	public CustomLock(Restaurant aRestaurantManager){
		setRestaurantManager(aRestaurantManager);
	}
	
	public synchronized void l_wait() throws InterruptedException{
		setWaitersCount(getWaitersCount()+1);
		getRestaurantManager().decrementNumberOfThreadsToCompleteExecution();
		this.wait();
	}
	
	public synchronized void l_notify(){
		setWaitersCount(getWaitersCount()-1);
		//System.out.println("Final Notify");
		getRestaurantManager().incrementNumberOfThreadsToCompleteExecution();
		this.notify();
	}
	public synchronized boolean l_queue(){
		return getWaitersCount()>0;
	}

	public int getWaitersCount() {
		return waitersCount;
	}

	public void setWaitersCount(int waitersCount) {
		this.waitersCount = waitersCount;
	}

	public Restaurant getRestaurantManager() {
		return restaurantManager;
	}

	public void setRestaurantManager(Restaurant restaurantManager) {
		this.restaurantManager = restaurantManager;
	}

}
