package widgets;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.domogik.domodroid13.R;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import Abstract.display_sensor_info;
import Abstract.pref_utils;
import Abstract.translate;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import Event.Entity_client_event_value;
import database.WidgetUpdate;
import misc.Color_Progress;
import misc.Color_RGBField;
import misc.Color_Result;
import misc.tracerengine;
import rinor.send_command;

public class Graphical_Color extends Basic_Graphical_widget implements OnSeekBarChangeListener, OnClickListener {


    private int mInitialColor, mDefaultColor;
    private String mKey;
    private RelativeTimeTextView TV_Timestamp;

    private LinearLayout featurePan2;
    private Color_Progress seekBarHueBar;
    private Color_Progress seekBarRGBXBar;
    private Color_Progress seekBarRGBYBar;
    private Color_RGBField rgbView;
    private Color_Result resultView;
    private Animation animation;
    private boolean touching;
    private int updating = 0;

    private int argb = 0;
    private String argbS = "";
    private static String mytag;
    public FrameLayout container = null;
    private FrameLayout myself = null;
    private Boolean switch_state = false;

    private Color currentColor;
    private SeekBar seekBarOnOff;
    private int[] mMainColors = new int[65536];
    private float mCurrentHue = 0;
    public int rgbHue = 0;
    private int rgbX = 0;
    private int rgbY = 0;
    private int r, g, b;

    private TextView title7;
    private TextView title8;
    private TextView title9;
    private String t7s;
    private String t8s;
    private String t9s = "";

    private JSONObject jparam;
    private final Entity_Feature feature;
    private String command_id = null;
    private String command_type = null;
    private final int session_type;
    private pref_utils prefUtils;
    private String state_key;
    private int dev_id;
    private String Value_timestamp;

