package restaurant;

import java.util.LinkedList;
import java.util.Queue;

public class MenuItem{
	private MenuItemType itemType;
	private int timeRequiredForCompletion;
	private int numberOfMachinesAvailable;
	private int totalMachines;
	private Queue<CookThread> waitingCooksPool;
	private CustomLock resourceLock;
	public MenuItem(MenuItemType type, int time, int onumberOfMachinesAvailable,Restaurant restaurantManager){
		resourceLock = new CustomLock(restaurantManager);
		setItemType(type);
		setTimeRequiredForCompletion(time);
		setNumberOfMachinesAvailable(onumberOfMachinesAvailable);
		setTotalMachines(onumberOfMachinesAvailable);
		waitingCooksPool = new LinkedList<CookThread>();
	}
	public MenuItemType getItemType() {
		return itemType;
	}
	public void setItemType(MenuItemType itemType) {
		this.itemType = itemType;
	}
	public int getTimeRequiredForCompletion() {
		return timeRequiredForCompletion;
	}
	public void setTimeRequiredForCompletion(int timeRequiredForCompletion) {
		this.timeRequiredForCompletion = timeRequiredForCompletion;
	}
	public  int getNumberOfMachinesAvailable() {
		return numberOfMachinesAvailable;
	}
	public void setNumberOfMachinesAvailable(int numberOfMachinesAvailable) {
		this.numberOfMachinesAvailable = numberOfMachinesAvailable;
	}
	public boolean checkForResource(){
		return getNumberOfMachinesAvailable() > 0;
	}
	public synchronized int fetchTheMachineAndCook(CookThread thread) throws InterruptedException{
		while(getNumberOfMachinesAvailable() <= 0){
			waitingCooksPool.add(thread);
			resourceLock.l_wait();
		}
		setNumberOfMachinesAvailable(getNumberOfMachinesAvailable() -1);
		return getTimeRequiredForCompletion();
	}
	public synchronized int peekTheMachineAndCookIfAvailable() throws InterruptedException{
		if(getNumberOfMachinesAvailable() <= 0){
			return -1;
		}
		setNumberOfMachinesAvailable(getNumberOfMachinesAvailable() -1);
		return getTimeRequiredForCompletion();
	}
	public synchronized void releaseMachine(){
		setNumberOfMachinesAvailable(getNumberOfMachinesAvailable() +1);
		if(!waitingCooksPool.isEmpty()){
			//CookThread threadToRelease = waitingCooksPool.remove();
			if(resourceLock.l_queue()){
				resourceLock.l_notify();
			}
		}
	}
	public int getTotalMachines() {
		return totalMachines;
	}
	public void setTotalMachines(int totalMachines) {
		this.totalMachines = totalMachines;
	}
	public String getItemTypeAsString(){
		switch(itemType){
			case BURGER:
				return "burger";
			case FRIES:
				return "fries";
			case COKE:
				return "coke";
			default:
				return "unknown";
		}
	}
	
}
