package com.inokisheo.kotlinwebview.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.webkit.WebViewAssetLoader
import com.inokisheo.kotlinwebview.BuildConfig
import com.inokisheo.kotlinwebview.LocalContentWebViewClient
import com.inokisheo.kotlinwebview.R

class HomeFragment : Fragment() {

    private lateinit var webView: WebView
    //private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        //progressBar = root.findViewById(R.id.progress_home)

        webView = root.findViewById(R.id.webview_home)
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.settings.allowUniversalAccessFromFileURLs = true
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler(
                "/assets/",
                WebViewAssetLoader.AssetsPathHandler(requireContext())
            )
            .addPathHandler(
                "/res/",
                WebViewAssetLoader.ResourcesPathHandler(requireContext())
            )
            .build()
        webView.webViewClient = LocalContentWebViewClient(assetLoader)
        //webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html")
        // webView.settings.allowExternalNavigation = true
        /*        webView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        // Handling the URL
                        if (view != null && url != null) {
                           view.loadUrl(url)
                        }
                        return super.shouldOverrideUrlLoading(view, url)
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        progressBar.isVisible=true
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        progressBar.isVisible =false

                        super.onPageFinished(view, url)
                    }
                }*/


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.url.observe(viewLifecycleOwner) { url ->
            webView.loadUrl(url)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            //progressBar.isVisible = isL6oading
        }
    }

    override fun onDestroyView() {
        webView.stopLoading()
        webView.destroy()
        super.onDestroyView()
    }
}

