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
	int bytesCountToRead = 16384; // 16Kb
	// CountThread[] pool = new CountThread[maxThreadCount];
	ArrayList<CountThread> pool = new ArrayList<CountThread>(maxThreadCount);

	// byte[] part;

	public static void main(String[] args) {
		CheckSumCounter ch = new CheckSumCounter();
		ch.countChecksum();
	}

	class CountThread extends Thread {
		byte[] bytes;
		private boolean busy = true;
		private boolean terminated = false;

		public void run() {
			while (!terminated) {
				try {
						synchronized (this) {
							busy = false;
							this.wait();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
						System.out.println("aaa ");
						break;
				}
				countCs();
			}

			
		}

		private void countCs() {
			int checkSum = 0;
			for (byte b : bytes) {
				checkSum ^= b;
			}
			synchronized (CheckSumCounter.this) {
				checkSumSum ^= checkSum;
			}
			System.out.println("checkSum " + checkSum);
		}
		
		public boolean setTask(byte[] bytes){
			synchronized (this) {
				if(busy) {
					return false;
				}
				busy = true;
				setBytes(bytes);
				notify();
				return true;
			}
		}

		public void setBytes(byte[] bytes) {
			this.bytes = bytes;
		}

		public void setTerminated(boolean terminated) {
			this.terminated = terminated;
		}

		public boolean isBusy() {
			return busy;
		}
	
		public void setBusy(boolean busy) {
			this.busy = busy;
		}
	}

	void schedule(byte[] part) {
		boolean isPartPassed = false;
		while (!isPartPassed) {
			for (CountThread th : pool) {
				if (th.setTask(part)){
					isPartPassed = true;
					break;
				}
			}
		}
	}

	void waitForTaskCompleting() throws InterruptedException {
		boolean ready = false;
		System.out.println("finish them!");
		while (!ready) {
			for (CountThread th : pool) {
				synchronized (th) {
					th.setTerminated(true);
					th.notify();
					th.join();
				}
				System.out.println("finished");
			}
			ready = true;

		}

	}

	public void countChecksum() {
		try {

			File f = new File("test.wmv");
			InputStream fi = new FileInputStream(f);

			// byte[] part = new byte[bytesCountToRead];

			byte[] allBytes = new byte[(int) f.length()];

			fi.read(allBytes);

			initPool();

			System.out.println(pool.size());

			ByteArrayInputStream data = new ByteArrayInputStream(allBytes);

			// while(fi.available() >= bytesCountToRead) {
			while (data.available() > 0) {
				byte[] part = new byte[bytesCountToRead];
				data.read(part);
				// if (data.available() < part.length) {
				// System.out.println(part[part.length-1] + " " +
				// part[part.length-2] + " " + part[part.length-3] );
				// }
				schedule(part);
			}
			System.out.println("all data passed");
			try {
				waitForTaskCompleting();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("finally: cs = " + checkSumSum);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initPool() {
		for (int i = 0; i < maxThreadCount; i++) {
			CountThread th = new CountThread();
			th.start();
			pool.add(i, th);

		}

	}

}