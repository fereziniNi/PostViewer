# PostViewer

**Aluno:** Nícolas Ferezini
**Disciplina:** Desenvolvimento para Dispositivos Móveis — IFSP

## Descrição

Aplicativo Android que consome a API pública [JSONPlaceholder](https://jsonplaceholder.typicode.com)
para exibir posts e comentários. O usuário pode adicionar comentários locais que persistem
entre sessões via Room.

### Funcionalidades

- Listagem de posts carregados da API REST
- Tela de detalhes com comentários da API por post
- Adição de comentários locais com persistência via Room
- Comentários locais aparecem imediatamente e persistem entre sessões
- Tratamento de estados de carregamento e erro na interface

## Como executar

1. Clone o repositório
2. Abra no Android Studio (Hedgehog ou superior)
3. Aguarde a sincronização do Gradle
4. Execute em emulador ou dispositivo com Android 8.0+ (API 26)

> Requer conexão com a internet para carregar posts e comentários da API.

## Tecnologias e Bibliotecas

| Biblioteca | Versão | Uso |
|---|---|---|
| Jetpack Compose | BOM 2024.09.00 | Interface declarativa |
| Navigation Compose | 2.8.9 | Navegação entre telas |
| ViewModel + StateFlow | 2.8.7 | Gerenciamento de estado reativo |
| Room | 2.6.1 | Persistência local de comentários |
| Retrofit + Gson | 2.11.0 | Consumo da API REST |
| Kotlin Coroutines | 1.9.0 | Operações assíncronas |

## Decisões de Design

- **Arquitetura em camadas (data / ui):** separa acesso a dados (Room, Retrofit, repositórios)
  da interface (ViewModels, telas Compose), com responsabilidade única por camada.

- **StateFlow em vez de LiveData:** nativo de Kotlin Coroutines, com suporte de primeira classe
  em Compose via `collectAsState()` sem conversão explícita.

- **Injeção de dependência manual via Application:** evita a complexidade do Hilt/Dagger dado
  o escopo reduzido do projeto. Cada repositório é instanciado uma vez como `lazy`.

- **Room com Flow no DAO:** `getCommentsByPostId` retorna `Flow`, fazendo a UI reagir
  automaticamente a novos comentários sem recarregar a tela manualmente.

- **ViewModelProvider.Factory:** como os ViewModels recebem dependências externas, Factories
  internas garantem criação correta pelo ciclo de vida do Android.
