package org.infinispan.server.resp.json;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.container.impl.InternalEntryFactory;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.functional.FunctionalMap;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadWriteMapImpl;

/**
 * A cache implementation for JSON data, providing various methods for interacting with and
 * manipulating JSON objects, arrays, and values. This class includes methods for setting,
 * retrieving, and querying JSON data in an embedded cache.
 *
 * <p>
 * Note: The implementation provides a set of functionalities for handling JSON objects, including
 * operations like recursively extracting values, checking types, and working with specific paths.
 * </p>
 *
 * @author Vittorio Rigamonti
 * @author Katia Aresti
 * @since 15.2
 */
public class EmbeddedJsonCache {
   public static final String ERR_KEY_CAN_T_BE_NULL = "key can't be null";
   public static final String ERR_VALUE_CAN_T_BE_NULL = "value can't be null";
   protected final FunctionalMap.ReadWriteMap<byte[], JsonBucket> readWriteMap;
   protected final AdvancedCache<byte[], JsonBucket> cache;
   protected final InternalEntryFactory entryFactory;

   public EmbeddedJsonCache(Cache<byte[], JsonBucket> cache) {
      this.cache = cache.getAdvancedCache();
      FunctionalMapImpl<byte[], JsonBucket> functionalMap = FunctionalMapImpl.create(this.cache);
      this.readWriteMap = ReadWriteMapImpl.create(functionalMap);
      this.entryFactory = ComponentRegistry.of(this.cache).getInternalEntryFactory().running();
   }

   /**
    * Retrieves the JSON value at the specified paths within the given key. The resulting JSON
    * content can be formatted with the provided spacing, newline, and indentation settings.
    *
    * @param key
    *           The key from which the JSON value will be retrieved, represented as a byte array.
    * @param paths
    *           A list of JSON paths used to access specific values within the JSON, each
    *           represented as a byte array.
    * @param space
    *           The byte array used to represent spaces for formatting the JSON output.
    * @param newline
    *           The byte array used to represent newline characters for formatting the JSON output.
    * @param indent
    *           The byte array used to represent indentation characters for formatting the JSON
    *           output.
    * @return A {@link CompletionStage} containing the formatted JSON content as a byte array.
    */
   public CompletionStage<byte[]> get(byte[] key, List<byte[]> paths, byte[] space, byte[] newline, byte[] indent) {
      return readWriteMap.eval(key, new JsonGetFunction(paths, space, newline, indent));
   }

   /**
    * Sets a JSON value at the specified path in the given key.
    *
    * @param key
    *           The key in which the JSON value should be stored, represented as a byte array.
    * @param value
    *           The JSON value to set, represented as a byte array.
    * @param path
    *           The JSON path where the value should be inserted, represented as a byte array.
    * @param nx
    *           If {@code true}, the operation will only succeed if the key does not already exist
    *           (NX - "Not Exists").
    * @param xx
    *           If {@code true}, the operation will only succeed if the key already exists (XX -
    *           "Exists").
    * @return A {@link CompletionStage} containing the result of the operation as a {@link String}.
    */
   public CompletionStage<String> set(byte[] key, byte[] value, byte[] path, boolean nx, boolean xx) {
      requireNonNull(key, ERR_KEY_CAN_T_BE_NULL);
      requireNonNull(value, ERR_VALUE_CAN_T_BE_NULL);
      return readWriteMap.eval(key, new JsonSetFunction(value, path, nx, xx));
   }

   /**
    * Retrieves the length of a JSON value at the specified path based on the given {@link LenType}.
    * <p>
    * Returns:
    * </p>
    * <ul>
    * <li>Object → Number of key-value pairs</li>
    * <li>Array → Number of elements</li>
    * <li>String → Character count</li>
    * </ul>
    * Returns {@code null} if the value does not match the specified {@code lenType}.
    *
    * @param key
    *           The JSON document key.
    * @param path
    *           The JSON path to evaluate.
    * @param lenType
    *           The type (OBJECT, ARRAY, STRING) whose length to compute.
    * @return A {@link CompletionStage} with a {@link List} of lengths or {@code null} if
    *         mismatched.
    */
   public CompletionStage<List<Long>> len(byte[] key, byte[] path, LenType lenType) {
      requireNonNull(key, ERR_KEY_CAN_T_BE_NULL);
      return readWriteMap.eval(key, new JsonLenFunction(path, lenType));
   }

   /**
    * Reports the type of the JSON value at the specified path within the given JSON. The result
    * will indicate the type of the value at each path in the list.
    *
    * @param key
    *           The key representing the JSON document, provided as a byte array.
    * @param path
    *           The JSON path at which the type of the value should be determined, provided as a
    *           byte array.
    * @return A {@link CompletionStage} containing a {@link List} of type strings, representing the
    *         type of the JSON value at each path (e.g., "object", "array", "string", etc.).
    */
   public CompletionStage<List<String>> type(byte[] key, byte[] path) {
      return readWriteMap.eval(key, new JsonTypeFunction(path));
   }

   /**
    * Deletes the value at the given path in the JSON document.
    *
    * @param key
    *           the key of the JSON document
    * @param path
    *           the path to the value to be deleted
    * @return a {@link CompletionStage} of the number of bytes deleted
    */
   public CompletionStage<Long> del(byte[] key, byte[] path) {
      return readWriteMap.eval(key, new JsonDelFunction(path));
   }

   /**
    * Appends the given values to the array at the specified paths in the JSON document associated
    * with the specified key. If the paths does not refer to an array, no changes are made to the
    * document.
    *
    * @param key
    *           The key of the JSON document to update.
    * @param path
    *           The JSON path of the array to append to.
    * @param values
    *           The values to append to the array.
    * @return A {@link CompletionStage} that will complete with the returning a list of the new
    *         lengths of the changed arrays. Null is returned for the matching paths that are not
    *         arrays.
    */
   public CompletionStage<List<Long>> arrAppend(byte[] key, byte[] path, List<byte[]> values) {
      return readWriteMap.eval(key, new JsonAppendFunction(path, values));
   }

   /**
    * Appends the given value to the string at the specified paths in the JSON document associated
    * with the specified key. If the path exists but is not a string, no changes are made.
    * {@link IllegalArgumentException} is thrown.
    *
    * @param key
    *           the key identifying the JSON document
    * @param path
    *           the path to the array in the JSON document
    * @param value
    *           the value to append to the array
    * @return A {@link CompletionStage} that will complete with the returning a list of the new
    *         lengths of the changed string. Null is returned for the matching paths that are not
    *         string.
    */
   public CompletionStage<List<Long>> strAppend(byte[] key, byte[] path, byte[] value) {
      return readWriteMap.eval(key, new JsonAppendFunction(path, value));
   }

   /**
    * Toggles the boolean value at the specified JSON path in the stored JSON document.
    * If the value is `true`, it becomes `false`, and vice versa.
    * Non-boolean values result in `null`.
    *
    * @param key  The key identifying the JSON document in the Infinispan cache.
    * @param path The JSON path where the boolean value should be toggled.
    * @return A {@code CompletionStage} with a {@code List<Integer>} of results:
    *         <ul>
    *           <li>{@code 1} if toggled to {@code true}</li>
    *           <li>{@code 0} if toggled to {@code false}</li>
    *           <li>{@code null} if the value is not a boolean</li>
    *         </ul>
    */
   public CompletionStage<List<Integer>> toggle(byte[] key, byte[] path) {
      return readWriteMap.eval(key, new JsonToggleFunction(path));
   }
}