    public Graphical_Color(tracerengine Trac,
                           final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                           final Entity_Feature feature) {
        super(activity, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag);
        this.feature = feature;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_Color(tracerengine Trac,
                           final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                           final Entity_Map feature_map) {
        super(activity, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag);
        this.feature = feature_map;
        this.session_type = session_type;
        onCreate();
    }

    private void onCreate() {
        myself = this;
        String parameters = feature.getParameters();
        state_key = feature.getState_key();
        command_id = feature.getAddress();
        prefUtils = new pref_utils();
        if (api_version <= 0.6f) {
            this.dev_id = feature.getDevId();
        } else if (api_version >= 0.7f) {
            this.dev_id = feature.getId();
            this.state_key = ""; //for entity_client
        }
        mytag = "Graphical_Color(" + dev_id + ")";
        String value0;
        String value1;
        try {
            jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
            value1 = jparam.getString("value1");
            value0 = jparam.getString("value0");

        } catch (Exception e) {
            Tracer.d(mytag, "no parameters for this device");
            value0 = "0";
            value1 = "1";
        }

        setOnClickListener(this);

        String[] model = feature.getDevice_type_id().split("\\.");
        command_type = model[0];
        Tracer.d(mytag, "model_id = <" + feature.getDevice_type_id() + "> command_type = <" + command_type + ">");

        //state key
        TextView state_key_view = new TextView(activity);
        try {
            state_key_view.setText(activity.getResources().getString(translate.do_translate(getContext(), Tracer, state_key)));
        } catch (Exception e) {
            state_key_view.setText(state_key);
        }
        state_key_view.setTextColor(Color.parseColor("#333333"));

        //first seekbar on/off
        seekBarOnOff = new SeekBar(activity);
        seekBarOnOff.setProgress(0);
        seekBarOnOff.setMax(100);
        Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.bgseekbaronoff);
        seekBarOnOff.setLayoutParams(new LayoutParams(bMap.getWidth(), bMap.getHeight()));
        seekBarOnOff.setProgressDrawable(getResources().getDrawable(R.drawable.bgseekbaronoff));
        seekBarOnOff.setThumb(getResources().getDrawable(R.drawable.buttonseekbar));
        seekBarOnOff.setThumbOffset(0);
        seekBarOnOff.setOnSeekBarChangeListener(this);
        seekBarOnOff.setTag("onoff");

        //feature panel 2
        featurePan2 = new LinearLayout(activity);
        featurePan2.setOrientation(LinearLayout.HORIZONTAL);
        //featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
        featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        featurePan2.setGravity(Gravity.CENTER_VERTICAL);
        featurePan2.setPadding(20, 0, 0, 10);

        //left panel
        LinearLayout color_LeftPan = new LinearLayout(activity);
        color_LeftPan.setOrientation(LinearLayout.VERTICAL);
        //color_LeftPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,1));
        color_LeftPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
        color_LeftPan.setPadding(0, 0, 0, 10);

        TextView title1 = new TextView(activity);
        title1.setText(activity.getString(R.string.Hue));
        title1.setTextSize(10);
        title1.setTextColor(Color.parseColor("#333333"));
        TextView title2 = new TextView(activity);
        title2.setText(activity.getString(R.string.Sat));
        title2.setTextSize(10);
        title2.setTextColor(Color.parseColor("#333333"));
        TextView title3 = new TextView(activity);
        title3.setText(activity.getString(R.string.Bright));
        title3.setTextSize(10);
        title3.setTextColor(Color.parseColor("#333333"));
        //TextView title4 = new TextView(activity);
        //title4.setText("Luminosity");
        //title4.setTextSize(10);
        //title4.setTextColor(Color.parseColor("#333333"));
        TextView title5 = new TextView(activity);
        title5.setText(activity.getString(R.string.Field));
        title5.setTextSize(10);
        title5.setTextColor(Color.parseColor("#333333"));
        TextView title6 = new TextView(activity);
        title6.setText(activity.getString(R.string.Curcolor));
        title6.setTextSize(10);
        title6.setTextColor(Color.parseColor("#333333"));
        title7 = new TextView(activity);
        t7s = activity.getString(R.string.Red);
        title7.setText(t7s + " : 255");
        title7.setTextSize(10);
        title7.setTextColor(Color.parseColor("#333333"));
        title8 = new TextView(activity);
        t8s = activity.getString(R.string.Green);
        title8.setText(t8s + " : 0");
        title8.setTextSize(10);
        title8.setTextColor(Color.parseColor("#333333"));
        title9 = new TextView(activity);
        t9s = activity.getString(R.string.Blue);
        title9.setText(t9s + " : 0");
        title9.setTextSize(10);
        title9.setTextColor(Color.parseColor("#333333"));


        //seekbar huebar
        seekBarHueBar = new Color_Progress(Tracer, activity, 0, 0);
        seekBarHueBar.setProgress(0);
        seekBarHueBar.setMax(255);
        seekBarHueBar.setProgressDrawable(null);
        seekBarHueBar.setOnSeekBarChangeListener(this);
        seekBarHueBar.setTag("hue");

        //seekbar rgbbarX
        seekBarRGBXBar = new Color_Progress(Tracer, activity, 1, 0);
        seekBarRGBXBar.setProgress(0);
        seekBarRGBXBar.setMax(255);
        seekBarRGBXBar.setProgressDrawable(null);
        seekBarRGBXBar.setOnSeekBarChangeListener(this);
        seekBarRGBXBar.setTag("rgbx");

        //seekbar rgbbarY
        seekBarRGBYBar = new Color_Progress(Tracer, activity, 2, 0);
        seekBarRGBYBar.setProgress(0);
        seekBarRGBYBar.setMax(255);
        seekBarRGBYBar.setProgressDrawable(null);
        seekBarRGBYBar.setOnSeekBarChangeListener(this);
        seekBarRGBYBar.setTag("rgby");

        //seekbar powerbar
        Color_Progress seekBarPowerBar = new Color_Progress(Tracer, activity, 3, 0);
        seekBarPowerBar.setProgress(0);
        seekBarPowerBar.setMax(255);
        seekBarPowerBar.setProgressDrawable(null);
        seekBarPowerBar.setOnSeekBarChangeListener(this);
        seekBarPowerBar.setTag("power");


        //RGBField
        rgbView = new Color_RGBField(getContext(), Color.RED, Color.RED);
        //rgbView.drawRGBField();

        //right panel
        LinearLayout color_RightPan = new LinearLayout(activity);
        color_RightPan.setOrientation(LinearLayout.VERTICAL);
        color_RightPan.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.RIGHT));
        color_RightPan.setPadding(20, 0, 0, 10);

        //Color result
        resultView = new Color_Result(activity);

        //Timestamp
        TV_Timestamp = new RelativeTimeTextView(activity, null);
        TV_Timestamp.setTextSize(10);
        TV_Timestamp.setTextColor(Color.BLUE);
        TV_Timestamp.setGravity(Gravity.RIGHT);

        LL_featurePan.addView(seekBarOnOff);
        LL_featurePan.addView(TV_Timestamp);
        LL_infoPan.addView(state_key_view);

        color_LeftPan.addView(title1);
        color_LeftPan.addView(seekBarHueBar);
        color_LeftPan.addView(title2);
        color_LeftPan.addView(seekBarRGBXBar);
        color_LeftPan.addView(title3);
        color_LeftPan.addView(seekBarRGBYBar);
        //color_LeftPan.addView(title4);
        //color_LeftPan.addView(seekBarPowerBar);
        color_LeftPan.addView(title5);
        color_LeftPan.addView(rgbView);

        color_RightPan.addView(title6);
        color_RightPan.addView(resultView);
        color_RightPan.addView(title7);
        color_RightPan.addView(title8);
        color_RightPan.addView(title9);


        featurePan2.addView(color_LeftPan);
        featurePan2.addView(color_RightPan);
        featurePan2.setVisibility(INVISIBLE);
        if (api_version >= 0.7f) {
            try {
                int number_of_command_parameters = jparam.getInt("number_of_command_parameters");
                if (number_of_command_parameters == 1) {
                    command_id = jparam.getString("command_id");
                    command_type = jparam.getString("command_type1");
                }
            } catch (JSONException e) {
                Tracer.d(mytag, "No command_id for this device");
                seekBarOnOff.setEnabled(false);
            }
        }

        updating = 0;
        //================================================================================
        /*
         * New mechanism to be notified by widgetupdate engine when our value is changed
		 * 
		 */
        WidgetUpdate cache_engine = WidgetUpdate.getInstance();
        if (cache_engine != null) {
            session = new Entity_client(dev_id, state_key, mytag, session_type);
            try {
                if (Tracer.get_engine().subscribe(session)) {
                    argbS = session.getValue();
                    Value_timestamp = session.getTimestamp();
                    update_display();
                    //register eventbus for new value
                    EventBus.getDefault().register(this);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //================================================================================
    }

    /**
     * @param event an Entity_client_event_value from EventBus when a new value is received from widgetupdate.
     */
    @Subscribe
    public void onEvent(Entity_client_event_value event) {
        // your implementation
        Tracer.d(mytag, "Receive event from Eventbus" + event.Entity_client_event_get_id() + " With value" + event.Entity_client_event_get_val());
        if (event.Entity_client_event_get_id() == dev_id) {
            argbS = event.Entity_client_event_get_val();
            Value_timestamp = event.Entity_client_event_get_timestamp();
            update_display();
        }
    }

    /**
     * Update the current widget information at creation
     * or when an eventbus is receive
     */
    private void update_display() {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Tracer.d(mytag, "update_display id:" + dev_id + " <" + argbS + "> at " + Value_timestamp);

                Long Value_timestamplong;
                Value_timestamplong = Long.valueOf(Value_timestamp) * 1000;

                if (prefUtils.GetWidgetTimestamp()) {
                    TV_Timestamp.setText(display_sensor_info.timestamp_convertion(Value_timestamplong.toString(), activity));
                } else {
                    TV_Timestamp.setReferenceTime(Value_timestamplong);
                }

                switch (argbS) {
                    case "off":
                        switch_state = false;
                        argbS = "000000";
                        argb = 0;
                        break;
                    case "on":
                        seekBarOnOff.setProgress(100);
                        switch_state = true;
                        LoadSelections();    //Recall last values known from shared preferences

                        // argb and argbS will be set when seekBars will be changed
                        break;
                    default:
                        try {
                            argbS = argbS.substring(1);    //It's the form #RRGGBB : ignore the #
                            //Tracer.d(mytag,"Handler ==> argbS after extraction = <"+argbS+">" );
                            argb = Integer.parseInt(argbS, 16);
                            //Tracer.d(mytag,"Handler ==> argb after parsing = <"+argb+">" );
                        } catch (Exception e) {
                            argb = 1;
                        }
                        break;
                }
                int value_save = argb;
                r = ((argb >> 16) & 0xFF);
                g = ((argb >> 8) & 0xFF);
                b = ((argb) & 0xFF);


                if (argb == 0) {
                    seekBarOnOff.setProgress(0);
                    switch_state = false;
                } else {
                    seekBarOnOff.setProgress(100);
                }
                //Convert RGB to HSV color, and set sliders
                float hsv[] = new float[3];

                Color.colorToHSV(value_save, hsv);
                //Tracer.d(mytag,"Handler ==> RGB ("+value_save+") values after process = <"+r+"> <"+g+"> <"+b+">" );
                //Tracer.d(mytag,"Handler ==> HSV values after process = <"+hsv[0]+"> <"+hsv[1]+"> <"+hsv[2]+">" );

                //Seekbars are in range 0-255 : convert HSV values
                //Hue is an angle : convert it to linear
                seekBarHueBar.setProgress((int) (255f - (hsv[0] * 255f / 360)));
                seekBarRGBXBar.setProgress((int) (hsv[1] * 255f));
                seekBarRGBYBar.setProgress((int) (hsv[2] * 255f));

                title7.setText(t7s + " : " + r);
                title8.setText(t8s + " : " + g);
                title9.setText(t9s + " : " + b);
                if ((r != 0) || (g != 0) || (b != 0)) {
                    SaveSelections();
                }
            }
        });
    }

    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
        if (arg0.getTag().equals("onoff")) {
            //User is moving on/off object....
        } else if (arg0.getTag().equals("hue")) {
            //rgb view
            mCurrentHue = (255 - arg0.getProgress()) * 360 / 255;
            rgbView.mCurrentHue = mCurrentHue;
            rgbView.invalidate();

            //rgb X
            float[] hsv0 = {0, 0, (float) rgbY / 255f};
            float[] hsv1 = {mCurrentHue, 1, (float) rgbY / 255f};
            seekBarRGBXBar.hsv0 = hsv0;
            seekBarRGBXBar.hsv1 = hsv1;
            seekBarRGBXBar.invalidate();

            //rgb Y
            float[] hsv2 = {0, 0, 0};
            float[] hsv3 = {mCurrentHue, (float) rgbX / 255f, 1};
            seekBarRGBYBar.hsv2 = hsv2;
            seekBarRGBYBar.hsv3 = hsv3;
            seekBarRGBYBar.invalidate();

        } else if (arg0.getTag().equals("rgbx")) {
            rgbX = arg0.getProgress();
            float[] hsv2 = {0, 0, 0};
            float[] hsv3 = {mCurrentHue, (float) rgbX / 255f, 1};
            seekBarRGBYBar.hsv2 = hsv2;
            seekBarRGBYBar.hsv3 = hsv3;
            seekBarRGBYBar.invalidate();

            rgbView.mCurrentX = arg0.getProgress();
            seekBarRGBYBar.invalidate();
            rgbView.invalidate();

        } else if (arg0.getTag().equals("rgby")) {
            rgbY = arg0.getProgress();
            float[] hsv0 = {0, 0, (float) rgbY / 255f};
            float[] hsv1 = {mCurrentHue, 1, (float) rgbY / 255f};
            seekBarRGBXBar.hsv0 = hsv0;
            seekBarRGBXBar.hsv1 = hsv1;
            rgbView.mCurrentY = 255 - arg0.getProgress();
            seekBarRGBXBar.invalidate();
            rgbView.invalidate();
        }

        float[] hsvCurrent = {mCurrentHue, (float) rgbX / 255f, (float) rgbY / 255f};
        argb = Color.HSVToColor(hsvCurrent);
        resultView.hsvCurrent = hsvCurrent;
        argbS = Integer.toHexString((argb >> 16) & 0xFF) + Integer.toHexString((argb >> 8) & 0xFF) + Integer.toHexString((argb) & 0xFF);
        r = ((argb >> 16) & 0xFF);
        g = ((argb >> 8) & 0xFF);
        b = ((argb) & 0xFF);
        title7.setText(t7s + " : " + r);
        title8.setText(t8s + " : " + g);
        title9.setText(t9s + " : " + b);
        resultView.invalidate();
    }


    public void onStartTrackingTouch(SeekBar seekBar) {
        touching = true;
        updating = 3;
    }


    public void onStopTrackingTouch(SeekBar seekBar) {
        String tag = (String) seekBar.getTag();
        if (tag.equals("onoff")) {
            if (seekBar.getProgress() < 20) {
                seekBar.setProgress(0);
                switch_state = false;
                Tracer.i(mytag, "Change switch to OFF");
                // Force color picker to black....
                seekBarRGBYBar.setProgress(0);        //No brightness => black !
            } else {
                seekBar.setProgress(100);
                switch_state = true;
                LoadSelections();            //Recall last known value, till state engine refresh...
                Tracer.i(mytag, "Change switch to ON");
            }
            new CommandeThread().execute();        //And send switch_state to Domogik

        } else {
            int state_progress = seekBar.getProgress();
            SaveSelections();
            Tracer.i(mytag, "End of change : new rgb value =  #" + argbS);
            if (seekBarOnOff.getProgress() > 50)
                if (switch_state)
                    new CommandeThread().execute();        //send new color
        }
        touching = false;

    }

    private class CommandeThread extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Handler temphandler = new Handler(activity.getMainLooper());
            temphandler.post(new Runnable() {
                                 public void run() {
                                     String state_progress = "";
                                     if (api_version >= 0.7f) {
                                         if ((argb != 0) && switch_state) {
                                             if (feature.getDevice_feature_model_id().startsWith("DT_ColorRGBHexa.")) {
                                                 String srgb = Integer.toHexString(argb);
                                                 if (srgb.length() > 6)
                                                     srgb = srgb.substring(2);
                                                 state_progress = srgb;
                                             } else if (feature.getDevice_feature_model_id().startsWith("DT_ColorRGB.")) {
                                                 state_progress = r + "," + g + "," + b;
                                             } else if (feature.getDevice_feature_model_id().startsWith("DT_ColorCMYK.")) {
                                                 int computedC, computedM, computedY;
                                                 int minCMY;

                                                 computedC = 1 - (r / 255);
                                                 computedM = 1 - (g / 255);
                                                 computedY = 1 - (b / 255);

                                                 if (r == 0 && g == 0 && b == 0) {
                                                     minCMY = 1;
                                                 } else {
                                                     minCMY = Math.min(computedC, Math.min(computedM, computedY));
                                                 }
                                                 computedC = (computedC - minCMY) / (1 - minCMY);
                                                 computedM = (computedM - minCMY) / (1 - minCMY);
                                                 computedY = (computedY - minCMY) / (1 - minCMY);
                                                 state_progress = computedC + "," + computedM + "," + computedY + "," + minCMY;
                                             }
                                         } else {
                                             if (switch_state) {
                                                 //To see
                                                 state_progress = "000000";
                                             } else {
                                                 state_progress = "000000";
                                                 seekBarHueBar.setProgress(255);
                                                 seekBarRGBXBar.setProgress(0);
                                                 seekBarRGBYBar.setProgress(0);
                                             }
                                         }
                                     } else {
                                         if ((argb != 0) && switch_state) {
                                             String srgb = Integer.toHexString(argb);
                                             if (srgb.length() > 6)
                                                 srgb = srgb.substring(2);
                                             state_progress = "#" + srgb;
                                         } else {
                                             if (switch_state) {
                                                 state_progress = "000000";

                                             } else {
                                                 state_progress = "000000";
                                                 seekBarHueBar.setProgress(255);
                                                 seekBarRGBXBar.setProgress(0);
                                                 seekBarRGBYBar.setProgress(0);
                                             }
                                         }
                                     }
                                     updating = 1;
                                     JSONObject json_Ack = null;
                                     try {
                                         send_command.send_it(activity, Tracer, command_id, command_type, String.valueOf(state_progress), api_version);
                                     } catch (Exception e) {
                                         Tracer.e(mytag, "Rinor exception sending command <" + e.getMessage() + ">");
                                     }
                                 }
                             }
            );
            return null;

        }
    }
     /*
     * Saving HSV parameters allow to restore them when Domogik server
	 * only notify 'on' state (without any RGB parameters)
	 * We've to know which was the last one, kept by Domogik
	 */

    private void SaveSelections() {
        prefUtils.SetColorHue(seekBarHueBar.getProgress());
        prefUtils.SetColorSaturation(seekBarRGBXBar.getProgress());
        prefUtils.SetColorBrightness(seekBarRGBYBar.getProgress());
        prefUtils.SetColorRgb("#" + argbS);
        /*
        Tracer.i(mytag, "SaveSelections()");
		Tracer.i(mytag,"Hue    = "+params.getInt("COLORHUE",0));
		Tracer.i(mytag,"Sat    = "+params.getInt("COLORSATURATION",0));
		Tracer.i(mytag,"Bright = "+params.getInt("COLORBRIGHTNESS",0));
		 */
    }

    private void LoadSelections() {
        seekBarHueBar.setProgress(prefUtils.GetLastColorHue());
        seekBarRGBXBar.setProgress(prefUtils.GetLastColorSaturation());
        seekBarRGBYBar.setProgress(prefUtils.GetLastColorBrightness());
        /*
        Tracer.i(mytag, "LoadSelections()");
		Tracer.i(mytag,"Hue    = "+params.getInt("COLORHUE",0));
		Tracer.i(mytag,"Sat    = "+params.getInt("COLORSATURATION",0));
		Tracer.i(mytag,"Bright = "+params.getInt("COLORBRIGHTNESS",0));
		 */
    }

    public void onClick(View arg0) {
        Tracer.i(mytag, "Touch....");
        if (featurePan2.getVisibility() == INVISIBLE) {
            try {
                LL_background.removeView(featurePan2);
            } catch (Exception e) {
                //to avoid #135
            }
            LL_background.addView(featurePan2);
            featurePan2.setVisibility(VISIBLE);
            Tracer.i(mytag, "FeaturePan2 set to VISIBLE");
        } else {
            LL_background.removeView(featurePan2);
            featurePan2.setVisibility(INVISIBLE);
            Tracer.i(mytag, "FeaturePan2 set to INVISIBLE");
        }
    }
}
