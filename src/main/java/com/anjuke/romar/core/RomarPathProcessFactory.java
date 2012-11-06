package com.anjuke.romar.core;

import java.io.File;

import com.anjuke.romar.core.handlers.CommitHandler;
import com.anjuke.romar.core.handlers.CompactHandler;
import com.anjuke.romar.core.handlers.EstimateHandler;
import com.anjuke.romar.core.handlers.ItemRecommendHandler;
import com.anjuke.romar.core.handlers.RecommendHandler;
import com.anjuke.romar.core.handlers.RemoveHandler;
import com.anjuke.romar.core.handlers.RemoveItemHandler;
import com.anjuke.romar.core.handlers.RemoveUserHandler;
import com.anjuke.romar.core.handlers.SimilarUserHandler;
import com.anjuke.romar.core.handlers.UpdateHandler;
import com.anjuke.romar.core.impl.SimpleRomarDispatcher;
import com.anjuke.romar.mahout.MahoutService;
import com.anjuke.romar.mahout.factory.MahoutServiceFactory;
import com.anjuke.romar.mahout.model.BDBIDMigrator;
import com.anjuke.romar.mahout.model.RomarMemoryIDMigrator;

public final class RomarPathProcessFactory {

    private RomarPathProcessFactory() {

    }

    public static <T> T createPathProcessor(RomarDefaultPathFactory<T> factory) {
        factory.init();
        factory.setRecommend(RequestPath.RECOMMEND);
        factory.setUpdate(RequestPath.UPDATE);
        factory.setRemove(RequestPath.REMOVE);
        factory.setCommit(RequestPath.COMMIT);
        factory.setItemRecommend(RequestPath.ITEM_RECOMMEND);
        factory.setSimilarUser(RequestPath.SIMILAR_USER);
        factory.setOptimize(RequestPath.OPTIMIZE);
        factory.setEstimate(RequestPath.ESTIMATE);
        factory.setRemoveUser(RequestPath.REMOVE_USER);
        factory.setRemoveItem(RequestPath.REMOVE_ITEM);
        T instance = factory.getInstance();
        return instance;
    }

    private static class RomarCoreFactory implements RomarDefaultPathFactory<RomarCore> {
        private RomarConfig _config = RomarConfig.getInstance();
        private MahoutServiceFactory _serviceFactory = _config.getMahoutServiceFactory();
        private RomarCore _core = new RomarCore();
        private SimpleRomarDispatcher _dispatcher = new SimpleRomarDispatcher();
        private MahoutService _service = _serviceFactory.createService();
        private final static int BDB_CACHE_SIZE = 102400;

        @Override
        public RomarCore getInstance() {
            _dispatcher.prepare();
            if (_config.isAllowStringID()) {
                String path = _config.getPersistencePath();
                if (path != null && !path.isEmpty()) {
                    File userPath = new File(_config.getPersistencePath()
                            + File.separator + "user_id");
                    if (!userPath.exists()) {
                        userPath.mkdirs();
                    }
                    _core.setUserIdMigrator(new BDBIDMigrator(userPath.getPath(),
                            BDB_CACHE_SIZE));
                    File itemPath = new File(_config.getPersistencePath()
                            + File.separator + "item_id");
                    if (!itemPath.exists()) {
                        itemPath.mkdirs();
                    }
                    _core.setItemIdMigrator(new BDBIDMigrator(itemPath.getPath(),
                            BDB_CACHE_SIZE));
                } else {
                    _core.setUserIdMigrator(new RomarMemoryIDMigrator());
                    _core.setItemIdMigrator(new RomarMemoryIDMigrator());
                }
            }

            _core.setDispatcher(_dispatcher);
            _core.setService(_service);
            return _core;
        }

        @Override
        public void setRecommend(RequestPath path) {
            _dispatcher.registerHandler(path, new RecommendHandler(_service));
        }

        @Override
        public void setUpdate(RequestPath path) {
            _dispatcher.registerHandler(path, new UpdateHandler(_service));
        }

        @Override
        public void setRemove(RequestPath path) {
            _dispatcher.registerHandler(path, new RemoveHandler(_service));
        }

        @Override
        public void setCommit(RequestPath path) {
            _dispatcher.registerHandler(path, new CommitHandler(_service));
        }

        @Override
        public void setItemRecommend(RequestPath path) {
            _dispatcher.registerHandler(path, new ItemRecommendHandler(_service));
        }

        @Override
        public void setOptimize(RequestPath path) {
            _dispatcher.registerHandler(path, new CompactHandler(_service));
        }

        @Override
        public void setEstimate(RequestPath path) {
            _dispatcher.registerHandler(path, new EstimateHandler(_service));
        }

        @Override
        public void setRemoveUser(RequestPath path) {
            _dispatcher.registerHandler(path, new RemoveUserHandler(_service));
        }

        @Override
        public void setRemoveItem(RequestPath path) {
            _dispatcher.registerHandler(path, new RemoveItemHandler(_service));
        }

        @Override
        public void setSimilarUser(RequestPath path) {
            _dispatcher.registerHandler(path, new SimilarUserHandler(_service));

        }

        @Override
        public void init() {

        }

    }

    public static RomarCore createCore() {
        return createPathProcessor(new RomarCoreFactory());
    }

}
