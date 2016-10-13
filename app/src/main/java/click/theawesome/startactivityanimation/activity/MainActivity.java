package click.theawesome.startactivityanimation.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;

import click.theawesome.startactivityanimation.BitmapUtils;
import click.theawesome.startactivityanimation.PictureData;
import click.theawesome.startactivityanimation.R;

public class MainActivity extends AppCompatActivity {


    private static final String PACKAGE = "click.theawesome.startactivityanimation";
    static float sAnimatorScale = 1;

    private GridLayout mGridLayout;
    HashMap<ImageView, PictureData> mPicturesData = new HashMap<ImageView, PictureData>();
    BitmapUtils mBitmapUtils = new BitmapUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createGridView();
    }

    private void createGridView() {

        ColorMatrix  greyMatrix =  new ColorMatrix();
        greyMatrix.setSaturation(0);
        ColorMatrixColorFilter greyScaleFilter = new ColorMatrixColorFilter(greyMatrix);

        mGridLayout = (GridLayout) findViewById(R.id.gridLayout);
        mGridLayout.setColumnCount(1);
        mGridLayout.setUseDefaultMargins(true);

        Resources resources = getResources();
        ArrayList<PictureData> pictures = mBitmapUtils.loadPhotos(resources);
        PictureData pictureData = pictures.get(0);

        BitmapDrawable thumbnailDrawable = new BitmapDrawable(resources, pictureData.thumbnail);
        thumbnailDrawable.setColorFilter(greyScaleFilter);

        ImageView imageView = new ImageView(this);
        imageView.setImageDrawable(thumbnailDrawable);
        imageView.setOnClickListener(thumbnailClickListener);
        mPicturesData.put(imageView, pictureData);
        mGridLayout.addView(imageView);

    }

    private View.OnClickListener thumbnailClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            // Interesting data to pass across are the thumbnail size/location, the
            // resourceId of the source bitmap, the picture description, and the
            // orientation (to avoid returning back to an obsolete configuration if
            // the device rotates again in the meantime)
            int[] screenLocation = new int[2];
            v.getLocationOnScreen(screenLocation);
            PictureData info = mPicturesData.get(v);
            Intent subActivity = new Intent(MainActivity.this,
                    PictureDetailsActivity.class);
            int orientation = getResources().getConfiguration().orientation;
            subActivity.
                    putExtra(PACKAGE + ".orientation", orientation).
                    putExtra(PACKAGE + ".resourceId", info.resourceId).
                    putExtra(PACKAGE + ".left", screenLocation[0]).
                    putExtra(PACKAGE + ".top", screenLocation[1]).
                    putExtra(PACKAGE + ".width", v.getWidth()).
                    putExtra(PACKAGE + ".height", v.getHeight()).
                    putExtra(PACKAGE + ".description", info.description);
            startActivity(subActivity);

            // Override transitions: we don't want the normal window animation in addition
            // to our custom one
            overridePendingTransition(0, 0);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);//Menu Resource, Menu
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        Intent startActivityIntent = new Intent(MainActivity.this,SecondActivity.class);
        MainActivity.this.startActivity(startActivityIntent);

        switch (item.getItemId()) {
            case R.id.bottom:
                startActivityFromBottom();
                return true;
            case R.id.top:
                startActivityFromTop();
                return true;
            case R.id.left:
                startActivityFromLeft();
                return true;
            case R.id.right:
                startActivityFromRight();
                return true;
            case R.id.zoom:
                startActivityZoom();
                return true;
            case R.id.rotate:
                startActivityRotate();
                return true;
            case R.id.fade:
                startActivityFade();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startActivityFromBottom() {
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
    }

    private void startActivityFromTop() {
       overridePendingTransition( R.anim.push_down_in, R.anim.push_down_out);
    }

    private void startActivityFromLeft() {
        overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void startActivityFromRight() {
        overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void startActivityZoom() {
        overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
    }

    private void startActivityRotate() {
        overridePendingTransition(R.anim.rotate_in, R.anim.rotate_out);
    }

    private void startActivityFade() {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}