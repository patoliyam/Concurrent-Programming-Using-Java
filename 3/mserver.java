import java.util.*;
import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

class Shop
{
	private static final String COMMA_DELIMITER = ",";
	private static final String itemsFile = "items.csv";
	private static final String lessStockFile = "lessStockItems.csv";
	private static final String salesFile = "sales.csv";

	public List <String> getAllItemList(){
		BufferedReader br = null;
		try{
		    br = new BufferedReader(new FileReader(itemsFile));
		    List <String> itemList = new ArrayList<String>();
		    String line = "";
		    while ((line = br.readLine()) != null) 
		    {
		        String[] itemDetails = line.split(COMMA_DELIMITER);
		        itemList.add(itemDetails[0]);
		    }
		    return itemList;
		} catch (Exception e){
		    System.out.println("can't find items");
		    return new ArrayList<String>();
		}
	}

	public synchronized int updateStock(String[] alldecitem, List<String> unavailable)
	{
		System.out.println("update stock start");
		List<String> allItemList = getAllItemList();
		int flag = 1;
		for (int i=0; i<alldecitem.length; i++)
        {
			String res = checkAvailability(allItemList.get(i), Integer.parseInt(alldecitem[i]));
			if(!res.equals("")){
				flag = 0;
				unavailable.add(res);
			}
        }
        if (flag==0){
        	System.out.println("stock is less, send to user this info");
        	return 0;
        } else {
        	int totalTime = 0;
        	int totalPrice = 0;
        	for (int i=0; i<alldecitem.length; i++)
	        {
				totalTime += Integer.parseInt(alldecitem[i]) * updateStockOneItem(allItemList.get(i), Integer.parseInt(alldecitem[i]));
				int price = getItemPrice(allItemList.get(i))*Integer.parseInt(alldecitem[i]);
				totalPrice += price;
				System.out.println(getItemPrice(allItemList.get(i)));
				addToSale(allItemList.get(i), Integer.parseInt(alldecitem[i]), getItemPrice(allItemList.get(i)), price);
	        }	
			unavailable.add(Integer.toString(totalTime+2));    
			unavailable.add(Integer.toString(totalPrice));    
			//create file for sale maintanence
        }
        return 1;
	} 

	public String checkAvailability(String item, int decrement){
		try{
			File originalFile = new File(itemsFile);
	        BufferedReader br = new BufferedReader(new FileReader(originalFile));
	    	String line = null;
	        while ((line = br.readLine()) != null) {
		        String[] itemDetails = line.split(COMMA_DELIMITER);
		        if(itemDetails[0].equals(item)){
	            	int currentStock = Integer.parseInt(itemDetails[1]);
	            	int threshold = Integer.parseInt(itemDetails[2]);
	            	int newStock = currentStock - decrement;
	            	if (newStock<0){
	            		System.out.println("Stock less in check");
	        			br.close();
	        			return item;
	            	}   
	            }
	        }
	        br.close();
	        return "";
	    } catch (Exception e) {
	    	System.out.println("some error in checkAvailability due to BufferedReader");
	    	return "";
	    }
	}

	public int getItemPrice(String item){
		try{
			File originalFile = new File(itemsFile);
	        BufferedReader br = new BufferedReader(new FileReader(originalFile));
	    	String line = null;
	        while ((line = br.readLine()) != null) {
		        String[] itemDetails = line.split(COMMA_DELIMITER);
		        if(itemDetails[0].equals(item)){
	            	int itemPrice = Integer.parseInt(itemDetails[3]);
	            	return itemPrice; 
	            }
	        }
	        br.close();
	        return -1;
	    } catch (Exception e) {
	    	System.out.println("some error in getItemPreparingTime due to BufferedReader");
	    	return -1;
	    }
	}

	public int addToLessStock(String item, int newStock){
		try{
			File tempFile = new File("tempfile2.csv");
	        PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			File originalFile = new File(lessStockFile);
	        BufferedReader br = new BufferedReader(new FileReader(originalFile));

	        String line = null;
	        int found = 0;
	        while ((line = br.readLine()) != null) {
		        String[] itemDetails = line.split(COMMA_DELIMITER);
		        if(!itemDetails[0].equals(item)){
	            	pw.println(line);
	            	pw.flush();    
	            } else {
	            	line = itemDetails[0]+ "," + Integer.toString(newStock);
	            	pw.println(line);
	            	pw.flush();
	            	found = 1;    
	            }
	        }
	        if(found==0){
            	line = item + "," + Integer.toString(newStock);
	        	pw.println(line);
	        	pw.flush();	
	        }
	        pw.close();
	        br.close();
	        // Delete the original file
	        if (!originalFile.delete()) {
	            System.out.println("Could not delete file");
	            return 0;
	        }

	        // Rename the new file to the filename the original file had.
	        if (!tempFile.renameTo(originalFile)){
	            System.out.println("Could not rename file");
	            return 0;
	        }
	        return 1;
	    } catch(Exception e){
	    	System.out.println("error in lesssockadditoin");
	    	return 0;
	    }
	}

