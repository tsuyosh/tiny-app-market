package io.github.tsuyosh.tinyappmarket.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.tsuyosh.tinyappmarket.R
import io.github.tsuyosh.tinyappmarket.marketapp.model.MarketApplication
import io.github.tsuyosh.tinyappmarket.marketapp.viewmodel.MarketApplicationViewModel
import io.github.tsuyosh.tinyappmarket.marketapp.viewmodel.MarketApplicationViewModelFactory
import io.github.tsuyosh.tinyappmarket.ui.theme.TinyAppMarketTheme
import java.io.File

class MainActivity : ComponentActivity() {
    private val applicationsViewModel: MarketApplicationViewModel by viewModels {
        MarketApplicationViewModelFactory(this)
    }

    private val mInstallerActionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive: intent=$intent")
            intent?.let(this@MainActivity::handleInstallerIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TinyAppMarketTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MarketApplicationListScreen(applicationsViewModel)
                }
            }
        }
        val intentFilter = IntentFilter().apply {
            addAction(PACKAGE_INSTALLED_ACTION)
        }
        registerReceiver(
            mInstallerActionReceiver,
            intentFilter
        )
    }

    override fun onDestroy() {
        unregisterReceiver(mInstallerActionReceiver)
        super.onDestroy()
    }

    private fun handleInstallerIntent(intent: Intent) {
        if (intent.action != PACKAGE_INSTALLED_ACTION) {
            return
        }
        val statusCode =
            intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        when (statusCode) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)?.let(this::startActivity)
            }
            PackageInstaller.STATUS_SUCCESS -> {
                Toast.makeText(this, "Install succeeded!", Toast.LENGTH_SHORT).show();
            }
            PackageInstaller.STATUS_FAILURE,
            PackageInstaller.STATUS_FAILURE_ABORTED,
            PackageInstaller.STATUS_FAILURE_BLOCKED,
            PackageInstaller.STATUS_FAILURE_CONFLICT,
            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE,
            PackageInstaller.STATUS_FAILURE_INVALID,
            PackageInstaller.STATUS_FAILURE_STORAGE -> {
                Toast.makeText(
                    this,
                    "Install failed! $statusCode, " +
                            "${intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                Toast.makeText(
                    this,
                    "Unrecognized status received from installer: $statusCode",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        const val PACKAGE_INSTALLED_ACTION =
            "io.github.tsuyosh.tinyappmarket.SESSION_API_PACKAGE_INSTALLED"
    }
}

@Composable
fun MarketApplicationListScreen(viewModel: MarketApplicationViewModel) {
    val applications: List<MarketApplication> by viewModel.allApplications.collectAsState(emptyList())
    Log.d(TAG, "applications = $applications")
    MarketApplicationListScreen(applications) { app ->
        viewModel.install(app)
    }
}

@Composable
fun MarketApplicationListScreen(
    applications: List<MarketApplication>,
    onClick: (MarketApplication) -> Unit
) {
    MarketApplicationList(applications, onClick)
}

@Composable
fun MarketApplicationList(
    applications: List<MarketApplication>,
    onClick: (MarketApplication) -> Unit
) {
    LazyColumn {
        items(applications) { item ->
            MarketApplicationListItem(item) {
                onClick(item)
            }
        }
    }
}

@Composable
fun MarketApplicationListItem(application: MarketApplication, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .clickable(onClick = onClick)
    ) {
        AppIcon(
            iconDrawable = application.iconDrawable,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = application.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MarketApplicationListItemPreview() {
    MarketApplicationListItem(
        MarketApplication(
            "com.example.app.a",
            "Sample App",
            File("/path/to/app.apk"),
            100_000_000L,
            AppCompatResources.getDrawable(LocalContext.current, R.mipmap.ic_launcher)
        )
    ) {
    }
}

@Composable
fun AppIcon(iconDrawable: Drawable?, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context)
        },
        update = { imageView ->
            imageView.setImageDrawable(iconDrawable)
        }
    )
}

@Preview
@Composable
fun AppIconPreview() {
    val drawable =
        AppCompatResources.getDrawable(LocalContext.current, R.mipmap.ic_launcher) ?: return
    AppIcon(drawable)
}


private const val TAG = "MarketApplicationList"