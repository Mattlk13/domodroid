/*
 * This file is part of Domodroid.
 * 
 * Domodroid is Copyright (C) 2011 Pierre LAINE, Maxime CHOFARDET
 * 
 * Domodroid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Domodroid is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Domodroid. If not, see <http://www.gnu.org/licenses/>.
 */
package widgets;

import java.util.ArrayList;
import java.util.List;

import rinor.CallUrl;
import rinor.Rest_com;
import widgets.Graphical_Binary.SBAnim;
import database.DmdContentProvider;
import database.JSONParser;
import database.WidgetUpdate;

import org.json.JSONException;
import org.json.JSONObject;

import activities.Gradients_Manager;
import activities.Graphics_Manager;

import org.domogik.domodroid13.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import misc.List_Icon_Adapter;
import misc.tracerengine;

import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class Graphical_Range extends Basic_Graphical_widget implements SeekBar.OnSeekBarChangeListener {

    private TextView state;
    private SeekBar seekBarVaria;
    private final String address;
    private int state_progress;
    private final String usage;
    private final String url;
    private Handler handler;
    private int scale;
    private int valueMin = 0;
    private int valueMax = 100;
    private int CustomMax;
    private String type;
    public static int stateThread;
    public final boolean activate = false;
    private Animation animation;
    private boolean touching;
    private int updating = 0;
    public static FrameLayout container = null;
    public static FrameLayout myself = null;
    private static String mytag;
    private tracerengine Tracer = null;
    private Message msg;

    private Entity_client session = null;
    private Boolean realtime = false;
    private String stateS = "";
    private final String login;
    private final String password;
    private final float api_version;
    private String test_unite;
    private final Activity context;
    private String command_id = null;
    private String command_type = null;

    public Graphical_Range(tracerengine Trac, Activity context, String address, String name, int id, int dev_id,
                           String state_key, String url, String usage,
                           String parameters, String model_id, int update,
                           int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params) throws JSONException {
        super(context, Trac, id, name, state_key, usage, widgetSize, session_type, place_id, place_type, mytag, container);
        this.Tracer = Trac;
        this.address = address;
        this.url = url;
        this.usage = usage;
        this.myself = this;
        this.context = context;
        stateThread = 1;
        try {
            this.stateS = getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), state_key.toLowerCase()));
        } catch (Exception e) {
            Tracer.d(mytag, "no translation for: " + state_key);
            this.stateS = state_key;
        }
        mytag = "Graphical_Range(" + dev_id + ")";
        login = params.getString("http_auth_username", null);
        password = params.getString("http_auth_password", null);
        api_version = params.getFloat("API_VERSION", 0);

        //get parameters
        JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
        if (api_version >= 0.7f) {
            try {
                command_id = jparam.getString("command_id");
                command_type = jparam.getString("command_type");
            } catch (JSONException e) {
                Tracer.i(mytag, "No parameters for command");
                seekBarVaria.setEnabled(false);
            }
        } else {
            String command = jparam.getString("command");
            valueMin = jparam.getInt("valueMin");
            valueMax = jparam.getInt("valueMax");
            int range = valueMax - valueMin;
            scale = 100 / range;
        }
        try {
            test_unite = jparam.getString("unit");
        } catch (JSONException e) {
            test_unite = "%";
        }
        if (test_unite == null || test_unite.length() == 0) {
            test_unite = "%";
        }
        String[] model = model_id.split("\\.");
        type = model[0];

        //linearlayout horizontal body
        LinearLayout bodyPanHorizontal = new LinearLayout(context);
        bodyPanHorizontal.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, Gravity.CENTER_VERTICAL));
        bodyPanHorizontal.setOrientation(LinearLayout.HORIZONTAL);

        //right panel with different info and seekbars
        FrameLayout rightPan = new FrameLayout(context);
        rightPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        rightPan.setPadding(0, 0, 10, 0);

        // panel
        LinearLayout leftPan = new LinearLayout(context);
        leftPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, Gravity.BOTTOM));
        leftPan.setOrientation(LinearLayout.VERTICAL);
        leftPan.setGravity(Gravity.CENTER_VERTICAL);
        leftPan.setPadding(4, 5, 0, 0);

        state = new TextView(context);
        state.setTextColor(Color.BLACK);
        state.setPadding(20, 0, 0, 0);
        animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);

        //first seekbar variator
        seekBarVaria = new SeekBar(context);
        seekBarVaria.setProgress(0);
        if (api_version < 0.7f)
            seekBarVaria.setMax(valueMax - valueMin);
        seekBarVaria.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
        seekBarVaria.setProgressDrawable(getResources().getDrawable(R.drawable.bgseekbarvaria));
        seekBarVaria.setThumb(getResources().getDrawable(R.drawable.buttonseekbar));
        seekBarVaria.setThumbOffset(-3);
        seekBarVaria.setOnSeekBarChangeListener(this);
        seekBarVaria.setPadding(0, 0, 15, 7);

        leftPan.addView(state);
        leftPan.addView(seekBarVaria);
        rightPan.addView(leftPan);
        bodyPanHorizontal.addView(rightPan);
        super.LL_topPan.removeView(super.LL_featurePan);
        super.LL_infoPan.addView(bodyPanHorizontal);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                /// Deprecated method to die /////////////////////////////////////////
                if (activate) {
                    Tracer.d(mytag, "Handler receives a request to die ");
                    //That seems to be a zombie
                    removeView(LL_background);
                    myself.setVisibility(GONE);
                    if (container != null) {
                        container.removeView(myself);
                        container.recomputeViewAttributes(myself);
                    }
                    try {
                        finalize();
                    } catch (Throwable t) {
                    }    //kill the handler thread itself
                    ///////////////////////////////////////////////////////////////////
                } else {
                    int new_val = 0;
                    if (msg.what == 9999) {
                        //state_engine send us a signal to notify value changed
                        if (session == null)
                            return;
                        try {
                            Tracer.d(mytag, "Handler receives a new value from cache_engine <" + session.getValue() + ">");
                            new_val = Integer.parseInt(session.getValue());
                        } catch (Exception e) {
                            new_val = 0;
                        }
                        //#1649
                        //Value min and max should be the limit of the widget
                        if (new_val <= valueMin) {
                            state.setText(stateS + " : " + valueMin + " " + test_unite);
                        } else if (new_val > valueMin && new_val < valueMax) {
                            state.setText(stateS + " : " + new_val + " " + test_unite);
                        } else if (new_val >= valueMax) {
                            state.setText(stateS + " : " + valueMax + " " + test_unite);
                        }
                        state.setAnimation(animation);
                        new SBAnim(seekBarVaria.getProgress(), new_val - valueMin).execute();

                    } else if (msg.what == 9998) {
                        // state_engine send us a signal to notify it'll die !
                        Tracer.d(mytag, "state engine disappeared ===> Harakiri !");
                        session = null;
                        realtime = false;
                        removeView(LL_background);
                        myself.setVisibility(GONE);
                        if (container != null) {
                            container.removeView(myself);
                            container.recomputeViewAttributes(myself);
                        }
                        try {
                            finalize();
                        } catch (Throwable t) {
                        }    //kill the handler thread itself
                    }


                }
            }
        };
        //================================================================================
        /*
         * New mechanism to be notified by widgetupdate engine when our value is changed
		 * 
		 */
        WidgetUpdate cache_engine = WidgetUpdate.getInstance();
        if (cache_engine != null) {
            if (api_version <= 0.6f) {
                session = new Entity_client(dev_id, state_key, mytag, handler, session_type);
            } else if (api_version >= 0.7f) {
                session = new Entity_client(id, "", mytag, handler, session_type);
            }
            if (Tracer.get_engine().subscribe(session)) {
                realtime = true;        //we're connected to engine
                //each time our value change, the engine will call handler
                handler.sendEmptyMessage(9999);    //Force to consider current value in session
            }

        }
        //================================================================================
        //updateTimer();	//Don't use anymore cyclic refresh....
    }


    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        //#1649
        //Value min and max should be the limit of the widget
        int realprogress = (progress + valueMin);
        if (realprogress <= valueMin) {
            state.setText(stateS + " : " + valueMin + " " + test_unite);
            change_this_icon(0);
        } else if (realprogress > valueMin && realprogress < valueMax) {
            state.setText(stateS + " : " + realprogress + " " + test_unite);
            change_this_icon(1);
        } else if (realprogress >= valueMax) {
            state.setText(stateS + " : " + valueMax + " " + test_unite);
            change_this_icon(2);
        }
    }


    public void onStartTrackingTouch(SeekBar arg0) {
        touching = true;
        updating = 3;
    }


    public void onStopTrackingTouch(SeekBar arg0) {
        //send the correct value by replacing it with a converted one.
        state_progress = arg0.getProgress() + valueMin;
        new CommandeThread().execute();
        touching = false;
    }


    public class CommandeThread extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Handler temphandler = new Handler(context.getMainLooper());
            temphandler.post(new Runnable() {
                                 public void run() {
                                     updating = 3;
                                     String Url2send;
                                     if (api_version >= 0.7f) {
                                         Url2send = url + "cmd/id/" + command_id + "?" + command_type + "=" + state_progress;
                                     } else {
                                         Url2send = url + "command/" + type + "/" + address + "/" + state_progress;
                                     }
                                     Tracer.i(mytag, "Sending to Rinor : <" + Url2send + ">");
                                     JSONObject json_Ack = null;
                                     try {
                                         new CallUrl().execute(Url2send, login, password, "3000");
                                         //json_Ack = Rest_com.connect_jsonobject(Url2send, login, password, 3000);
                                     } catch (Exception e) {
                                         Tracer.e(mytag, "Rinor exception sending command <" + e.getMessage() + ">");
                                         Toast.makeText(context, "Rinor exception sending command", Toast.LENGTH_LONG).show();
                                     }
                                     /*
                                     try {
                                         Boolean ack = JSONParser.Ack(json_Ack);
                                         if (!ack) {
                                             Tracer.i(mytag, "Received error from Rinor : <" + json_Ack.toString() + ">");
                                             Toast.makeText(context, "Received error from Rinor", Toast.LENGTH_LONG).show();
                                             handler.sendEmptyMessage(2);
                                         }
                                     } catch (Exception e) {
                                         e.printStackTrace();
                                     }
                                     */
                                 }
                             }
            );
            return null;
        }
    }

    public class SBAnim extends AsyncTask<Void, Integer, Void> {
        private final int begin;
        private final int end;

        public SBAnim(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        protected Void doInBackground(Void... params) {
            final int steps = java.lang.Math.abs(end - begin);
            new Thread(new Runnable() {
                public synchronized void run() {
                    for (int i = 0; i <= steps; i++) {
                        try {
                            this.wait(7 * scale);
                            if (!touching) {
                                if (end - begin > 0) seekBarVaria.setProgress(begin + i);
                                else seekBarVaria.setProgress(begin - i);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            return null;
        }

    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == View.VISIBLE) {
            //activate=true;
        }
    }

}