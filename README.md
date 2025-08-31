
# CareClock

## 🚀 Sobre o Projeto

O CareClock é um aplicativo Android desenvolvido em Kotlin para auxiliar no gerenciamento de tratamentos de saúde. Ele foi projetado para ser intuitivo e fácil de usar, permitindo que indivíduos e cuidadores agendem e acompanhem a medicação de forma eficiente. A aplicação vai além dos lembretes básicos, oferecendo um sistema robusto de gerenciamento de perfis, incluindo a possibilidade de gerenciar dependentes.

A aplicação utiliza a arquitetura tradicional de Views e XML do Android, o que a torna um excelente ponto de partida para desenvolvedores que desejam aprender a construir aplicações Android nativas.

## ✨ Funcionalidades Principais

  * **Autenticação de Usuário**: Os utilizadores podem criar uma conta com e-mail e senha ou fazer login de forma rápida e segura através da Conta Google.
  * **Gestão de Perfis**: O aplicativo permite adicionar e gerenciar perfis de "dependentes", tornando-o ideal para cuidadores que precisam acompanhar a medicação de várias pessoas.
  * **Cadastro de Medicamentos**: Adicione novos medicamentos com detalhes como nome, data e hora de início, intervalo das doses (em horas) e, opcionalmente, a duração do tratamento.
  * **Lembretes e Notificações**: O sistema de alarmes agendados garante que os utilizadores recebam notificações na hora certa, mesmo com o aplicativo fechado. É possível agendar alarmes para doses e também lembretes de renovação de receita.
  * **Lista Dinâmica**: A tela principal exibe uma lista interativa de todos os medicamentos cadastrados, com um contador regressivo para a próxima dose. A lista também indica a data de início e a data de término, se aplicável.
  * **Edição e Exclusão**: Os utilizadores podem editar um tratamento com um toque ou apagar um medicamento com um simples gesto de arrastar para o lado.

## 🛠️ Tecnologias Utilizadas

  * **Linguagem de Programação**: Kotlin.
  * **Interface do Usuário**: Views e XML.
  * **Persistência de Dados**: Firebase Firestore para armazenar dados de utilizadores e medicamentos.
  * **Autenticação**: Firebase Authentication para login com e-mail/senha e Google Sign-In.
  * **Agendamento de Alarmes**: `AlarmManager` para agendar lembretes recorrentes.
  * **Notificações**: `BroadcastReceiver` para processar os alarmes e exibir notificações na barra de status.
  * **Gerenciador de Dependências**: Gradle.

## 📂 Estrutura do Código

O projeto segue uma estrutura modular para facilitar a manutenção e o desenvolvimento:

  * **`ui`**: Contém todas as `Activities` (telas) e `Adapters` do aplicativo.
  * **`models`**: Armazena as classes de dados, como a classe `Medication`, que define a estrutura de um medicamento.
  * **`storage`**: Lida com a lógica de salvar, carregar e apagar dados de medicamentos no Firestore.
  * **`notifications`**: Contém a lógica de agendamento de alarmes (`AlarmScheduler`) e a exibição de notificações.

## ⚙️ Como Começar

Para executar este projeto localmente, siga os passos abaixo:

1.  **Clone o Repositório**:
    ```bash
    git clone https://github.com/joaolpdr/CareClock.git
    ```
2.  **Configurar o Firebase**:
      * Crie um novo projeto no [Firebase Console](https://console.firebase.google.com/).
      * Habilite o Firestore Database e o Firebase Authentication (incluindo o método de login por e-mail/senha e Google Sign-In).
      * Siga as instruções para adicionar o seu aplicativo Android ao projeto Firebase.
      * Baixe o arquivo `google-services.json` e coloque-o na pasta `app/` do seu projeto.
3.  **Abra no Android Studio** e execute a aplicação.
