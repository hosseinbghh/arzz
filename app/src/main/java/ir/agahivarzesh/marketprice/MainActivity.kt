package ir.agahivarzesh.marketprice

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var splashOverlay: LinearLayout
    private lateinit var errorOverlay: LinearLayout
    private lateinit var errorTitle: TextView
    private lateinit var errorMessage: TextView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        splashOverlay = findViewById(R.id.splashOverlay)
        errorOverlay = findViewById(R.id.errorOverlay)
        errorTitle = findViewById(R.id.errorTitle)
        errorMessage = findViewById(R.id.errorMessage)

        findViewById<Button>(R.id.btnRetry).setOnClickListener {
            loadAppUrl()
        }

        swipeRefresh.setColorSchemeResources(R.color.mp_accent)
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.mp_card)
        swipeRefresh.setOnRefreshListener {
            if (isOnline()) {
                webView.reload()
            } else {
                swipeRefresh.isRefreshing = false
                showError(getString(R.string.offline_title), getString(R.string.offline_message))
            }
        }

        configureWebView()
        setupBackNavigation()
        loadAppUrl()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            allowFileAccess = false
            allowContentAccess = false
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            userAgentString = "$userAgentString MarketPriceAndroid/1.0"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return true
                return if (isAllowedAppUrl(uri)) {
                    false // Load inside WebView
                } else {
                    // Open external URLs safely in system browser
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, uri))
                    } catch (_: Exception) { }
                    true
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                errorOverlay.visibility = View.GONE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                swipeRefresh.isRefreshing = false
                splashOverlay.animate().alpha(0f).setDuration(220).withEndAction {
                    splashOverlay.visibility = View.GONE
                }.start()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    swipeRefresh.isRefreshing = false
                    splashOverlay.visibility = View.GONE
                    showError(getString(R.string.offline_title), getString(R.string.offline_message))
                }
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                // Strictly enforce valid SSL certificates
                handler?.cancel()
                swipeRefresh.isRefreshing = false
                splashOverlay.visibility = View.GONE
                showError(getString(R.string.ssl_error_title), getString(R.string.ssl_error_message))
            }
        }
    }

    private fun isAllowedAppUrl(uri: Uri): Boolean {
        val schemeOk = uri.scheme.equals("https", ignoreCase = true)
        val hostOk = uri.host.equals(BuildConfig.ALLOWED_HOST, ignoreCase = true)
        val path = uri.path ?: ""
        val pathOk = path == BuildConfig.ALLOWED_PATH_PREFIX || path.startsWith("${BuildConfig.ALLOWED_PATH_PREFIX}/")
        return schemeOk && hostOk && pathOk
    }

    private fun loadAppUrl() {
        if (!isOnline()) {
            splashOverlay.visibility = View.GONE
            showError(getString(R.string.offline_title), getString(R.string.offline_message))
            return
        }
        errorOverlay.visibility = View.GONE
        webView.loadUrl(BuildConfig.APP_BASE_URL)
    }

    private fun showError(title: String, message: String) {
        errorTitle.text = title
        errorMessage.text = message
        errorOverlay.visibility = View.VISIBLE
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}
