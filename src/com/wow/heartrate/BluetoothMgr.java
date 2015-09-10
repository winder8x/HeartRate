package com.wow.heartrate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothMgr{

	// 自定义测试消息
	public static int REQUEST_ENABLE_BT = 1;
	
	public CallWriteMessage writeMessage;
	
	public void setWriteMessage(CallWriteMessage w){
		this.writeMessage = w;
	}
	
	

	BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
	HashMap<String,BluetoothDevice> mArrayAdapter = new HashMap<String,BluetoothDevice>();
	BluetoothManager bluetoothManager = null;
	private Context context ;
	
	public BluetoothMgr (Context context,BluetoothManager mgr){
		bluetoothManager = mgr;
		this.context = context;
		blueAdapter = bluetoothManager.getAdapter();
	}
	
	/**
	 * 获取蓝牙设备
	 */
	public HashMap<String,BluetoothDevice> optBluetooth() {
		if (blueAdapter == null) {
			// Device does not support Bluetooth
			return mArrayAdapter;
		}
		
//		if (blueAdapter == null || !blueAdapter.isEnabled()) {
//		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//		}
		
		if (!blueAdapter.isEnabled()) {// try to open bt
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			// startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		// 查找已经匹配的设备
		Set<BluetoothDevice> pairedDevices = blueAdapter.getBondedDevices();

		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a
				// ListView
				mArrayAdapter.put(device.getName(), device);

			}

		}
		// Register the BroadcastReceiver 注册并扫描蓝牙
		// IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		// registerReceiver(mReceiver, filter); // Don't forget to unregister
		// during onDestroy
		return mArrayAdapter;
	}

	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// Add the name and address to an array adapter to show in a
				// ListView
				mArrayAdapter.put(device.getName() , device);
			}
		}
	};

	public void conn(BluetoothDevice device) {
		
		BluetoothGatt gatt = device.connectGatt(context, true, callback);
		boolean isConnect = gatt.connect();
		
//		ConnectThread t = new ConnectThread(device);
//		t.run();

	}
	
	BluetoothGattCallback callback = null;
	
	public void setBluetoothGattCallback(BluetoothGattCallback cb){
		callback = cb;
	}
	/**
	 * 服务端线程
	 * 
	 * @author gongjan
	 * 
	 */
	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			BluetoothServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client
				// code
				tmp = blueAdapter.listenUsingRfcommWithServiceRecord(
						"HeartRateApp", UUID.fromString("62f2ad15-647c-4ac0-bfe9-9caf475b921b"));
			} catch (IOException e) {
			}
			mmServerSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					socket = mmServerSocket.accept();
					// read message from socket
					socket.getInputStream();

					// If a connection was accepted
					if (socket != null) {
						// Do work to manage the connection (in a separate
						// thread)
						// manageConnectedSocket(socket);
						mmServerSocket.close();
						break;
					}
				} catch (IOException e) {
					break;
				}

			}
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				mmServerSocket.close();
			} catch (IOException e) {
			}
		}
	}

	
	/**
	 * 客户端线程
	 * 
	 * @author gongjan
	 * 
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;

			
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				BluetoothGatt gatt = device.connectGatt(context, true, callback);
				boolean isConnect = gatt.connect();
				
				
				tmp = device.createRfcommSocketToServiceRecord(UUID
						.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
//			Method m;
//
//			try{
//				
//			 m = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
//
//			 tmp=(BluetoothSocket) m.invoke(device,Integer.valueOf(1));

//			 }catch(SecurityException e1) {
//
//			 //TODOAuto-generated catch block
//
//			 e1.printStackTrace();
//
//			 }catch(NoSuchMethodException e1) {
//
//			 //TODOAuto-generated catch block
//
//			 e1.printStackTrace();
//
//			 }catch(IllegalArgumentException e) {
//
//			 //TODOAuto-generated catch block
//
//			 e.printStackTrace();
//
//			 }catch(IllegalAccessException e) {
//
//			 //TODOAuto-generated catch block
//
//			 e.printStackTrace();
//
//			 }catch(Exception e) {
//
//			 //TODOAuto-generated catch block
//
//			 e.printStackTrace();
//
//			 }
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			// mAdapter.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				int i = 0;
				IOException e = null;
				while(!mmSocket.isConnected()){
					try{
					mmSocket.connect();
					} catch (IOException ex) {e = ex;}
					if(i++ > 5) break;
				}
				if(!mmSocket.isConnected()){
					
					throw new Exception("can not conn bluetooth device " + mmSocket.getRemoteDevice().getName() ,e);
				}
				
				
				ConnectedSocketThread t = new ConnectedSocketThread(mmSocket);
				t.run();
				
			} catch (Exception e) {
				// Unable to connect; close the socket and get out
				e.printStackTrace();
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}

			// Do work to manage the connection (in a separate thread)
			// manageConnectedSocket(mmSocket);
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 连接后接收发送消息的线程
	 * @author gongjan
	 *
	 */
	private class ConnectedSocketThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedSocketThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
//				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[1024]; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					// Send the obtained bytes to the UI Activity
					String tmp = new String(buffer);
					writeMessage.setMessage(tmp.trim());
					
//					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//							.sendToTarget();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}

		/* Call this from the main Activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) {
			}
		}

		/* Call this from the main Activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}
}
