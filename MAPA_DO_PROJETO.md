# Mapa Completo do Projeto PostViewer

## Estrutura de Pastas

```
PostViewer/
├── app/src/main/
│   ├── AndroidManifest.xml
│   └── java/br/edu/ifsp/scl/sc3044025/postviewer/
│       ├── MainActivity.kt
│       ├── PostViewerApplication.kt
│       ├── data/
│       │   ├── local/
│       │   │   ├── AppDatabase.kt
│       │   │   ├── dao/LocalCommentDao.kt
│       │   │   └── entity/LocalCommentEntity.kt
│       │   ├── remote/
│       │   │   ├── RetrofitInstance.kt
│       │   │   ├── api/PostApiService.kt
│       │   │   └── model/
│       │   │       ├── PostDto.kt
│       │   │       └── CommentDto.kt
│       │   └── repository/
│       │       ├── PostRepository.kt
│       │       └── CommentRepository.kt
│       └── ui/
│           ├── navigation/AppNavGraph.kt
│           ├── screens/
│           │   ├── postlist/
│           │   │   ├── PostListScreen.kt
│           │   │   ├── PostListUiState.kt
│           │   │   └── PostListViewModel.kt
│           │   └── postdetail/
│           │       ├── PostDetailScreen.kt
│           │       ├── PostDetailUiState.kt
│           │       └── PostDetailViewModel.kt
│           └── theme/
├── gradle/libs.versions.toml
├── app/build.gradle.kts
└── gradle.properties
```

---

## Arquivo por Arquivo

### AndroidManifest.xml
**O que faz:** Declara permissões e configura a Activity inicial.
**Mexer aqui quando:** Precisar adicionar permissão (INTERNET já está), nova Activity, ou mudar o nome da Application class.
```xml
<uses-permission android:name="android.permission.INTERNET" />
<application android:name=".PostViewerApplication" ...>
    <activity android:name=".MainActivity" ...>
```

---

### MainActivity.kt
**O que faz:** Ponto de entrada do app. Inicializa o Compose e entrega a Application para a navegação.
**Mexer aqui quando:** Precisar mudar o tema raiz ou o ponto de entrada da navegação.
```kotlin
val application = application as PostViewerApplication
setContent {
    PostViewerTheme {
        AppNavGraph(application = application)
    }
}
```

---

### PostViewerApplication.kt
**O que faz:** Cria e fornece as dependências globais (banco, API, repositórios) via `lazy`.
**Mexer aqui quando:** Adicionar um novo repositório, novo banco, ou novo serviço de API.
```kotlin
val database by lazy { AppDatabase.getInstance(this) }
val postRepository by lazy { PostRepository(RetrofitInstance.apiService) }
val commentRepository by lazy {
    CommentRepository(RetrofitInstance.apiService, database.localCommentDao())
}
// Para adicionar algo novo:
val meuNovoRepository by lazy { MeuNovoRepository(...) }
```

---

## CAMADA DE DADOS (data/)

### data/remote/RetrofitInstance.kt
**O que faz:** Cria o cliente HTTP Retrofit como singleton. Define a BASE_URL da API.
**Mexer aqui quando:** Mudar a URL base da API.
```kotlin
private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
val apiService: PostApiService by lazy { ... }
```

---

### data/remote/api/PostApiService.kt
**O que faz:** Define os endpoints da API REST com anotações Retrofit.
**Mexer aqui quando:** Adicionar um novo endpoint (novo GET, POST, etc).
```kotlin
@GET("posts")
suspend fun getPosts(): List<PostDto>

@GET("posts/{id}/comments")
suspend fun getCommentsByPostId(@Path("id") postId: Int): List<CommentDto>

// Para adicionar endpoint novo:
@GET("users")
suspend fun getUsers(): List<UserDto>

@GET("users/{id}")
suspend fun getUserById(@Path("id") userId: Int): UserDto
```

---

### data/remote/model/PostDto.kt
**O que faz:** Representa o JSON de um Post da API.
**Mexer aqui quando:** A API retornar campos novos ou diferentes.
```kotlin
data class PostDto(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String
)
```

---

### data/remote/model/CommentDto.kt
**O que faz:** Representa o JSON de um Comentário da API.
**Mexer aqui quando:** A estrutura JSON do comentário mudar.
```kotlin
data class CommentDto(
    @SerializedName("id") val id: Int,
    @SerializedName("postId") val postId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("body") val body: String
)
```

---

### data/local/entity/LocalCommentEntity.kt
**O que faz:** Define a tabela `local_comments` no banco Room.
**Mexer aqui quando:** Precisar adicionar colunas à tabela ou criar uma nova entidade.
```kotlin
@Entity(tableName = "local_comments")
data class LocalCommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Int,
    val body: String
)
// ATENÇÃO: se mudar colunas, incrementar version no AppDatabase e criar Migration!
```

