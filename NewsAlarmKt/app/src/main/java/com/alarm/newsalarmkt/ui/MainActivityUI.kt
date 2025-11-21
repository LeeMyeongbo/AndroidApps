package com.alarm.newsalarmkt.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.alarm.newsalarmkt.ui.veiwmodel.MainActivityViewModel

@Composable
fun MainView(viewModel: ViewModel = MainActivityViewModel()) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(
            left = 10.dp,
            top = 120.dp,
            right = 10.dp,
            bottom = 10.dp
        ),
        floatingActionButton = { CreateFloatingActionBtn(viewModel = viewModel) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                top = paddingValues.calculateTopPadding(),
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                bottom = paddingValues.calculateBottomPadding()
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                CreateBtnAddAlarm(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun CreateFloatingActionBtn(viewModel: ViewModel) {
    FloatingActionButton(
        modifier = Modifier.size(size = 60.dp),
        onClick = {},
        containerColor = MaterialTheme.colorScheme.primary,
        shape = CircleShape,
    ) {
        Image(
            imageVector = Icons.Default.Menu,
            colorFilter = ColorFilter.tint(
                color = if (!isSystemInDarkTheme()) Color.White else Color.Black
            ),
            contentDescription = "help"
        )
    }
}

@Composable
private fun CreateBtnAddAlarm(viewModel: ViewModel) {
    Button(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(corner = CornerSize(size = 12.dp))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                modifier = Modifier.padding(horizontal = 5.dp),
                imageVector = Icons.Default.AddAlarm,
                colorFilter = ColorFilter.tint(
                    color = if (!isSystemInDarkTheme()) Color.White else Color.Black
                ),
                contentDescription = "add_alarm"
            )
            Text(
                modifier = Modifier.padding(horizontal = 5.dp),
                text = "알람 추가",
                fontSize = 18.sp
            )
        }
    }
}
