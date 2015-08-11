package nl.giovanniterlingen.whatsapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class ChatAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;

	public ChatAdapter(Context context, String[] values) {
		super(context, -1, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflater.inflate(R.layout.chat_item, null);

		TextView left = (TextView) view.findViewById(R.id.lefttext);
		LinearLayout leftBubble = (LinearLayout) view
				.findViewById(R.id.left_bubble);
		TextView leftDate = (TextView) view.findViewById(R.id.leftdate);
		TextView right = (TextView) view.findViewById(R.id.righttext);
		LinearLayout rightBubble = (LinearLayout) view
				.findViewById(R.id.right_bubble);
		TextView rightDate = (TextView) view.findViewById(R.id.rightdate);

		String txt = values[position];
		if (txt.startsWith("me;")) {

			String message = getItem(position).replaceAll("[^: ]*: ", "");
			Pattern regex = Pattern.compile(";([^: ]*): ");
			Matcher matcher = regex.matcher(getItem(position));

			if (matcher.find()) {
				String date = (matcher.group(1));
				long datevalue = Long.valueOf(date) * 1000;
				Date dateformat = new java.util.Date(datevalue);
				String convert = new SimpleDateFormat("HH:mm")
						.format(dateformat);

				right.setText(message);
				left.setText("");
				leftBubble.setBackgroundDrawable(null);
				rightDate.setText(convert);
				leftDate.setVisibility(View.GONE);

			}
		}

		else {

			String message = getItem(position).replaceAll("[^: ]*: ", "");
			Pattern regex = Pattern.compile(";([^: ]*): ");
			Matcher matcher = regex.matcher(getItem(position));

			if (matcher.find()) {
				String date = (matcher.group(1));
				long datevalue = Long.valueOf(date) * 1000;
				Date dateformat = new java.util.Date(datevalue);
				String convert = new SimpleDateFormat("HH:mm")
						.format(dateformat);

				left.setText(message);
				right.setText("");
				rightBubble.setBackgroundDrawable(null);
				leftDate.setText(convert);
				rightDate.setVisibility(View.GONE);
			}
		}
		return view;
	}
}
