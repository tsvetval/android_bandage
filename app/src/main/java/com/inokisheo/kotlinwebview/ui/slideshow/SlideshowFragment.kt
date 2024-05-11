package com.inokisheo.kotlinwebview.ui.slideshow

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
import com.inokisheo.kotlinwebview.databinding.FragmentSlideshowBinding
import com.inokisheo.kotlinwebview.ui.home.HomeViewModel

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    private lateinit var webView: WebView
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val root = inflater.inflate(R.layout.fragment_slideshow, container, false)
        //progressBar = root.findViewById(R.id.progress_home)

        webView = root.findViewById(R.id.webview_about)
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
        webView.loadUrl("https://appassets.androidplatform.net/assets/about.html")
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
//        viewModel.url.observe(viewLifecycleOwner) { url ->
//            webView.loadUrl(url)
//        }
//        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
//            //progressBar.isVisible = isL6oading
//        }
    }

    override fun onDestroyView() {
        webView.stopLoading()
        webView.destroy()
        super.onDestroyView()
    }
/*    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }*/
}