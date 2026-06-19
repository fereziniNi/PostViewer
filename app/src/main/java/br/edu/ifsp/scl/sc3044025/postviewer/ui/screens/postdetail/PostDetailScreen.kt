package br.edu.ifsp.scl.sc3044025.postviewer.ui.screens.postdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.edu.ifsp.scl.sc3044025.postviewer.data.local.entity.LocalCommentEntity
import br.edu.ifsp.scl.sc3044025.postviewer.data.remote.model.CommentDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    viewModel: PostDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var commentInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comentários") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.errorMessage != null -> {
                        Text(
                            text = "Erro: ${uiState.errorMessage}",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        CommentList(
                            apiComments = uiState.apiComments,
                            localComments = uiState.localComments
                        )
                    }
                }
            }

            HorizontalDivider()
            AddCommentSection(
                value = commentInput,
                onValueChange = { commentInput = it },
                onSend = {
                    viewModel.addLocalComment(commentInput)
                    commentInput = ""
                }
            )
        }
    }
}

@Composable
private fun CommentList(
    apiComments: List<CommentDto>,
    localComments: List<LocalCommentEntity>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(localComments, key = { "local_${it.id}" }) { comment ->
            LocalCommentItem(comment = comment)
        }
        items(apiComments, key = { "api_${it.id}" }) { comment ->
            ApiCommentItem(comment = comment)
        }
    }
}

@Composable
private fun LocalCommentItem(comment: LocalCommentEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Meu comentário",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = comment.body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ApiCommentItem(comment: CommentDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = comment.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = comment.body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AddCommentSection(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Adicionar comentário...") },
            maxLines = 3
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onSend, enabled = value.isNotBlank()) {
            Icon(imageVector = Icons.Filled.Send, contentDescription = "Enviar")
        }
    }
}