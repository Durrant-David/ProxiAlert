package edu.byui.team06.proxialert.view;
import android.os.Bundle;
import android.preference.Preference;
import edu.byui.team06.proxialert.R;

public class SettingsActivity extends AppCompatPreferenceActivity {
    public static final int RESULT_CODE_THEME_UPDATED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
//      findPreference("theme").setOnPreferenceChangeListener(new RefershActivityOnPreferenceChangeListener(RESULT_CODE_THEME_UPDATED));
//    }
//
//    private class RefershActivityOnPreferenceChangeListener implements Preference.OnPreferenceChangeListener {
//        private final int resultCode;
//        public RefershActivityOnPreferenceChangeListener(int resultCode) {
//            this.resultCode = resultCode;
//        }
//
//        @Override
//        public boolean onPreferenceChange(Preference p, Object newValue) {
//            setResult(resultCode);
//            return true;
//        }
    }

}
