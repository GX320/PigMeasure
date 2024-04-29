package cn.edu.cidp.www.myapplication

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import cn.edu.cidp.www.myapplication.controller.CameraXController
import cn.edu.cidp.www.myapplication.controller.ICameraController
import cn.edu.cidp.www.myapplication.navigation.Screen
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun photoCompose(navController: NavController){
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.CAMERA)
    PermissionRequired(
        permissionState = permissionState,
        permissionNotGrantedContent = {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {

                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        permissionState.launchPermissionRequest()
                    }) {
                        Text(text = "申请权限")
                    }
                }
            }
        },
        permissionNotAvailableContent = { /*TODO*/ }) {
        CameraX(navController)
    }
}

fun getFileName(context: Context): String?{
    val path = FileUtilsImpl().getPhotoPath(context)
    if (FileUtilsImpl().ergodicFiles(path).isEmpty()){
        return null
    }
    return path + File.separator + FileUtilsImpl().ergodicFiles(path).first()
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CameraX(navController: NavController){
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var bitmap = remember {
        mutableStateOf<Bitmap?>(FileUtilsImpl().openFile(context, getFileName(context)))
    }
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    val previewView = remember {
        PreviewView(context).apply {
            id = R.id.preview_view
        }
    }

    val imageUri = remember {
        mutableStateOf<Uri?>(null)
    }
    val state = rememberPagerState(initialPage = 0)
    /**
     * 解耦方案
     */
    val iCameraController: ICameraController by lazy { CameraXController(previewView, context, lifecycleOwner, bitmap) }

    Column(modifier = Modifier.fillMaxSize()){
        Box(){
            AndroidView(factory = { previewView }, modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
            )
            PagerContent(previewView, state)
        }
        iCameraController.openCameraPreView()
        BottomButton(
            bitmap,
            navController,
            iCameraController,
            context,
            state
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun BottomButton(
    bitmap: MutableState<Bitmap?>,
    navController: NavController,
    iCameraController: ICameraController,
    context: Context,
    state: PagerState
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(1f)
        .background(Color.Black)
        .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Log.d("CameraX", "CameraX: 检测Row渲染时机")
        Image(
            painter = rememberImagePainter(data = if (bitmap.value == null) R.drawable.preview else bitmap.value),
            contentDescription = "预览",
            modifier = Modifier
                .size(60.dp)
                .clickable {
//                    navController.popBackStack()
                    navController.navigate(Screen.Preview.route)
                },
            contentScale = ContentScale.Crop,
        )
        IconButton(onClick = {
            if (state.currentPage == 0){
                iCameraController.takePhoto()
            }else{
                iCameraController.takeVideo()
            }

            bitmap.value = FileUtilsImpl().openFile(context, getFileName(context))
        },
            modifier = Modifier.size(60.dp)
        ) {
            Image(painter = painterResource(id = R.drawable.takepohto), contentDescription = "拍照")
        }
        Image(painter = rememberImagePainter(data = R.drawable.qiehuan, builder = {
            transformations(CircleCropTransformation())
        }),contentDescription = "前后摄像头转换按钮", modifier = Modifier
            .size(60.dp)
            .clickable {
                iCameraController.switchCamera()
            })
    }
}




//引导页案例1
@OptIn(ExperimentalPagerApi::class)
@Composable
fun PagerContent(
    previewView: PreviewView,
    state: PagerState
) {
    val context = LocalContext.current


    Box(
        modifier = Modifier.fillMaxHeight(0.7f),
    ) {
        HorizontalPager(count = 2, state = state, modifier = Modifier.fillMaxWidth(0.5f).align(Alignment.Center)) { pager ->
            pagerContent()
        }
        Indicator(size = 2, index = state.currentPage, modifier = Modifier.fillMaxSize())

    }
}

@Composable
private fun Indicator(size: Int, index: Int, modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(31.dp)
        ) {
            repeat(size) {
                IndicatorHorizontal(it == index)
            }
        }
        Text(text = "拍照  录像")
    }
}

@Composable
private fun IndicatorHorizontal(isSelected: Boolean) {
    val width = animateDpAsState(
        targetValue = if (isSelected) 30.dp else 10.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)
    )
    Box(
        modifier = Modifier
            .height(10.dp)
            .width(width.value)
            .clip(CircleShape)
            .background(if (isSelected) Color.Black else Color.LightGray)
    )
}

@Composable
private fun pagerContent(
) {
    Box(modifier = Modifier.fillMaxSize()){
    }

}
