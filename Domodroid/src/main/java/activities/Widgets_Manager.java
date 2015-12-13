package activities;

import java.util.ArrayList;
import java.util.HashMap;

import database.DomodroidDB;
import database.WidgetUpdate;

import org.domogik.domodroid13.R;
import org.domogik.domodroid13.R.string;
import org.json.JSONException;

import rinor.Stats_Com;

import widgets.Com_Stats;
import widgets.Entity_Area;
import widgets.Entity_Feature;
import widgets.Entity_Room;
import widgets.Graphical_Area;
import widgets.Graphical_Binary;
import widgets.Graphical_Binary_New;
import widgets.Graphical_Boolean;
import widgets.Graphical_Cam;
import widgets.Graphical_Color;
import widgets.Graphical_History;
import widgets.Graphical_Info;
import widgets.Graphical_Info_commands;
import widgets.Graphical_Info_with_achartengine;
import widgets.Graphical_List;
import widgets.Graphical_Range;
import widgets.Graphical_Room;
import widgets.Graphical_Trigger;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.DisplayMetrics;
import misc.tracerengine;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class Widgets_Manager {

	private int widgetSize;
	private boolean columns=false;
	private final Handler widgetHandler;
	public WidgetUpdate widgetupdate = null;
	private tracerengine Tracer = null;
	private final String mytag="Widgets_Manager";

	public Widgets_Manager(tracerengine Trac, Handler handler) {
		super();
		this.widgetHandler=handler;
		this.Tracer = Trac;
	}

	public LinearLayout loadActivWidgets(Activity context, int id, 
			String zone, LinearLayout ll, SharedPreferences params, int session_type) throws JSONException
			{

		DomodroidDB domodb = new DomodroidDB(Tracer, context);
		domodb.owner="Widgets_Manager.loadActivWidgets";
		Entity_Feature[] listFeature = domodb.requestFeatures(id, zone);

		LinearLayout.LayoutParams layout_param = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT, 1.0f);
		LinearLayout mainPan = new LinearLayout(context);
		mainPan.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout leftPan = new LinearLayout(context);
		leftPan.setOrientation(LinearLayout.VERTICAL);
		leftPan.setLayoutParams(layout_param);
		LinearLayout rightPan = new LinearLayout(context);
		rightPan.setOrientation(LinearLayout.VERTICAL);
		rightPan.setLayoutParams(layout_param);
		FrameLayout tmpPan = new FrameLayout(context);

		int counter=0;

		//check option and adapt columns in function
		columns=false;
		colonnes(context, ll, mainPan, leftPan, rightPan, params);

		if(id == -1) {
			//We've to display statistics widget
			Tracer.i(mytag, "Call to process statistics widget");
			Com_Stats  statistics = new Com_Stats(Tracer, context, counter);
			statistics.container=tmpPan;
			tmpPan.addView(statistics);
			ll.addView(tmpPan);
			return ll;
		}
		int size = listFeature.length;
		Entity_Feature feature;

		for (int pos = 0; pos < size; ++pos) {
			feature=listFeature[pos];

			//-----add component-------
			tmpPan=null;
			tmpPan=new FrameLayout(context);
			String label = feature.getDescription();
			if(label.length() < 1||label.equalsIgnoreCase("null"))
				label = feature.getName();
			String Value_type = feature.getValue_type();
			String Address = feature.getAddress();
			String URL = params.getString("URL","1.1.1.1");
			String parameters = feature.getParameters();
			String device_type_id = feature.getDevice_type_id();
			String State_key = feature.getState_key();
			int Graph = params.getInt("GRAPH",3);
			int update_timer = params.getInt("UPDATE_TIMER",300);
			int DevId = feature.getDevId();
			int Id = feature.getId();
			String iconName = "unknow";
			try {
				iconName = domodb.requestIcons(Id, "feature").getValue();
			} catch (Exception e) {
				//e.printStackTrace();
			}
			if (iconName.equals("unknow"))
				iconName=feature.getDevice_usage_id();

			//add debug option to change label adding its Id
			if (params.getBoolean("DEV", false))
				label = label+" ("+Id+")";

			String[] model = device_type_id.split("\\.");
			String type ="";
			try{
				type = model[1];
			}catch (Exception e){
				type= model[0];
			}

			Tracer.i(mytag, "Call to process device : "+DevId+" Address : "+Address+" Value_type : "+Value_type+" Label : "+label+" Key : "+State_key);
			//TODO add missing datatype from 0.4
			//String but carreful
			//datetime done
			//ColorCII
			//Char
			//DayOfWeek
			//HVACVent
			//HVACFan
			//HVACMode
			//HVACHeat
			//UPSEvent
			//UPSState
			Graphical_Binary onoff;
			Graphical_Binary_New onoff_New;
			Graphical_Info info;
			Graphical_Info_commands info_commands;
			if (Value_type.equals("binary")) {
				if(type.equals("rgb_leds") && (State_key.equals("command"))) {
					//ignore it : it'll have another device for Color, displaying the switch !)
				} else {
					if (!params.getBoolean("WIDGET_CHOICE", false)) {
						onoff = new Graphical_Binary(Tracer, context,Address,label,
								Id,DevId,State_key,URL,iconName,parameters,device_type_id,
								update_timer,widgetSize, session_type,id,zone,params);
						onoff.container=tmpPan;
						tmpPan.addView(onoff);
						Tracer.i(mytag,"   ==> Graphical_Binary");
					}
					else {
						onoff_New = new Graphical_Binary_New(Tracer, context,Address,label,
								Id,DevId,State_key,URL,iconName,parameters,device_type_id,
								update_timer,widgetSize, session_type,id,zone,params);
						onoff_New.container=tmpPan;
						tmpPan.addView(onoff_New);
						Tracer.i(mytag,"   ==> Graphical_Binary");
					}
				}
			} else if (Value_type.equals("boolean")||Value_type.equals("bool")) {
				if (parameters.contains("command")) {
					if (!params.getBoolean("WIDGET_CHOICE", false)) {
						onoff = new Graphical_Binary(Tracer, context,Address,label,
								Id,DevId,State_key,URL,iconName,parameters,device_type_id,
								update_timer,widgetSize, session_type,id,zone,params);
						onoff.container=tmpPan;
						tmpPan.addView(onoff);
						Tracer.i(mytag,"   ==> Graphical_Binary");
					}
					else {
						onoff_New = new Graphical_Binary_New(Tracer, context,Address,label,
								Id,DevId,State_key,URL,iconName,parameters,device_type_id,
								update_timer,widgetSize, session_type,id,zone,params);
						onoff_New.container=tmpPan;
						tmpPan.addView(onoff_New);
						Tracer.i(mytag,"   ==> Graphical_Binary");
					}
				}
				else {
					Graphical_Boolean bool = new Graphical_Boolean(Tracer, context, Address, label,
							Id, DevId, State_key, iconName, parameters, device_type_id,
							update_timer, widgetSize, session_type, id, zone, params);
					bool.container=tmpPan;
					tmpPan.addView(bool);
					Tracer.i(mytag,"   ==> Graphical_Boolean");
				}
			} else if (Value_type.equals("range")||((parameters.contains("command"))&&(feature.getDevice_feature_model_id().startsWith("DT_Scaling")))){
				Graphical_Range variator = new Graphical_Range(Tracer, context, Address, label,
						Id, DevId, State_key, URL, iconName, parameters, device_type_id,
						update_timer, widgetSize, session_type, id, zone, params);
				variator.container=tmpPan;
				tmpPan.addView(variator);
				Tracer.i(mytag,"   ==> Graphical_Range");
			} else if (Value_type.equals("trigger")) {
				//#51 change widget for 0.4 if it's not a command
				if (parameters.contains("command")){
					Graphical_Trigger trigger = new Graphical_Trigger(Tracer, context, Address, label,
							State_key, Id, DevId, State_key, URL, iconName, parameters, device_type_id,
							widgetSize, session_type, id, zone, params);
					trigger.container=tmpPan;
					tmpPan.addView(trigger);
					Tracer.i(mytag,"   ==> Graphical_Trigger");
				}else{
					info = new Graphical_Info(Tracer, context,Id,DevId,label,
							State_key,URL,iconName,update_timer,
							widgetSize, session_type, parameters,id,zone,params);
					info.setLayoutParams(layout_param);
					info.with_graph=false;
					info.container=tmpPan;
					tmpPan.addView(info);
					Tracer.i(mytag,"   ==> Graphical_Info + No graphic !!!");
				}
			} else if(State_key.equals("color")){
				Tracer.d(mytag,"add Graphical_Color for "+label+" ("+DevId+") key="+State_key);
				Graphical_Color color = new Graphical_Color(Tracer, context,
						params, Id, DevId, label,
						device_type_id,    //Added by Doume to know the 'techno'
						Address,//  idem to know the address
						State_key, URL, iconName, update_timer,
						widgetSize, session_type, id, zone);
				color.container=tmpPan;
				tmpPan.addView(color);
				Tracer.i(mytag,"   ==> Graphical_Color");
			} else if (Value_type.equals("number")) {
				if(feature.getParameters().contains("command_type")) {
					info_commands = new Graphical_Info_commands(Tracer, context,Id,DevId,label,
							State_key,URL,iconName,update_timer,
							widgetSize, session_type, parameters,id,zone,params,Value_type);
					info_commands.setLayoutParams(layout_param);
					info_commands.container=tmpPan;
					tmpPan.addView(info_commands);
					Tracer.i(mytag,"   ==> Graphical_Info_commands !!!");
				}else if (params.getBoolean("Graph_CHOICE", false)) {
					Tracer.d(mytag,"add Graphical_Info_with_achartengine for "+label+" ("+DevId+") key="+State_key);
					Graphical_Info_with_achartengine info_with_achartengine = new Graphical_Info_with_achartengine(Tracer, context,
							Id, DevId, label, State_key, URL, iconName, Graph,
							update_timer, widgetSize, session_type, parameters, id, zone, params);
					info_with_achartengine.setLayoutParams(layout_param);
					info_with_achartengine.container=tmpPan;
					tmpPan.addView(info_with_achartengine);
					Tracer.i(mytag,"   ==> Graphical_Info_with_achartengine + Graphic");
				} else{
					Tracer.d(mytag,"add Graphical_Info for "+label+" ("+DevId+") key="+State_key);
					info = new Graphical_Info(Tracer, context,Id,DevId, label,
							State_key,URL,iconName,update_timer,
							widgetSize, session_type, parameters,id,zone,params);
					info.setLayoutParams(layout_param);
					info.with_graph=true;
					info.container=tmpPan;
					tmpPan.addView(info);
					Tracer.i(mytag,"   ==> Graphical_Info + Graphic");
				}
			} else if (Value_type.equals("list")) {
				Tracer.d(mytag,"add Graphical_List for "+label+" ("+DevId+") key="+State_key);
				Graphical_List list = new Graphical_List(Tracer, context, Id, DevId, label,
						device_type_id,    //Added by Doume to know the 'techno'
						Address,            //  idem to know the address
						State_key, URL, iconName, Graph,
						update_timer, widgetSize, session_type, parameters, device_type_id, id, zone, params);
				list.setLayoutParams(layout_param);
				list.container=tmpPan;
				tmpPan.addView(list);
				Tracer.i(mytag,"   ==> Graphical_List");
			} else if(Value_type.equals("string")||Value_type.equals("datetime")){
				//New widget for callerID
				if(feature.getDevice_feature_model_id().contains("call")) {
					Graphical_History info_with_history = new Graphical_History(Tracer, context, Id, DevId, label,
							State_key, URL, iconName, update_timer,
							widgetSize, session_type, parameters, id, zone, params);
					info_with_history.setLayoutParams(layout_param);
					info_with_history.container=tmpPan;
					tmpPan.addView(info_with_history);
					Tracer.i(mytag,"   ==> Graphical_history");
				}else if(feature.getDevice_feature_model_id().contains("camera")) {
					Graphical_Cam cam = new Graphical_Cam(Tracer, context, Id, DevId, label,
							State_key, Address, iconName, widgetSize, session_type, id, zone);
					cam.container=tmpPan;
					tmpPan.addView(cam);
					Tracer.i(mytag,"   ==> Graphical_Cam");
				}else if(feature.getParameters().contains("command_type")) {
					info_commands = new Graphical_Info_commands(Tracer, context,Id,DevId,label,
							State_key,URL,iconName,update_timer,
							widgetSize, session_type, parameters,id,zone,params,Value_type);
					info_commands.setLayoutParams(layout_param);
					info_commands.container=tmpPan;
					tmpPan.addView(info_commands);
					Tracer.i(mytag,"   ==> Graphical_Info_commands !!!");
				}else {
					info = new Graphical_Info(Tracer, context,Id,DevId,label,
							State_key,URL,iconName,update_timer,
							widgetSize, session_type, parameters,id,zone,params);
					info.setLayoutParams(layout_param);
					info.with_graph=false;
					info.container=tmpPan;
					tmpPan.addView(info);
					Tracer.i(mytag,"   ==> Graphical_Info + No graphic !!!");
				}
				//used by knx.HVACMode 	HVACMode 	actuator 	knx.HVACMode
			}

			if(columns){	
				if(counter==0){
					leftPan.addView(tmpPan);
				}
				else if(counter==1){
					rightPan.addView(tmpPan);
				}
				counter++;
				if(counter==2)counter=0;
			}else ll.addView(tmpPan);
		}
		return ll;
			}

	public LinearLayout loadAreaWidgets(Activity context, LinearLayout ll, SharedPreferences params) {

		DomodroidDB domodb = new DomodroidDB(Tracer, context);
		domodb.owner="Widgets_Manager.loadAreaWidgets";
		Entity_Area[] listArea = domodb.requestArea();
		Tracer.d(mytag+" loadAreaWidgets","Areas list size : "+listArea.length);

		LinearLayout.LayoutParams layout_param = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT, 1.0f);
		LinearLayout mainPan = new LinearLayout(context);
		mainPan.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout leftPan = new LinearLayout(context);
		leftPan.setOrientation(LinearLayout.VERTICAL);
		leftPan.setLayoutParams(layout_param);
		LinearLayout rightPan = new LinearLayout(context);
		rightPan.setOrientation(LinearLayout.VERTICAL);
		rightPan.setLayoutParams(layout_param);
		FrameLayout tmpPan = new FrameLayout(context);

		int counter=0;

		//check option and adapt columns in function
		columns=false;
		colonnes(context, ll, mainPan, leftPan, rightPan, params);

		int size = listArea.length;
		Entity_Area area;

		//TODO iprove navigation drawer
		Activity_Main.Navigation_drawer_ItemsList=new ArrayList<HashMap<String,String>>();


		try {
			for (int pos = 0; pos < size; ++pos) {
				area=listArea[pos];
				int Id = area.getId();
				String iconId = "unknown";
				try {
					iconId = domodb.requestIcons(Id, "area").getValue();
				} catch (Exception e) {
					//e.printStackTrace();
				}
				tmpPan=null;
				tmpPan=new FrameLayout(context);
				Tracer.d(mytag+" loadRoomWidgets","Adding area : "+area.getName());
				String name = area.getName();
				name = Graphics_Manager.Names_Agent(context, name);

//				map=new HashMap<String,String>();
//				map.put("name",area.getName());
//				String iconName = "unknow";
//				try {
//					iconName = domodb.requestIcons(area.getId(), "area").getValue().toString();
//				} catch (Exception e) {
//					Tracer.e(mytag,e.toString());
//				}
//				map.put("icon", Integer.toString(Graphics_Manager.Icones_Agent(iconName,1)));
//				Activity_Main.Navigation_drawer_ItemsList.add(map);

				Graphical_Area graph_area = new Graphical_Area(Tracer, context, Id, name, area.getDescription(), iconId, widgetSize, widgetHandler);
				tmpPan.addView(graph_area);
				if(columns){	
					if(counter==0){
						leftPan.addView(tmpPan);
					}
					else if(counter==1){
						rightPan.addView(tmpPan);
					}
					counter++;
					if(counter==2)counter=0;
				}else ll.addView(tmpPan);
			}
		}catch (Exception e) {
			//e.printStackTrace();
		}

		return ll;
	}

	public LinearLayout loadRoomWidgets(Activity context, int id, LinearLayout ll, SharedPreferences params) {

		DomodroidDB domodb = new DomodroidDB(Tracer, context);
		domodb.owner="Widgets_Manager.loadRoomWidgets";
		Entity_Room[] listRoom = domodb.requestRoom(id);
		Tracer.d(mytag+" loadRoomWidgets","Rooms list size : "+listRoom.length);

		LinearLayout.LayoutParams layout_param = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT, 1.0f);
		LinearLayout mainPan = new LinearLayout(context);
		mainPan.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout leftPan = new LinearLayout(context);
		leftPan.setOrientation(LinearLayout.VERTICAL);
		leftPan.setLayoutParams(layout_param);
		LinearLayout rightPan = new LinearLayout(context);
		rightPan.setOrientation(LinearLayout.VERTICAL);
		rightPan.setLayoutParams(layout_param);
		FrameLayout tmpPan = new FrameLayout(context);
		int counter=0;

		//check option and adapt columns in function
		columns=false;
		colonnes(context, ll, mainPan, leftPan, rightPan, params);
		int size = listRoom.length;
		Entity_Room room;

		//TODO improve navigation drawer
		Activity_Main.Navigation_drawer_ItemsList=new ArrayList<HashMap<String,String>>();
		for (int pos = 0; pos < size; ++pos) {
			room=listRoom[pos];
			int Id = room.getId();
			String iconId = "unknown";
			try{
				iconId = domodb.requestIcons(Id, "room").getValue();
			}catch(Exception e){}
			if(iconId.equals("unknown")) {
				//iconId="usage";
				iconId=room.getName();
			}
			tmpPan=null;
			tmpPan=new FrameLayout(context);
			String ref = room.getDescription();
			if(ref.length() == 0)
				ref = room.getName();
			Tracer.d(mytag+" loadRoomWidgets","Adding room : "+ref);
			String name = room.getName();
			name = Graphics_Manager.Names_Agent(context, name);
			
			//populate the NAvigationDrawer list
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("name", name);
			map.put("icon", Integer.toString(Graphics_Manager.Icones_Agent(iconId, 0)));
			Activity_Main.Navigation_drawer_ItemsList.add(map);

			Graphical_Room graph_room = new Graphical_Room(Tracer, context, Id, name, room.getDescription(), iconId, widgetSize, widgetHandler);
			tmpPan.addView(graph_room);

			if(columns){	
				if(counter==0){
					leftPan.addView(tmpPan);
				}


				else if(counter==1){
					rightPan.addView(tmpPan);
				}
				counter++;
				if(counter==2)counter=0;
			}else ll.addView(tmpPan);
		}

		return ll;
	}

	public void colonnes(Activity context, LinearLayout ll,LinearLayout mainPan, LinearLayout leftPan,LinearLayout rightPan, SharedPreferences params){
		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;

		boolean landscape;
		landscape = width > height;

		int maxSize = 700;
		if(width > maxSize){
			if(landscape && !params.getBoolean("twocol_lanscape", false)){
				Tracer.v(mytag, "params.getBoolean twocol_lanscape "+params.getBoolean("twocol_lanscape",false));

				columns=true;
				mainPan.addView(leftPan);
				mainPan.addView(rightPan);
				ll.addView(mainPan);
			}else if(!landscape && !params.getBoolean("twocol_portrait", false)){
				Tracer.v(mytag, "params.getBoolean twocol_portrait "+params.getBoolean("twocol_portrait",false));

				columns=true;
				mainPan.addView(leftPan);
				mainPan.addView(rightPan);
				ll.addView(mainPan);
			}
		}

	}
}

