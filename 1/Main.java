import java.util.*;


class Sock
{
    private int availability;
    private int color;
    Sock(int col)
    {
        color = col;
        availability = 1;
    }
    
    public int getColor(){
        return color;
    }

    public synchronized int getSock(){
        if(availability==1){
            availability = 0;
            return color;
        } else {
            return -1;
        }
    }

    public void setAvailability(){
        availability = 1;
    }
}


class MatchingMachine
{
    public static int availability = 2;
    private static Sock sock1;
    private static Sock sock2;

    public static synchronized void receiveSock(Sock sock){
        if(availability > 0)
        {
            if(availability == 2){
                sock1 = sock;
                System.out.format("socks1 set color %d\n", sock.getColor());
            } else if (availability == 1){
                sock2 = sock;
                System.out.format("socks2 set color %d\n", sock.getColor());
            }
            --availability;
            if (availability == 0)
            {
                int sockColor1 = sock1.getColor();
                int sockColor2 = sock2.getColor();
                if( sockColor1 == sockColor2 ){
                    System.out.format("matched pair of color: %d\n", sockColor1);
                    availability = 2;
                    sock1 = null;
                    sock2 = null;
                }
                else {
                    sock1.setAvailability();
                    SockSortingSystem sobj = null;
                    sobj.incrementColor(sockColor1);
                    sobj.incrementColor(sockColor2);
                    sock2.setAvailability();
                    availability = 2;
                }
            }
        }
    }
}


class SockSortingSystem
{
	private int totalSocks;

	private int sockArms;

    // 0
    public static int countRed; 
    // 1
    public static int countGreen;
    // 2
    public static int countBlue;
    // 3
    public static int countWhite;

    private ArrayList <Sock> SockArray;

	// constructor to create hashmap with given no. of socks and random color
	SockSortingSystem(int socks, int arms)
    {
        totalSocks = socks;
        sockArms = arms;
        countRed = 0;
        countGreen = 0;
        countWhite = 0;
        countBlue = 0;
        SockArray = new ArrayList<>(totalSocks);
        
        for(int i = 0;i<totalSocks;i++)
        {
            Random r = new Random();
            int randomColor = r.nextInt(4);
            System.out.format("Sock %d, Color %d\n",i,randomColor);
            Sock objToSet = new Sock(randomColor);
            if (randomColor == 0){
                countRed++;
            } else if (randomColor == 1)
            {
                countGreen++;
            } else if (randomColor == 2)
            {
                countBlue++;
            } else if (randomColor == 3)
            {
                countWhite++;
            }
            SockArray.add(objToSet);
        }

        for (int i =0; i<sockArms; i++) 
        {
            Thread thread = new Thread(createRunnableSockArms());
   			thread.start();
        }
    }

    public static void incrementColor(int color){
        if (color == 0){
            countRed++;
        } else if (color == 1)
        {
            countGreen++;
        } else if (color == 2)
        {
            countBlue++;
        } else if (color == 3)
        {
            countWhite++;
        }
    }

	private Runnable createRunnableSockArms(){
	    Runnable aRunnable = new Runnable(){
	        public void run(){
                MatchingMachine mobj = null;
                while(countRed>1 || countGreen>1 || countBlue>1 || countWhite>1 || mobj.availability<2)
                {
                    Sock sockObj = tryToGetRandomSock();
                    if (sockObj!=null){
                        int color = sockObj.getColor();
                        if (color == 0){
                            countRed--;
                        } else if (color == 1)
                        {
                            countGreen--;
                        } else if (color == 2)
                        {
                            countBlue--;
                        } else if (color == 3)
                        {
                            countWhite--;
                        }
                        MatchingMachine machineobj = null;
                        machineobj.receiveSock(sockObj);    
                    }
                }
	        }
	    };
	    return aRunnable;
	}

    private Sock tryToGetRandomSock(){
    	int upperRange = totalSocks;
    	Random r = new Random();
        int sockNumber = r.nextInt(totalSocks);
        System.out.format("sock number TRYING : %d\n", sockNumber);    	
    	Sock objs = SockArray.get(sockNumber);
        int status = objs.getSock();
    	if (status != -1){
            System.out.format("sock number SUCCESS : %d \n", sockNumber);
    		return objs;
    	}
        else{
            System.out.format("sock number FAILED : %d \n", sockNumber);
    		return null;
    	}
    }
}


public class Main{
   public static void main(String args[]){
        Scanner reader = new Scanner(System.in);
        System.out.print("Enter Total no. of socks: ");
        String totalsocks = reader.next();
        int totalSocks = Integer.parseInt(totalsocks);
        System.out.print("Enter Total no. of arms: ");
        String totalarms = reader.next();
        int sockArms = Integer.parseInt(totalarms);
   		SockSortingSystem obj = new SockSortingSystem(totalSocks, sockArms);
    }   
}
