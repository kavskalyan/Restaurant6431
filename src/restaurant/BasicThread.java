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
	public void l_notify(){
		//System.out.println("Notify out");
		synchronized(localLock){
			//System.out.println("Notify in");
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
	public CustomLock getLocalLock()
	{
		return localLock;
	}
}
