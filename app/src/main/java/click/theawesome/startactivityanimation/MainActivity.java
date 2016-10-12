package click.theawesome.startactivityanimation;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
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

}