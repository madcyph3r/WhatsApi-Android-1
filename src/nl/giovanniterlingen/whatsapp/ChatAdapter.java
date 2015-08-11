package nl.giovanniterlingen.whatsapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;
	private final String nEdit;

	public ChatAdapter(Context context, String[] values, String nEdit) {
		super(context, -1, values);
		this.context = context;
		this.values = values;
		this.nEdit = nEdit;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflater.inflate(R.layout.chat_item, null);

		TextView left = (TextView) view.findViewById(R.id.lefttext);
		LinearLayout leftBubble = (LinearLayout) view.findViewById(R.id.left_bubble);
		TextView leftDate = (TextView) view.findViewById(R.id.leftdate);
		TextView right = (TextView) view.findViewById(R.id.righttext);
		LinearLayout rightBubble = (LinearLayout) view.findViewById(R.id.right_bubble);
		TextView rightDate = (TextView) view.findViewById(R.id.rightdate);

		String txt = values[position];
		if (txt.startsWith("me: ")) {
			
			String message = getItem(position).replaceAll("[^: ]*: ", "");
			right.setText(message);
			left.setText("");
			leftBubble.setBackgroundDrawable(null);
			leftDate.setVisibility(View.GONE);
		} else {
			String message = getItem(position).replaceAll("[^: ]*: ", "");
			left.setText(message);
			right.setText("");
			rightBubble.setBackgroundDrawable(null);
			rightDate.setVisibility(View.GONE);
		}
		return view;
	}
}
