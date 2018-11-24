package edu.byui.team06.proxialert.view.settings;

import android.os.Bundle;
import android.preference.Preference;

import edu.byui.team06.proxialert.R;

public class SettingsActivity extends AppCompatPreferenceActivity {
    public static final int RESULT_CODE_THEME_UPDATED = 1;
    public static final int RESULT_UNITS_UPDATED = 2;
    private boolean THEME_IS_UPDATED = false;
    private boolean UNITS_IS_UPDATED = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        findPreference("themes").setOnPreferenceChangeListener(new PreferenceChangeListener(RESULT_CODE_THEME_UPDATED));
        findPreference("units").setOnPreferenceChangeListener(new PreferenceChangeListener(RESULT_UNITS_UPDATED));
     }
// TODO: Fix bugs with Theme Change
      private class PreferenceChangeListener implements Preference.OnPreferenceChangeListener {
        private final int resultCode;

      public PreferenceChangeListener(int resultCode) {
          this.resultCode = resultCode;

      }

      @Override
      public boolean onPreferenceChange(Preference p, Object newValue) {
          //just return true

          return true;
      }
    }
}