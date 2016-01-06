package com.sina.util.dnscache.simulationtask;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import android.os.Handler;
import android.os.Message;

import com.sina.util.dnscache.R;
import com.sina.util.dnscache.tasksetting.Config;
import com.sina.util.dnscache.tasksetting.SpfConfig;

public class TaskManager {

    // ///////////////////////////////////////////////////////////////

    private static TaskManager Instance = null;

    private TaskManager() {
        initThreadPool();
    }

    private void initThreadPool() {
        threadPool = new ThreadPool(Config.concurrencyNum);
    }

    public void reInitThreadPool() {
        initThreadPool();
    }

    public static TaskManager getInstance() {

        if (Instance == null) {
            Instance = new TaskManager();
        }
        return Instance;
    }

    // ///////////////////////////////////////////////////////////////

    public ArrayList<TaskModel> list = null;

    public ThreadPool threadPool = null;

    // ///////////////////////////////////////////////////////////////

    int finishTag = 0;

    public void startTask(final Handler handler) {
        if (threadPool.isShutdown()) {
            initThreadPool();
        }
        finishTag = 0;
        threadPool.reset();
        for (final TaskModel temp : list) {

            Callable<TaskModel> call = new Callable<TaskModel>() {
                @Override
                public TaskModel call() throws Exception {
                    Message message = new Message();
                    message.what = 1;
                    message.obj = temp;
                    handler.sendMessage(message);

                    finishTag++;
                    if (finishTag >= list.size()) {
                        message = new Message();
                        message.what = 3;
                        handler.sendMessage(message);
                    }
                    return temp;
                }
            };

            threadPool.runTask(temp, call);
        }
    }

    public void stopTask() {
        if (threadPool != null) {
            threadPool.shutdownNow();
        }
    }

    // ///////////////////////////////////////////////////////////////

    public void initData(ArrayList<String> dataList) {
        list = new ArrayList<TaskModel>();
        for (int i = 0; i < Config.requestsNum; i++) {
            TaskModel modelTemp = new TaskModel();
            modelTemp.url = dataList.get((int) (Math.random() * (dataList.size() - 1)));
            list.add(modelTemp);
        }
    }

    public void clear() {
        list = null;
    }

    // ///////////////////////////////////////////////////////////////

}
