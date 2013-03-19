package activities;

import org.domogik.domodroid.R;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import database.WidgetUpdate;
import map.Dialog_Help;
import map.MapView;
import activities.Sliding_Drawer.OnPanelListener;
import widgets.Entity_Feature;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import misc.tracerengine;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Activity_Map extends Activity implements OnPanelListener,OnClickListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Sliding_Drawer panel;
	private Sliding_Drawer topPanel;
	private Sliding_Drawer bottomPanel;
	private Button add;
	private Button help;
	private Button remove;
	private Button move;
	private Button remove_all;
	private Dialog dialog_feature;

	private Entity_Feature[] listFeature;
	private HashMap<String,String> map;

	private Vector<String> list_usable_files;
	private MapView mapView;
	private SharedPreferences.Editor prefEditor;
	private SharedPreferences params;
	private ViewGroup panel_widget;
	private ViewGroup panel_button;

	private ListView listeMap;
	private ArrayList<HashMap<String,String>> listItem;
	private Animation animation1;
	private Animation animation2;
	private TextView menu_white;
	private TextView menu_green;
	
	private WidgetUpdate widgetUpdate;
	private Handler sbanim;
	private File f = null;
	String files[] = null;
	private tracerengine Tracer = null;
	private String owner = "Map";
	private Boolean dont_freeze = false;
	/*
	 * WARNING : this class does'nt access anymore directly the database
	 * 		It must use methods located into WidgetUpdate engine
	 * 		which is permanently connected to local database
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		params = getSharedPreferences("PREFS",MODE_PRIVATE);
		Tracer = tracerengine.getInstance(params);
		prefEditor=params.edit();
		mapView = new MapView(Tracer, this);
		mapView.setParams(params);
		mapView.setUpdate(params.getInt("UPDATE_TIMER",300));
		setContentView(R.layout.activity_map);
		ViewGroup parent = (ViewGroup) findViewById(R.id.map_container);

		//titlebar
		final FrameLayout titlebar = (FrameLayout) findViewById(R.id.TitleBar);
		titlebar.setBackgroundDrawable(Gradients_Manager.LoadDrawable("title",40));

		//menu button
		menu_white = (TextView) findViewById(R.id.menu_button1);
		menu_white.setOnClickListener(this);
		menu_white.setTag("menu");
		menu_green = (TextView) findViewById(R.id.menu_button2);
		menu_green.setVisibility(View.GONE);

		animation1 = new AlphaAnimation(0.0f, 1.0f);
		animation1.setDuration(500);
		animation2 = new AlphaAnimation(1.0f, 0.0f);
		animation2.setDuration(500);

		//read files from SDCARD + create directory
		
		createDirIfNotExists();
		f=new File(Environment.getExternalStorageDirectory()+"/domodroid/"); 
		if(f.isDirectory()){ 
			files= f.list(); 
		}


		build_maps_list();
		
		
		//sliding drawer
		topPanel = panel = (Sliding_Drawer) findViewById(R.id.map_slidingdrawer);
		panel.setOnPanelListener(this);
		panel.setOnTouchListener(new OnTouchListener() {	 
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		
		bottomPanel = panel = (Sliding_Drawer) findViewById(R.id.bottomPanel);
		panel.setOnPanelListener(this);
		mapView.setTopDrawer(topPanel);
		mapView.setBottomDrawer(bottomPanel);

		panel_widget = (ViewGroup) findViewById(R.id.panelWidget);
		panel_button = (ViewGroup) findViewById(R.id.panelButton);

		mapView.setPanel_widget(panel_widget);
		mapView.setPanel_button(panel_button);

		//add remove buttonObject engine = (Object)widgetUpdate;
		add = new Button(this);
		add.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,1));
		add.setPadding(10, 13, 10, 13);
		add.setText(R.string.map_button1);
		add.setTextColor(Color.parseColor("#cfD1D1"));
		add.setTextSize(15);
		add.setTag("add");
		add.setBackgroundColor(Color.parseColor("#00000000"));
		add.setOnClickListener(this);
		
		help = new Button(this);
		help.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,1));
		help.setPadding(10, 13, 10, 13);
		help.setText(R.string.map_button3);
		help.setTextColor(Color.parseColor("#cfD1D1"));
		help.setTextSize(15);
		help.setTag("help");
		help.setBackgroundColor(Color.parseColor("#00000000"));
		help.setOnClickListener(this);
		
		remove = new Button(this);
		remove.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,1));
		remove.setPadding(10, 13, 10, 13);
		remove.setText(R.string.map_button2);
		remove.setTextColor(Color.parseColor("#cfD1D1"));
		remove.setTextSize(15);
		remove.setTag("remove");
		remove.setBackgroundColor(Color.parseColor("#00000000"));
		remove.setOnClickListener(this);
		
		remove_all = new Button(this);
		remove_all.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,1));
		remove_all.setPadding(10, 13, 10, 13);
		remove_all.setText(R.string.map_button2b);
		remove_all.setTextColor(Color.parseColor("#cfD1D1"));
		remove_all.setTextSize(15);
		remove_all.setTag("remove_all");
		remove_all.setBackgroundColor(Color.parseColor("#00000000"));
		remove_all.setOnClickListener(this);
		
		move = new Button(this);
		move.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,1));
		move.setPadding(10, 13, 10, 13);
		move.setText(R.string.map_button2c);
		move.setTextColor(Color.parseColor("#cfD1D1"));
		move.setTextSize(15);
		move.setTag("move");
		move.setBackgroundColor(Color.parseColor("#00000000"));
		move.setOnClickListener(this);
		
		
		panel_button.addView(add);
		panel_button.addView(help);
		panel_button.addView(remove);
		panel_button.addView(move);
		panel_button.addView(remove_all);


		bottomPanel = panel = (Sliding_Drawer) findViewById(R.id.bottomPanel);
		panel.setOnPanelListener(this);

		dialog_feature = new Dialog(this);


		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.Add_widget_title);

		startCacheEngine();		//Get reference to WidgetUpdate engine
		//When back, the engine should be ready.... (mini widgets and widgets require it to connect !)
		
		
		//get feature list
		listFeature = widgetUpdate.requestFeatures();
		
		//listview feature
		ListView listview_feature = new ListView(this);
		ArrayList<HashMap<String,String>> listItem1=new ArrayList<HashMap<String,String>>();
		if(listFeature != null) {
			for (Entity_Feature feature : listFeature) {
				if(feature != null) {
					map=new HashMap<String,String>();
					map.put("name",feature.getName());
					map.put("type",feature.getValue_type());
					map.put("state_key", feature.getState_key());
					listItem1.add(map);
				}
			}
		}
		int i;
		if(list_usable_files != null) {
			for ( i=0;i<list_usable_files.size();i++) {
				map=new HashMap<String,String>();
				map.put("name",getText(R.string.go_to_Map).toString());
				map.put("type", "");
				map.put("state_key", list_usable_files.elementAt(i));
				listItem1.add(map);
			}
		}
		if((listItem1 != null) && (listItem1.size() > 0) ) {
			SimpleAdapter adapter_feature=new SimpleAdapter(getBaseContext(),listItem1,
					R.layout.item_feature,new String[] {"name","type","state_key"},new int[] {R.id.name,R.id.description,R.id.state_key});
			listview_feature.setAdapter(adapter_feature);
			listview_feature.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if(position < listFeature.length) {
						// It's a feature element
						mapView.map_id = -1;
						mapView.temp_id = listFeature[position].getId();
					} else {
						//It's a map switch element
						mapView.temp_id = -1;
						mapView.map_id = position - listFeature.length;
						Tracer.e("Activity_Main","map selected <"+list_usable_files.elementAt(mapView.map_id)+">");
					}
					mapView.setAddMode(true);
					dialog_feature.dismiss();
				}
			});
		}

		builder.setView(listview_feature);
		dialog_feature = builder.create();

		if(!list_usable_files.isEmpty()){
			mapView.initMap();
			//mapView.updateTimer();
			parent.addView(mapView);
		} else {
			Dialog_Help dialog_help = new Dialog_Help(this);
			dialog_help.show();
		}
		//update thread
		sbanim = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				/*
				if(msg.what==0){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name2));
				}else if(msg.what==1){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name3));
				}else if(msg.what==2){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name1));
				}else if(msg.what==3){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name4));
				}
				*/
			}	
		};

		try {
			mapView.drawWidgets();
		} catch (Exception e) {
			e.printStackTrace();
		}


	}
	private void startCacheEngine() {
		
		if(widgetUpdate == null) {
			Tracer.w("Activity_Map", "Starting WidgetUpdate engine !");
			widgetUpdate = WidgetUpdate.getInstance();
			//Map is'nt the first caller, so init is'nt required (already done by View)
			widgetUpdate.set_handler(sbanim, 1);	//Put our main handler to cache engine (as Map)
			widgetUpdate.wakeup();
		}  
		Tracer.set_engine(widgetUpdate);
		Tracer.w("Activity_Map", "WidgetUpdate engine connected !");
		
	}
	
	private void build_maps_list() {
		
		if(listeMap != null)
			listeMap = null;
		if(listItem != null)
			listItem = null;
		if(list_usable_files != null)
			list_usable_files = null;
		
		//list Map
		listeMap = (ListView)findViewById(R.id.listeMap);
		listItem=new ArrayList<HashMap<String,String>>();
		list_usable_files = new Vector<String>();
		int i = 0;
		for ( i=0;i<files.length;i++) {
			if(!files[i].startsWith(".")){
				list_usable_files.add(files[i]);
				map=new HashMap<String,String>();
				map.put("name",files[i].substring(0, files[i].lastIndexOf('.')));
				map.put("position",String.valueOf(i));
				listItem.add(map);
			}
		}
		if(mapView != null)
			mapView.setFiles(list_usable_files);
		
		if((Tracer != null) && (Tracer.Map_as_main )) {
			// Add possibility to invoke Main activity
			map=new HashMap<String,String>();
			map.put("name",getText(R.string.go_Main).toString());
			map.put("position",String.valueOf(i));
			listItem.add(map);
		}
		SimpleAdapter adapter_map=new SimpleAdapter(getBaseContext(),listItem,
				R.layout.item_map,new String[] {"name"},new int[] {R.id.name});
		listeMap.setAdapter(adapter_map);
		listeMap.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//Tracer.d("Activity_Map.onclick","Map selected at Position = "+position);
				int last_map = list_usable_files.size() - 1;
				if((position <= last_map) && (position > -1) ) {
					mapView.setCurrentFile(position);
					mapView.initMap();
				} else {
					if((position > last_map) && (Tracer.Map_as_main)) { 
						//Go to main screen...
						Tracer.force_Main = true;	//Flag to allow widgets display, even if START_ON_MAP is set !
						Intent mapI = new Intent(Activity_Map.this,Activity_Main.class);
						Tracer.d("Activity_Map","Call to Main, run it now !");
						startActivity(mapI);
					}
				}
			}
		});


	}
	@Override
	public void onPause(){
		super.onPause();
		panel.setOpen(false, false);
		if(Tracer != null) {
			Tracer.e("Activity_Map", "onPause");
			if(Tracer.Map_as_main) {
				widgetUpdate.set_sleeping();	//We act as main screen : if going to pause, freeze cache engine
			}
		}
		
	}
	
	public void onResume() {
		super.onResume();
		if(Tracer == null) {
			Tracer = tracerengine.getInstance(params);
		}
		Tracer.e("Activity_Map.onResume","Try to connect on cache engine !");
		
		if(widgetUpdate == null) {
			startCacheEngine();
		} else {
			widgetUpdate.wakeup();
			build_maps_list();
			if(mapView != null) {
				mapView.refreshMap();
			}
		}
		
		
		
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(Tracer != null)
			Tracer.e("ActivityMap.onDestroy","Leaving Map_Activity : disconnect from engines");
		
		if(widgetUpdate != null) {
			widgetUpdate.Disconnect(2);	//That'll purge all connected widgets for MapView
		}
	
		if(mapView != null)
			mapView.purge();
			mapView=null;
		
		if(Tracer != null) {
			Tracer.close();		//To eventually flush and close txt log file
			Tracer = null;		//Stop own Tracer engine
		}
		
		System.gc();
	}
	
	public void onPanelClosed(Sliding_Drawer panel) {
		if(Tracer != null)
			Tracer.e("ActivityMap.onPanelClosed","panel request to close");
		menu_green.startAnimation(animation2);
		menu_green.setVisibility(View.GONE);
		panel_widget.removeAllViews();
		mapView.setRemoveMode(false);
		mapView.setMoveMode(false);
		remove.setTextColor(Color.parseColor("#cfD1D1"));
		move.setTextColor(Color.parseColor("#cfD1D1"));
	}


	public void onPanelOpened(Sliding_Drawer panel) {
		//disable menu if set in option
		if(params.getBoolean("map_menu_disable",false)==false){ 
			if(Tracer != null)
				Tracer.e("ActivityMap.onPanelOpened","panel request to be displayed");
			menu_green.setVisibility(View.VISIBLE);
			menu_green.startAnimation(animation1);
		}
	}



	public void onClick(View v) {
		if(v.getTag().equals("menu")){
			//disable menu if set in option
			if(params.getBoolean("map_menu_disable",false)==false){ 
				if(!topPanel.isOpen()){
					bottomPanel.setOpen(true, true);
					panel_button.setVisibility(View.VISIBLE);
					topPanel.setOpen(true, true);
				}else if(topPanel.isOpen() && !bottomPanel.isOpen()){
					panel_widget.setVisibility(View.GONE);
					panel_button.setVisibility(View.VISIBLE);
					bottomPanel.setOpen(true, true);
				}else{
					bottomPanel.setOpen(false, true);
					topPanel.setOpen(false, true);
				}			

			}
		}
		else if(v.getTag().equals("add")){
			//Add a widget
			panel.setOpen(false, true);
			if(list_usable_files.isEmpty()){
				Toast.makeText(this,  getText(R.string.map_nothing), Toast.LENGTH_LONG).show();
			}else{
				//show list of feature available
				dialog_feature.show();
				remove.setTextColor(Color.parseColor("#cfD1D1"));
				move.setTextColor(Color.parseColor("#cfD1D1"));
				mapView.setRemoveMode(false);
				mapView.setMoveMode(false);
			}
			
		}else if(v.getTag().equals("remove")){
			//case when user want to remove only one widget
			if(list_usable_files.isEmpty()){
				Toast.makeText(this,  getText(R.string.map_nothing), Toast.LENGTH_LONG).show();
			}else{
				if(mapView.isRemoveMode()==false){
					//if remove mode is select for the first time
					//Turn menu text color to green
					move.setTextColor(Color.parseColor("#cfD1D1"));
					remove.setTextColor(Color.GREEN);
					//say Mapview.java to turn on remove mode
					mapView.setMoveMode(false);
					mapView.setRemoveMode(true);
				}else{
					//Remove mode was active, return to normal mode
					//Turn menu text color back
					remove.setTextColor(Color.parseColor("#cfD1D1"));
					move.setTextColor(Color.parseColor("#cfD1D1"));
					mapView.setRemoveMode(false);
				}
			}
			
		}else if(v.getTag().equals("move")){
			//case when user want to move one widget
			// first step remove, second add the removed widget
			if(list_usable_files.isEmpty()){
				Toast.makeText(this,  getText(R.string.map_nothing), Toast.LENGTH_LONG).show();
			}else{
				if(mapView.isMoveMode()==false){
					//if remove mode is select for the first time
					//Turn menu text color to green
					remove.setTextColor(Color.parseColor("#cfD1D1"));
					move.setTextColor(Color.GREEN);
					//say Mapview.java to turn on remove mode
					mapView.setRemoveMode(false);
					mapView.setMoveMode(true);
				}else{
					//Remove mode was active, return to normal mode
					//Turn menu text color back
					remove.setTextColor(Color.parseColor("#cfD1D1"));
					move.setTextColor(Color.parseColor("#cfD1D1"));
					mapView.setRemoveMode(false);
				}
			}
			
		} else if(v.getTag().equals("remove_all")){
			//case when user select remove all from menu
				if(list_usable_files.isEmpty()){
					Toast.makeText(this, getText(R.string.map_nothing), Toast.LENGTH_LONG).show();
				}else{
					Tracer.e("Activity_Map","request to clear widgets");
					mapView.clear_Widgets();
					remove.setTextColor(Color.parseColor("#cfD1D1"));
					mapView.setRemoveMode(false);					
				}
		}else if(v.getTag().equals("help")){
			Dialog_Help dialog_help = new Dialog_Help(this);
			dialog_help.show();
			prefEditor.putBoolean("SPLASH", true);
			prefEditor.commit();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//disable menu if set in option
		if(params.getBoolean("map_menu_disable",false)==false){ 
			Tracer.d("Activity_Map","onKeyDown keyCode = "+keyCode);
			if(keyCode==82 && !topPanel.isOpen()){
				bottomPanel.setOpen(true, true);
				panel_button.setVisibility(View.VISIBLE);
				topPanel.setOpen(true, true);
				return false;

			}else if(keyCode==82 && topPanel.isOpen() && !bottomPanel.isOpen()){
				panel_widget.setVisibility(View.GONE);
				panel_button.setVisibility(View.VISIBLE);
				bottomPanel.setOpen(true, true);
				return false;

			}else if((keyCode==82 || keyCode == 4) && topPanel.isOpen()){
				bottomPanel.setOpen(false, true);
				topPanel.setOpen(false, true);
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public String getFileAsString(File file){ 
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		StringBuffer sb = new StringBuffer();
		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			while (dis.available() != 0) {
				sb.append( dis.readLine() +"\n");
			}
			fis.close();
			bis.close();
			dis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}



	public static boolean createDirIfNotExists() {
		boolean ret = true;
		File file = new File(Environment.getExternalStorageDirectory(), "/domodroid");
		if (!file.exists()) {
			if (!file.mkdirs()) {
				ret = false;
			}
		}
		return ret;
	}
}
