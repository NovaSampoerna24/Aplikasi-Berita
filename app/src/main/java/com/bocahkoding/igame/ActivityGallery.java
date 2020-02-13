package com.bocahkoding.igame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bocahkoding.igame.data.ThisApp;
import com.bocahkoding.igame.utils.Tools;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

public class ActivityGallery extends AppCompatActivity {

    public static final String EXTRA_POS = "key.EXTRA_POS";
    public static final String EXTRA_IMGS = "key.EXTRA_IMGS";

    public static Intent navigateBase(Context context, ArrayList<String> images) {
        Intent i = new Intent(context, ActivityGallery.class);
        i.putExtra(EXTRA_IMGS, images);
        return i;
    }

    private AdapterFullScreenImage adapter;
    private ViewPager viewPager;
    private TextView text_page;

    ArrayList<String> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Intent i = getIntent();
        items = i.getStringArrayListExtra(EXTRA_IMGS);

        initComponent();

        // for system bar in lollipop
        Tools.setSystemBarColor(this, android.R.color.black);
        Tools.RTLMode(getWindow());
        ThisApp.get().saveClassLogEvent(getClass());
    }

    private void initComponent() {
        viewPager = findViewById(R.id.pager);
        text_page = findViewById(R.id.text_page);

        adapter = new AdapterFullScreenImage(this, items);
        final int total = adapter.getCount();
        viewPager.setAdapter(adapter);
        text_page.setText(String.format(getString(R.string.image_of), 1, total));

        // displaying selected image first
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int pos) {
                text_page.setText(String.format(getString(R.string.image_of), (pos + 1), total));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        (findViewById(R.id.btnClose)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private class AdapterFullScreenImage extends PagerAdapter {

        private Activity act;
        private List<String> image_paths;
        private LayoutInflater inflater;

        // constructor
        public AdapterFullScreenImage(Activity activity, List<String> imagePaths) {
            this.act = activity;
            this.image_paths = imagePaths;
        }

        @Override
        public int getCount() {
            return this.image_paths.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView image;
            inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewLayout = inflater.inflate(R.layout.item_gallery_image, container, false);

            image = viewLayout.findViewById(R.id.image);
            Tools.displayImage(act, image, image_paths.get(position));
            (container).addView(viewLayout);

            return viewLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            (container).removeView((RelativeLayout) object);

        }

    }


}
