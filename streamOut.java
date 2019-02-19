import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class streamOut extends Thread {
    Buckets b;

    streamOut(Buckets b){
        this.b = b;
    }

    public void run(){
        //System.out.println("thread is running ");
        Socket socket = null;
        DataInputStream DataIn = null;
        String line;

        try {
            socket = new Socket(P2.host, P2.port);
            DataIn = new DataInputStream(socket.getInputStream());

            while (DataIn.readLine()!= null) {
                line = DataIn.readLine();
                if(!line.isEmpty()) {
                    b.printBucket(line);
                }
                //System.out.println(line);
            }
            socket.close();
            DataIn.close();

            }catch (UnknownHostException e){
                System.out.println(e);
            }catch (IOException e){
                System.out.println(e);
            } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
