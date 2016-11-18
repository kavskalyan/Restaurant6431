package restaurant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class Restaurant {
	private int cooksTotalCount;
	private int currentTime;
	private int dinersTotalCount;
	private final int totalTime = 120;
	private int tablesTotalCount;
	private int tablesFreeCount;
	private int cooksFreeCount;
	private Queue<CookThread> freeCooksPool;
	private Queue<Integer> freeTablesPool;
	private List<DinerThread> dinerList;
	private List<CookThread> cookList;
	private Map<MenuItemType,MenuItem> menuItems;
	private final String inputFileName = "/Users/kalyan/Documents/OS/Assignment4/project-sample-input-1.txt";
	private final String outputFilename = "output.txt";
	private PriorityQueue< RestaurantEventsByTime > eventsQueue;
	private Map< Integer, RestaurantEventsByTime> eventsMap;
	private int numberOfThreadsToCompleteExecution;
	private Object numberOfThreadsToCompleteExecutionlock;
	private CustomLock tablesLock;
	private Object mainLoopLock;
	private Object mapsQueuesLock;
	public static void main(String[] args) {
		
		Restaurant dasda = new Restaurant();
		dasda.Start();
	}
	public Restaurant(){
		
		eventsQueue = new PriorityQueue<RestaurantEventsByTime>(0, new Comparator<RestaurantEventsByTime>(){
			@Override
			public int compare(RestaurantEventsByTime o1, RestaurantEventsByTime o2) {
				// TODO Auto-generated method stub
				return o1.getEventTime() - o2.getEventTime();
			}
		});
		eventsMap = new HashMap<Integer, RestaurantEventsByTime>();
		currentTime = 0;

		numberOfThreadsToCompleteExecutionlock =  new Object();
		dinerList = new LinkedList<DinerThread>();
		cookList = new ArrayList<CookThread>();
		freeCooksPool = new LinkedList<CookThread>();
		freeTablesPool = new LinkedList<Integer>();
		menuItems = new HashMap<MenuItemType,MenuItem>();
		menuItems.put(MenuItemType.BURGER,new MenuItem(MenuItemType.BURGER, 5,1,this));
		menuItems.put(MenuItemType.FRIES, new MenuItem(MenuItemType.FRIES,3, 1,this));
		menuItems.put(MenuItemType.COKE,new MenuItem(MenuItemType.COKE,1, 1,this));
		File file = new File(outputFilename);
		tablesLock = new CustomLock(this);
		mainLoopLock = new Object();
		mapsQueuesLock = new Object();
		// if file doesnt exists, then create it
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ParseAndProcessInput();
	}
	public void Start(){
		executeMainLoop();
		WriteToOutput("The last diner leaves the restaurant");
	}
	private void ParseAndProcessInput(){
		try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(inputFileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);

           String line;
			for(int i=0; (line = bufferedReader.readLine()) != null ; i++) {
				if(i<2){
					setDinersTotalCount(Integer.parseInt(line));
					line = bufferedReader.readLine();
					setTablesTotalCount( Integer.parseInt(line));
					setTablesFreeCount(Integer.parseInt(line));
					for(int p = 0; p< getTablesTotalCount(); p++) freeTablesPool.add(p+1);
					line = bufferedReader.readLine();
					setCooksTotalCount( Integer.parseInt(line));
					generateCookThreads(getCooksTotalCount());
					i = 2;
				}
				else{
					addDiner(line);
				}
            }   

            // Always close files.
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                		inputFileName + "'");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + inputFileName + "'");                  
            // Or we could just do this: 
            // ex.printStackTrace();
        }
	}
	void generateCookThreads(int numberOfThreads){
		for(int i=0; i<numberOfThreads; i++ ){
			CookThread newThread = new CookThread(this,i+1);
			cookList.add(newThread);
			addCookThreadToFreePool(newThread);
			newThread.start();
		}
	}
	void addDiner(String dinerString){
		DinerThread newDiner = new DinerThread(this,dinerList.size()+1);
		dinerList.add(newDiner);
		newDiner.setDinerData(dinerString);
		newDiner.start();
	}
	public int getCooksTotalCount() {
		return cooksTotalCount;
	}
	public void setCooksTotalCount(int cooksTotalCount) {
		this.cooksTotalCount = cooksTotalCount;
	}
	public int getCurrentTime() {
		return currentTime;
	}
	public void setCurrentTime(int currentTime) {
		this.currentTime = currentTime;
	}
	public int getTablesTotalCount() {
		return tablesTotalCount;
	}
	public void setTablesTotalCount(int tablesTotalCount) {
		this.tablesTotalCount = tablesTotalCount;
	}
	public int getTablesFreeCount() {
		return tablesFreeCount;
	}
	public void setTablesFreeCount(int tablesFreeCount) {
		this.tablesFreeCount = tablesFreeCount;
	}
	public int getCooksFreeCount() {
		return cooksFreeCount;
	}
	public void setCooksFreeCount(int cooksFreeCount) {
		this.cooksFreeCount = cooksFreeCount;
	}
	public int getTotalTime() {
		return totalTime;
	}
	public int getDinersTotalCount() {
		return dinersTotalCount;
	}
	public void setDinersTotalCount(int dinersTotalCount) {
		this.dinersTotalCount = dinersTotalCount;
	}
	public void addCookThreadToFreePool(CookThread o){
		setCooksFreeCount(getCooksFreeCount()+1);
		freeCooksPool.add(o);
	}
	public synchronized CookThread getFreeCook() throws InterruptedException{
		while(getCooksFreeCount() <= 0)
			wait();
	setCooksFreeCount(getCooksFreeCount()-1);
	return freeCooksPool.remove();
	}
	
	public synchronized void releaseCook(CookThread thread){
		freeCooksPool.add(thread);
		setCooksFreeCount(getCooksFreeCount()+1);
	}
	public boolean registerEventCallback(RestaurantEvent evt){
		synchronized(mapsQueuesLock){
		System.out.println("Event registering at"+ Integer.toString(evt.getEventTime()));
		if(eventsMap.containsKey(evt.getEventTime())){
			RestaurantEventsByTime eventListObj = eventsMap.get(evt.getEventTime());
			eventListObj.addEvent(evt);
		}
		else{
			RestaurantEventsByTime eventListObj = new RestaurantEventsByTime(evt.getEventTime());
			eventListObj.addEvent(evt);
			eventsQueue.add(eventListObj);
		}
		}
		return true;
	}
	public synchronized int getATable() throws InterruptedException{
		while(getTablesFreeCount() <= 0){
			decrementNumberOfThreadsToCompleteExecution();
			tablesLock.l_wait();
		}
		setTablesFreeCount(getTablesFreeCount() - 1);
		int freeTabelNumber = freeTablesPool.remove();
		return freeTabelNumber;
	}
	public synchronized void releaseTable(int tableNumber){
		freeTablesPool.add(tableNumber);
		setTablesFreeCount(getTablesFreeCount() + 1);
		if(tablesLock.l_queue()){
			incrementNumberOfThreadsToCompleteExecution();
			tablesLock.l_notify();
		}
	}
	public synchronized void WriteToOutput(String str){
		try {
			str = "00:" + Integer.toString(getCurrentTime()) + " - "+str;
			FileWriter fw = new FileWriter(outputFilename);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(str);
			bw.close();

			System.out.println(str);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void executeMainLoop(){
		try {
			synchronized(mainLoopLock){
				{
					mainLoopLock.wait();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Executng Mainloop:"+Boolean.toString(eventsQueue.isEmpty())+" With length:"+Integer.toString(eventsMap.size()));
		while(true){
			RestaurantEventsByTime event;
			synchronized(mapsQueuesLock){
				if(eventsQueue.isEmpty()) break;
				event =  eventsQueue.remove();
			}
			currentTime = event.getEventTime();
			setNumberOfThreadsToCompleteExecution(0);
			System.out.println("Number of events at time:"+Integer.toString(currentTime)+" is"+Integer.toString(event.getEventList().size()));
			for(RestaurantEvent actualEvent : event.getEventList()){
				synchronized(actualEvent.getThreadToWakeUp()){
					actualEvent.getThreadToWakeUp().l_notify();
				}
			}
			try {
				synchronized(mainLoopLock){
					{
						mainLoopLock.wait();
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	public int getNumberOfThreadsToCompleteExecution() {
		return numberOfThreadsToCompleteExecution;
	}
	public void setNumberOfThreadsToCompleteExecution(
			int numberOfThreadsToCompleteExecution) {
		synchronized(numberOfThreadsToCompleteExecutionlock){
			this.numberOfThreadsToCompleteExecution = numberOfThreadsToCompleteExecution;
		}
	}
	public void decrementNumberOfThreadsToCompleteExecution(){
		synchronized(numberOfThreadsToCompleteExecutionlock){
			this.numberOfThreadsToCompleteExecution  -= 1;
		}
		if(getNumberOfThreadsToCompleteExecution() == 0){
		synchronized(mainLoopLock){
			mainLoopLock.notify();
		}
		}
	}
	public void incrementNumberOfThreadsToCompleteExecution(){
		synchronized(numberOfThreadsToCompleteExecutionlock){
			this.numberOfThreadsToCompleteExecution  += 1;
		}
	}
	public MenuItem getMenuItemOfType(MenuItemType type){
		synchronized(numberOfThreadsToCompleteExecutionlock){
			return menuItems.get(type);
		}
	}
}
