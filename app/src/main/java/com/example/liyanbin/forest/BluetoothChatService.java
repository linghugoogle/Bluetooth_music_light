package com.example.liyanbin.forest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * ���������еĹ����������͹������� ��������װ�á�����һ���̣߳����� �������ӣ���������װ�ã���һ�� ִ�����ݴ���������ʱ��
 */
public class BluetoothChatService {
	// ����
	private static final String TAG = "BluetoothChatService";
	private static final boolean D = true;

	// �����񵳼�¼�������������׽���
	private static final String NAME = "Light";

	// ���ص������Ӧ�ó���

	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static StringBuffer hexString = new StringBuffer();
	// ��������Ա
	private final BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private AcceptThread mAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;
	// ������ָʾ��ǰ������״̬
	public static final int STATE_NONE = 0; // ��ǰû�п��õ�����
	public static final int STATE_LISTEN = 1; // �����������������
	public static final int STATE_CONNECTING = 2; // ���ڿ�ʼ������ϵ
	public static final int STATE_CONNECTED = 3; // �������ӵ�Զ���豸
	public static boolean bRun = true;

	/**
	 * ���캯����׼��һ���µ�bluetoothchat�Ự��
	 * 
	 * @param context
	 *            �û������ı���
	 * @param handler
	 *            һ������������ʼ����û��������
	 */
	public BluetoothChatService(Context context, Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = handler;

	}

	/**
	 * ���õ�ǰ״̬����������
	 * 
	 * @param state
	 *            �������嵱ǰ����״̬
	 */
	private synchronized void setState(int state) {
		if (D)
			Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;
		// ����״̬�Ĵ�����򣬽�����Կ��Ը���
		mHandler.obtainMessage(Light.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}

	/**
	 * ���ص�ǰ������״̬��
	 */
	public synchronized int getState() {
		return mState;
	}

	/**
	 * ��ʼ��������ر�acceptthread��ʼ ��ʼ �Ự��������������ģʽ����ν�Ļonresume()
	 */
	public synchronized void start() {
		if (D)
			Log.d(TAG, "start");

		// ȡ���κ��߳���ͼ��������
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// ȡ���κ��߳��������е�����
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// �����߳�����һ��bluetoothserversocket
		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}
		setState(STATE_LISTEN);
	}

	// ���Ӱ�����Ӧ����
	/**
	 * ��ʼconnectthread�������ӵ�Զ���豸��
	 * 
	 * @param װ�����ӵ������豸
	 */
	public synchronized void connect(BluetoothDevice device) {
		if (D)
			Log.d(TAG, "connect to: " + device);

		// ȡ���κ��߳���ͼ��������
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// ȡ���κ��߳��������е�����
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// �����߳����ӵ��豸
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	/**
	 * ��ʼconnectedthread��ʼ����һ����������
	 * 
	 * @param bluetoothsocket������������
	 * @param �豸�����ӵ������豸
	 */
	@SuppressWarnings("unused")
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		if (D)
			Log.d(TAG, "connected");

		// ȡ���߳��������
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// ȡ���κ��߳��������е�����
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// ȡ�������߳�ֻ��Ϊ����Ҫ���ӵ�һ���豸
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}

		// �����̹߳������Ӻʹ���
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();

		// �����ֵ������豸��Activity

		Message msg = mHandler.obtainMessage(Light.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(Light.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);
		setState(STATE_CONNECTED);
	}

	/**
	 * ֹͣ���е��߳�
	 */
	public synchronized void stop() {
		if (D)
			Log.d(TAG, "stop");
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
		setState(STATE_NONE);
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out) {
		// ������ʱ����
		ConnectedThread r;
		// ͬ��������connectedthread
		synchronized (this) {
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		// ִ��дͬ��
		r.write(out);
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		setState(STATE_LISTEN);
		// ����ʧ�ܵ���Ϣ���ػ
		Message msg = mHandler.obtainMessage(Light.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(Light.TOAST, "�޷�����װ��");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		setState(STATE_LISTEN);
		// ����ʧ�ܵ���Ϣ����Activity
		Message msg = mHandler.obtainMessage(Light.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(Light.TOAST, "װ�����Ӷ�ʧ");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * ����ͬʱ������������ӡ�������Ϊ ϲ��һ���������˵Ŀͻ��ˡ�������ֱ�����ӱ����� ����ȡ������
	 */
	private class AcceptThread extends Thread {
		// ���ط������׽���
		private final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {

			BluetoothServerSocket tmp = null;

			// ����һ���µ������������׽���
			try {
				tmp = mAdapter
						.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "listen() failed", e);
			}
			mmServerSocket = tmp;
		}

		public void run() {
			if (D)
				Log.d(TAG, "BEGIN mAcceptThread" + this);
			setName("AcceptThread");
			BluetoothSocket socket = null;

			// ���������׽����������û������
			while (mState != STATE_CONNECTED) {
				try {
					// ����һ���������úͽ�ֻ����һ��
					// �ɹ������ӻ�����
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "accept() failed", e);
					break;
				}

				// ������ӱ�����
				if (socket != null) {
					synchronized (BluetoothChatService.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// ��������������������ơ�
							connected(socket, socket.getRemoteDevice());
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// û��׼���������ӡ��²�����ֹ��
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG, "Could not close unwanted socket", e);
							}
							break;
						}
					}
				}
			}
			if (D)
				Log.i(TAG, "END mAcceptThread");
		}

		public void cancel() {
			if (D)
				Log.d(TAG, "cancel " + this);
			try {
				mmServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of server failed", e);
			}
		}
	}

	/**
	 * ��������ͼʹ������ϵ ���豸������ֱ�������ӣ����� �ɹ���ʧ�ܡ�
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tmp = null;
			// �õ�һ��bluetoothsocketΪ������
			// ���������豸
			try {
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "create() failed", e);
			}
			mmSocket = tmp;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectThread");
			setName("ConnectThread");

			// ����ȡ���ķ��֣���Ϊ�����������
			mAdapter.cancelDiscovery();
			// ʹһ�����ӵ�bluetoothsocket
			try {
				// ����һ���������úͽ�ֻ����һ��
				// �ɹ������ӻ�����
				mmSocket.connect();
			} catch (IOException e) {
				connectionFailed();
				// �ر����socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG,"unable to close() socket during connection failure",e2);
				}
				// ����������������������ģʽ
				BluetoothChatService.this.start();
				return;
			}
			// ��Ϊ����������connectthread��λ
			synchronized (BluetoothChatService.this) {
				mConnectThread = null;
			}
			// ���������߳�
			connected(mmSocket, mmDevice);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	/**
	 * ������������Զ���豸�� ���������д���ʹ����Ĵ��䡣
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			Log.d(TAG, "create ConnectedThread");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			// ���bluetoothsocket���������
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "û�д�����ʱsockets", e);
			}
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}


		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");
			byte[] buffer = new byte[1024];
			int bytes;

			// ������InputStreamͬʱ����
			while (true) {
				try {
					try {
						ConnectedThread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// ��ȡ������
					bytes = mmInStream.read(buffer);
					// ���ͻ�õ��ֽڵ��û�����
					mHandler.obtainMessage(Light.MESSAGE_READ, bytes,-1, buffer).sendToTarget();
					// Ϊ�޸ı�־λ�ṩ�ȴ�ʱ��
					try {
						ConnectedThread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					break;
				}
			}

		}


		/**
		 * д��������ӡ� @param buffer ����һ���ֽ���
		 */
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);
				// �����͵���Ϣ��Activity
				mHandler.obtainMessage(Light.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
}
