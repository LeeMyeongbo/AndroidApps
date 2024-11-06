package com.test.material.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.test.material.ui.login.LoginActivity
import com.test.material.ui.theme.MaterialStudyTheme
import com.test.material.ui.theme.Purple80

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialStudyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting(this)
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        MaterialStudyTheme {
            Greeting(this)
        }
    }
}

@Composable
fun Greeting(context: Context) {
    val rows = 4
    val columns = 3
    Column(modifier = Modifier.padding(1.5.dp)) {
        repeat(times = rows) { r ->
            Row(modifier = Modifier.weight(1f)) {
                repeat(times = columns) { c ->
                    val index = r * columns + c
                    Button(
                        onClick = processClicked(index, context),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(1.5.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(Purple80)
                    ) {
                        Text(text = "wow $index")
                    }
                }
            }
        }
    }
}

fun processClicked(index: Int, context: Context): () -> Unit {
    if (index == 0) {
        return {
            Log.d("processClicked", "start Loopback!")
            val intent = Intent()
            intent.setClass(context, LoopbackTest::class.java)
            context.startActivity(intent)
        }
    } else if (index % 3 == 0) {
        return {
            Log.d("processClicked", "start LoginActivity!")
            val intent = Intent()
            intent.setClass(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    } else {
        return {
            Log.d("processClicked", "just Toast...")
            Toast.makeText(context, "clicked : $index", Toast.LENGTH_SHORT).show()
        }
    }
}
