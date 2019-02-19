
public class queryInput extends Thread{
    Buckets b;

    queryInput(Buckets b){
        this.b = b;
    }

    public void run(){
       // System.out.println("enter...");
        while(true){
            b.printQuery();
        }
    }
}
