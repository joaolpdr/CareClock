# CareClock
🚀 Sobre o Projeto
O careClock é um aplicativo Android simples e intuitivo, projetado para ajudar os usuários a gerenciar seus lembretes de medicamentos. Desenvolvido em Kotlin, o projeto utiliza a arquitetura de Views e XML tradicional, tornando-o um excelente ponto de partida para quem está aprendendo a desenvolver para a plataforma Android.

A aplicação permite que os usuários cadastrem tratamentos, agendem lembretes em intervalos específicos e recebam notificações, garantindo que nunca se esqueçam de tomar seus remédios.

✨ Funcionalidades
Cadastro de Medicamentos: Adicione novos medicamentos com nome, horário de início e intervalo de doses.

Lista Dinâmica: Visualize todos os medicamentos cadastrados em uma lista interativa com um contador regressivo para a próxima dose.

Notificações em Segundo Plano: Receba lembretes na barra de status do Android, garantindo que os alarmes sejam entregues mesmo com o aplicativo fechado.

Persistência de Dados: Os dados dos medicamentos são salvos localmente, permitindo que as informações persistam mesmo após o fechamento do app ou o reinício do dispositivo.

🛠️ Tecnologias Utilizadas
Linguagem de Programação: Kotlin

Interface do Usuário: Views e XML

Gerenciador de Build: Gradle

Persistência de Dados: SharedPreferences com a biblioteca Gson

Agendamento de Tarefas: AlarmManager para agendar alarmes recorrentes

Notificações: BroadcastReceiver para receber eventos de alarme e exibir notificações

📂 Estrutura do Código
O projeto está organizado de forma modular para facilitar a manutenção e o entendimento:

*ui/*: Contém todas as Activities (telas) e o Adapter que gerencia a exibição da lista.

*models/*: Armazena a classe de dados Medication, que define a estrutura de um medicamento.

*storage/*: Gerencia o salvamento e carregamento dos dados de forma local e simples.

*notifications/*: Lida com a lógica de agendamento de alarmes e a exibição de notificações.

⚙️ Como Contribuir?

Fique à vontade para clonar este projeto, explorar o código e sugerir melhorias. Este repositório serve como um recurso educacional, e qualquer contribuição ou feedback é bem-vindo.
