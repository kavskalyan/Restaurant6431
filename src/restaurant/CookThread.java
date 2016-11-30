package restaurant;

import java.util.Map;
import java.util.Map.Entry;

public class CookThread extends BasicThread {
	private DinerThread assignedDiner;
	private Map<MenuItemType,Integer> order;
	private int totalNumberOfResourcesRequired;
	
	public CookThread(){
		super();
	}
	
	public CookThread(Restaurant restaurantManager,int id) {
		super(restaurantManager,id);
		// TODO Auto-generated constructor stub
	}
	public Map<MenuItemType,Integer> getOrder() {
		return order;
	}
	public void setOrder(Map<MenuItemType,Integer> order) {
		this.order = order;
		totalNumberOfResourcesRequired = 0;
		for (int value : order.values()) {
			totalNumberOfResourcesRequired += value;
		}
	}
	
	
	private boolean checkAndFectchMachinesToCook(boolean shouldPeek) throws InterruptedException{
		boolean gotAtleastOneResource = false;
		//System.out.println("Check and fetch");
		for (Entry<MenuItemType, Integer> entry : order.entrySet()) {
			MenuItemType key = entry.getKey();
		    int value = entry.getValue();
		    if(value <= 0) continue;
		    MenuItem menuItem = getRestaurantManager().getMenuItemOfType(key);
		    System.out.println("Check and fetch 1st:"+menuItem.getItemTypeAsString());
		    int response = shouldPeek?menuItem.peekTheMachineAndCookIfAvailable():menuItem.fetchTheMachineAndCook(this) ;
		    System.out.println("Check and fetch 2nd:"+menuItem.getItemTypeAsString()+" :"+ Integer.toString(response));
		    if(response != -1){
		    	gotAResource(menuItem.getItemTypeAsString());
		    	totalNumberOfResourcesRequired -= 1;
		    	gotAtleastOneResource = true;
		    	registerEventCallbackInTime(response,false);
		    	order.replace(key, value - 1);
		    	System.out.println("Check and fetch 3rd:"+menuItem.getItemTypeAsString()+" :"+ Integer.toString(getRestaurantManager().getNumberOfThreadsToCompleteExecution()));
		    	//getRestaurantManager().decrementNumberOfThreadsToCompleteExecution();
		    	l_wait();
		    	//getRestaurantManager().incrementNumberOfThreadsToCompleteExecution();
		    	menuItem.releaseMachine();//Nongreedy approach
		    }
		    // ...
		}
		return gotAtleastOneResource;
	}
	private void gotAResource(String resourceName){
		String resourceString = "Cook "+getThreadId() +" uses the "+resourceName+" machine.";
		getRestaurantManager().WriteToOutput(resourceString);
	}
	private boolean registerEventCallbackInTime(int time, boolean isAbsolute){
		int currentTime = isAbsolute?0 : getRestaurantManager().getCurrentTime();
		RestaurantEvent finishEvent = new RestaurantEvent(currentTime+time,this);
		return getRestaurantManager().registerEventCallback(finishEvent);
	}
	
	public DinerThread getAssignedDiner() {
		return assignedDiner;
	}
	public void setAssignedDiner(DinerThread assignedDiner) {
		this.assignedDiner = assignedDiner;
	}
	@Override
	public synchronized void run(){
		//System.out.println("Run called Cook Thread"+getThreadId());
		try {
			while(true){
				l_wait();
				processingOrder();
				getAssignedDiner().getLocalLock().l_notify();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("Interrupted");
			//e.printStackTrace();
		}
	}
	private void processingOrder() throws InterruptedException{
		String processString = "Cook "+getThreadId() +" processes Diner "+getAssignedDiner().getThreadId()+"'s order.";
		getRestaurantManager().WriteToOutput(processString);
		while(checkAndFectchMachinesToCook(true));
		while(totalNumberOfResourcesRequired > 0)
			checkAndFectchMachinesToCook(false);
		
		
	}

	

}
