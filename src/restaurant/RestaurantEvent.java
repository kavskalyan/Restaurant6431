package restaurant;

public class RestaurantEvent {
	private int eventTime;
	private BasicThread threadToWakeUp;
	
	public RestaurantEvent(int aEventTime, BasicThread thread){
		setEventTime(aEventTime);
		setThreadToWakeUp(thread);
	}
	
	public int getEventTime() {
		return eventTime;
	}

	public void setEventTime(int eventTime) {
		this.eventTime = eventTime;
	}

	public BasicThread getThreadToWakeUp() {
		return threadToWakeUp;
	}

	public void setThreadToWakeUp(BasicThread threadToWakeUp) {
		this.threadToWakeUp = threadToWakeUp;
	}
	
}
