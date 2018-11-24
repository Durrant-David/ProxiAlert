package edu.byui.team06.proxialert.view.settings.;
import android.os.Bundle;
import android.preference.Preference;
import edu.byui.team06.proxialert.R;

public class SettingsActivity extends AppCompatPreferenceActivity {
    public static final int RESULT_CODE_THEME_UPDATED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        findPreference("themes").setOnPreferenceChangeListener(new RefreshActivityOnPreferenceChangeListener(RESULT_CODE_THEME_UPDATED));
     }
// TODO: Fix bugs with Theme Change
      private class RefreshActivityOnPreferenceChangeListener implements Preference.OnPreferenceChangeListener { private final int resultCode;
      public RefreshActivityOnPreferenceChangeListener(int resultCode) {
          this.resultCode = resultCode;

      }

        @Override
        public boolean onPreferenceChange(Preference p, Object newValue) {
            setResult(resultCode);
            return true;
        }
    }

}