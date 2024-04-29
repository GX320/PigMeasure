package cn.edu.cidp.www.myapplication

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import java.io.File

//@Composable
//fun previewCompose(
//    navController: NavController,
//    lifecycleScope: LifecycleCoroutineScope){
//    Log.d("PagerContent1", "previewCompose: 检测最外层渲染时机")
//    PagerContent1(
//        onUploadClick = {
////            if (FileUtil.size(File(it)) <= 2097152){
////                FileUtilsImpl().update(it,lifecycleScope)   // 单独上传
////            }else{
////                FileUtilsImpl().chunkUpdate(fileName = it, lifecycleScope)  //分片上传
////            }
//            FileUtilsImpl().update(it,lifecycleScope)
//        }
//    )
//}
@Composable
fun previewCompose(
    navController: NavController,
    lifecycleScope: LifecycleCoroutineScope
) {
    Log.d("PagerContent1", "previewCompose: 检测最外层渲染时机")
    PagerContent1 { imageByteArray, imageName, callback: (String) -> Unit ->
        FileUtilsImpl().update(imageByteArray, imageName, lifecycleScope) { message ->
            // 在上传成功后调用回调函数，显示上传成功对话框
            callback(message)
        }
    }
}




//引导页案例1
@OptIn(ExperimentalPagerApi::class)
@Composable
fun PagerContent1(
    onUploadClick: (ByteArray, String, (String) -> Unit) -> Unit // 修改 onUploadClick 参数为接受 ByteArray、String 和 () -> Unit 的函数
) {
    val context = LocalContext.current
    val path = FileUtilsImpl().getPhotoPath(context)
    var ergodicFiles2 by remember {
        mutableStateOf<List<String>>(FileUtilsImpl().ergodicFiles(path))
    }
    val state = rememberPagerState(initialPage = 0)

    var alertDialogShown by remember { mutableStateOf(false) }
    var alertDialogMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(count = ergodicFiles2.size, state = state) { pager ->
            val fileName = path + File.separator + ergodicFiles2[pager]
            pagerContent(context, fileName, onDeleteClick = {
                FileUtilsImpl().delete(fileName)
                ergodicFiles2 = FileUtilsImpl().ergodicFiles(path)
            },
                onUploadClick = {
                    // 读取图像数据并转换为 ByteArray
                    val imageByteArray = FileUtilsImpl().readImageAsByteArray(fileName)
                    if (imageByteArray != null) {
                        // 提取文件名
                        val imageName = File(fileName).name
                        // 调用上传函数，传递图片字节数组、图片名称和回调函数
                        onUploadClick(imageByteArray, imageName) { message ->
                            // 接口返回后，显示 AlertDialog
                            alertDialogMessage = message
                            alertDialogShown = true
                        }
                    } else {
                        Log.e("PagerContent1", "Failed to read image data")
                    }
                }


            )
        }
    }

    // 显示上传成功对话框
    if (alertDialogShown) {
        AlertDialog(
            onDismissRequest = { alertDialogShown = false },
            title = { Text("上传成功") },
            text = { Text(alertDialogMessage) },
            confirmButton = {
                Button(
                    onClick = { alertDialogShown = false },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Green,
                        contentColor = Color.White
                    )
                ) {
                    Text("关闭")
                }
            }
        )
    }

}






@Composable
private fun pagerContent(
    context : Context,
    fileName : String,
    onDeleteClick: () -> Unit,
    onUploadClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberImagePainter(data = FileUtilsImpl().openFile(context, fileName)),
                contentScale = ContentScale.Fit,
                contentDescription = "",
//                modifier = Modifier.rotate()
            )
        }
        Text(text = fileName, modifier = Modifier.padding(top = 10.dp))
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onDeleteClick
            ) {
                Text(text = "删除")
            }
            Button(onClick = onUploadClick) {
                Text(text = "上传")
            }
        }
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
private fun Indicator(size: Int, index: Int) {
    Column() {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(31.dp)
        ) {
            repeat(size) {
                IndicatorHorizontal(it == index)
            }
        }
        Text(text = "拍照")
    }
}

private data class ItemData(
    val image: Int,
    val title: String,
    val content: String
)

