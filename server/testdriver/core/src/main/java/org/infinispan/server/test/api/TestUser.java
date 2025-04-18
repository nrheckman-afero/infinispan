package org.infinispan.server.test.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Users and Roles generated by the testing driver
 *
 * @see org.infinispan.server.test.core.AbstractInfinispanServerDriver
 *
 * @author Katia Aresti
 * @since 11
 */
public enum TestUser {
   ADMIN("admin", "strongPassword", Arrays.asList("admin", "___schema_manager", "___script_manager")),
   OBSERVER("observer", "password", Collections.singletonList("observer")),
   APPLICATION("application", "somePassword", Collections.singletonList("application")),
   DEPLOYER("deployer", "lessStrongPassword", Collections.singletonList("deployer")),
   MONITOR("monitor", "weakPassword", Collections.singletonList("monitor")),
   READER("reader", "readerPassword", Collections.singletonList("reader")),
   WRITER("writer", "writerPassword", Collections.singletonList("writer")),
   ANONYMOUS(null, null, null);

   public static final EnumSet<TestUser> NON_ADMINS = EnumSet.complementOf(EnumSet.of(TestUser.ADMIN, TestUser.ANONYMOUS));
   public static final EnumSet<TestUser> ALL = EnumSet.complementOf(EnumSet.of(TestUser.ANONYMOUS));

   private final String user;
   private final String password;
   private final List<String> roles;

   TestUser(String user, String password, List<String> roles) {
      this.user = user;
      this.password = password;
      this.roles = roles;
   }

   public String getUser() {
      return user;
   }

   public String getPassword() {
      return password;
   }

   public List<String> getRoles() {
      return roles;
   }
}
