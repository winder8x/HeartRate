package com.wow.heartrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends Activity {

	private final static String TAG = MainActivity.class.getSimpleName();
	private String mDeviceAddress;
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
//        this.bmgr = new BluetoothMgr(this,(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));
        
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        boolean ret = this.getApplicationContext().bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        if(!ret){
        	Log.e(TAG, "bind mServiceConnection error.");
        }
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

    	        TextView tvList =(TextView  )findViewById(R.id.tv_listBluetooth);
    	        BluetoothDevice bt = btDevice.values().iterator().next();
    	        tvList.setText(bt.getName() + "#" + bt.getAddress()); 
        	}
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void tv_listBluetoothClick(View view)
    {
    	TextView tvList =(TextView  )view;
    	String dev = tvList.getText().toString();
//    	String devName = dev.substring(0,dev.indexOf("#"));
    	String devName = "RHYTHM+";
    	String addr = "EF:00:05:AF:0B:DD";
    	BluetoothLeService srv = new BluetoothLeService();
    	
//    	bmgr.setWriteMessage(msg);
//    	bmgr.setBluetoothGattCallback(srv.mGattCallback);
//    	bmgr.conn(btDevice.get(devName));
    	
    	mDeviceAddress = addr;//dev.substring(dev.indexOf("#") + 1 );

		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
            if(result){
    			TextView tmp =(TextView  )findViewById(R.id.tv_listBluetoothStatus);
    			tmp.setText("conn sucess."); 
    		}
          
          
        }
		
    	
		
		
//        Uri uri = Uri.parse("http://blog.const.net.cn/");
//        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
//        startActivity(intent);
    	
    }
    /**
Bluetooth Generic Access Profile    {00001800-0000-1000-8000-00805f9b34fb}

Bluetooth Generic Attribute Profile {00001801-0000-1000-8000-00805F9B34FB}
     */
    private String CLIENT_CHARACTERISTIC_CONFIG = "00001800-0000-1000-8000-00805f9b34fb";
    private BluetoothGatt mBluetoothGatt;
    BluetoothGattCharacteristic characteristic;
    boolean enabled;
    //自定义测试消息
    public static int REQUEST_ENABLE_BT = 1;
    public HashMap<String,BluetoothDevice> btDevice = new HashMap<String,BluetoothDevice>();
    private BluetoothMgr bmgr = null;
    
    List<String> mArrayAdapter = new ArrayList<String>();
    

	        
    
	
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
 	private void displayData(String str){
 		TextView tvList =(TextView  )findViewById(R.id.tv_listBluetooth);
		tvList.setText(str); 
 	}
 	private void clearUI(){
 		displayData("");
 	}
 	
 	/**
 	 * 显示GATT服务及特征
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
 	BluetoothGattCharacteristic mGattCharacteristic;

 	
    /**
     * 回到DeviceControlActivity，下面的事件通过一个BroadcaseReceiver处理：
     */
 	boolean mConnected = false; 
 	BluetoothLeService mBluetoothLeService;
 	
 	
	// 处理Service发送过来的各种时间.
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
				updateConnectionState("连上了");
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				mConnected = false;
//				updateConnectionState(R.string.disconnected);
				updateConnectionState("断开了");
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
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
