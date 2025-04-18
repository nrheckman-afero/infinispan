package org.infinispan.test.hibernate.cache.commons.tm;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

/**
 * XaResourceCapableTransactionManagerImpl.
 *
 * @author Galder Zamarreño
 * @since 3.5
 */
public class XaTransactionManagerImpl implements TransactionManager {
   private static final XaTransactionManagerImpl INSTANCE = new XaTransactionManagerImpl();
   private final ThreadLocal<XaTransactionImpl> currentTransaction = new ThreadLocal<>();

   public static XaTransactionManagerImpl getInstance() {
      return INSTANCE;
   }

   public int getStatus() throws SystemException {
      XaTransactionImpl currentTransaction = this.currentTransaction.get();
      return currentTransaction == null ? Status.STATUS_NO_TRANSACTION : currentTransaction.getStatus();
   }

   public Transaction getTransaction() throws SystemException {
      return currentTransaction.get();
   }

   public XaTransactionImpl getCurrentTransaction() {
      return currentTransaction.get();
   }

   public void begin() throws NotSupportedException, SystemException {
      if (currentTransaction.get() != null) throw new IllegalStateException("Transaction already started.");
      currentTransaction.set(new XaTransactionImpl(this));
   }

   public Transaction suspend() throws SystemException {
      Transaction suspended = currentTransaction.get();
      currentTransaction.remove();
      return suspended;
   }

   public void resume(Transaction transaction) throws InvalidTransactionException, IllegalStateException,
            SystemException {
      currentTransaction.set((XaTransactionImpl) transaction);
   }

   public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, IllegalStateException, SystemException {
      XaTransactionImpl currentTransaction = this.currentTransaction.get();
      if (currentTransaction == null) {
         throw new IllegalStateException("no current transaction to commit");
      }
      currentTransaction.commit();
   }

   public void rollback() throws IllegalStateException, SecurityException, SystemException {
      XaTransactionImpl currentTransaction = this.currentTransaction.get();
      if (currentTransaction == null) {
         throw new IllegalStateException("no current transaction");
      }
      currentTransaction.rollback();
   }

   public void setRollbackOnly() throws IllegalStateException, SystemException {
      XaTransactionImpl currentTransaction = this.currentTransaction.get();
      if (currentTransaction == null) {
         throw new IllegalStateException("no current transaction");
      }
      currentTransaction.setRollbackOnly();
   }

   public void setTransactionTimeout(int i) throws SystemException {
   }

   void endCurrent(Transaction transaction) {
      currentTransaction.remove();
   }
}
