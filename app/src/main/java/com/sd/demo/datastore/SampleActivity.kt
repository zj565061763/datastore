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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.sd.demo.datastore.theme.AppTheme
import com.sd.lib.datastore.updateBlocking
import kotlinx.coroutines.launch

class SampleActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContent {
         AppTheme {
            ContentView()
         }
      }

      lifecycleScope.launch {
         userInfoApi.dataFlow.collect {
            logMsg { "dataFlow:$it" }
         }
      }
   }
}

@Composable
private fun ContentView(
   modifier: Modifier = Modifier,
) {
   val user by userInfoApi.dataFlow.collectAsStateWithLifecycle(initialValue = null)

   Column(
      modifier = modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
   ) {
      Button(onClick = {
         userInfoApi.updateBlocking {
            it.copy(age = it.age + 1)
         }
      }) {
         Text(text = "+")
      }

      Button(onClick = {
         userInfoApi.updateBlocking {
            it.copy(age = it.age - 1)
         }
      }) {
         Text(text = "-")
      }

      Text(text = user?.age.toString())
   }
}
