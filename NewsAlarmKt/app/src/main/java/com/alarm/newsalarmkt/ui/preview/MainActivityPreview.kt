package com.alarm.newsalarmkt.ui.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alarm.newsalarmkt.ui.MainView
import com.alarm.newsalarmkt.ui.theme.NewsAlarmKtTheme

@Preview(
    name = "Day",
    device = "spec:width=500dp,height=1050dp",
    showBackground = true,
    showSystemUi = true,
)
@Composable
fun MainPreview() {
    NewsAlarmKtTheme {
        MainView()
    }
}

@Preview(
    name = "Night",
    device = "spec:width=500dp,height=1050dp",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun MainDarkPreview() {
    NewsAlarmKtTheme {
        MainView()
    }
}
