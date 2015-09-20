package com.wow.heartrate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends Activity {

	private final static String TAG = MainActivity.class.getSimpleName();
	private String mDeviceAddress;
	
	SQLiteDatabase db = null;
	//每次测试的开始时间
	String testDate = null;
	
	DBUtils dbUtils = null;
	

    BluetoothGattCharacteristic characteristic;
    boolean enabled;
    //自定义测试消息
    public static int REQUEST_ENABLE_BT = 1;
    public HashMap<String,BluetoothDevice> btDevice = new HashMap<String,BluetoothDevice>();
    private BluetoothMgr bmgr = null;

 	BluetoothGattCharacteristic mGattCharacteristic;
 	
    /**
     * 回到DeviceControlActivity，下面的事件通过一个BroadcaseReceiver处理：
     */
 	boolean mConnected = false; 
 	
 	BluetoothLeService mBluetoothLeService;
    List<String> mArrayAdapter = new ArrayList<String>();
    
    /**
     * 每次测试记录的心率，重新开始后清零
     */
    List<String> heartRateData = new ArrayList<String>();
    
	 // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            //蓝牙初始化成功后，自动连接制定的设备。
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
        	unbindService(mServiceConnection);
        }catch(IllegalArgumentException e){
        	Log.e(TAG, "unbind Service ", e);
        }
        mBluetoothLeService = null;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.bmgr = new BluetoothMgr(this,(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));
        
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        boolean ret = this.getApplicationContext().bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        if(!ret){
        	Log.e(TAG, "bind mServiceConnection error.");
        }
        //db init
        DatabaseHelper database = new DatabaseHelper(this);
        db = database.getReadableDatabase();
        dbUtils = new DBUtils(db);
        
        List<String> recordData = dbUtils.query();
        //list record 
        ListView lvList =(ListView)findViewById(R.id.lv_listRecord);
        ListViewArrayAdapter<String> adapter = new ListViewArrayAdapter<String>(this, 
        		android.R.layout.simple_expandable_list_item_1,
        		recordData);
        
        lvList.setAdapter(adapter);
