package restaurant;

public class BasicThread extends Thread {
	private CustomLock localLock;
	private Restaurant restaurantManager;
	private String threadId;
	
	public BasicThread(){
		
	}
	@Override
	public void start(){
		getRestaurantManager().incrementNumberOfThreadsToCompleteExecution();
		super.start();
	}
	public BasicThread(Restaurant restaurantManager,int id){
		localLock = new CustomLock(restaurantManager);
		setRestaurantManager(restaurantManager);
		setThreadId(Integer.toString(id));
	}
	
	public void l_wait() throws InterruptedException{
		synchronized(localLock){
			localLock.l_wait();
		}
	}
	public synchronized void l_notify(){
		synchronized(localLock){
			localLock.l_notify();
		}
	}
	public Restaurant getRestaurantManager() {
		return restaurantManager;
	}
	public void setRestaurantManager(Restaurant restaurantManager) {
		this.restaurantManager = restaurantManager;
	}
	public String getThreadId() {
		return threadId;
	}
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
}
