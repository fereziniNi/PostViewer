# Guia de Implementação — Prova PostViewer

## Como o sistema funciona (fluxo de dados)

```
API JSONPlaceholder
       ↓ (Retrofit)
  PostApiService          Room Database
       ↓                       ↓
  PostRepository      LocalCommentDao
  CommentRepository ←────────────────
       ↓
  ViewModel (StateFlow)
       ↓
    Screen (Composable)
       ↓ (collectAsState)
    UI renderizada
```

**Regra de ouro:** Para implementar qualquer coisa nova, siga sempre essa ordem:
1. Modelo de dados (Dto ou Entity)
2. API ou DAO
3. Repository
4. UiState
5. ViewModel
6. Screen
7. Rota na navegação

---

## Cenário 1: Exibir um dado novo da API que já existe

**Exemplo:** Mostrar o `userId` do post na tela de lista.

### Passo 1 — O dado já está no PostDto
```kotlin
// PostDto.kt já tem: val userId: Int
```

### Passo 2 — Mostrar na tela (PostListScreen.kt)
```kotlin
// Dentro de PostItem():
Text(text = "Usuário: ${post.userId}", style = MaterialTheme.typography.bodySmall)
```

Só isso. O dado já chega pelo `uiState`.

---

## Cenário 2: Consumir um endpoint novo da API

**Exemplo:** Buscar usuários — `GET /users` e `GET /users/{id}`.

### Passo 1 — Criar o modelo (novo arquivo)
```kotlin
// data/remote/model/UserDto.kt
package br.edu.ifsp.scl.sc3044025.postviewer.data.remote.model

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String
)
```

### Passo 2 — Adicionar endpoint na API (PostApiService.kt)
```kotlin
// Dentro de PostApiService:
@GET("users")
suspend fun getUsers(): List<UserDto>

@GET("users/{id}")
suspend fun getUserById(@Path("id") userId: Int): UserDto
```

### Passo 3 — Criar Repository (novo arquivo)
```kotlin
// data/repository/UserRepository.kt
class UserRepository(private val apiService: PostApiService) {
    suspend fun getUsers(): List<UserDto> = apiService.getUsers()
    suspend fun getUserById(userId: Int): UserDto = apiService.getUserById(userId)
}
```

### Passo 4 — Registrar no Application (PostViewerApplication.kt)
```kotlin
val userRepository by lazy { UserRepository(RetrofitInstance.apiService) }
```

### Passo 5 — UiState (novo arquivo)
```kotlin
// ui/screens/userlist/UserListUiState.kt
sealed interface UserListUiState {
    data object Loading : UserListUiState
    data class Success(val users: List<UserDto>) : UserListUiState
    data class Error(val message: String) : UserListUiState
}
```

### Passo 6 — ViewModel (novo arquivo)
```kotlin
// ui/screens/userlist/UserListViewModel.kt
class UserListViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UserListUiState>(UserListUiState.Loading)
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    init { loadUsers() }

    private fun loadUsers() {
        viewModelScope.launch {
            try {
                val users = userRepository.getUsers()
                _uiState.value = UserListUiState.Success(users)
            } catch (e: Exception) {
                _uiState.value = UserListUiState.Error(e.message ?: "Erro")
            }
        }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            UserListViewModel(repository) as T
    }
}
```

