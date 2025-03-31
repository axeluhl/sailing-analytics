package com.sap.sse.landscape.aws.orchestration;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.mongodb.client.MongoDatabase;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.util.ThreadPoolUtil;

/**
 * A procedure that is provided with a source and a target database configuration, expected to differ from one another.
 * The content of the source database is copied to the target and then compared using MD5 hashes. Those hashes are
 * computed using {@link Database#getMD5Hash()}.
 * <p>
 * 
 * The {@link #dropTargetFirst} field decides whether in the target endpoint the database with the
 * {@link #sourceDatabase}'s name will be dropped before starting with moving. The
 * {@link #dropSourceAfterSuccessfulCopy} decides whether the {@link #sourceDatabase} will be dropped after moving it
 * successfully.
 * <p>
 * 
 * If the comparison after the copy process fails, an {@link IllegalStateException} will be thrown by the {@link #run()}
 * method.
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
    private final boolean dropTargetFirst;
    private final boolean dropSourceAfterSuccessfulCopy;
    
    /**
     * Holds an error or exception message, not mapped through i18n, in case an exception was thrown while trying
     * to archive the DB; {@code null} otherwise.
     */
    private String mongoDbArchivingErrorMessage;

    public static interface Builder<BuilderT extends AbstractAwsProcedureImpl.Builder<BuilderT, CopyAndCompareMongoDatabase<ShardingKey>, ShardingKey>, ShardingKey>
    extends AbstractAwsProcedureImpl.Builder<BuilderT, CopyAndCompareMongoDatabase<ShardingKey>, ShardingKey> {
        BuilderT setSourceDatabase(Database sourceDatabase);
        BuilderT setTargetDatabase(Database targetDatabase);
        BuilderT setAdditionalDatabasesToDelete(Iterable<Database> additionalDatabasesToDelete);
        BuilderT dropTargetFirst(boolean dropTargetFirst);
        BuilderT dropSourceAfterSuccessfulCopy(boolean dropSourceAfterSuccessfulCopy);
    }
    
    public static class BuilderImpl<BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    extends AbstractAwsProcedureImpl.BuilderImpl<BuilderT, CopyAndCompareMongoDatabase<ShardingKey>, ShardingKey>
    implements Builder<BuilderT, ShardingKey> {
        private Database sourceDatabase;
        private Database targetDatabase;
        private Iterable<Database> additionalDatabasesToDelete;
        private boolean dropTargetFirst;
        private boolean dropSourceAfterSuccessfulCopy;
        
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

        @Override
        public BuilderT dropTargetFirst(boolean dropTargetFirst) {
            this.dropTargetFirst = dropTargetFirst;
            return self();
        }

        @Override
        public BuilderT dropSourceAfterSuccessfulCopy(boolean dropSourceAfterSuccessfulCopy) {
            this.dropSourceAfterSuccessfulCopy = dropSourceAfterSuccessfulCopy;
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
        
        protected boolean isDropTargetFirst() {
            return dropTargetFirst;
        }
        
        protected boolean isDropSourceAfterSuccessfulCopy() {
            return dropSourceAfterSuccessfulCopy;
        }
    }
    
    public static <BuilderT extends Builder<BuilderT, ShardingKey>, ShardingKey>
    Builder<BuilderT, ShardingKey> builder() {
        return new BuilderImpl<BuilderT, ShardingKey>();
    }
    
    protected CopyAndCompareMongoDatabase(BuilderImpl<?, ShardingKey> builder) {
        super(builder);
        this.sourceDatabase = builder.getSourceDatabase();
        this.targetDatabase = builder.getTargetDatabase();
        this.additionalDatabasesToDelete = builder.getAdditionalDatabasesToDelete() == null ? Collections.emptySet() : builder.getAdditionalDatabasesToDelete();
        this.dropTargetFirst = builder.isDropTargetFirst();
        this.dropSourceAfterSuccessfulCopy = builder.isDropSourceAfterSuccessfulCopy();
    }
    
    public String getMongoDbArchivingErrorMessage() {
        return mongoDbArchivingErrorMessage;
    }

    @Override
    public void run() throws Exception {
        if (targetDatabase.equals(sourceDatabase)) {
            throw new IllegalArgumentException("Source and target database must be different: "+sourceDatabase);
        }
        if (dropTargetFirst) {
            logger.info("Dropping target database "+targetDatabase+" before importing from "+sourceDatabase);
            targetDatabase.drop();
        }
        final MongoDatabase result = targetDatabase.getEndpoint().importDatabase(sourceDatabase.getMongoDatabase());
        if (result == null) {
            mongoDbArchivingErrorMessage = "This didn't work. No database resulted from importing "+sourceDatabase+" into "+targetDatabase;
            throw new IllegalStateException(mongoDbArchivingErrorMessage);
        }
        final ScheduledExecutorService executor = ThreadPoolUtil.INSTANCE.createBackgroundTaskThreadPoolExecutor(2, "MongoDB MD5 Hasher "+UUID.randomUUID(),
                /* executeExistingDelayedTasksAfterShutdownPolicy */ true);
        final Future<String> sourceMd5 = executor.submit(()->sourceDatabase.getMD5Hash());
        final Future<String> targetMd5 = executor.submit(()->targetDatabase.getMD5Hash());
        if (sourceMd5.get(Landscape.WAIT_FOR_HOST_TIMEOUT.get().asMillis(), TimeUnit.MILLISECONDS).equals(targetMd5.get(Landscape.WAIT_FOR_HOST_TIMEOUT.get().asMillis(), TimeUnit.MILLISECONDS))) {
            logger.info("Databases "+sourceDatabase+" and "+targetDatabase+" have equal MD5 hash "+sourceMd5.get()+".");
            if (dropSourceAfterSuccessfulCopy) {
                logger.info("Removing "+sourceDatabase+" and "+Util.joinStrings(", ", additionalDatabasesToDelete));
                sourceDatabase.drop();
                for (final Database additionalDatabaseToDelete : additionalDatabasesToDelete) {
                    additionalDatabaseToDelete.drop();
                }
            }
        } else {
            mongoDbArchivingErrorMessage = "Import failed; hashes are different. "+sourceDatabase+" has "+sourceMd5.get()+
                    ", "+targetDatabase+" has "+targetMd5.get();
            throw new IllegalStateException(mongoDbArchivingErrorMessage);
        }
        executor.shutdown();
    }
}
