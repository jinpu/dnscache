package com.sina.util.dnscache.datagraph;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sina.util.dnscache.R;
import com.sina.util.dnscache.datagraph.ServerSpeedActivity.DataRate;
import com.sina.util.dnscache.simulationtask.TaskManager;
import com.sina.util.dnscache.simulationtask.TaskModel;

/**
 * Created by Doraemon on 2014/7/15.
 */
public class DataGraphFragment extends Fragment{


	public LinearLayout all_taskspeed_bi_but = null ; 
	public LinearLayout httpdnslib_expendtime_but = null ; 
	public LinearLayout alltask_expendtime_but = null ; 
	public LinearLayout server_speed_but = null ; 
	
	
	public TextView all_taskspeed_bi_fast = null ; 
	public TextView all_taskspeed_bi_slow = null ; 
	
	public TextView httpdnslib_expendtime_max = null ; 
	public TextView httpdnslib_expendtime_min = null ; 
	
	public TextView alltask_expendtime_max = null ; 
	public TextView alltask_expendtime_average = null ; 
	
	public TextView server_speed_fast = null ; 
	public TextView server_speed_slow = null ; 
	
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_data_graph, null);
        
        all_taskspeed_bi_but = (LinearLayout)contentView.findViewById(R.id.all_taskspeed_bi_but) ;
        all_taskspeed_bi_but.setOnClickListener(new Button.OnClickListener() {// 创建监听
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(DataGraphFragment.this.getActivity(), AllTaskSpeedBIActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        
        httpdnslib_expendtime_but = (LinearLayout)contentView.findViewById(R.id.httpdnslib_expendtime_but) ; 
        httpdnslib_expendtime_but.setOnClickListener(new Button.OnClickListener() {// 创建监听
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(DataGraphFragment.this.getActivity(), HttpDNSExpendTimeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        
        alltask_expendtime_but = (LinearLayout)contentView.findViewById(R.id.alltask_expendtime_but) ; 
        alltask_expendtime_but.setOnClickListener(new Button.OnClickListener() {// 创建监听
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(DataGraphFragment.this.getActivity(), AllTaskExpendTimeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        
        server_speed_but = (LinearLayout)contentView.findViewById(R.id.server_speed_but) ; 
        server_speed_but.setOnClickListener(new Button.OnClickListener() {// 创建监听
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(DataGraphFragment.this.getActivity(), ServerSpeedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        
        
        all_taskspeed_bi_fast = (TextView)contentView.findViewById(R.id.all_taskspeed_bi_fast) ; 
        all_taskspeed_bi_slow = (TextView)contentView.findViewById(R.id.all_taskspeed_bi_slow) ; 
        
        httpdnslib_expendtime_max = (TextView)contentView.findViewById(R.id.httpdnslib_expendtime_max) ; 
        httpdnslib_expendtime_min = (TextView)contentView.findViewById(R.id.httpdnslib_expendtime_min) ; 
        
        alltask_expendtime_max = (TextView)contentView.findViewById(R.id.alltask_expendtime_max) ; 
        alltask_expendtime_average = (TextView)contentView.findViewById(R.id.alltask_expendtime_average) ; 
        
        server_speed_fast = (TextView)contentView.findViewById(R.id.server_speed_fast) ; 
        server_speed_slow = (TextView)contentView.findViewById(R.id.server_speed_slow) ; 
        
        
        return contentView ;
    }
    
    @Override
    public void onResume(){
    	super.onResume(); 
    	
		ArrayList<TaskModel> list = TaskManager.getInstance().list;
		if (list == null)
			return;
		
		
		// 
		int all_taskspeed_bi_fast_Num = 0;
		int all_taskspeed_bi_slow_Num = 0;
		
		//
		long httpdnslib_expendtime_max_Num = 0 ; 
		long httpdnslib_expendtime_min_Num = 9999;
		//
		long alltask_expendtime_max_Num = 0 ; 
		long alltask_expendtime_average_Num = 0 ;
		long httpSun = 0, domainSun = 0 ;
		
		
		for (TaskModel taskModel : list) {
			
			//
			if (taskModel.domainExpendTime - (taskModel.hostExpendTime + taskModel.httpDnsExpendTime) <= 0) {
				all_taskspeed_bi_slow_Num++;
			} else {
				all_taskspeed_bi_fast_Num++;
			}
			
			//
			if ( taskModel.httpDnsExpendTime >  httpdnslib_expendtime_max_Num ) {
				httpdnslib_expendtime_max_Num = taskModel.httpDnsExpendTime ; 
			}
			if( taskModel.httpDnsExpendTime <  httpdnslib_expendtime_max_Num ){
				httpdnslib_expendtime_min_Num = taskModel.httpDnsExpendTime ; 
			}
			
			//
			long temp = (taskModel.hostExpendTime + taskModel.httpDnsExpendTime) - taskModel.domainExpendTime ;
			if(  temp > alltask_expendtime_max_Num ){
				alltask_expendtime_max_Num = temp ; 
			}
			domainSun += taskModel.domainExpendTime ; 
			httpSun += taskModel.hostExpendTime + taskModel.httpDnsExpendTime ;
			
		}
		
		
		//大小 / 时间
		
		
		//
		all_taskspeed_bi_fast.setText("加速：" + all_taskspeed_bi_fast_Num + "个");
		all_taskspeed_bi_slow.setText("延迟：" + all_taskspeed_bi_slow_Num + "个");
		//
		httpdnslib_expendtime_max.setText("最快：" + httpdnslib_expendtime_min_Num + "毫秒");
		httpdnslib_expendtime_min.setText("最慢：" + httpdnslib_expendtime_max_Num + "毫秒");
		// 
		alltask_expendtime_average_Num = domainSun - httpSun ; 
		alltask_expendtime_max.setText("最大速度提升：" + alltask_expendtime_max_Num + "毫秒");
		alltask_expendtime_average.setText("全局速度提升：" + alltask_expendtime_average_Num+ "毫秒");
		//
		String[] sern = serverName(list) ; 
		if( sern == null || sern.length != 2 ) return ; 
		server_speed_fast.setText("最快服务器：" + sern[1] );
		server_speed_slow.setText("最慢服务器：" + sern[0] );
    }
 
    
    
    
    public String[] serverName( ArrayList<TaskModel> list ){
    	
		Hashtable< String, ArrayList<DataRate> > serverIpSpeedList = new Hashtable<String, ArrayList<DataRate>>() ; 
		for( TaskModel taskModel : list ){
			ArrayList<DataRate> ips = serverIpSpeedList.get(taskModel.hostIp) ; 
			if( ips == null ){
				ips = new ArrayList<DataRate>() ;
				serverIpSpeedList.put(  taskModel.hostIp , ips ) ; 
			}
			if (null != taskModel.hostInputStreamResult) {
			    ips.add(new DataRate(taskModel.hostInputStreamResult.length, taskModel.hostExpendTime + taskModel.httpDnsExpendTime)) ; 
            }
			
			ips = serverIpSpeedList.get(taskModel.domainIp) ; 
			if( ips == null ){
				ips = new ArrayList<DataRate>() ;
				serverIpSpeedList.put(taskModel.domainIp, ips) ; 
			}
			if (null != taskModel.domainInputStreamResult) {
			    ips.add(new DataRate(taskModel.domainInputStreamResult.length, taskModel.domainExpendTime)) ; 
			}
		}
		
		ArrayList<String> ip = new ArrayList<String>(); 
		ArrayList<Long> speed = new ArrayList<Long>(); 
		for(Iterator<String> itr = serverIpSpeedList.keySet().iterator(); itr.hasNext();){
			String key = (String) itr.next();
			ArrayList<DataRate> value = (ArrayList<DataRate>) serverIpSpeedList.get(key);
			ip.add(key) ; 
			long sunSize = 0 ; 
			long sunElapsed = 1 ; 
			for( DataRate l : value ){
			    sunSize += l.dataLength ; 
			    sunElapsed += l.dataElapsed;
			}
			speed.add( (long) ((sunSize/sunElapsed) * 1.024) ) ; 
		}
		
		long max = 0 ; 
		long min = 99999 ; 
		
		String[] max_min = {"",""};
		for( int i = 0 ; i < ip.size() ; i++ ){

			if( speed.get(i) > max ){
				max = speed.get(i).longValue() ;
				max_min[1] = ip.get(i) ; 
			}
			if( speed.get(i) < min ){
				min = speed.get(i).longValue() ; 
				max_min[0] = ip.get(i) ;
			}
		}  
		
		return max_min ;
    }
    
    
    
}
