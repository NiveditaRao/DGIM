import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Buckets {
    List<List<UnitWindow>> windowContainer; // single window
    List<List<UnitBuck>> bucketContainer;// 16 streams for 16 bits.
    int[][] freqCount = new int[16][50];
    int WindowSize;
    long timeStampCounter = 0;
    int r;
    private final Lock lock = new ReentrantLock();
    private final Condition inp;

    // constructor
    Buckets(int n,int r){
        windowContainer = new ArrayList<>();
        bucketContainer = new ArrayList<>();
        WindowSize = n;
        this.r= r;
        inp = lock.newCondition();

        for(int i=0;i<16;i++){
            List<UnitWindow> temp = new ArrayList<>();
            windowContainer.add(temp);
        }

        for (int i=0;i<16;i++){
            List<UnitBuck> temp = new ArrayList<>();
            bucketContainer.add(temp);
        }
        //System.out.println("size of bucket" + bucketContainer.size());
       // System.out.println("Just set the window size to "+windowContainer.size());
    }

    //function to convert integers to binary.
    public String convertToBinary(String n){
        int raw = Integer.parseInt(n);
        String binary = Integer.toBinaryString(raw);
        //System.out.println(binary);
        if (binary.length()>16){
            System.exit(1);
        }
        return binary;
    }

    //add the binary digit to the window
    public void addBitToWindow(int binary,int streamNumber){
        if(timeStampCounter > WindowSize-1){
            windowContainer.get(streamNumber).remove(0);
        }
        windowContainer.get(streamNumber).add(new UnitWindow(binary,timeStampCounter));
        //adds to the last
        //removes the first.
    }

    public void addBitToBucket(int bit,long timeStampCounter,int streamNumber){
        UnitBuck temp = new UnitBuck(1,timeStampCounter);

        if(bit==1){
            if (bucketContainer.get(streamNumber).size()==0){
                bucketContainer.get(streamNumber).add(0,temp);
                freqCount[streamNumber][0]=1;
            }
            else {
                bucketContainer.get(streamNumber).add(temp);// add the new bucket at index 0
                int index = 0;//index of freq
                freqCount[streamNumber][index]++; // increment the frequency count
                if (freqCount[streamNumber][index] > r){
                    while(index<10000){
                        int tempIndex=0;
                        // get to the oldest element of the index we are looking at.
                        if (index>0) {
                            for (int k = 0; k <= index; k++) {
                                tempIndex = tempIndex + freqCount[streamNumber][k];
                            }
                            tempIndex = bucketContainer.get(streamNumber).size()-tempIndex;
                        }
                        else {
                            tempIndex = bucketContainer.get(streamNumber).size() - r-1;
                        }
                        //update the second oldest.
                        bucketContainer.get(streamNumber).get(tempIndex+1).buckSize = (bucketContainer.get(streamNumber).get(tempIndex+1).buckSize)*2;
                        // change
                        bucketContainer.get(streamNumber).remove(tempIndex);
                        freqCount[streamNumber][index]=freqCount[streamNumber][index]-2;
                        freqCount[streamNumber][index+1]++;
                        // change
                        if (freqCount[streamNumber][index+1]<=r){
                            break;
                        }
                        else {
                            index++;
                        }
                    }

                }
            }
        }
    }

    public double countSum(Long k){
        double acksum=0;
        if (k> timeStampCounter){
            System.out.println("not enough data");
        }
        else{
            //int[] sumHolder = new int[16];
            //System.out.println("time "+timeStampCounter);

            acksum = 0;
            for(int i=0;i<16;i++){
                //sum for each streams -
                long timeoverlap = 0; // time overlap
                int tempsum = 0;
                int index = bucketContainer.get(i).size()-1;
                //System.out.println("index " + bucketContainer.get(i).get(0).buckSize);

                while(((timeoverlap + (bucketContainer.get(i).get(index).startTimeStamp - bucketContainer.get(i).get(index-1).startTimeStamp)) <k) && ((index)>=1)){
                    tempsum = tempsum + (bucketContainer.get(i).get(index).buckSize);
                    timeoverlap = timeoverlap + (bucketContainer.get(i).get(index).startTimeStamp - bucketContainer.get(i).get(index-1).startTimeStamp);
                    index--;
                }

                if(bucketContainer.get(i).get(index).startTimeStamp < k) {
                    tempsum = tempsum + ((bucketContainer.get(i).get(index).buckSize) / 2);
                }
                //System.out.println("tempsum"+tempsum);
                acksum = acksum + Math.pow(2,i)*tempsum;
            }

        }
        return acksum;

    }

    //function used by streamout thread
    public void printBucket(String n) throws InterruptedException {
        //System.out.println("in");
        String binString = convertToBinary(n);
        int binlen = binString.length();
        //System.out.println("binstring "+binString);
        lock.lock();
        for(int i=16-binlen;i<16;i++){
            //System.out.println("char at sent "+binString.charAt(i-(16-binlen)));
            addBitToWindow(Integer.parseInt(new Character(binString.charAt(i-(16-binlen))).toString()),i);
            addBitToBucket(Integer.parseInt(new Character(binString.charAt(i-(16-binlen))).toString()),timeStampCounter,i);
        }
        for(int i = 0;i<16-binlen;i++){
            addBitToWindow(0,i);
            addBitToBucket(0,timeStampCounter,i);
        }
        timeStampCounter++;
        //lock.lock();
        lock.unlock();
    }



    public void printQuery() {
        Scanner input = new Scanner(System.in);
        //System.out.println("enter the query..");
        String n = input.nextLine();
        System.out.println(n);
        long actualN = 0L;

        if(!n.isEmpty()) {
            String nPrime = n.replaceAll("\\D+", "");
            actualN = Long.parseLong(nPrime);
            if (n.equals("end")) {
                System.exit(0);
            }
        }
        if(!n.isEmpty()){
            lock.lock();
            //System.out.println("lockedd");
            //take the input give it to function to compute.
            double ans = countSum(actualN);
            System.out.println("The sum of last "+actualN+"integers is " + ans);
            lock.unlock();
            //System.out.println("unlocked");
        }

    }

}
