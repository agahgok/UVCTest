package com.vartech.uvctest.ui

import android.Manifest
import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.vartech.uvctest.UsbCameraHelper
import com.jiangdg.ausbc.widget.AspectRatioSurfaceView

@Composable
fun CameraPreviewScreen() {
    val context = LocalContext.current

    var cameraPermissionGranted by remember { mutableStateOf(false) }
    val usbCameraHelperState = remember { mutableStateOf<UsbCameraHelper?>(null) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraPermissionGranted = isGranted
    }

    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    DisposableEffect(Unit) {
        onDispose {
            usbCameraHelperState.value?.close()
        }
    }

    if (cameraPermissionGranted) {
        val usbCameraHelper = UsbCameraHelper(context)
        usbCameraHelperState.value = usbCameraHelper

        CameraPreviewBox(context, usbCameraHelper)
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = "")
        }
    }
}

@Composable
fun CameraPreviewBox(context: Context, usbCameraHelper: UsbCameraHelper) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                AspectRatioSurfaceView(context).apply {
                    this.holder.addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            usbCameraHelper.startPreview(this@apply)
                        }

                        override fun surfaceChanged(
                            holder: SurfaceHolder,
                            format: Int,
                            width: Int,
                            height: Int
                        ) {
                            usbCameraHelper.usbCamera?.setRenderSize(width, height)
                        }

                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            usbCameraHelper.close()
                        }
                    })
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
        )
    }
}