	public int addToSale(String item, int quantity, int perItemPrice, int price){
		try{
			if (quantity!=0){
				File file = new File(salesFile);
				FileWriter fr = new FileWriter(file, true);
				BufferedWriter br = new BufferedWriter(fr);
				DateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
				Date date = new Date();
				String outputText = outputFormat.format(date);
				br.write(outputText + "," + item+","+Integer.toString(quantity)+","+Integer.toString(perItemPrice)+","+Integer.toString(price)+"\n");
				br.close();
				fr.close();
			}
			return 1;
		} catch(Exception e){
			System.out.println("some error in addtosale");
			e.printStackTrace();
			return 0;
		}
	}

	public int updateStockOneItem(String item, int decrement){
		int curRate = 0;
		int curTime = 0;
		try{
			File tempFile = new File("tempfile.csv");
	        PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

	        File originalFile = new File(itemsFile);
	        BufferedReader br = new BufferedReader(new FileReader(originalFile));
        	String line = null;
	        while ((line = br.readLine()) != null) {

		        String[] itemDetails = line.split(COMMA_DELIMITER);
		        if(!itemDetails[0].equals(item)){
	            	pw.println(line);
	            	pw.flush();    
	            } else {
	            	int currentStock = Integer.parseInt(itemDetails[1]);
	            	int threshold = Integer.parseInt(itemDetails[2]);
	            	curRate = Integer.parseInt(itemDetails[3]);
	            	curTime = Integer.parseInt(itemDetails[4]);
	            	int newStock = currentStock - decrement;
	            	if (newStock < 0){
	            		System.out.println("Stock less in update");
            			return 0;
	            	}
	            	if(newStock <= threshold){
	            		// do something if some item gets out of stock
	            		if(addToLessStock(itemDetails[0], newStock)==0){
	            			System.out.println("some problem in addToLessStock");
	            		}
	            	}
	            	line = itemDetails[0]+ "," + Integer.toString(newStock) + "," + itemDetails[2] + "," + itemDetails[3] + "," + itemDetails[4];
	            	pw.println(line);
	            	pw.flush();    
	            }
	        }
	        pw.close();
	        br.close();
	        // Delete the original file
	        if (!originalFile.delete()) {
	            System.out.println("Could not delete file");
	            return 0;
	        }

	        // Rename the new file to the filename the original file had.
	        if (!tempFile.renameTo(originalFile)){
	            System.out.println("Could not rename file");
	            return 0;
	        }
			return curTime;
		} catch(Exception e){
	    	System.out.println("some error in updateStockOneItem due to BufferedReader");
			return 0;
		}
	}
}	

class MyServer extends Thread
{
	private Socket server; 
	private DataInputStream dis; 
    private DataOutputStream dos; 
	private Thread toWait;
	
	public MyServer(Socket ourserver, DataInputStream ourdis, DataOutputStream ourdos, Thread ourtoWait)  
    { 
        server = ourserver; 
        dis = ourdis; 
        dos = ourdos; 
        toWait = ourtoWait;
    }

    @Override
	public void run() {
        try {
        	if(toWait!=null){
        		toWait.join();
        	}
            String responseFromClient = dis.readUTF();
            switch (responseFromClient) {
            	case "1":
            		Shop obj = new Shop();
            		List <String> itemList = obj.getAllItemList();
            		String joinedItems = String.join(",", itemList);
        			dos.writeUTF(joinedItems);    
            		String res = dis.readUTF();
            		System.out.println(res);
			        String[] orderNo = res.split(",", 0);
        			List<String> unavailable = new ArrayList<String>();
        			int result = obj.updateStock(orderNo, unavailable);
        			if (result == 0){
        				System.out.println(unavailable);
        				dos.writeUTF("0");
        				// send the list of unavaialble items
        				dos.writeUTF(String.join(",", unavailable));

        			} else {
        				// send the totalTime, totalPrice 
        				System.out.println(unavailable);
        				dos.writeUTF("1");
        				dos.writeUTF(String.join(",", unavailable));
        			}
            		break;
        		case "exit":
        			server.close();
        			System.out.println("exit");
        			break;
        		default:
        			System.out.println("default");
            }
        } catch (Exception e){
            e.printStackTrace(); 
        }
   	}
}


public class mserver 
{
	public static void main(String[] args) throws IOException  
    { 
        // server is listening on port 5056 
        ServerSocket ss = new ServerSocket(10000); 
      	
      	List<Thread> allThread = new ArrayList<Thread>();
        // running infinite loop for getting 
        // client request 
        while (true)  
        { 
            Socket s = null;     
            try 
            { 
                s = ss.accept();                   
                System.out.println("A new client is connected : " + s); 
                  
                DataInputStream dis = new DataInputStream(s.getInputStream()); 
                DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
                  
                System.out.println("Assigning new thread for this client"); 
  				Thread t;
  				if (allThread.size() > 1)
  				{
	                t = new MyServer(s, dis, dos, allThread.get(allThread.size()-1)); 
  				} else {
	                t = new MyServer(s, dis, dos, null); 
  				}
                allThread.add(t);
                t.start();                   
            } 
            catch (Exception e){ 
            	System.out.println("otside exception");
                s.close(); 
                e.printStackTrace(); 
            } 
        } 
    }
}