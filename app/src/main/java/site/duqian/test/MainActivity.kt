package site.duqian.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {

    private lateinit var mProgressView: ProgressView
    private lateinit var mProgressView2: ProgressView
    private var mProgress = 1f
    private val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (mProgress <= 100) {
                mProgressView.setProgress(mProgress)
                mProgressView2.setProgress(mProgress)
                mProgress++
                Log.d("dq-pb", "progress=$mProgress")
            } else {
                mProgress = 1f
            }
            sendEmptyMessageDelayed(100, 200)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        mProgressView = findViewById(R.id.progressView)
        mProgressView2 = findViewById(R.id.progressView2)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Just a demo from duqian2010@gmail.com", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        mHandler.postDelayed({
            mProgressView.setProgress(mProgress)
            mProgressView2.setProgress(mProgress)
            mProgress++
            Log.d("dq-pb", "progress1=$mProgress")
            mHandler.sendEmptyMessageDelayed(100, 1000)
        }, 1000)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                openFeaturePage()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openFeaturePage() {
        val intent = Intent()
        intent.setClassName(this, "com.duqian.progress.MainActivity")
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
    }
}
