
https://blogs.oracle.com/javamagazine/post/java-thread-synchronization-raceconditions-locks-conditions

public void transfer(int from, int to, double amount)
   // CAUTION: unsafe when called from multiple threads
{
   System.out.print(Thread.currentThread());
   accounts[from] -= amount;
   System.out.printf(" %10.2f from %d to %d", amount, from, to);
   accounts[to] += amount;
   System.out.printf(" Total Balance: %10.2f%n", getTotalBalance());
}


Runnable r = () -> {
  
  try
      {
         while (true)
         {
            int toAccount = (int) (bank.size() * Math.random());
            double amount = MAX_AMOUNT * Math.random();
            bank.transfer(fromAccount, toAccount, amount);
            Thread.sleep((int) (DELAY * Math.random()));
         }
      }
      catch (InterruptedException e) {}

}

Thread[Thread-4,5,main]Thread[Thread-33,5,main] 
7.31 from 31 to 32 Total Balance: 99979.24
627.50 from 4 to 5 Total Balance: 99979.24

As you can see, something is very wrong. For a few transactions,
the bank balance remains at $100,000, which is the correct total for 100 accounts of $1,000 each.
But after some time, the balance changes slightly. 
The errors may happen quickly, or it may take a very long time for the balance to become corrupted. 
This situation does not inspire confidence, 
and you would probably not want to deposit your hard-earned money in such a bank.


public class Bank
{
   private Lock bankLock = new ReentrantLock();
   ...
   public void transfer(int from, int to, int amount)
   {
      bankLock.lock();
      try
      {
         System.out.print(Thread.currentThread());
         accounts[from] -= amount;
         System.out.printf(" %10.2f from %d to %d", amount, from, to);
         accounts[to] += amount;
         System.out.printf(" Total Balance: %10.2f%n", getTotalBalance());
      }
      finally
      {
         bankLock.unlock();
      }
   }
}


with condition and lock 

public class Bank
{
   private final double[] accounts;
   private Lock bankLock;
   private Condition sufficientFunds;
   
   public Bank(int n, double initialBalance)
   {
      accounts = new double[n];
      Arrays.fill(accounts, initialBalance);
      bankLock = new ReentrantLock();
      sufficientFunds = bankLock.newCondition();
   }
   
  
  public void transfer(int from, int to, double amount) throws InterruptedException
   {
      bankLock.lock();
      try
      {
         while (accounts[from] < amount)
            sufficientFunds.await();
         System.out.print(Thread.currentThread());
         accounts[from] -= amount;
         System.out.printf(" %10.2f from %d to %d", amount, from, to);
         accounts[to] += amount;
         System.out.printf(" Total Balance: %10.2f%n", getTotalBalance());
         sufficientFunds.signalAll();
      }
      finally
      {
         bankLock.unlock();
      }
   }

public double getTotalBalance()
   {
      bankLock.lock();
      try
      {
         double sum = 0;
         for (double a : accounts)
            sum += a;
            return sum;
      }
      finally
      {
         bankLock.unlock();
      }
   }
   
}
