import java.util.*;
import java.io.*;
import java.util.concurrent.CountDownLatch; 

class Edit
{
    private String teacher;
    private String rollNumber;
    private int marksUpdate;

    Edit(String per, String rollNo, int marksUpd){
        teacher = per;
        rollNumber = rollNo;
        marksUpdate = marksUpd;
    }
    public String getTeacher() {
        return teacher;
    }
    public String getRoll() {
        return rollNumber;
    }
    public int getMarksUpdate() {
        return marksUpdate;
    }
}

class Student
{
    private String roll;
    private String name;
    private String email;
    private int marks;
    private String teacher;

    private int index;
    private int locked;


    Student(String roll, String name, String email, int marks, String teacher, int index)
    {
        this.roll = roll;
        this.name = name;
        this.email = email;
        this.marks = marks;
        this.teacher = teacher;
        this.locked = 0;
        this.index = index;
    }

    public String getRoll() {
        return roll;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public int getMarks() {
        return marks;
    }
    public int getIndex() {
        return index;
    }
    public String getTeacher() {
        return teacher;
    }
    public synchronized void updateStudent(int marksUpdate, String teacherUpdate){
        
        System.out.format("Attempting to update student %s by teacher %s by marks %d\n", this.roll, teacherUpdate, marksUpdate);
        System.out.format("Previous editor : %s, Current editor : %s\n", this.teacher, teacherUpdate);

        if((teacherUpdate.equals("CC")) || (!this.teacher.equals("CC")))
		{
			this.marks += marksUpdate;
			this.teacher = teacherUpdate;
			System.out.format("Student %s updated by %s by %d\n", this.roll, teacherUpdate, marksUpdate);
		}
		else
		{
			System.out.format("Student %s could not be updated by %s by %d\n", this.roll, teacherUpdate, marksUpdate);	
		}
    }
    /*Comparator for sorting the list by Student Name*/
    public static Comparator<Student> StuNameComparator = new Comparator<Student>() {

        public int compare(Student s1, Student s2) {
           String StudentName1 = s1.getName().toUpperCase();
           String StudentName2 = s2.getName().toUpperCase();

           //ascending order
           return StudentName1.compareTo(StudentName2);

           //descending order
           //return StudentName2.compareTo(StudentName1);
    }};

    /*Comparator for sorting the list by roll no*/
    public static Comparator<Student> StuRollComparator = new Comparator<Student>() {

    public int compare(Student s1, Student s2) {

       String rollno1 = s1.getRoll();
       String rollno2 = s2.getRoll();

       /*For ascending order*/
       return rollno1.compareTo(rollno2);

       /*For descending order*/
       //return rollno2.compareTo(rollno1);
   }};

   @Override
    public String toString() {
        return "[ rollno=" + roll + ", name=" + name + ", email=" + email + ", marks=" + marks +  ", teacher=" + teacher + "]";
    }



}

class StudentSystem
{
    private static int totalStudents;
    private static String studentInfoFile;
   
   //Delimiters used in the CSV file
    private static final String COMMA_DELIMITER = ",";
    private static List <Student> studentList;
    private static List <Student> studentList1;
    private static List <Student> studentList2;
    private static List<Edit> allUpdates;

