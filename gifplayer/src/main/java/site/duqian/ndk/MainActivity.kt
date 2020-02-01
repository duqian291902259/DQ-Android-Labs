package site.duqian.ndk

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE = 1000
    var bitmap: Bitmap? = null
    var gifHandler: GifHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = stringFromJNI()

        sample_text.setOnClickListener {
            //加载gif图片
            ndkLoadGif()
        }

        applyForPermissions()
    }


    private fun applyForPermissions() { //申请sdcard读写权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "no permissions for rw sdcard", Toast.LENGTH_LONG).show()
                    return
                }
            }
        }
    }

    private var handler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) { // 需要摔性能下一帧
            try {
                val mNextFrame = gifHandler?.updateFrame(bitmap) ?: 0
                sendEmptyMessageDelayed(1, mNextFrame.toLong())
            } catch (e: Exception) {
            }
            //Log.d("dq-gif", "bitmap=$bitmap")
            image.setImageBitmap(bitmap)
        }
    }

    private fun ndkLoadGif() {
        //图片用的是assets目录里面的demo2.gif，能解析，但是还有些丢帧。自行拷贝到sdcard试用
        val file = File(Environment.getExternalStorageDirectory(), "demo2.gif")
        gifHandler = GifHandler(file.absolutePath)
        //得到gif   width  height  生成Bitmap
        val width = gifHandler!!.width
        val height = gifHandler!!.height
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        //        下一帧的刷新事件
        var nextFrame = 0
        try {
            nextFrame = gifHandler!!.updateFrame(bitmap)
        } catch (e: Exception) {
        }
        handler.sendEmptyMessageDelayed(1, nextFrame.toLong())
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    private external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
