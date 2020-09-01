package com.example.numerica;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.hanheng.a53.beep.BeepClass;
import com.example.aircondition.R;
import com.hanheng.a53.seg7.Seg7Class;
import com.hanheng.a53.led.LedClass;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

@SuppressLint({ "ShowToast", "HandlerLeak" }) 
public class MainActivity extends Activity implements OnClickListener {
	private View display;
	private EditText text;
	private EditText text2;
	private TextView counterText;
	private TextView textview1;
	private TextView textview2;
	private Button count_btn;
	private Button count_submit;
	private Button count_stop;
	private Button count_okoil;
	private Button lock;
//	定义标志位控制启动线程
	private boolean flag;
	
	public static final int BEEP_ON = 0;
    public static final int BEEP_OFF = 1;
    
    public static int is_lock = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		初始化标签
		init();
	}

	// 初始化获取每个视图和控件,并添加按钮点击事件
	public void init() {
		this.display = (View) findViewById(R.id.display1);
		this.count_btn = (Button) display.findViewById(R.id.count);
		this.text = (EditText) display.findViewById(R.id.editText1);
		this.text2 = (EditText) display.findViewById(R.id.editText2);
		this.counterText = (TextView) display.findViewById(R.id.counterText);
		this.textview1 = (TextView) display.findViewById(R.id.textView1);
		//textview2.setText("100");
		this.textview2 = (TextView) display.findViewById(R.id.textView2);
		textview2.setText("油量：");
		this.count_submit = (Button) display.findViewById(R.id.submit);
		this.count_okoil = (Button) display.findViewById(R.id.okoil);
		this.count_stop = (Button) display.findViewById(R.id.stop);
		this.lock = (Button) display.findViewById(R.id.button1);
		this.count_submit.setOnClickListener(this);
		this.count_okoil.setOnClickListener(this);
		this.count_btn.setOnClickListener(this);
		this.count_stop.setOnClickListener(this);
		this.lock.setOnClickListener(this);
		int err = Seg7Class.Init();
		LedClass.Init();
		BeepClass.Init();
		System.out.println("测试:"+err);
	}

	// 定义handler接收线程回传内容
	private Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				updateText(msg.arg1);
				
				break;
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.submit:
			String content = text.getText().toString();
			Air.tent=content;
			flag = false;
			if (content.length() > 4) {
				Log.i("监听", content);
				Toast.makeText(getApplication(), "请输入4位数", Toast.LENGTH_LONG);
			} else {
				this.updateText(Integer.valueOf(content));
			}
			break;
		case R.id.okoil:
			String content1 = text2.getText().toString();
			Air.oil=content1;
			//flag = false;
			if (content1.length() > 4) {
				Log.i("监听", content1);
				Toast.makeText(getApplication(), "请输入4位数", Toast.LENGTH_LONG);
			} else {
			    textview1.setText(Air.oil);
			}
			break;
		case R.id.count:
			if (!this.flag) {
				MyThread thread = new MyThread();
				this.flag = true;
				LedClass.IoctlLed(0, 1);
				thread.start();
			}
			break;
		case R.id.stop:
			this.flag = false;
			LedClass.IoctlLed(0, 0);
			break;
		case R.id.button1:
			if(is_lock==0){
				count_btn.setEnabled(false);
				count_submit.setEnabled(false);
				count_stop.setEnabled(false);
				count_okoil.setEnabled(false);
				is_lock=1;
			}
			else{
				count_btn.setEnabled(true);
				count_submit.setEnabled(true);
				count_stop.setEnabled(true);
				count_okoil.setEnabled(true);
				is_lock=0;
			}
		default:
			break;
		}
	}
	
	public void updateText(final int arg){
		String str = addZero(String.valueOf(arg));
		this.counterText.setText(str);
	
		this.textview1.setText(Air.oil);
		/**
		 * 请在此补充硬件调用函数
		*/
		new Thread(new Runnable() {
			public void run() {
				Seg7Class.Seg7Show(arg);
			}
		}).start();
	}

	// 不足4位进行补零
	public String addZero(String content) {
		int count = 4 - content.length();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < count; i++) {
			sb.append("0");
		}
		StringBuffer str = sb.append(content);
		return str.toString();
	}

	// 定义内部线程类
	class MyThread extends Thread {
		public void run() {
			int num = 0;
			int current;
			int aim;
			int temp;
			int oilint=Integer.parseInt(Air.oil);
     		temp=Integer.parseInt(Air.tent);
		    current=temp/100;
		    aim=temp%100;
		    
		    BeepClass.IoctlRelay(BEEP_ON);
			try{
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			BeepClass.IoctlRelay(BEEP_OFF);
		    
		    boolean isHot=true;
		    if(current>aim){
		    	isHot=false;//制冷
		    	current++;
		    }
		    else{
		    	isHot=true;//制热
		    	current--;
		    }
		  
			while (flag) {
				try {
					if(isHot)
						current++;
					else
						current--;
					
		    		temp=current*100+aim;
		    		num=temp;
		    		int speed = 1000;
		  		    
		             if(oilint>0&&oilint<=30){
		          	   speed=2000;
		          	   oilint=oilint-1;
		          	   //String str1="油量："+oilint;
		          	   LedClass.IoctlLed(1, 0);
		          	   LedClass.IoctlLed(2, 0);
		          	   LedClass.IoctlLed(3, 1);
		             }
		             if(oilint>30&&oilint<=70){
		          	   speed=1000;
		          	   oilint=oilint-2;
		          	   LedClass.IoctlLed(1, 0);
		          	   LedClass.IoctlLed(2, 1);
		          	   LedClass.IoctlLed(3, 1);
		          	   //String str1="油量："+oilint;
		          	   //textview1.setText(str1);
		          	   
		             }
		             if(oilint>70&&oilint<=100){
		          	   speed=500;
		          	   oilint=oilint-3;
		          	   LedClass.IoctlLed(1, 1);
		          	   LedClass.IoctlLed(2, 1);
		          	   LedClass.IoctlLed(3, 1);
		          	   //String str1="油量："+oilint;
		          	  // textview1.setText(str1);
		             }
		             
		            Air.oil=String.valueOf(oilint);
//					定义消息对象
					Message msg = new Message();
					msg.what = 1;
					msg.arg1 = num;
//					通过Handler发送消息
					uiHandler.sendMessage(msg);
		    		
					if(current==aim||oilint<=0){
						flag=false;
						BeepClass.IoctlRelay(BEEP_ON);
						Thread.sleep(500);
						BeepClass.IoctlRelay(BEEP_OFF);
					}
					Thread.sleep(speed);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event){  
		if (keyCode == KeyEvent.KEYCODE_BACK  ){  
            AlertDialog isExit = new AlertDialog.Builder(this).create();  
            isExit.setTitle("系统提示");  
            isExit.setMessage("确定要退出吗");  
            isExit.setButton("确定", listener);  
            isExit.setButton2("取消", listener);  
            isExit.show();    
        }  
		return false;  
	}  
	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){  
		public void onClick(DialogInterface dialog, int which){  
			switch (which){  
			case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序  
				Seg7Class.Exit();
				finish();   
				break;  
			case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框  
				break;  
			default:  
				break;  
            }  
        }  
    };    
}
