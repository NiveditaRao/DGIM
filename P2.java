import java.io.*;
import java.util.Scanner;

public class P2 {
    static String host;
    static int port;
    static int r;
    static final int MAX_NUM_BUCKETS = 30;
    public static void main(String[] args) throws IOException {
        System.out.println("started...");
        Scanner sc = new Scanner(System.in);
        String errorP = sc.nextLine();
        String hostport = sc.nextLine();
        String[] hostPortArr = hostport.split(":");
        host = hostPortArr[0];
        port = Integer.parseInt(hostPortArr[1]);
        r = getRFomErroPercentage(errorP);
        Buckets buck = new Buckets(MAX_NUM_BUCKETS,r);
        streamOut SO = new streamOut(buck);
        queryInput QO = new queryInput(buck);
        QO.start();
        SO.start();

    }

    public static int getRFomErroPercentage(String errorP){
        int r = Integer.parseInt(errorP.replaceAll("\\D+", ""));
        r = (int)Math.floor(100/r);
        //System.out.println("r " + r);
        return r;
    }
}
