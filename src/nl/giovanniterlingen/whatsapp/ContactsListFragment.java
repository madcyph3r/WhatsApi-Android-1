package nl.giovanniterlingen.whatsapp;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class ContactsListFragment extends ListFragment implements 
	LoaderCallbacks<Cursor>{

	private OnContactSelectedListener mContactsListener;
	private SimpleCursorAdapter mAdapter;
	private String mCurrentFilter = null;
	
	private static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
			Contacts._ID, 
			Contacts.DISPLAY_NAME, 
			Contacts.HAS_PHONE_NUMBER,
			Contacts.LOOKUP_KEY
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_contacts_list, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setHasOptionsMenu(true);
		
		getLoaderManager().initLoader(0, null, this);
		
		mAdapter = new IndexedListAdapter(
				this.getActivity(),
				R.layout.list_item_contacts,
				null,
				new String[] {ContactsContract.Contacts.DISPLAY_NAME},
				new int[] {R.id.display_name});
				
		setListAdapter(mAdapter);
		getListView().setFastScrollEnabled(true);
		
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		ViewHolder viewHolder = (ViewHolder) v.getTag();
		String phoneNumber = viewHolder.phoneNumber.getText().toString();
		String name = viewHolder.contactName.getText().toString();
		mContactsListener.onContactNumberSelected(phoneNumber, name);

    	if (phoneNumber != null){
    		mContactsListener.onContactNumberSelected(phoneNumber, name);
    	}
    	else {
    		mContactsListener.onContactNameSelected(id);
    	}
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {			
			mContactsListener = (OnContactSelectedListener) activity;
		} catch (ClassCastException	e) {
			throw new ClassCastException(activity.toString() + " must implement OnContactSelectedListener");
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Uri baseUri;
		
		if (mCurrentFilter != null) {
            baseUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI,
                    Uri.encode(mCurrentFilter));
        } else {
            baseUri = Contacts.CONTENT_URI;
        }
		
		String selection = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
	            + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
	            + Contacts.DISPLAY_NAME + " != '' ))";
		
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
		
		return new CursorLoader(getActivity(), baseUri, CONTACTS_SUMMARY_PROJECTION, selection, null, sortOrder);
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	static class ViewHolder{
		TextView contactName;
		TextView phoneNumber;
		PhoneNumberLookupTask phoneNumberLookupTask;
	}

	class IndexedListAdapter extends SimpleCursorAdapter implements SectionIndexer{

		AlphabetIndexer alphaIndexer;
		
		public IndexedListAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to, 0);		
		}

		@Override
		public Cursor swapCursor(Cursor c) {
			if (c != null) {
				alphaIndexer = new AlphabetIndexer(c,
						c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME),
						" ABCDEFGHIJKLMNOPQRSTUVWXYZ");
			}

			return super.swapCursor(c);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null){
				LayoutInflater inflater = getLayoutInflater(null);
				convertView = inflater.inflate(R.layout.list_item_contacts, parent, false);
				viewHolder = new ViewHolder();
				viewHolder.contactName = (TextView) convertView.findViewById(R.id.display_name);
				viewHolder.phoneNumber = (TextView) convertView.findViewById(R.id.display_number);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
				viewHolder.phoneNumberLookupTask.cancel(true);
			}

			return super.getView(position, convertView, parent);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			super.bindView(view, context, cursor);

			long contactId = cursor.getLong(cursor.getColumnIndexOrThrow(Contacts._ID));
			ViewHolder viewHolder = (ViewHolder) view.getTag();
			viewHolder.phoneNumberLookupTask = new PhoneNumberLookupTask(view);
			viewHolder.phoneNumberLookupTask.execute(contactId);
		}


		@Override
		public int getPositionForSection(int section) {
			return alphaIndexer.getPositionForSection(section);
		}

		@Override
		public int getSectionForPosition(int position) {
			return alphaIndexer.getSectionForPosition(position);
		}

		@Override
		public Object[] getSections() {
			return alphaIndexer == null ? null : alphaIndexer.getSections();
		}
	
	}

	private class PhoneNumberLookupTask extends AsyncTask<Long, Void, Void> {
		final WeakReference<View> mViewReference;

		String mPhoneNumber;

		public PhoneNumberLookupTask(View view) {
			mViewReference = new WeakReference<>(view);
		}

		@Override
		protected Void doInBackground(Long... ids) {
			String[] projection = new String[]{Phone.DISPLAY_NAME, Phone.TYPE, Phone.NUMBER, Phone.LABEL};
			long contactId = ids[0];

			final Cursor phoneCursor = getActivity().getContentResolver().query(
					Phone.CONTENT_URI,
					projection,
					Data.CONTACT_ID + "=?",
					new String[]{String.valueOf(contactId)},
					null);

			if (phoneCursor != null && phoneCursor.moveToFirst() && phoneCursor.getCount() == 1) {
				final int contactNumberColumnIndex = phoneCursor.getColumnIndex(Phone.NUMBER);
				mPhoneNumber = phoneCursor.getString(contactNumberColumnIndex);
				phoneCursor.close();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			View view = mViewReference.get();
			if (view != null) {
				ViewHolder viewHolder = (ViewHolder) view.getTag();
				if (mPhoneNumber != null) {
					viewHolder.phoneNumber.setText(mPhoneNumber);
				} else {
					viewHolder.phoneNumber.setText(getString(R.string.label_multiple_numbers));
				}
			}
		}
	}

}