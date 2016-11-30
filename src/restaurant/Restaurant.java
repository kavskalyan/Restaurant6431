package restaurant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class Restaurant {
	private int cooksTotalCount;
	private Integer currentTime;
	private int dinersTotalCount;
	private final int totalTime = 120;
	private int tablesTotalCount;
	private Integer tablesFreeCount;
	private int cooksFreeCount;
	private Queue<CookThread> freeCooksPool;
	private Queue<Integer> freeTablesPool;
	private List<DinerThread> dinerList;
	private List<CookThread> cookList;
	private Map<MenuItemType,MenuItem> menuItems;
	public static String inputFileName = "/Users/kalyan/Documents/OS/Assignment4/project-sample-input-2.txt";
	public static String outputFilename = "/Users/kalyan/Documents/OS/Assignment4/output-for-sample-input2.txt";
	private PriorityQueue< Integer > eventsQueue;
	private Map< Integer, RestaurantEventsByTime> eventsMap;
	private Integer numberOfThreadsToCompleteExecution;
	private CustomLock tablesLock;
	private CustomLock cooksLock;
	private Object mainLoopLock;
	private Object mapsQueuesLock;
	private Object logLock;
	public static void main(String[] args) {
		if(args != null && args.length >0 &&args[0] != null && !args[0].equals("")){
			Restaurant.inputFileName = args[0];
			System.out.println("InputFile:"+args[0]);
			File file = new File(Restaurant.inputFileName);
			if (!file.exists()) {
				System.out.println("Input file does not exist");
				System.exit(1);
			}
			if(args.length >1 &&args[1] != null  && !args[1].equals("")){
				Restaurant.outputFilename = args[1];
				System.out.println("OutputFile:"+args[1]);
			}
		}
		Restaurant restaurant = new Restaurant();
		restaurant.ParseAndProcessInput();
		restaurant.Start();
		//exit(0);
	}
	public Restaurant(){
		
		eventsQueue = new PriorityQueue<Integer>();
		eventsMap = new HashMap<Integer, RestaurantEventsByTime>();
		this.currentTime = 0;
		this.tablesFreeCount = 0;
		//this.currentTime = new I
		this.numberOfThreadsToCompleteExecution = 0;
		dinerList = new LinkedList<DinerThread>();
		cookList = new ArrayList<CookThread>();
		freeCooksPool = new LinkedList<CookThread>();
		freeTablesPool = new LinkedList<Integer>();
		menuItems = new HashMap<MenuItemType,MenuItem>();
		menuItems.put(MenuItemType.BURGER,new MenuItem(MenuItemType.BURGER, 5,1,this));
		menuItems.put(MenuItemType.FRIES, new MenuItem(MenuItemType.FRIES,3, 1,this));
		menuItems.put(MenuItemType.COKE,new MenuItem(MenuItemType.COKE,1, 1,this));
		tablesLock = new CustomLock(this);
		cooksLock = new CustomLock(this);
		mainLoopLock = new Object();
		logLock = new Object();
		mapsQueuesLock = new Object();
		// Making sure the output file is empty
		FileWriter fw;
		try {
			fw = new FileWriter(outputFilename);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void Start(){
		executeMainLoop();
		System.exit(1);
		
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
		synchronized(tablesFreeCount){
			return tablesFreeCount;
		}
	}
	public void setTablesFreeCount(int tablesFreeCount) {
		synchronized(this.tablesFreeCount){
			this.tablesFreeCount = tablesFreeCount;
		}
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
		synchronized(cooksLock){
			setCooksFreeCount(getCooksFreeCount()+1);
			freeCooksPool.add(o);
		}
	}
	public CookThread getFreeCook() throws InterruptedException{
		synchronized(cooksLock){
			while(getCooksFreeCount() <= 0)
				cooksLock.l_wait();
			setCooksFreeCount(getCooksFreeCount()-1);
			return freeCooksPool.remove();
		}
	}
	
	public void releaseCook(CookThread thread){
		synchronized(cooksLock){
			freeCooksPool.add(thread);
			setCooksFreeCount(getCooksFreeCount()+1);
			if(cooksLock.l_queue()){
				cooksLock.l_notify();
			}
		}
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
				eventsMap.put(evt.getEventTime(), eventListObj);
				eventsQueue.add(evt.getEventTime());
			}
		}
		return true;
	}
	public int getATable() throws InterruptedException{
		while(getTablesFreeCount() <= 0){
			System.out.println("Did not get a table"+ Integer.toString(getNumberOfThreadsToCompleteExecution()));
			tablesLock.l_wait();
		}
		setTablesFreeCount(getTablesFreeCount() - 1);
		synchronized(freeTablesPool){
			int freeTabelNumber = freeTablesPool.remove();
			return freeTabelNumber;
		}
	}
	public void releaseTable(int tableNumber){
		synchronized(freeTablesPool){
			freeTablesPool.add(tableNumber);
		}
		setTablesFreeCount(getTablesFreeCount() + 1);
		if(tablesLock.l_queue()){
			tablesLock.l_notify();
		}
	}
	public void WriteToOutput(String str){
		int currentTime = getCurrentTime();
		synchronized(logLock){
			try {
				String str1 = String.format("%02d", (currentTime / 60));
				String str2 = String.format("%02d", (currentTime % 60));
				str = str1+ ":" + str2 + " - "+str +"\n";
				FileWriter fw = new FileWriter(outputFilename,true);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(str);
				bw.close();
	
				System.out.println(str);
	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void executeMainLoop(){
		try {
			if(getNumberOfThreadsToCompleteExecution() > 0){
				synchronized(mainLoopLock){
					mainLoopLock.wait();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Executng Mainloop:"+Boolean.toString(eventsQueue.isEmpty())+" With length:"+Integer.toString(eventsMap.size()));
		while(true){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			RestaurantEventsByTime event;
			synchronized(mapsQueuesLock){
				if(eventsQueue.isEmpty()) break;
				int eventTime =  eventsQueue.remove();
				event = eventsMap.get(eventTime);
			}
			setCurrentTime(event.getEventTime());
			setNumberOfThreadsToCompleteExecution(0);
			System.out.println("Number of events at time:"+Integer.toString(currentTime)+" is"+Integer.toString(event.getEventList().size()));
			for(RestaurantEvent actualEvent : event.getEventList()){
				//synchronized(actualEvent.getThreadToWakeUp().getLocalLock()){
					System.out.println("Notifying");
					actualEvent.getThreadToWakeUp().l_notify();
				//}
			}
			try {
				synchronized(mainLoopLock){
					mainLoopLock.wait();					
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		WriteToOutput("The last diner leaves the restaurant");
		for(Thread thread: dinerList){
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(Thread thread: cookList){
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	public int getNumberOfThreadsToCompleteExecution() {
		synchronized(numberOfThreadsToCompleteExecution){
			return numberOfThreadsToCompleteExecution;
		}
	}
	public void setNumberOfThreadsToCompleteExecution(
			int numberOfThreadsToCompleteExecution) {
		synchronized(this.numberOfThreadsToCompleteExecution){
			this.numberOfThreadsToCompleteExecution = numberOfThreadsToCompleteExecution;
		}
	}
	public void decrementNumberOfThreadsToCompleteExecution(){
		synchronized(numberOfThreadsToCompleteExecution){
			this.numberOfThreadsToCompleteExecution  -= 1;
			System.out.println("Decrementing:"+ Integer.toString(numberOfThreadsToCompleteExecution));
		}
		if(getNumberOfThreadsToCompleteExecution() == 0){
			synchronized(mainLoopLock){
				mainLoopLock.notify();
			}
		}
	}
	public void incrementNumberOfThreadsToCompleteExecution(){
		synchronized(numberOfThreadsToCompleteExecution){
			this.numberOfThreadsToCompleteExecution  += 1;
			System.out.println("Incrementing:"+ Integer.toString(numberOfThreadsToCompleteExecution));
		}
	}
	public MenuItem getMenuItemOfType(MenuItemType type){
			return menuItems.get(type);
	}
}