//        Drawable drawable= new ColorDrawable(0xFF053525);
//        lvList.setSelector(drawable);  
        lvList.setCacheColorHint(0);
        
        lvList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				ListView p = (ListView)parent;
				ListViewArrayAdapter<?> adapter = (ListViewArrayAdapter<?>)p.getAdapter();
				String text = (String)adapter.getItem(position);
				chartData(text);
				
				adapter.setPosition(position);
				adapter.notifyDataSetChanged();
				
			}
		});
        
        lvList.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				ListView p = (ListView)parent;
				
				ListViewArrayAdapter adapter = (ListViewArrayAdapter)p.getAdapter();
				String text = (String)adapter.getItem(position);

				if(position == adapter.pos){
					//adapter.clear();
					//adapter.addAll(recordData);
					dbUtils.delete(text);
					List<String> recordData = dbUtils.query();
					adapter.clear();
					adapter.addAll(recordData);
					adapter.pos = -1;
					adapter.notifyDataSetChanged();
				}
				
				
				
				
				return true;
				
			}
        	
        });
        
        
    }
    
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
        	
            return true;
        }else if (id == R.id.action_bluetooth) {
        	//setting
        	//to open bluetooth
        	
        	bmgr.setWriteMessage(msg);
        	btDevice = bmgr.optBluetooth();
        	if(btDevice.size() >0){
        		//显示蓝牙地址
    	        BluetoothDevice bt = btDevice.values().iterator().next();
    	        //TextView tvList =(TextView  )findViewById(R.id.tv_listBluetooth);
    	        //tvList.setText(bt.getName() + "#" + bt.getAddress()); 
    	        Dialog alertDialog = new AlertDialog.Builder(this).   
    	                setTitle("first device").   
    	                setMessage(bt.getName()+ "#" + bt.getAddress()).   
    	                setIcon(R.drawable.ic_launcher).    	                
    	                setPositiveButton("OK", new DialogInterface.OnClickListener() {   
    	                    @Override   
    	                    public void onClick(DialogInterface dialog, int which) {   
    	                        // TODO Auto-generated method stub    
    	                    }   
    	                }).
    	                create();   
    	        alertDialog.show();  
        	}
            return true;
        }else if(id == R.id.action_export){
        	String path = "/sdcard/wow";
        	File f = new File(path);
        	if(!f.exists()){
        		boolean ret =  f.mkdir();
        		if(ret == false) Log.e(TAG, "can not mkdir " + path);
        	}
        	try{
	        	BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "/testData.txt")));
	            Long dataNum = 0L;
	            List<String> tdata = dbUtils.query();
	            for(String test : tdata){
	            	List<HeartRateEntity> data = dbUtils.query(test);
	            	for(HeartRateEntity hr : data){
	            		String s = hr.getTestDate() + "," + hr.getDataDate() + "," + hr.getRate() + "\n";
	            		writer.write(s);
	            		dataNum++;
	            	}
	            }
	            writer.flush();
	            writer.close();
	            Dialog alertDialog = new AlertDialog.Builder(this).   
    	                setTitle("Export").   
    	                setMessage("共导出[" + dataNum + "]行数据。").   
    	                setIcon(R.drawable.ic_launcher).    	                
    	                setPositiveButton("OK", new DialogInterface.OnClickListener() {   
    	                    @Override   
    	                    public void onClick(DialogInterface dialog, int which) {   
    	                        // TODO Auto-generated method stub    
    	                    }   
    	                }).
    	                create();   
    	        alertDialog.show();  
        	}catch(Exception e){
        		Log.e(TAG, e.getMessage());
        	}
        	
        }
        return super.onOptionsItemSelected(item);
    }
    

  
    /**
     * 绑定事件，连接并处理蓝牙数据
     * @param view
     */
    public void tv_listBluetoothClick(View view)
    {
    	TextView tvList =(TextView  )view;
    	String dev = tvList.getText().toString();
//    	String devName = dev.substring(0,dev.indexOf("#"));
    	//指定地址连接蓝牙设备
    	String devName = "RHYTHM+";
    	String addr = "EF:00:05:AF:0B:DD";
    	BluetoothLeService srv = new BluetoothLeService();
    	
//    	bmgr.setWriteMessage(msg);
//    	bmgr.setBluetoothGattCallback(srv.mGattCallback);
//    	bmgr.conn(btDevice.get(devName));
    	
    	mDeviceAddress = addr;//dev.substring(dev.indexOf("#") + 1 );
    	
    	//注册消息接收器
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			if("Stop".equals(dev)){ //断开蓝牙
				mBluetoothLeService.disconnect();
            	tvList.setText("start");
            	
            	chart(true);
            	if(this.heartRateData.size() < 1){
                	testDate = null;
            		return;
            	}
            	//提示是否保存
            	Dialog alertDialog = new AlertDialog.Builder(this).   
    	                setTitle("Info").   
    	                setMessage("是否保存本次测试数据？").   
    	                setIcon(R.drawable.ic_launcher).    	                
    	                setPositiveButton("OK", new DialogInterface.OnClickListener() {   
    	                    @Override   
    	                    public void onClick(DialogInterface dialog, int which) {   
    	                        // TODO Auto-generated method stub
    	                    	ListView lvList =(ListView)findViewById(R.id.lv_listRecord);
    	                    	ListViewArrayAdapter adapter = (ListViewArrayAdapter)lvList.getAdapter();
    	        				
    	        				List<String> recordData = dbUtils.query();
    	        				adapter.clear();
    	        				adapter.addAll(recordData);
    	        				adapter.pos = -1;
    	        				adapter.notifyDataSetChanged();
    	                    	testDate = null;
    	                    }   
    	                }).    	                
    	                setNegativeButton("No", new DialogInterface.OnClickListener() {   
    	                    @Override   
    	                    public void onClick(DialogInterface dialog, int which) {   
    	                    	dbUtils.delete(testDate);
    	                    	testDate = null;
    	                    	
    	                    }   
    	                }).
    	                create();
            	
    	        alertDialog.show();  
            	
            	
			}else{//连接蓝牙
	            boolean result = mBluetoothLeService.connect(mDeviceAddress);
	            Log.d(TAG, "Connect request result=" + result);
            	this.heartRateData.clear();
            	testDate = null;
	            if(result){
	            	
	    	        
	            	tvList.setText("Stop");
	            	if(testDate == null)
	            		testDate = DBUtils.getDatetime();
	    		}
			}
          
        }

    	
    }
    /**
     * 用指定的记录数据画图
     * @param testData
     */
    private void chartData(String testData){
    	List<HeartRateEntity> data =  this.dbUtils.query(testData);
    	this.heartRateData.clear();
    	if(data != null){
    		for(HeartRateEntity d : data){
    			heartRateData.add(d.getRate() + "");
    		}
    		chart(true);
    	}
    }
    /**
     * 图形处理
     * @param all
     */
    private void chart(boolean all){
    	LineChart chart = (LineChart) findViewById(R.id.chart);
    	int dataSize =  20;
    	if(all) dataSize = heartRateData.size();
    	
    	Log.e(TAG, "get " + dataSize + " data to chart.");
    	LineData data = getData(dataSize);
    	
    	// if enabled, the chart will always start at zero on the y-axis
    	
    	chart.setScaleEnabled(false);
        // no description text
        chart.setDescription("");// 数据描述
        // 如果没有数据的时候，会显示这个，类似listview的emtpyview
        chart.setNoDataTextDescription("You need to provide data for the chart.");


        // enable / disable grid background
        chart.setDrawGridBackground(false); // 是否显示表格颜色
        chart.setBackgroundColor(Color.WHITE & 0x70FFFFFF); // 表格的的颜色，在这里是是给颜色设置一个透明度
        chart.setBorderWidth(1.25f);// 表格线的线宽

        // enable touch gestures
        chart.setTouchEnabled(true); // 设置是否可以触摸

        // enable scaling and dragging
        chart.setDragEnabled(true);// 是否可以拖拽
        chart.setScaleEnabled(true);// 是否可以缩放

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);// 

        chart.setBackgroundColor(Color.WHITE);// 设置背景

