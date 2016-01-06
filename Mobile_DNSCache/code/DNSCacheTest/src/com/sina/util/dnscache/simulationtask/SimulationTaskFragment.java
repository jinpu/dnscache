package com.sina.util.dnscache.simulationtask;

import java.util.Collections;
import java.util.Comparator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.sina.util.dnscache.R;
import com.sina.util.dnscache.tasksetting.Config;
import com.sina.util.dnscache.util.FileTools;
import com.sina.util.dnscache.util.ToastUtil;

/**
 * Created by Doraemon on 2014/7/15.
 */
public class SimulationTaskFragment extends Fragment {

    public Button switchBtn = null;
    public boolean isSwitchBtn = false;
	
	public ListView listView = null;
	public TaskModelAdapter taskAdapter = null;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View  contentView=  inflater.inflate(R.layout.fragment_simulation_task,null);
        
        
        
        Config.fileUrlList = FileTools.getFromAssets(this.getActivity(), "url.txt");
        
        
        switchBtn = (Button) contentView.findViewById(R.id.switchBtn);
        switchBtn.setOnClickListener(new Button.OnClickListener() {// 创建监听
            public void onClick(View v) {
            	isSwitchBtn = !isSwitchBtn;
                switchBtn.setText(isSwitchBtn == false ? "模拟开始\n" : "模拟停止\n");
                if (isSwitchBtn) {
                    taskAdapter.initDtaa();
                    taskAdapter.notifyDataSetChanged();
                    new Thread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Thread.currentThread().setName("TaskManager Start"); 
                            TaskManager.getInstance().clear();
                            TaskManager.getInstance().initData(Config.fileUrlList);
                            TaskManager.getInstance().startTask(handler);
						}
					}).start();

                } else {
                    TaskManager.getInstance().stopTask();
                }
            }
        });
        
        taskAdapter = new TaskModelAdapter(this.getActivity());
        listView = (ListView) contentView.findViewById(R.id.listView);
        listView.setAdapter(taskAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long arg3) {

                TaskModel taskModel = taskAdapter.list.get(index);
                TaskInfoActivity.InitData(taskModel);
                Intent intent = new Intent();
                intent.setClass(SimulationTaskFragment.this.getActivity(), TaskInfoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        
        
        return contentView;
    }
    
    @Override
    public void onResume(){
    	super.onStart();  
    	if( TaskManager.getInstance().list == null ){
    		taskAdapter.initDtaa();
    		
    	}
    }
    
    public final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 1:
                TaskModel taskModel = (TaskModel) msg.obj;
                taskAdapter.list.add(taskModel);
//                HttpDnsRecordUtil.record(taskModel);
                listView.smoothScrollToPosition(listView.getCount());//移动到尾部
                break;
            case 2:
                taskAdapter.initDtaa();
                break;
            case 3:
                isSwitchBtn = !isSwitchBtn;
                switchBtn.setText(isSwitchBtn == false ? "模拟开始\n" : "模拟停止\n");
                
                Collections.sort(taskAdapter.list, comparator);
//                for (TaskModel model : taskAdapter.list) {
//                    HttpDnsRecordUtil.record(model);
//                }
                
                ToastUtil.showText(SimulationTaskFragment.this.getActivity(), "任务全部完成");
                break;
            }
            
            taskAdapter.notifyDataSetChanged();
        }
    };
    
    Comparator<TaskModel> comparator = new Comparator<TaskModel>() {
        public int compare(TaskModel s1, TaskModel s2) {
            return (int) (s1.taskID - s2.taskID);
        }
    };
    
}
