package com.example.liyanbin.forest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

/**
 * �����ƺ���һ���Ի������г����κ���Ե��豸��װ�����ֳ����ֺ�ķ��֡�
 * ��һ���豸�����û�ѡ�񣬵�ַ���豸���͸��ҳ���Ľ����ͼ��
 */
public class DeviceListActivity extends Activity
{
	// ����
	private static final String TAG = "DeviceListActivity";
	private static final boolean D = true;
	// ���ر����ͼ
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	// ������
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		// ָ��������ʽ
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);

		// ���ȡ������û�����
		setResult(Activity.RESULT_CANCELED);

		// ���ȡ������û�����
		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				doDiscovery();
				v.setVisibility(View.GONE);
			}
		});

		// ��ʼ��������������һ�������װ�ú�
         //һ���·��ֵ��豸
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_item);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_item);

		//Ѱ�Һͽ�������豸�б�
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// Ѱ�Һͽ���Ϊ�·��ֵ��豸�б�
		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		// ע��ʱ���͹㲥���豸
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		// �㲥ʱ���������ע��
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		// ��ȡ��������������
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		// �õ�һ��Ŀǰ����豸
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0){
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices){
				mPairedDevicesArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
			}
		}
		else{
			String noDevices = "û��ƥ���豸";
			mPairedDevicesArrayAdapter.add(noDevices);
		}
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		// ȷ������û�з�����
		if (mBtAdapter != null){
			mBtAdapter.cancelDiscovery();
		}
		// ע���㲥����
		this.unregisterReceiver(mReceiver);
	}

	/**
	 * ������bluetoothadapter����װ��
	 */
	private void doDiscovery(){
		if (D) Log.d(TAG, "doDiscovery()");
		// ��ʾɨ��ĳƺ�
		setProgressBarIndeterminateVisibility(true);
		setTitle("����ɨ��...");
		// �����豸����Ļ
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
		// ��������Ѿ����֣���ֹ��
		if (mBtAdapter.isDiscovering()){
			mBtAdapter.cancelDiscovery();
		}
		// Ҫ���bluetoothadapter����
		mBtAdapter.startDiscovery();
	}

	// ������ڵ������豸��listviews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3){
			// ��Ϊ�����˷ѵģ�ȡ���������ǵ�����
			mBtAdapter.cancelDiscovery();
			// ����豸��ַ�����ǽ�17�ֵ�
			//��ͼ
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);
			//���������ͼ�Ͱ�����ַ
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			//������������
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

	// ��broadcastreceiver�����豸��
	// �仯�ı���ʱ���������
	private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			// �������豸
			if (BluetoothDevice.ACTION_FOUND.equals(action)){
				//�������豸�������ͼ
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// ������Ѿ���ԣ�����������Ϊ��������
				// ����
				if (device.getBondState() != BluetoothDevice.BOND_BONDED){
					mNewDevicesArrayAdapter.add(device.getName() + "\n"
							+ device.getAddress());
				}
				//�����ֺ󣬸ı�����
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				setProgressBarIndeterminateVisibility(false);
				setTitle("ѡ��һ���豸����");
				if (mNewDevicesArrayAdapter.getCount() == 0){
					String noDevices = "û���ҵ��豸";
					mNewDevicesArrayAdapter.add(noDevices);
				}
			}
		}
	};
	
}
