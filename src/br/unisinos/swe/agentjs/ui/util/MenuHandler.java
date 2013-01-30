package br.unisinos.swe.agentjs.ui.util;

import br.unisinos.swe.agentjs.R;
import br.unisinos.swe.agentjs.ui.ConfigActivity;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MenuHandler {
	public static boolean onCreateOptionsMenu(MenuInflater inflater, Menu menu) {
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	public static boolean onOptionsItemSelected(MenuItem item, Activity parent) {

		try {
			switch (item.getItemId()) {
			case R.id.config:
				Intent settingsActivity = new Intent(
						parent.getApplicationContext(), ConfigActivity.class);
				parent.startActivity(settingsActivity);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return false;
	}
}
