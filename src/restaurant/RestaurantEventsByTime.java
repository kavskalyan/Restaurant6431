package restaurant;

import java.util.LinkedList;
import java.util.List;

public class RestaurantEventsByTime {
	private int eventTime;
	private List<RestaurantEvent> eventList;
	
	public RestaurantEventsByTime(int time){
		setEventTime(time);
		eventList = new LinkedList<RestaurantEvent>();
	}
	
	public int getEventTime() {
		return eventTime;
	}
	public void setEventTime(int eventTime) {
		this.eventTime = eventTime;
	}
	public List<RestaurantEvent> getEventList() {
		return eventList;
	}
	public void setEventList(List<RestaurantEvent> eventList) {
		this.eventList = eventList;
	}
	public void addEvent(RestaurantEvent evt){
		eventList.add(evt);
		
	}
}
