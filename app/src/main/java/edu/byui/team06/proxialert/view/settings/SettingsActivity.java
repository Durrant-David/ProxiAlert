package edu.byui.team06.proxialert.view.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import edu.byui.team06.proxialert.R;

public class SettingsActivity extends AppCompatPreferenceActivity {
    public static final int RESULT_CODE_THEME_UPDATED = 1;
    public static final int RESULT_UNITS_UPDATED = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean themeName = pref.getBoolean("themes", false);
        if (themeName) {
            setTheme(R.style.ThemeOverlay_MaterialComponents_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        findPreference("themes").setOnPreferenceChangeListener(new PreferenceChangeListener(RESULT_CODE_THEME_UPDATED));
        Preference p = findPreference("ProxiUnits");
        p.setOnPreferenceChangeListener(new PreferenceChangeListener(RESULT_UNITS_UPDATED));
     }
      private class PreferenceChangeListener implements Preference.OnPreferenceChangeListener {
        private final int resultCode;

      public PreferenceChangeListener(int resultCode) {
          this.resultCode = resultCode;

      }

      @Override
      public boolean onPreferenceChange(Preference p, Object newValue) {
          //just return true
          if(resultCode == RESULT_CODE_THEME_UPDATED) {
              recreate();
          }

          return true;
      }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return true;
    }
}