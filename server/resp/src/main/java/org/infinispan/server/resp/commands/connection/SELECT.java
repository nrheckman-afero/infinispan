package org.infinispan.server.resp.commands.connection;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.infinispan.Cache;
import org.infinispan.commons.CacheException;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.server.core.transport.ConnectionMetadata;
import org.infinispan.server.resp.AclCategory;
import org.infinispan.server.resp.Resp3Handler;
import org.infinispan.server.resp.RespCommand;
import org.infinispan.server.resp.RespRequestHandler;
import org.infinispan.server.resp.commands.Resp3Command;

import io.netty.channel.ChannelHandlerContext;

/**
 * SELECT
 *
 * @see <a href="https://redis.io/commands/select/">SELECT</a>
 * @since 14.0
 */
public class SELECT extends RespCommand implements Resp3Command {
   public SELECT() {
      super(-1, 0, 0, 0, AclCategory.FAST.mask() | AclCategory.CONNECTION.mask());
   }

   @Override
   public CompletionStage<RespRequestHandler> perform(Resp3Handler handler,
                                                      ChannelHandlerContext ctx,
                                                      List<byte[]> arguments) {
      String db = new String(arguments.get(0), StandardCharsets.US_ASCII);
      try {
         Cache<byte[], byte[]> cache = handler.respServer().getCacheManager().getCache(db);
         ConnectionMetadata metadata = ConnectionMetadata.getInstance(ctx.channel());
         handler.setCache(cache.getAdvancedCache().withSubject(metadata.subject()).withMediaType(MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_OCTET_STREAM));
         metadata.cache(cache.getName());
         handler.writer().ok();
      } catch (CacheException e) {
         handler.writer().customError("DB index is out of range");
      }
      return handler.myStage();
   }
}
