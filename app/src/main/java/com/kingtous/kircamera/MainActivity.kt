package com.kingtous.kircamera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Bundle
import android.util.Log.d
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity(), View.OnClickListener {

    val CAMERA_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        getCameraView()
        capture.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        cameraDevice?.close()
        super.onDestroy()
    }

    private val callback = object : CameraDevice.StateCallback() {
        override fun onOpened(p0: CameraDevice) {
            cameraDevice = p0
            cameraDevice?.let { cameraDevice ->
                cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                cameraDevice.createCaptureSession(
                    SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR,
                        listOf(OutputConfiguration(surface!!)),
                        Executor {
                            // 直接执行
                            it.run()
                        },
                        sessionCallback
                    )
                )
            }
        }

        override fun onDisconnected(p0: CameraDevice) {
            d(packageName, "onOpened")
        }

        override fun onError(p0: CameraDevice, p1: Int) {
            d(packageName, "onOpened")
        }
    }

    private var cameraDevice: CameraDevice? = null
    private var manager: CameraManager? = null
    private var surface: Surface? = null
    private var captureSession: CameraCaptureSession? = null
    private val sessionCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            captureSession = session
            val builder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            surface?.let {
                builder?.addTarget(it)
            }
            session.setRepeatingRequest(builder!!.build(), null, null)
        }

        override fun onConfigureFailed(p0: CameraCaptureSession) {
            //TODO("Not yet implemented")
            p0.close()
        }
    }

    private fun getCameraView() {
        // 初始化Surface
        findViewById<TextureView>(R.id.camera_view).surfaceTextureListener =
            object : TextureView.SurfaceTextureListener {
                @SuppressLint("NewApi")
                override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                    surface = Surface(p0)
                    if (PermissionChecker.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.CAMERA
                        ) != PermissionChecker.PERMISSION_GRANTED
                    ) {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ), CAMERA_CODE
                        )
                    } else {
                        startCamera()
                    }
                }

                override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

                }

                override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                    //TODO("Not yet implemented")
                    return false
                }

                override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                    //TODO("Not yet implemented")
                }

            }
    }

    @SuppressLint("MissingPermission")
    private fun startCamera() {
        manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        manager?.openCamera("5", callback, null)
        return
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_CODE) {
            for (element in grantResults) {
                if (element != PermissionChecker.PERMISSION_GRANTED) {
                    Snackbar.make(capture.rootView, "权限未允许", Snackbar.LENGTH_LONG).show()
                    return
                }
            }
            startCamera()
        }
    }

    override fun onClick(view: View?) {
        view?.let {
            if (it.id == R.id.capture) {
                val captureBuilder =
                    cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                surface?.let { sf ->
                    captureBuilder?.let { builder ->
                        builder.addTarget(sf)
                        captureSession?.stopRepeating()
                        captureSession?.abortCaptures()
                        captureSession?.capture(builder.build(), captureCallback, null)

                    }
                }
            }
        }

    }

    private var captureCallback = object :
        CameraCaptureSession.CaptureCallback() {

    }
}