# UEG_PROVIDER - JAR de Provedor de Serviço da Universidade Estadual de Goiás

O **UEG_PROVIDER** é um microsserviço desenvolvido com **Spring Boot** 
que atua como fornecedor de dados e operações relacionadas a UEG 
Elaborado para ser compatível com a PIPA.

`StudentService` e `TeacherService` usam `AiInferenceClient` do `PIPA_INTEGRATOR` nos métodos que precisam interpretar nomes de disciplinas, alunos, cursos, períodos ou dias. O PIPA Core injeta `PipaAiInferenceClient` e aplica o mesmo `AI_ROUTING_MODE` do agente Guará; o provider não seleciona nem autentica diretamente um modelo. A validação funcional com Ollama real ainda está pendente.

| Classe | Responsabilidade de IA |
|---|---|
| `StudentService` | Chama `AiInferenceClient.complete` para resolver disciplina, semestre e dia da semana nos métodos existentes. |
| `TeacherService` | Chama `AiInferenceClient.complete` para resolver nome de aluno e curso nos métodos existentes. |

---
## Personas
- Alunos
- Professores
- Convidados

##  Serviços já disponíveis

### Alunos
 - [StudentService.java](src/main/java/br/ueg/tc/ueg_provider/serviceprovider/StudentService.java)
   1. Consultar o horário de aulas
   2. Consultar aulas pelo dia
   3. Consultar aulas pela disciplina
   4. Consultar média geral
   5. Consultar média geral de uma disciplina
---
