import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ekpss.quizapp.firebase.FirestoreHelper
import com.ekpss.quizapp.model.Test

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestListScreen(
    subject: String,
    onBack: () -> Unit,
    onTestSelected: (testId: String) -> Unit
) {
    val firestoreHelper = remember { FirestoreHelper() }
    var tests by remember { mutableStateOf<List<Test>>(emptyList()) }

    LaunchedEffect(subject) {
        firestoreHelper.getTests(
            subject,
            onSuccess = { tests = it },
            onFailure = { Log.e("TestListScreen", "Testler alınamadı: ${it.message}") }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$subject - Testler") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues).padding(16.dp)) {
            if (tests.isEmpty()) {
                CircularProgressIndicator()
            } else {
                LazyColumn {
                    items(tests) { test ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { onTestSelected(test.id) },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Text(
                                text = test.title,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}