---

### data/local/dao/LocalCommentDao.kt
**O que faz:** Define as operações SQL para a tabela `local_comments`.
**Mexer aqui quando:** Precisar de nova query (DELETE, UPDATE, nova SELECT).
```kotlin
@Dao
interface LocalCommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: LocalCommentEntity)

    @Query("SELECT * FROM local_comments WHERE postId = :postId ORDER BY id DESC")
    fun getCommentsByPostId(postId: Int): Flow<List<LocalCommentEntity>>

    // Para deletar:
    @Delete
    suspend fun delete(comment: LocalCommentEntity)

    // Para deletar por id:
    @Query("DELETE FROM local_comments WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

---

### data/local/AppDatabase.kt
**O que faz:** Configura o banco Room. Lista todas as entidades e fornece os DAOs.
**Mexer aqui quando:** Adicionar nova entidade (tabela) ou novo DAO. Lembre de incrementar `version`.
```kotlin
@Database(entities = [LocalCommentEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localCommentDao(): LocalCommentDao

    // Para adicionar nova entidade:
    // @Database(entities = [LocalCommentEntity::class, MinhaEntidade::class], version = 2, ...)
    // abstract fun minhaEntidadeDao(): MinhaEntidadeDao
}
```

---

### data/repository/PostRepository.kt
**O que faz:** Intermediário entre a API e a UI para Posts. Chama o `PostApiService`.
**Mexer aqui quando:** Adicionar lógica de negócio relacionada a posts (cache, filtro, etc).
```kotlin
class PostRepository(private val apiService: PostApiService) {
    suspend fun getPosts(): List<PostDto> = apiService.getPosts()
}
```

---

### data/repository/CommentRepository.kt
**O que faz:** Intermediário para comentários. Combina API remota e banco local.
**Mexer aqui quando:** Adicionar nova operação de comentário (deletar, editar).
```kotlin
class CommentRepository(
    private val apiService: PostApiService,
    private val localCommentDao: LocalCommentDao
) {
    suspend fun getApiComments(postId: Int): List<CommentDto> = ...
    fun getLocalComments(postId: Int): Flow<List<LocalCommentEntity>> = ...
    suspend fun addLocalComment(postId: Int, body: String) { ... }
}
```

---

## CAMADA DE UI (ui/)

### ui/navigation/AppNavGraph.kt
**O que faz:** Define todas as rotas (telas) do app e como navegar entre elas.
**Mexer aqui quando:** Adicionar uma nova tela ou nova rota de navegação.
```kotlin
// Rotas existentes:
"post_list"              -> PostListScreen
"post_detail/{postId}"  -> PostDetailScreen (recebe Int)

// Para adicionar nova tela:
composable("nova_tela") {
    NovaTela(onNavigateBack = { navController.popBackStack() })
}

// Para navegar passando argumento:
composable(
    route = "outra_tela/{itemId}",
    arguments = listOf(navArgument("itemId") { type = NavType.IntType })
) { backStackEntry ->
    val itemId = backStackEntry.arguments?.getInt("itemId") ?: return@composable
    OutraTela(itemId = itemId)
}
```

---

### ui/screens/postlist/PostListUiState.kt
**O que faz:** Define os estados possíveis da tela de lista (Loading, Success, Error).
**Mexer aqui quando:** Adicionar novo estado ou dado ao estado de sucesso.
```kotlin
sealed interface PostListUiState {
    data object Loading : PostListUiState
    data class Success(val posts: List<PostDto>) : PostListUiState
    data class Error(val message: String) : PostListUiState
}
```

---

### ui/screens/postlist/PostListViewModel.kt
**O que faz:** Lógica da tela de lista. Carrega posts da API e expõe o estado via StateFlow.
**Mexer aqui quando:** Adicionar filtro, busca, ordenação ou novo dado para a lista.
```kotlin
class PostListViewModel(private val postRepository: PostRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<PostListUiState>(PostListUiState.Loading)
    val uiState: StateFlow<PostListUiState> = _uiState.asStateFlow()

    init { loadPosts() }

    private fun loadPosts() {
        viewModelScope.launch {
            try {
                val posts = postRepository.getPosts()
                _uiState.value = PostListUiState.Success(posts)
            } catch (e: Exception) {
                _uiState.value = PostListUiState.Error(e.message ?: "Erro")
            }
        }
    }
}
```

---

### ui/screens/postlist/PostListScreen.kt
**O que faz:** UI da lista de posts. Mostra loading, lista ou erro. Cada item é clicável.
**Mexer aqui quando:** Mudar visual da lista, adicionar botão, mostrar mais dados do post.
```kotlin
@Composable
fun PostListScreen(viewModel: PostListViewModel, onPostClick: (Int) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    // when(uiState) -> Loading | Success | Error
}
```

---

### ui/screens/postdetail/PostDetailUiState.kt
**O que faz:** Estado da tela de detalhe (data class, não sealed). Contém loading, comentários API e locais.
**Mexer aqui quando:** Adicionar novo dado ao estado (ex: título do post).
```kotlin
data class PostDetailUiState(
    val isLoading: Boolean = true,
    val apiComments: List<CommentDto> = emptyList(),
    val localComments: List<LocalCommentEntity> = emptyList(),
    val errorMessage: String? = null
)
```

---

### ui/screens/postdetail/PostDetailViewModel.kt
**O que faz:** Lógica da tela de detalhe. Carrega comentários da API + observa comentários locais (Flow). Permite adicionar comentário local.
**Mexer aqui quando:** Adicionar nova ação (deletar comentário, editar, etc).
```kotlin
class PostDetailViewModel(
    private val postId: Int,
    private val commentRepository: CommentRepository
) : ViewModel() {
    // _uiState usa update {} para modificar partes do estado
    fun addLocalComment(body: String) {
        if (body.isBlank()) return
        viewModelScope.launch { commentRepository.addLocalComment(postId, body) }
    }
}
```

---

### ui/screens/postdetail/PostDetailScreen.kt
**O que faz:** UI do detalhe do post. Lista comentários API (branco) e locais (azul). Campo de texto + botão enviar no rodapé.
**Mexer aqui quando:** Mudar visual dos comentários, adicionar swipe-to-delete, mostrar dados do post.
```kotlin
@Composable
fun PostDetailScreen(viewModel: PostDetailViewModel, onNavigateBack: () -> Unit) {
    // Scaffold com TopAppBar + seta voltar
    // LazyColumn com LocalCommentItem (primaryContainer) e ApiCommentItem (padrão)
    // AddCommentSection no rodapé: OutlinedTextField + IconButton(Send)
}
```

---

## Arquivos de Configuração

### gradle/libs.versions.toml
**O que faz:** Catálogo centralizado de versões de bibliotecas e plugins.
**Mexer aqui quando:** Adicionar nova dependência ou atualizar versão.
```toml
[versions]
agp = "8.7.3"
kotlin = "2.0.21"
room = "2.6.1"
ksp = "2.0.21-1.0.27"
# Adicionar nova lib: minhaLib = "1.0.0"

[libraries]
# Adicionar: minha-lib = { group = "com.exemplo", name = "minhalib", version.ref = "minhaLib" }

[plugins]
# android-application, kotlin-android, kotlin-compose, ksp
```

### app/build.gradle.kts
**O que faz:** Configura o módulo app (compileSdk, plugins, dependências).
**Mexer aqui quando:** Adicionar nova dependência ou mudar configuração de build.
```kotlin
plugins { android.application + kotlin.android + kotlin.compose + ksp }
android { compileSdk = 35, targetSdk = 35, minSdk = 26 }
dependencies {
    // Room usa ksp(libs.androidx.room.compiler)
    // Adicionar lib: implementation(libs.minha.lib)
}
```

---

## Funcionalidade: Contagem de Comentarios na Tela Principal

### O que faz
Cada card da lista de posts exibe quantos comentarios aquele post possui (ex: "Comentarios: 5").

### Como funciona (fluxo completo)
```
API /comments (1 chamada) --> CommentRepository.getCommentCounts()
    --> Map<postId, count>
    --> PostListViewModel (async junto com os posts)
    --> PostListUiState.Success.commentCounts
    --> PostListScreen > PostItem > Text("Comentarios: N")
```

### Por que 1 chamada e nao 100
A API jsonplaceholder tem o endpoint `GET /comments` que retorna TODOS os 500 comentarios de uma vez.
O metodo `.groupingBy { it.postId }.eachCount()` agrupa e conta em memoria — muito mais rapido do que
chamar `GET /posts/{id}/comments` para cada um dos 100 posts.

### Arquivos alterados
| Arquivo | O que mudou |
|---|---|
| `PostApiService.kt` | Adicionado `@GET("comments") suspend fun getAllComments()` |
| `CommentRepository.kt` | Adicionado `suspend fun getCommentCounts(): Map<Int, Int>` |
| `PostListUiState.kt` | `Success` ganhou campo `commentCounts: Map<Int, Int>` |
| `PostListViewModel.kt` | Recebe `CommentRepository`; carrega posts e contagens em paralelo com `async {}` |
| `AppNavGraph.kt` | Factory do `PostListViewModel` agora recebe `commentRepository` |
| `PostListScreen.kt` | `PostItem` exibe "Comentarios: N" abaixo do titulo |

### Como adicionar contagem de outras coisas no futuro
1. Adicionar endpoint em `PostApiService.kt`
2. Adicionar metodo no repositorio correspondente
3. Adicionar campo no `PostListUiState.Success`
4. Carregar com `async {}` no `PostListViewModel.loadPosts()`
5. Exibir em `PostItem`