    StudentSystem(String filename) throws InterruptedException 
    {
    	//Reading the file and storing it in array of student structure.
        this.studentInfoFile= filename;
        BufferedReader br = null;
        try{
            //Reading the file
            br = new BufferedReader(new FileReader(studentInfoFile));
            studentList  = new ArrayList<Student>();
            studentList1 = new ArrayList<Student>();
            studentList2 = new ArrayList<Student>();
            String line  = "";
            //Read to skip the header
            br.readLine();
            //Reading from the second line
            int index=0;
            while ((line = br.readLine()) != null) 
            {
                String[] studentDetails = line.split(COMMA_DELIMITER);
                
                if(studentDetails.length > 0 )
                {
                    Student stud = new Student(studentDetails[0], 
                                            studentDetails[1],
                                            studentDetails[2],
                                            Integer.parseInt(studentDetails[3]),
                                            studentDetails[4],
                                            index);
                    studentList.add(stud);
                    index++;
                }
                this.totalStudents++;
            }
            //Replica for updates with syncronization 
            studentList1 = new ArrayList<Student>(studentList);
            //Replica for updates without syncronization 
            studentList2 = new ArrayList<Student>(studentList);
            
        }
        catch(Exception ee)
        {
            ee.printStackTrace();
        }

       	//Take input of all edits which need to be done.
        Scanner reader = new Scanner(System.in);
		int takeEditInput = 1;
		allUpdates = new ArrayList<Edit>();
		while(takeEditInput==1) {
			System.out.print("Enter Teacher: ");
			String teacha = reader.next();
			System.out.print("Enter Roll Number: ");
			String rollNu = reader.next();
			System.out.print("Enter Marks Update (+ve for increase and -ve for decrement): ");
			int marksUpdt = reader.nextInt();
			Edit editObj = new Edit(teacha, rollNu, marksUpdt);
			allUpdates.add(editObj);
			System.out.print("Enter more updates ? (1/0)");
			takeEditInput = reader.nextInt(); 
		}

		int runProgram = 1;
		while(runProgram == 1){
			System.out.print("Choose : with syncronization or without syncronization (1/0)");
			int sync = reader.nextInt();
			int numberOfThreads;
			CountDownLatch latch;
			if (sync == 1){
				// code for synchronisation.
				Set<String> distinctRoll = new HashSet<String>();
		    	for(Edit e : allUpdates)
		    	{
		    		distinctRoll.add(e.getRoll());
		    	}
				numberOfThreads = distinctRoll.size();
				latch = new CountDownLatch(numberOfThreads); 
				withSyncronization(latch);
	        	// wait for numberOfThreads threads to finish before it starts 
				latch.await();
				generateSortedRollFile(studentList1);
				System.out.println("Sorted_Roll.txt File Generated.");
				generateSortedNameFile(studentList1);;
				System.out.println("Sorted_Name.txt File Generated.");
				
			} 
			else 
			{
				// code for non synchronisation.
				numberOfThreads = allUpdates.size();
				latch = new CountDownLatch(numberOfThreads); 
				withoutSyncronization(latch);
	        	// wait for numberOfThreads threads to finish before it starts 
				latch.await();
				generateSortedRollFile(studentList2);
				System.out.println("Sorted_Roll.txt File Generated.");
				generateSortedNameFile(studentList2);;
				System.out.println("Sorted_Name.txt File Generated.");
				
			}
	        System.out.print("Continue ? (1/0)");
			runProgram = reader.nextInt();
		}
    }

    private static void withSyncronization(CountDownLatch latch)
    {
    	Set<String> studentSet = new HashSet<String>();
    	for(Edit e : allUpdates)
    	{

    		studentSet.add(e.getRoll());
    	}
    	for(String roll : studentSet)
    	{
    		
    		Thread thread = new Thread(updateGivenStudent(roll, latch));
            System.out.format("Thread for roll number %s formed.\n",roll);
            thread.start();
    	}
    }

    private static void withoutSyncronization(CountDownLatch latch)
    {
    	for(Edit e : allUpdates)
    	{
    		Thread thread = new Thread(updateGivenEdit(e, latch));
            thread.start();
    	}
    }

    private static Runnable updateGivenStudent(String roll, CountDownLatch latch)
    {
    	Runnable aRunnable = new Runnable(){
            public void run(){
            	String fooString = new String(roll);
		    	for(Edit e: allUpdates)
		    	{
		    		String fooString1 = new String(e.getRoll());
		    		if(fooString1.equals(fooString))
		    		{
		    			for(Student s : studentList1)
		    			{
							String fooString2 = new String(s.getRoll());
		    				if(fooString2.equals(fooString))
		    				{
		    					s.updateStudent(e.getMarksUpdate(), e.getTeacher());
		    				}
		    			}
		    		}
		    	}
				latch.countDown();
		    }
		};
		return aRunnable;
    }

    private static Runnable updateGivenEdit(Edit e, CountDownLatch latch)
    {
    	Runnable aRunnable = new Runnable(){
            public void run(){
				for(Student s : studentList2)
				{
					if(new String(s.getRoll()).equals(e.getRoll()))
					{
						s.updateStudent(e.getMarksUpdate(), e.getTeacher());
					
					}
				}
				latch.countDown();
			}
		};
		return aRunnable;
    }

    private static void generateSortedRollFile(List <Student> studentList3)
    {
        try{
            File tempFile = new File("Sorted_Roll.txt");
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
            String line = null;
            Collections.sort(studentList3, Student.StuRollComparator);
            for(Student s : studentList3)
            {
                line = s.getRoll()+ "," +s.getName()+ "," + s.getEmail()+","+Integer.toString(s.getMarks())+","+s.getTeacher();
                pw.println(line);
                pw.flush();    
            }
            pw.close();
        }
        catch(Exception ee){
            ee.printStackTrace();
        }
    }

    private static void generateSortedNameFile(List <Student> studentList3)
    {
        try{
            File tempFile = new File("Sorted_Name.txt");
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
            String line = null;
            Collections.sort(studentList3, Student.StuNameComparator);
            for(Student s : studentList3)
            {
                line = s.getRoll()+ "," +s.getName()+ "," + s.getEmail()+","+Integer.toString(s.getMarks())+","+s.getTeacher();
                pw.println(line);
                pw.flush();    
            }
            pw.close();
        }
        catch(Exception ee){
            ee.printStackTrace();
        }
    }
}



public class problem2{
   public static void main(String args[])
   		throws InterruptedException{
        StudentSystem obj = new StudentSystem("Stud_Info.txt");  
    } 
}


