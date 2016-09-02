package cn.jianke.imageswitcher.module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {

    private static ThreadManager instance;

    private ExecutorService cachedThreadPool;

    private ThreadManager(){
        cachedThreadPool = Executors.newCachedThreadPool();
    }

    public static ThreadManager getInstance(){
        if (instance == null)
            instance = new ThreadManager();

        return instance;
    }

    public ExecutorService getNewCachedThreadPool(){
        if (cachedThreadPool == null)
            cachedThreadPool = Executors.newCachedThreadPool();
        return cachedThreadPool;
    }
}
