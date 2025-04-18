package org.infinispan.notifications.cachelistener.event;

import org.infinispan.metadata.Metadata;

/**
 * This event subtype is passed in to any method annotated with {@link org.infinispan.notifications.cachelistener.annotation.CacheEntryModified}
 * The {@link #getValue()} method's behavior is specific to whether the callback is triggered before or after the event
 * in question.  For example, if <code>event.isPre()</code> is <code>true</code>, then <code>event.getValue()</code> would return the
 * <i>old</i> value, prior to modification.  If <code>event.isPre()</code> is <code>false</code>, then <code>event.getValue()</code>
 * would return new <i>new</i> value.  If the event is creating and inserting a new entry, the old value would be <code>null</code>.
 * @author Manik Surtani
 * @since 4.0
 */
public interface CacheEntryModifiedEvent<K, V> extends CacheEntryEvent<K, V> {

   /**
    * Retrieves the value of the entry being modified.
       * @return the previous or new value of the entry, depending on whether isPre() is true or false.
    * @deprecated use {@link #getOldValue()} or {@link #getNewValue()} instead
    */
   @Deprecated(forRemoval=true, since = "13.0")
   V getValue();

   /**
    * Retrieves the old value of the entry being modified.
       * @return the previous value of the entry, regardless of whether isPre() is true or false.
    */
   V getOldValue();

   /**
    * Retrieves the new value of the entry being modified.
       * @return the new value of the entry, regardless of whether isPre() is true or false.
    */
   V getNewValue();

   /**
    * Regardless of whether <code>isPre()</code> is <code>true</code> or is
    * <code>false</code>, this method returns the metadata of the entry being
    * deleted. This method is useful for situations where cache listeners
    * need to know what the old value being deleted is when getting
    * <code>isPre()</code> is <code>false</code> callbacks.
    *
    * @return the metadata of the entry being deleted, regardless of
    * <code>isPre()</code> value
    */
   Metadata getOldMetadata();

   /**
    * Indicates whether the cache entry modification event is the result of
    * the cache entry being created. This method helps determine if the cache
    * entry was created when <code>event.isPre()</code> is <code>false</code>.
    *
    * @return true if the event is the result of the entry being created,
    * otherwise it returns false indicating that the event was the result of
    * a cache entry being updated
    */
   boolean isCreated();

   /**
    * This will be true if the write command that caused this had to be retried again due to a topology change.  This
    * could be a sign that this event has been duplicated or another event was dropped and replaced
    * (eg: ModifiedEvent replaced CreateEvent)
    * @return Whether the command that caused this event was retried
    */
   boolean isCommandRetried();

}
