import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckSumCounter {
	int checkSumSum;
	int threadCount;
	int maxThreadCount = 5;
	int bytesCountToRead = 16384; //16Kb
//	CountThread[] pool = new CountThread[maxThreadCount];
	ArrayList<CountThread> pool = new ArrayList<CountThread>(maxThreadCount);
	
//	byte[] part;
	
    public static void main(String[] args) {
    	CheckSumCounter ch = new CheckSumCounter();
        ch.countChecksum();
    }
    
    class CountThread extends Thread{
    	byte[] bytes;
    	int checkSum = 0;
    	private boolean busy = false;
    	
    	public void run() {
    		while(bytes == null) {
    			try {
    				synchronized (this) {
						this.wait();
					}
    					

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					System.out.println("aaa ");
					continue;
				}
    		}
    		busy = true;
    		System.out.println("beginning to count ");
    		countCs(bytes);
    		busy = false;
    		bytes = null;
    		run();
//    		try {
//    			while(true) {
//    				wait();
//    			}
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
////				e.printStackTrace();
//				System.out.println("notified");
//			}
        }
    	
    	private void countCs(byte[] bytes) {
    		for (byte b: bytes) {       
            	checkSum ^= b;
            }
    		synchronized(CheckSumCounter.this) {
    			checkSumSum ^= checkSum;    
        	}
    		System.out.println("checkSum " + checkSum);
       	}
    	
    	public void setBytes(byte[] bytes) {
    		this.bytes = bytes;
    	}
    	
    	public boolean isBusy(){
    		return busy;
    	}
    }
    
    void schedule(byte[] part){
    	boolean isPartPassed = false;
    	System.out.println("schedule start");
    	System.out.println(pool.size());
    	while(! isPartPassed) {
    		for (CountThread th: pool) {
    			System.out.println("thread");
	    		if (! th.isBusy()) {
	    			System.out.println("not busy");
	    			th.setBytes(part);
	    			synchronized (th) {
	    				th.notify();
					}
	    			
	    			isPartPassed = true;
	    			break;
	    		}
    		}
    		
    	
    	}
    	System.out.println("part passed " + part.length);
    } 
    
    void waitForTaskCompleting() throws InterruptedException{
    	boolean ready = false;
    
    	while(! ready) {
    		for (CountThread th: pool) {
//    			System.out.println("thread");
	    		if (! th.isBusy()) {
//	    			System.out.println("not busy");
	    			
	    			synchronized (th) {
	    				th.notify();
	    				th.join();
					}
	    			System.out.println("finished");
	    		}
	    		System.out.println("poolsize" + pool.size());
    		}
    		System.out.println("poolsize" + pool.size());
    		if (pool.size() == 0) {
    			ready = true;
    		}
    	}
    	
    }
    
    
    public void countChecksum() {
        try {
        	
        	File f = new File("test.wmv");
     	    InputStream fi = new FileInputStream(f);
     	    
//        	byte[] part = new byte[bytesCountToRead];
        	
           	byte[] allBytes = new byte[(int)f.length()];
           	
           	fi.read(allBytes);
           	
           	initPool();
           	
           	System.out.println(pool.size());
           	
           	ByteArrayInputStream data = new ByteArrayInputStream(allBytes);
           	
           	
//        	while(fi.available() >= bytesCountToRead) {
           	while(data.available() > 0) {
           		byte[] part = new byte[bytesCountToRead];
           		data.read(part);
//           		if (data.available() < part.length) {
//           			System.out.println(part[part.length-1] + " " + part[part.length-2] + " " + part[part.length-3]  );
//           		}
           		schedule(part);
            }
           	System.out.println("all data passed");
           	try {
				waitForTaskCompleting();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           	
           	
        	System.out.println("cs " + checkSumSum);

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

    private void initPool(){
    	for (int i = 0; i < maxThreadCount; i++) {
    		CountThread th = new CountThread();
    		th.start();
    		pool.add(i,th);
    		
    	}
    	
    }
       
}