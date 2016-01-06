package com.sina.util.dnscache;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageButton;

import com.sina.util.dnscache.cachedata.CacheDataFragment;
import com.sina.util.dnscache.datagraph.DataGraphFragment;
import com.sina.util.dnscache.simulationtask.SimulationTaskFragment;
import com.sina.util.dnscache.tasksetting.TaskSettingFragment;



public class DNSCacheTestActivityTabMain extends FragmentActivity {
	
	public ArrayList<Fragment> fragments = new ArrayList<Fragment>();
	public ArrayList<ImageButton> buttons = new ArrayList<ImageButton>() ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_main);
        initComponents();
    }
    

    private void initComponents() {

    	buttons.add((ImageButton) findViewById(R.id.buttom_simulation_task)) ; 
        buttons.add( (ImageButton) findViewById(R.id.buttom_task_setting)) ; 
        buttons.add((ImageButton) findViewById(R.id.buttom_data_graph)) ; 
        buttons.add((ImageButton) findViewById(R.id.buttom_cache_data)) ; 
        
        fragments.add(new SimulationTaskFragment()) ; 
        fragments.add(new TaskSettingFragment()) ; 
        fragments.add(new DataGraphFragment()) ; 
        fragments.add(new CacheDataFragment()) ; 
        
        FragmentTabAdapter tabAdapter = new FragmentTabAdapter(this, fragments, R.id.fl_content, buttons);
        tabAdapter.init(); 

    }
    
    public class FragmentTabAdapter{
    	
    	private ArrayList<Fragment> fragments; // 一个tab页面对应一个Fragment
    	private ArrayList<ImageButton> rgs; // 用于切换tab
    	private FragmentActivity fragmentActivity; // Fragment所属的Activity
    	private int fragmentContentId; // Activity中所要被替换的区域的id
    	
    	private Fragment currentFragment = null ; 
    	
    	public FragmentTabAdapter(FragmentActivity fragmentActivity, ArrayList<Fragment> fragments, int fragmentContentId, ArrayList<ImageButton> rgs) {
    		
    		this.fragmentActivity = fragmentActivity ; 
    		this.fragments = fragments ; 
    		this.fragmentContentId = fragmentContentId ;
    		this.rgs = rgs ; 
    		
    		FragmentTransaction ft = fragmentActivity.getSupportFragmentManager().beginTransaction();
    		currentFragment = fragments.get(0) ; 
    		ft.add(fragmentContentId, currentFragment);
    		ft.commit();
    	}
    	
    	public void init (){
    		
    		for( int i = 0 ; i < rgs.size() ; i++ ){
    			
    			final ImageButton button = rgs.get(i) ; 
    			final Fragment fragment = fragments.get(i) ; 
    			
    			
    			button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						
						FragmentTransaction ft = fragmentActivity.getSupportFragmentManager().beginTransaction();
						
						currentFragment.onPause(); 
						
						if( fragment.isAdded() ){
							fragment.onResume(); 
						}else{
							ft.add(fragmentContentId, fragment) ; 
						}
						
						ft.hide(currentFragment) ; 
						ft.show( fragment ) ; 
						ft.commit();
						
						currentFragment = fragment ; 
						
						setButton(v);
				}});
    		}
    		
    		setButton( rgs.get(0)); 
    	}
    	
    	private View currentButton;
        /**
         * 设置按钮的背景图片
         *
         * @param v
         */
        private void setButton(View v) {
            if (currentButton != null && currentButton.getId() != v.getId()) {
                currentButton.setEnabled(true);
            }
            v.setEnabled(false);
            currentButton = v;
        }
    	
    }

}
