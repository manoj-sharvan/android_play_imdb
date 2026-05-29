package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    imdbId: String,
    movieTitle: String,
    useInternalPlayer: Boolean,
    playUrl: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Fallback launcher using native implicit intent matching standard browser packages
    val launchFallback = {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            errorMessage = "No web browser is installed on this device."
            hasError = true
        }
    }

    LaunchedEffect(useInternalPlayer) {
        if (!useInternalPlayer) {
            launchFallback()
            onBackClick() // Pop detail backstack
        }
    }

    Scaffold(
        modifier = modifier.testTag("player_screen"),
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = movieTitle,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val streamHost = remember(playUrl) {
                            try {
                                Uri.parse(playUrl).host ?: "Custom Server"
                            } catch (e: Exception) {
                                "Custom Server"
                            }
                        }
                        Text(
                            text = "Streaming $streamHost",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("player_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { launchFallback() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Open in Browser",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            if (useInternalPlayer) {
                if (!hasError) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("player_webview"),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                @SuppressLint("SetJavaScriptEnabled")
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.databaseEnabled = true
                                settings.loadsImagesAutomatically = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true
                                settings.setSupportZoom(true)
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                                settings.setSupportMultipleWindows(false)
                                settings.setJavaScriptCanOpenWindowsAutomatically(false)
                                
                                // Enable mixed content loading since video platforms often load media stream chunks over HTTP inside an HTTPS page
                                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                                // Use a clean, standard mobile browser User-Agent to bypass any anti-webview scraper blocks
                                val originalAgent = settings.userAgentString
                                if (originalAgent != null) {
                                    settings.userAgentString = originalAgent
                                        .replace("; wv", "")
                                        .replace("Version/4.0 ", "")
                                }
                                
                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        if (newProgress >= 90) {
                                            isLoading = false
                                        }
                                    }

                                    override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                                        try {
                                            request?.deny()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }

                                    override fun onGeolocationPermissionsShowPrompt(
                                        origin: String?,
                                        callback: android.webkit.GeolocationPermissions.Callback?
                                    ) {
                                        try {
                                            callback?.invoke(origin, false, false)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                                
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoading = false
                                    }

                                    @Deprecated("Deprecated in Java")
                                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                        if (url == null) return false
                                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                            return true // Handled / Blocked
                                        }
                                        
                                        // Block main frame external popups/redirects on older API levels
                                        try {
                                            val loadedHost = Uri.parse(url).host
                                            val originalHost = Uri.parse(playUrl).host
                                            if (loadedHost != null && originalHost != null) {
                                                if (loadedHost != originalHost && !loadedHost.endsWith("." + originalHost) && !loadedHost.contains("streamimdb")) {
                                                    return true // Block external hijack redirects
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        return false
                                    }

                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                     ): Boolean {
                                         val url = request?.url?.toString() ?: return false
                                         
                                         // Block non-HTTP/HTTPS links to avoid deep link app launches or crash triggers (e.g. intent:, market:, play:)
                                         if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                             return true // Handled / Blocked
                                         }

                                         // To prevent ad networks/popups from hijacking the main player frame:
                                         if (request.isForMainFrame) {
                                             val loadedUri = request.url
                                             val originalUri = Uri.parse(playUrl)
                                             val loadedHost = loadedUri.host
                                             val originalHost = originalUri.host
                                             
                                             if (loadedHost != null && originalHost != null) {
                                                 if (loadedHost != originalHost && !loadedHost.endsWith("." + originalHost) && !loadedHost.contains("streamimdb")) {
                                                     return true // Block cross-origin hijacking redirects on main frame
                                                 }
                                             }
                                         }

                                         return false
                                     }

                                     override fun onReceivedError(
                                         view: WebView?,
                                         request: WebResourceRequest?,
                                         error: WebResourceError?
                                     ) {
                                         if (request?.isForMainFrame == true) {
                                             val errorCode = error?.errorCode ?: 0
                                             val description = error?.description?.toString() ?: ""
                                             // Only block the screen on complete physical connection failure,
                                             // and allow all other pages to load naturally (ignoring abort/cancel).
                                             if (errorCode == WebViewClient.ERROR_HOST_LOOKUP || errorCode == WebViewClient.ERROR_CONNECT) {
                                                 hasError = true
                                                 errorMessage = description
                                             }
                                         }
                                     }
                                }
                                
                                loadUrl(playUrl)
                            }
                        },
                        onRelease = { webView ->
                            try {
                                webView.stopLoading()
                                webView.onPause()
                                webView.loadUrl("about:blank")
                                webView.clearHistory()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                }

                if (isLoading && !hasError) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                         Text(text = "Opening secure web stream...", color = Color.LightGray, fontSize = 13.sp)
                    }
                }

                if (hasError) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Playback Screen Blocked",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This stream provider may restrict running within in-app viewports for some mobile user agents. Tap 'Open Fallback' to run this stream directly on your external web browser.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { 
                                    hasError = false
                                    isLoading = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E))
                            ) {
                                Text("Retry Player", color = Color.White)
                            }

                            Button(
                                onClick = { launchFallback() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Open Fallback", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