### Passo 7 — Screen (novo arquivo)
```kotlin
// ui/screens/userlist/UserListScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    viewModel: UserListViewModel,
    onUserClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Usuários") }) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is UserListUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UserListUiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center))
                is UserListUiState.Success -> {
                    LazyColumn {
                        items(state.users, key = { it.id }) { user ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onUserClick(user.id) }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(user.name, fontWeight = FontWeight.Bold)
                                    Text(user.email, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### Passo 8 — Adicionar rota na navegação (AppNavGraph.kt)
```kotlin
// Dentro do NavHost:
composable("user_list") {
    val viewModel: UserListViewModel = viewModel(
        factory = UserListViewModel.Factory(application.userRepository)
    )
    UserListScreen(
        viewModel = viewModel,
        onUserClick = { userId -> navController.navigate("user_detail/$userId") }
    )
}
```

### Passo 9 — Ir para a nova tela (de alguma tela existente)
```kotlin
// Em PostListScreen.kt ou onde fizer sentido:
Button(onClick = { navController.navigate("user_list") }) {
    Text("Ver Usuários")
}
```

---

## Cenário 3: Salvar algo novo no banco local (Room)

**Exemplo:** Salvar posts favoritos localmente.

### Passo 1 — Entity (novo arquivo)
```kotlin
// data/local/entity/FavoritePostEntity.kt
@Entity(tableName = "favorite_posts")
data class FavoritePostEntity(
    @PrimaryKey val postId: Int,
    val title: String,
    val savedAt: Long = System.currentTimeMillis()
)
```

### Passo 2 — DAO (novo arquivo)
```kotlin
// data/local/dao/FavoritePostDao.kt
@Dao
interface FavoritePostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: FavoritePostEntity)

    @Delete
    suspend fun delete(post: FavoritePostEntity)

    @Query("SELECT * FROM favorite_posts ORDER BY savedAt DESC")
    fun getAll(): Flow<List<FavoritePostEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_posts WHERE postId = :postId)")
    fun isFavorite(postId: Int): Flow<Boolean>
}
```

### Passo 3 — Registrar no AppDatabase.kt
```kotlin
// ATENÇÃO: version = 2 (incrementar!), adicionar FavoritePostEntity e addMigrations
@Database(
    entities = [LocalCommentEntity::class, FavoritePostEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localCommentDao(): LocalCommentDao
    abstract fun favoritePostDao(): FavoritePostDao  // ADICIONAR

    companion object {
        // Dentro do databaseBuilder, adicionar migration:
        // .addMigrations(MIGRATION_1_2)
        // val MIGRATION_1_2 = object : Migration(1, 2) {
        //     override fun migrate(db: SupportSQLiteDatabase) {
        //         db.execSQL("CREATE TABLE IF NOT EXISTS favorite_posts (postId INTEGER PRIMARY KEY NOT NULL, title TEXT NOT NULL, savedAt INTEGER NOT NULL)")
        //     }
        // }
    }
}
```

### Passo 4 — Repository
```kotlin
class FavoritePostRepository(private val dao: FavoritePostDao) {
    fun getAll(): Flow<List<FavoritePostEntity>> = dao.getAll()
    fun isFavorite(postId: Int): Flow<Boolean> = dao.isFavorite(postId)
    suspend fun toggle(post: FavoritePostEntity) {
        // lógica de favoritar/desfavoritar
    }
}
```

---

## Cenário 4: Adicionar um campo de busca/filtro em uma lista existente

**Exemplo:** Filtrar posts por título na PostListScreen.

### No ViewModel (PostListViewModel.kt) — adicionar:
```kotlin
// Guardar lista original e lista filtrada
private var allPosts: List<PostDto> = emptyList()

private val _searchQuery = MutableStateFlow("")
val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

fun onSearchQueryChange(query: String) {
    _searchQuery.value = query
    filterPosts(query)
}

private fun filterPosts(query: String) {
    val filtered = if (query.isBlank()) allPosts
    else allPosts.filter { it.title.contains(query, ignoreCase = true) }
    _uiState.value = PostListUiState.Success(filtered)
}

// No loadPosts(), após receber os dados:
// allPosts = posts
// _uiState.value = PostListUiState.Success(posts)
```

### Na Screen (PostListScreen.kt) — adicionar campo de busca:
```kotlin
val searchQuery by viewModel.searchQuery.collectAsState()

// No Scaffold, acima da lista:
OutlinedTextField(
    value = searchQuery,
    onValueChange = viewModel::onSearchQueryChange,
    modifier = Modifier.fillMaxWidth().padding(8.dp),
    placeholder = { Text("Buscar posts...") }
)
```

---

## Cenário 5: Deletar item do banco local

### No DAO — adicionar:
```kotlin
@Delete
suspend fun delete(comment: LocalCommentEntity)

// ou por id:
@Query("DELETE FROM local_comments WHERE id = :id")
suspend fun deleteById(id: Long)
```

### No Repository — adicionar:
```kotlin
suspend fun deleteLocalComment(comment: LocalCommentEntity) {
    localCommentDao.delete(comment)
}
```

### No ViewModel — adicionar:
```kotlin
fun deleteLocalComment(comment: LocalCommentEntity) {
    viewModelScope.launch {
        commentRepository.deleteLocalComment(comment)
    }
}
```

### Na Screen — adicionar botão de deletar no item:
```kotlin
Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(comment.body, modifier = Modifier.weight(1f))
    IconButton(onClick = { viewModel.deleteLocalComment(comment) }) {
        Icon(Icons.Default.Delete, contentDescription = "Deletar")
    }
}
```

---

## Padrões importantes para memorizar

### StateFlow (como observar estado na UI)
```kotlin
// No ViewModel:
private val _uiState = MutableStateFlow(MeuEstado())
val uiState: StateFlow<MeuEstado> = _uiState.asStateFlow()

