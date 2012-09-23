import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

//class FixedThreadPool {
//    public static void main(String[] args) throws IOException {
//    	int maxThreadCount = 5;
//    	int bytesCountToRead = 16384*2000; //16Kb
//    	ExecutorService exec = Executors.newFixedThreadPool(maxThreadCount);
//    	InputStream fi = new FileInputStream("radio.avi");
//    	while(fi.available() >= bytesCountToRead) {
//
////        			System.out.println("threadCount " + threadCount);
//        			
//	        		byte[] bytes = new byte[bytesCountToRead];
//	        		fi.read(bytes);
//	        		Thread th = new CountThread(bytes);
//	        		th.start();
//	        		System.out.println("thread started");
////	        		th.join();
////	        		System.out.println("thread finished");
//        		}
//
//    	
//    }
// 
//	
//}


public class CheckSumCounter {
	int checkSumSum;
	int threadCount;
	int maxThreadCount = 5;
	int bytesCountToRead = 16384*2000; //16Kb
	
	
    public static void main(String[] args) {
    	CheckSumCounter ch = new CheckSumCounter();
        ch.countChecksum();
    }
    
    class CountThread extends Thread{
    	byte[] bytes;
    	int checkSum = 0;
    	
    	public CountThread(byte[] bytes) {
			this.bytes = bytes;
		}
    	
    	public void run() {
    		synchronized(CheckSumCounter.this) {
        		threadCount++;    
        	}
            for (byte b: bytes) {       
            	
            	checkSum ^= b;
            }
    		synchronized(CheckSumCounter.this) {
    			checkSumSum ^= checkSum;    
    			threadCount--;  
        	}
    		System.out.println(checkSum);
        }
    }
    
    public void countChecksum() {
        try {
        	InputStream fi = new FileInputStream("radio.avi");
        	
        	System.out.println(fi.available());
        	
        	while(fi.available() >= bytesCountToRead) {
    		
        		if (threadCount < maxThreadCount){
        			System.out.println("threadCount " + threadCount);
        			
	        		byte[] bytes = new byte[bytesCountToRead];
	        		fi.read(bytes);
	        		Thread th = new CountThread(bytes);
	        		th.start();
	        		System.out.println("thread started");
//	        		th.join();
//	        		System.out.println("thread finished");
        		}
        	}
        	
        	System.out.println("cs " + checkSumSum);
//            Thread th1 = new CountThread();
//            Thread th2 = new CountThread();
//            th1.start();
//            th2.start();
//            th1.join();
//            th2.join();
//            System.out.println(counter);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(CheckSumCounter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

       
}