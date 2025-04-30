import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class hw3 {

	public static void main(String[] args) throws InterruptedException {

		int size = 100000;

		int need = (int)(Math.random() * size / 2);

		boolean exs = false;
		Integer[] arr = new Integer[size];
		for (int i = 0; i < size; i++) {
			arr[i] = (int)(Math.random() * size / 2);
			if (arr[i] == need) { exs = true; }
		}
		if (!exs) { arr[size - 1] = need; }

		System.out.println("first needle index: " + hw3.<Integer>search(need, arr, 10));

	}

	public static <T> int search(T needle, T[] haystack, int numThreads) throws InterruptedException {

		int perTh = haystack.length / numThreads;
		int rem = haystack.length - perTh * numThreads;

		Lock lock = new ReentrantLock();
		Condition cond = lock.newCondition();
		CountDownLatch startSignal = new CountDownLatch(1);

		List<searcher<T>> searchers = new ArrayList<searcher<T>>();
		for (int i = 0; i < numThreads; i++) {
			searcher<T> s = new searcher<T>(needle, haystack, (perTh * i), ((perTh * (i + 1)) + ((i == (numThreads - 1)) ? rem : 0)), lock, cond, startSignal);
			searchers.add(s);
			s.start();
		}
		startSignal.countDown();

		lock.lock();
		try { cond.await(); } finally { lock.unlock(); }

		for (searcher<T> s : searchers) { s.die(); }

		for (searcher<T> s : searchers) {
			int r = s.res();
			if (r != -1) { return r; }
		}

		return -1;

	}

}

class searcher <T> extends Thread {

	private T needle;
	private T[] haystack;
	private int start;
	private int end;
	private static Lock lock;
	private Condition cond;
	private CountDownLatch startSignal;

	private AtomicBoolean done;
	private int n;

	public searcher(T needle, T[] haystack, int start, int end, Lock lock, Condition cond, CountDownLatch startSignal) {
		this.needle = needle;
		this.haystack = haystack;
		this.start = start;
		this.end = end;
		this.lock = lock;
		this.cond = cond;
		this.startSignal = startSignal;

		done = new AtomicBoolean(false);
		n = -1;

	}

	public void run() {

		try {

			startSignal.await();

			for (int i = start; i < end && !done.get(); i++){
				if (haystack[i].equals(needle)) {
					n = i;
					lock.lock();
					try { cond.signal(); } finally { lock.unlock(); }
					return;
				}
			}

		} catch (InterruptedException ex) {}

	}

	public void die() { done.set(true); }

	public int res() { return n; }

}