// Atualizar estado inteiro:
_uiState.value = NovoEstado()

// Atualizar parte do estado (data class):
_uiState.update { it.copy(isLoading = false) }

// Na Screen:
val uiState by viewModel.uiState.collectAsState()
```

### Coroutine no ViewModel
```kotlin
// Para chamadas suspensas (API, Room insert/delete):
viewModelScope.launch {
    try {
        val resultado = repository.buscarAlgo()
        _uiState.value = UiState.Success(resultado)
    } catch (e: Exception) {
        _uiState.value = UiState.Error(e.message ?: "Erro")
    }
}
```

### Flow (Room queries que atualizam automaticamente)
```kotlin
// No DAO: retorna Flow, não suspend
@Query("SELECT * FROM tabela") fun getAll(): Flow<List<MinhaEntity>>

// No Repository: retorna Flow diretamente
fun getAll(): Flow<List<MinhaEntity>> = dao.getAll()

// No ViewModel: usa collect dentro de viewModelScope.launch
viewModelScope.launch {
    repository.getAll().collect { lista ->
        _uiState.update { it.copy(items = lista) }
    }
}
```

### Factory do ViewModel (obrigatório quando o ViewModel recebe parâmetros)
```kotlin
class MeuViewModel(private val repo: MeuRepository) : ViewModel() {
    class Factory(private val repo: MeuRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MeuViewModel(repo) as T
    }
}

// Na Screen/NavGraph:
val viewModel: MeuViewModel = viewModel(
    factory = MeuViewModel.Factory(application.meuRepository)
)
```

### Navegação com argumento
```kotlin
// Navegar PARA a tela:
navController.navigate("detalhe/${item.id}")

// Declarar a rota com argumento:
composable(
    route = "detalhe/{itemId}",
    arguments = listOf(navArgument("itemId") { type = NavType.IntType })
) { backStackEntry ->
    val itemId = backStackEntry.arguments?.getInt("itemId") ?: return@composable
    // usar itemId
}

// Voltar:
navController.popBackStack()
```

---

## Checklist rápido — o que fazer na prova

- [ ] Criar Dto/Entity com os campos necessários
- [ ] Adicionar endpoint no `PostApiService` OU operação no DAO
- [ ] Criar/atualizar Repository
- [ ] Registrar no `PostViewerApplication` se for repositório novo
- [ ] Atualizar `AppDatabase` se for nova Entity (incrementar version + migration)
- [ ] Criar UiState
- [ ] Criar ViewModel com Factory
- [ ] Criar Screen com `collectAsState()`
- [ ] Adicionar rota no `AppNavGraph`
- [ ] Testar navegação

## Imports mais usados

```kotlin
// Compose
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Room
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName
```
