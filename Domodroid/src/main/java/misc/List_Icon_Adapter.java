package misc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.domogik.domodroid13.R;

import java.util.List;

import Abstract.translate;
import activities.Graphics_Manager;

public class List_Icon_Adapter extends ArrayAdapter<String> {
    private tracerengine Tracer = null;
    private final Context context;
    private final String[] values;
    private final String[] values_icon;
    private boolean bool_type_List;    //Will be used to change icon grabber method.
    private String icon;
    private final String mytag = this.getClass().getName();


    public List_Icon_Adapter(tracerengine Trac, Context context, String[] values, String[] values_icon) {
        super(context, R.layout.row_layout_list_icon, values);
        this.context = context;
        this.values = values;
        this.values_icon = values_icon;
        this.bool_type_List = false;//Will be used to change icon grabber method.
        this.Tracer = Trac;
    }

    public List_Icon_Adapter(tracerengine Trac, Context context, List<String> values_list, List<String> values_list_icon) {
        super(context, R.layout.row_layout_list_icon, values_list);
        this.context = context;
        this.values = values_list.toArray(new String[values_list.size()]);
        this.values_icon = values_list_icon.toArray(new String[values_list.size()]);
        this.bool_type_List = true;//Will be used to change icon grabber method.
        this.Tracer = Trac;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_layout_list_icon, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        try {
            textView.setText(context.getResources().getString(translate.do_translate(getContext(), Tracer, values[position])));
        } catch (Exception e) {
            textView.setText(values[position]);
        }
        // change the icon for Windows and iPhone
        String s = values[position];
        if (bool_type_List = false) {
            //Will be used to change icon grabber method.
            Tracer.d(mytag, "search icon for: " + values[position]);
            imageView.setImageResource(Graphics_Manager.Icones_Agent(values_icon[position], 0));
        } else if (bool_type_List = true) {
            //Will be used to change icon grabber method.
            Tracer.d(mytag, "search icon for: " + values[position]);
            imageView.setImageResource(Graphics_Manager.Icones_Agent(values_icon[position], 0));
        }
        return rowView;
    }
} 