import java.util.*;
import javax.swing.*;  
import java.net.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.*;

public class Main{
    private static Socket connection;

    public static void createFrame(String itemList){
        String[] allItems = itemList.split(",", 0);
        List<JTextField> allTextFields = new ArrayList<JTextField>();
        JFrame f=new JFrame();
        f.setSize(550,700);
        int cury = 50;
        for (String s: allItems) {           
            // curItem = allItems.get(i);
            JLabel label = new JLabel(s);
            label.setBounds(50,cury, 200,50);
            f.add(label);
            JTextField textfield = new JTextField();
            allTextFields.add(textfield);
            textfield.setBounds(300,cury, 200,50);
            f.add(textfield);
            cury = cury + 100;
        }
        
        JButton btn = new JButton("Submit");    
        btn.setBounds(225,550,100, 50);   
        f.add(btn);

        //close on closing the jframe
        // f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent we)
            {
                closeSocketConnection(connection);
                System.exit(0);
                // f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
        f.setLayout(null);//using no layout managers  
        f.setVisible(true);//making the frame visible 

        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String toSend = "";
                for(int i = 0; i<allTextFields.size(); ++i){
                    toSend += (String)allTextFields.get(i).getText();
                    toSend += ",";
                    // allTextFields.set(i, allTextFields.get(i).getText());
                }
                System.out.println(toSend);
                writeToServer(connection, toSend);
                String status = readFromServer(connection);
                if(status.equals("1")){
                    String a = readFromServer(connection);
                    JFrame popup=new JFrame();
                    popup.setSize(200,200);

                    JLabel resultPrice = new JLabel();
                    JLabel resultTime = new JLabel();
                    
                    resultPrice.setBounds(0, 10, 100, 50);
                    resultTime.setBounds(0, 70, 100, 50);
                    
                    resultPrice.setText("Total Price:" + a.split(",", 0)[1]);
                    resultTime.setText("Expected Waiting Time:" + a.split(",", 0)[0]);
            
                    popup.add(resultPrice);
                    popup.add(resultTime);
                    popup.setVisible(true);//making the frame visible 
                    f.setVisible(false);//making the frame visible 
                    System.out.println(a);
                } else {
                    String a = readFromServer(connection);
                    JFrame popup=new JFrame();
                    popup.setSize(300,200);
                    JLabel resultPrice = new JLabel();
                    resultPrice.setBounds(50, 50, 100, 50);
                    resultPrice.setText("Unavaialble: " + a);
                    popup.add(resultPrice);
                    popup.setVisible(true);//making the frame visible 
                    System.out.println(a);
                }

            }          
        });
    }

    public static Socket createSocketConnection(String address, int port){
        try {
            System.out.println("Connecting to " + address + " on port " + port);
            Socket client = new Socket(address, port);
            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            return client;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int closeSocketConnection(Socket connection){
        try {
            writeToServer(connection, "exit");
            connection.close();
            return 1;
        } catch (Exception e) {
            System.out.println("error in closing connection");
            return -1;
        }
    }

    public static int writeToServer(Socket connection, String toWrite){
        try{
            OutputStream outToServer = connection.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            // out.writeUTF("Hello from " + client.getLocalSocketAddress());
            out.writeUTF(toWrite);
            return 1;
        } catch (Exception e) {
            System.out.println("Error in sending to Server");    
            return -1;
        }
    }
    
    public static String readFromServer(Socket connection){
        try{
            InputStream inFromServer = connection.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            String fromServer = in.readUTF();
            System.out.println("Server says: " + fromServer);    
            return fromServer;
        } catch (Exception e){
            System.out.println("Error in reading from Server");    
            return "-1";
        }
    }

    public static void main(String args[]){
        int port = Integer.parseInt(args[0]);
        System.out.println(port);
        String address = "127.0.0.1"; 
        connection = createSocketConnection(address, port);
        if (connection!=null){
            writeToServer(connection, "1");
            String itemList = readFromServer(connection);
            createFrame(itemList);
        }
    }   
}
