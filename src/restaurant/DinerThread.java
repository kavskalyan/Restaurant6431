package restaurant;

import java.util.HashMap;
import java.util.Map;

public class DinerThread extends BasicThread {
	private int arrivalTime;
	private Map<MenuItemType,Integer> order;
	private int tableNumberAllotted;
	
	
	public DinerThread(Restaurant arestaurantManager,int odinerId) {
		super(arestaurantManager,odinerId);
		order = new HashMap<MenuItemType, Integer>();
		// TODO Auto-generated constructor stub
	}

	public int getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(int arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public void setDinerData(String inData){
		String[]orders = inData.split(",");
		setArrivalTime(Integer.parseInt(orders[0]));
		order.put(MenuItemType.BURGER,Integer.parseInt(orders[1]));
		order.put(MenuItemType.FRIES,Integer.parseInt(orders[2]));
		order.put(MenuItemType.COKE,Integer.parseInt(orders[3]));
	}
	@Override
	public synchronized void run(){
		try {
				//System.out.println("Run called Diner Thread"+getThreadId());
				registerEventCallbackInTime(getArrivalTime(),true);
				l_wait();
				dinerArrived();
				int tableNumber = getRestaurantManager().getATable();
				gotATable(tableNumber);
				CookThread cook = getRestaurantManager().getFreeCook();
				cook.setAssignedDiner(this);
				cook.setOrder(order);
				//synchronized(cook){
				cook.l_notify();
				l_wait();
				orderReceived(cook);
				eat();
			//}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void gotATable(int tabelNumber){
		setTableNumberAllotted(tabelNumber);
		String tabelString = "Diner "+getThreadId() +" is seated on table "+Integer.toString(tabelNumber)+".";
		getRestaurantManager().WriteToOutput(tabelString);
	}
	private void dinerArrived(){
		String arrivedString = "Diner "+getThreadId() +" arrives.";
		getRestaurantManager().WriteToOutput(arrivedString);
	}
	private void orderReceived(CookThread thread){
		getRestaurantManager().releaseCook(thread);
		String orderReceivedString = "Diner "+getThreadId() + "'s order is ready. Diner " +getThreadId() +" starts eating.";
		getRestaurantManager().WriteToOutput(orderReceivedString);
	}
	private void eat() throws InterruptedException{
		registerEventCallbackInTime(30,false);
		l_wait();
		getRestaurantManager().releaseTable(getTableNumberAllotted());
		String finishString = "Diner "+getThreadId() + "finishes. Diner " +getThreadId() +" leaves the restaurant.";
		getRestaurantManager().WriteToOutput(finishString);
		getRestaurantManager().decrementNumberOfThreadsToCompleteExecution();
	}

	private boolean registerEventCallbackInTime(int time, boolean isAbsolute){
		int currentTime = isAbsolute?0 : getRestaurantManager().getCurrentTime();
		RestaurantEvent finishEvent = new RestaurantEvent(currentTime+time,this);
		return getRestaurantManager().registerEventCallback(finishEvent);
	}

	public int getTableNumberAllotted() {
		return tableNumberAllotted;
	}

	public void setTableNumberAllotted(int tableNumberAllotted) {
		this.tableNumberAllotted = tableNumberAllotted;
	}

}
