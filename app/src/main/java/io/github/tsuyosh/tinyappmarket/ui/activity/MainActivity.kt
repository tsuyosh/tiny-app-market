package io.github.tsuyosh.tinyappmarket.ui.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.tsuyosh.tinyappmarket.marketapp.model.MarketApplication
import io.github.tsuyosh.tinyappmarket.marketapp.viewmodel.MarketApplicationViewModel
import io.github.tsuyosh.tinyappmarket.marketapp.viewmodel.MarketApplicationViewModelFactory
import io.github.tsuyosh.tinyappmarket.ui.theme.TinyAppMarketTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    private val applicationsViewModel: MarketApplicationViewModel by viewModels {
        MarketApplicationViewModelFactory(this)
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
        Text(text = application.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun MarketApplicationListItemPreview() {
    MarketApplicationListItem(
        MarketApplication(
            "com.example.app.a",
            "Sample App",
            File("/path/to/app.apk")
        )
    ) {
    }
}

private const val TAG = "MarketApplicationList"