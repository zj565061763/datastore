package com.sd.demo.datastore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sd.demo.datastore.theme.AppTheme
import kotlinx.coroutines.launch

class SampleActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContent {
         AppTheme {
            ContentView()
         }
      }
   }
}

@Composable
private fun ContentView(
   modifier: Modifier = Modifier,
) {
   val user by UserInfo.flow.collectAsStateWithLifecycle(initialValue = null)
   val scope = rememberCoroutineScope()

   Column(
      modifier = modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
   ) {
      Button(onClick = {
         scope.launch {
            UserInfo.increment()
         }
      }) {
         Text(text = "+")
      }

      Button(onClick = {
         scope.launch {
            UserInfo.decrement()
         }
      }) {
         Text(text = "-")
      }

      Text(text = user?.age.toString())
   }
}
