package com.termux.app.terminal.io;

//import android.content.Intent;  //..SimplyTheBest (for back button on InputText)
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.Button;  //..SimplyTheBest  (for back button on InputText)
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.termux.R;
import com.termux.app.TermuxActivity;
//import com.termux.app.activities.SettingsActivity;  //..SimplyTheBest (for back button on InputText)
//import com.termux.shared.activity.ActivityUtils;  //..SimplyTheBest  (for back button on InputText)
import com.termux.shared.termux.extrakeys.ExtraKeysView;
import com.termux.terminal.TerminalSession;

public class TerminalToolbarViewPager {

    public static class PageAdapter extends PagerAdapter {

        final TermuxActivity mActivity;

        String mSavedTextInput;

        public PageAdapter(TermuxActivity activity, String savedTextInput) {
            this.mActivity = activity;
            this.mSavedTextInput = savedTextInput;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup collection, int position) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            View layout;
            if (position == 0 || position == 1) {
                layout = inflater.inflate(R.layout.view_terminal_toolbar_extra_keys, collection, false);
                ExtraKeysView extraKeysView = (ExtraKeysView) layout;
                //ExtraKeysView extraKeysView = (ExtraKeysView) layout.findViewById(R.id.terminal_toolbar_extra_keys);  //..SimplyTheBest (for blur bg on ExtraKeys)
                extraKeysView.setExtraKeysViewClient(mActivity.getTermuxTerminalExtraKeys(position));
                extraKeysView.setButtonTextAllCaps(mActivity.getProperties().shouldExtraKeysTextBeAllCaps());
                mActivity.setExtraKeysView(extraKeysView, position);
                extraKeysView.reload(mActivity.getTermuxTerminalExtraKeys(position).getExtraKeysInfo(), mActivity.getTerminalToolbarDefaultHeight());
                // apply extra keys fix if enabled in prefs
                if (mActivity.getProperties().isUsingFullScreen() && mActivity.getProperties().isUsingFullScreenWorkAround()) {
                    FullScreenWorkAround.apply(mActivity);
                }
                // Update toolbar background corresponding to prefs
                mActivity.getmTermuxBackgroundManager().updateToolbarBackground();  //..SimplyTheBest (for transparent bg on ExtraKeys)
            } else {
                layout = inflater.inflate(R.layout.view_terminal_toolbar_text_input, collection, false);
                // •○● @SimplyTheBest: (for back button on InputText)
                /*final Button button = layout.findViewById(R.id.terminal_toolbar_text_input_button);
                button.setOnClickListener(v -> {
                    ViewPager pager = mActivity.getTerminalToolbarViewPager();
                    pager.setCurrentItem(1, true);
                });*/
                // •○●
                final EditText editText = layout.findViewById(R.id.terminal_toolbar_text_input);
                if (mSavedTextInput != null) {
                    editText.setText(mSavedTextInput);
                    mSavedTextInput = null;
                }
                editText.setOnEditorActionListener((v, actionId, event) -> {
                    TerminalSession session = mActivity.getCurrentSession();
                    if (session != null) {
                        if (session.isRunning()) {
                            String textToSend = editText.getText().toString();
                            if (textToSend.length() == 0)
                                textToSend = "\r";
                            session.write(textToSend);
                        } else {
                            mActivity.getTermuxTerminalSessionClient().removeFinishedSession(session);
                        }
                        editText.setText("");
                    }
                    return true;
                });
            }
            collection.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
            collection.removeView((View) view);
        }
    }

    public static class OnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

        final TermuxActivity mActivity;

        final ViewPager mTerminalToolbarViewPager;

        public OnPageChangeListener(TermuxActivity activity, ViewPager viewPager) {
            this.mActivity = activity;
            this.mTerminalToolbarViewPager = viewPager;
        }

        @Override
        public void onPageSelected(int position) {
            mActivity.setTerminalToolbarHeight();
            if (position == 0 || position == 1) {
                mActivity.getTerminalView().requestFocus();
            } else {
                final EditText editText = mTerminalToolbarViewPager.findViewById(R.id.terminal_toolbar_text_input);
                if (editText != null)
                    editText.requestFocus();
            }
        }
    }
}
