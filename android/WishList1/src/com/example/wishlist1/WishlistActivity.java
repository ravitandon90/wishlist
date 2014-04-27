package com.example.wishlist1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ListActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.app.NavUtils;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class WishlistActivity extends ListActivity {
	public final static String param_userId = "param_userId";
	
	private String mUserId;
	
	private View mWishlistGetStatusView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wishlist);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Intent intent = getIntent();
        mUserId = intent.getStringExtra(param_userId);

		mWishlistGetStatusView = findViewById(R.id.wishlist_get_status);
		showProgress(true);
		
	    WishlistRequestPost post = new WishlistRequestPost(mUserId);
	    WishlistRequestPostTask task = new WishlistRequestPostTask();
	    task.execute(post);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wishlist, menu);
		return true;
	}
	
	/**
	 * Shows the progress UI and hides the wishlist view.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mWishlistGetStatusView.setVisibility(View.VISIBLE);
			mWishlistGetStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mWishlistGetStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mWishlistGetStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class WishlistRequestPost extends HttpPost.SendableObject {
		private String userId;
		public WishlistRequestPost(String _userId){
			userId = _userId;
			url = "http://ec2-54-186-87-220.us-west-2.compute.amazonaws.com:3000/user/" + userId +"/items/get";
			httpType = HttpType.GET;
		}
		public String toJSON() {
			/*
			JSONObject obj = new JSONObject();
			try {
				obj.put("userId", userId);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return obj.toString(); */
			return "{}";
		}
	}
	
	public class WishlistRequestPostTask extends AsyncTask<WishlistRequestPost, Void, String> {
		@Override
		protected String doInBackground(WishlistRequestPost... posts) {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			// Attempt to log in.
			WishlistRequestPost post = posts[0];
			return HttpPost.post(post, cm);
		}

		@Override
		protected void onPostExecute(final String strResponse) {
			showProgress(false);
			
			// Check if there was an error in transmission.
			if (strResponse == HttpPost.ERROR) {
				// TODO: Notify user that there was a connection error.
				Log.d("tatewty", "Connection failure.");
				return;
			}
			
			try {
				JSONObject obj = new JSONObject(strResponse);
				
				JSONObject jsonResponse = obj.getJSONObject("response");
				String strMessage = jsonResponse.getString("message");
				if (strMessage.equalsIgnoreCase("Get Items query successful")) {
					JSONObject jsonMiscellaneous = jsonResponse.getJSONObject("miscellaneous");
					JSONArray jsonRows = jsonMiscellaneous.getJSONArray("rows");
					
					WishlistAdapter adapter = new WishlistAdapter(WishlistActivity.this,jsonRows);
					setListAdapter(adapter);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