//        chart.setValueTypeface(mTf);// 设置字体

        
        // add data
        chart.setData(data); // 设置数据

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend(); // 设置标示，就是那个一组y的value的

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(LegendForm.CIRCLE);// 样式
        l.setFormSize(6f);// 字体
        l.setTextColor(Color.WHITE);// 颜色
        
//        l.setTypeface(mTf);// 字体
        
        chart.invalidate();

        // animate calls invalidate()...
//        chart.animateX(2500); 
    	
    	
    }
    
    /**
     * 图形数据包装
     * @param count
     * @param range
     * @return
     */
    LineData getData(int count) {
        ArrayList<String> xVals = new ArrayList<String>();
        int size = heartRateData.size();
        if(size < count) count = size;
        
        int x = 0;
        for (int i = (size - count); i < size; i++) {
          // 2秒一个心率数据
          if(i > size ) break;
          xVals.add(String.valueOf(++x));
          
        }

        // y轴的数据
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for (int i = (size - count); i < size; i++) {
          float val = 0;
          try{
        	  val = (Integer.valueOf(heartRateData.get(i)));
          }catch(NumberFormatException e){}
          yVals.add(new Entry(val, i - (size -  count)));
        }

        // create a dataset and give it a type
        // y轴的数据集合
        LineDataSet set1 = new LineDataSet(yVals, "HearthRate");
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        set1.setLineWidth(1.75f); // 线宽
        set1.setCircleSize(3f);// 显示的圆形大小
        set1.setColor(Color.RED);// 显示颜色
        set1.setCircleColor(Color.RED);// 圆形的颜色
        set1.setHighLightColor(Color.RED); // 高亮的线的颜色
        set1.setDrawValues(false);//不显示值
        set1.setDrawCircles(false);
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.05f);
        set1.setDrawFilled(true);
        set1.setFillColor(Color.rgb(0, 255, 255));
        

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        return data;
    }
    
    
	
 // Create a BroadcastReceiver for ACTION_FOUND
 	private final CallWriteMessage msg = new CallWriteMessage() {

		public void setMessage(String msg) {
			TextView tvList =(TextView  )findViewById(R.id.tv_listBluetooth);
			tvList.setText(msg); 
			
		} 
 	};
    
 	//更新链接状态
 	private void updateConnectionState(String str){
 		TextView tvList =(TextView  )findViewById(R.id.tv_listBluetooth);
		tvList.setText(str); 
 	}
 	
 	//处理接收到的蓝牙数据
 	private void displayData(String str){
 		if(str == null ) return;
 		str = str.trim();
 		if(str.equals("")) return;
 		int rate = 0;
		try{
			rate = Integer.parseInt(str);
			if(rate < 30) return;
			dbUtils.insert(testDate, rate);
		}catch( NumberFormatException e){}
		
 		
 		TextView tvList =(TextView  )findViewById(R.id.tv_listBluetooth);
		tvList.setText(str); 
		heartRateData.add(str);
		chart(false);
		
 	}
 	
 	private void clearUI(){
 		displayData("");
 	}
 	
 	/**
 	 * 显示GATT服务提供的服务及特征。即蓝牙设备能提供的数据
 	 * @param gattServices
 	 */
 	private void displayGattServices(List<BluetoothGattService> gattServices) {
 		if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList gattCharacteristicData  = new ArrayList();
        ArrayList mGattCharacteristics = new ArrayList();
 
        // 循环迭代可访问的GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            
//            currentServiceData.put(
//                    LIST_NAME, SampleGattAttributes.
//                            lookup(uuid, unknownServiceString));
//            currentServiceData.put(LIST_UUID, uuid);
            
            gattServiceData.add(currentServiceData);
 
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
           // 循环迭代可访问的Characteristics.
            if(uuid.equals("0000180d-0000-1000-8000-00805f9b34fb")){
            	Log.d(TAG, "health service.");
            }
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData =new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                if("00002a37-0000-1000-8000-00805f9b34fb".equals(uuid)){
                	mGattCharacteristic = gattCharacteristic;
                	mBluetoothLeService.readCharacteristic(gattCharacteristic);
                	mBluetoothLeService.setCharacteristicNotification(
                			gattCharacteristic, true);
                }
//                currentCharaData.put(
//                        LIST_NAME, SampleGattAttributes.lookup(uuid,unknownCharaString));
//                currentCharaData.put(LIST_UUID, uuid);
                
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
         }
    
    }
 	
 	
	// 广播接收器，处理Service发送过来的各种事件.
	// ACTION_GATT_CONNECTED: 连接上了一个GATT服务.
	// ACTION_GATT_DISCONNECTED: 断开了一个GATT服务.
	// ACTION_GATT_SERVICES_DISCOVERED: 发现了GATT服务.
	// ACTION_DATA_AVAILABLE: 从设备接收到数据. 这里可能是一个读取或者通知操作的结果。
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
//				updateConnectionState(R.string.connected);
				updateConnectionState("conn");
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				mConnected = false;
//				updateConnectionState(R.string.disconnected);
				updateConnectionState("dis");
				invalidateOptionsMenu();
				clearUI();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				// 显示所有支持的service和characteristic。
				displayGattServices(mBluetoothLeService
						.getSupportedGattServices());
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				displayData(intent
						.getStringExtra(BluetoothLeService.EXTRA_DATA));
			}
		}
	};
	/**
	 * 定义处理系统的那些消息
	 * @return
	 */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);//连接
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);//断开
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);//查找
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);//接受蓝牙数据
        return intentFilter;
    }
    
    
}
