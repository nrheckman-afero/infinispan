package org.infinispan.hibernate.cache.commons.access;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.SoftLock;

/**
 * Defines the strategy for access to entity or collection data in an Infinispan instance.
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
public interface AccessDelegate {
	Object get(Object session, Object key, long txTimestamp) throws CacheException;

	/**
	 * Attempt to cache an object, after loading from the database.
	 *
	 * @param session Current session
	 * @param key The item key
	 * @param value The item
	 * @param txTimestamp a timestamp prior to the transaction start time
	 * @param version the item version number
	 * @return <code>true</code> if the object was successfully cached
	 */
	boolean putFromLoad(Object session, Object key, Object value, long txTimestamp, Object version);

	/**
	 * Attempt to cache an object, after loading from the database, explicitly
	 * specifying the minimalPut behavior.
	 *
	 * @param session Current session.
	 * @param key The item key
	 * @param value The item
	 * @param txTimestamp a timestamp prior to the transaction start time
	 * @param version the item version number
	 * @param minimalPutOverride Explicit minimalPut flag
	 * @return <code>true</code> if the object was successfully cached
	 * @throws org.hibernate.cache.CacheException Propagated from underlying {@link org.hibernate.cache.spi.Region}
	 */
	boolean putFromLoad(Object session, Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride)
			throws CacheException;

	/**
	 * Called after an item has been inserted (before the transaction completes),
	 * instead of calling evict().
	 *
	 * @param session Current session
	 * @param key The item key
	 * @param value The item
	 * @param version The item's version value
	 * @return Were the contents of the cache actual changed by this operation?
	 * @throws CacheException if the insert fails
	 */
	boolean insert(Object session, Object key, Object value, Object version) throws CacheException;

	/**
	 * Called after an item has been updated (before the transaction completes),
	 * instead of calling evict().
	 *
	 * @param session Current session
	 * @param key The item key
	 * @param value The item
	 * @param currentVersion The item's current version value
	 * @param previousVersion The item's previous version value
	 * @return Whether the contents of the cache actual changed by this operation
	 * @throws CacheException if the update fails
	 */
	boolean update(Object session, Object key, Object value, Object currentVersion, Object previousVersion)
			throws CacheException;

	/**
	 * Called after an item has become stale (before the transaction completes).
	 *
	 * @param session Current session
	 * @param key The key of the item to remove
	 * @throws CacheException if removing the cached item fails
	 */
	void remove(Object session, Object key) throws CacheException;

	/**
	 * Called just before the delegate will have all entries removed. Any work to prevent concurrent modifications
	 * while this occurs should happen here
	 * @throws CacheException if locking had an issue
	 */
	void lockAll() throws CacheException;

	/**
	 * Called just after the delegate had all entries removed via {@link #removeAll()}. Any work required to allow
	 * for new modifications to happen should be done here
	 * @throws CacheException if unlocking had an issue
	 */
	void unlockAll() throws CacheException;

	/**
	 * Called to evict data from the entire region
	 *
	 * @throws CacheException if eviction the region fails
	 */
	void removeAll() throws CacheException;

	/**
	 * Forcibly evict an item from the cache immediately without regard for transaction
	 * isolation.
	 *
	 * @param key The key of the item to remove
	 * @throws CacheException if evicting the item fails
	 */
	void evict(Object key) throws CacheException;

	/**
	 * Forcibly evict all items from the cache immediately without regard for transaction
	 * isolation.
	 *
	 * @throws CacheException if evicting items fails
	 */
	void evictAll() throws CacheException;

	/**
	 * Called when we have finished the attempted update/delete (which may or
	 * may not have been successful), after transaction completion.  This method
	 * is used by "asynchronous" concurrency strategies.
	 *
	 *
	 * @param session
	 * @param key The item key
	 * @throws org.hibernate.cache.CacheException Propagated from underlying {@link org.hibernate.cache.spi.Region}
	 */
	void unlockItem(Object session, Object key) throws CacheException;

	/**
	 * Called after an item has been inserted (after the transaction completes),
	 * instead of calling release().
	 * This method is used by "asynchronous" concurrency strategies.
	 *
	 *
	 * @param session
	 * @param key The item key
	 * @param value The item
	 * @param version The item's version value
	 * @return Were the contents of the cache actual changed by this operation?
	 * @throws CacheException Propagated from underlying {@link org.hibernate.cache.spi.Region}
	 */
	boolean afterInsert(Object session, Object key, Object value, Object version);

	/**
	 * Called after an item has been updated (after the transaction completes),
	 * instead of calling release().  This method is used by "asynchronous"
	 * concurrency strategies.
	 *
	 *
	 * @param session
	 * @param key The item key
	 * @param value The item
	 * @param currentVersion The item's current version value
	 * @param previousVersion The item's previous version value
	 * @param lock The lock
	 * @return Were the contents of the cache actual changed by this operation?
	 * @throws CacheException Propagated from underlying {@link org.hibernate.cache.spi.Region}
	 */
	boolean afterUpdate(Object session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock);
}
