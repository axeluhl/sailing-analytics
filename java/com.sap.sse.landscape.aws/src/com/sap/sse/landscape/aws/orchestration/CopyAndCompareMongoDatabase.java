package com.sap.sse.landscape.aws.orchestration;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import com.mongodb.client.MongoDatabase;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.util.ThreadPoolUtil;

/**
 * A procedure that is provided with a source and a target database configuration, expected to differ from one another.
 * The content of the source database is copied to the target and then compared using MD5 hashes. Those hashes are computed
 * using {@link Database#getMD5Hash()}.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ShardingKey>
 */
public class CopyAndCompareMongoDatabase<ShardingKey>
extends AbstractAwsProcedureImpl<ShardingKey> {
    private static final Logger logger = Logger.getLogger(CopyAndCompareMongoDatabase.BuilderImpl.class.getName());
    private final Database sourceDatabase;
    private final Database targetDatabase;
    private final Iterable<Database> additionalDatabasesToDelete;

    public static interface Builder<BuilderT extends AbstractAwsProcedureImpl.Builder<BuilderT, CopyAndCompareMongoDatabase<ShardingKey>, ShardingKey>, ShardingKey>
    extends AbstractAwsProcedureImpl.Builder<BuilderT, CopyAndCompareMongoDatabase<ShardingKey>, ShardingKey> {
        BuilderT setSourceDatabase(Database sourceDatabase);
        BuilderT setTargetDatabase(Database targetDatabase);
        BuilderT setAdditionalDatabasesToDelete(Iterable<Database> additionalDatabasesToDelete);
    }
    
    public static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends AbstractAwsProcedureImpl.BuilderImpl<BuilderT, CopyAndCompareMongoDatabase<ShardingKey>, ShardingKey>
    implements Builder<BuilderT, ShardingKey> {
        private Database sourceDatabase;
        private Database targetDatabase;
        private Iterable<Database> additionalDatabasesToDelete;
        
        @Override
        public CopyAndCompareMongoDatabase<ShardingKey> build() throws Exception {
            return new CopyAndCompareMongoDatabase<>(this);
        }

        @Override
        public BuilderT setSourceDatabase(Database sourceDatabase) {
            this.sourceDatabase = sourceDatabase;
            return self();
        }

        @Override
        public BuilderT setTargetDatabase(Database targetDatabase) {
            this.targetDatabase = targetDatabase;
            return self();
        }

        @Override
        public BuilderT setAdditionalDatabasesToDelete(Iterable<Database> additionalDatabasesToDelete) {
            this.additionalDatabasesToDelete = additionalDatabasesToDelete;
            return self();
        }

        protected Database getSourceDatabase() {
            return sourceDatabase;
        }

        protected Database getTargetDatabase() {
            return targetDatabase;
        }

        protected Iterable<Database> getAdditionalDatabasesToDelete() {
            return additionalDatabasesToDelete;
        }
    }
    
    protected CopyAndCompareMongoDatabase(BuilderImpl<?, ShardingKey> builder) {
        super(builder);
        this.sourceDatabase = builder.getSourceDatabase();
        this.targetDatabase = builder.getTargetDatabase();
        this.additionalDatabasesToDelete = builder.getAdditionalDatabasesToDelete();
    }

    @Override
    public void run() throws Exception {
        if (targetDatabase.equals(sourceDatabase)) {
            throw new IllegalArgumentException("Source and target database must be different: "+sourceDatabase);
        }
        final MongoDatabase result = targetDatabase.getEndpoint().importDatabase(sourceDatabase.getMongoDatabase());
        if (result == null) {
            throw new IllegalStateException("This didn't work. No database resulted from importing "+sourceDatabase+" into "+targetDatabase);
        }
        final ScheduledExecutorService executor = ThreadPoolUtil.INSTANCE.createBackgroundTaskThreadPoolExecutor(2, "MongoDB MD5 Hasher "+UUID.randomUUID());
        final Future<String> sourceMd5 = executor.submit(()->sourceDatabase.getMD5Hash());
        final Future<String> targetMd5 = executor.submit(()->sourceDatabase.getMD5Hash());
        if (sourceMd5.get().equals(targetMd5.get())) {
            logger.info("Databases "+sourceDatabase+" and "+targetDatabase+" have equal MD5 hash "+sourceMd5.get()+". Removing "+sourceDatabase+" and "+Util.joinStrings(", ", additionalDatabasesToDelete));
            sourceDatabase.drop();
            for (final Database additionalDatabaseToDelete : additionalDatabasesToDelete) {
                additionalDatabaseToDelete.drop();
            }
        }
    }
}
