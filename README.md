# UEG_PROVIDER - JAR de Provedor de Serviço da Universidade Estadual de Goiás

O **UEG_PROVIDER** é um microsserviço desenvolvido com **Spring Boot** 
que atua como fornecedor de dados e operações relacionadas a UEG 
Elaborado para ser compatível com a PIPA.

`StudentService` e `TeacherService` usam `AiInferenceClient` do `PIPA_INTEGRATOR` nos métodos que precisam interpretar nomes de disciplinas, alunos, cursos, períodos ou dias. O PIPA Core injeta `PipaAiInferenceClient` e aplica o mesmo `AI_ROUTING_MODE` do agente Guará; o provider não seleciona nem autentica diretamente um modelo. Em 03/10/2026, a resolução determinística foi revertida e a interpretação obrigatória por IA restaurada. A avaliação com Ollama está em [ia-local-2026-10-03.md](../../relatorios/ia-local-2026-10-03.md).

| Classe | Responsabilidade de IA |
|---|---|
| `StudentService` | Usa `getDisciplineNameResponse` e `AiInferenceClient.complete` para disciplinas; semestre e dia também são interpretados por IA. |
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


`getDisciplineNameResponse` voltou a compor `AIApi.disciplineNameQuestion`, a referência de nomes via `Formatter.formatDiscipline` e a entrada do usuário, chamando `AiInferenceClient.complete` em toda consulta por disciplina. A decisão de correspondência fica no modelo. `getAbsencesByDiscipline` e `getScheduleByDisciplineName` passam apenas os nomes à IA, como `getGradesByDiscipline`; os DTOs completos são usados na resposta acadêmica. `Formatter.formatDiscipline` aceita `List<? extends IDiscipline>` para reutilizar os tipos existentes. O Core acrescenta instruções estruturadas somente para modelos locais; não há resolução determinística, filtro de nomes ou bypass de inferência. Ver [diagnóstico atual](../../relatorios/ia-local-2026-10-03.md).

`StudentServiceDisciplinePromptTest` cobre a chamada obrigatória ao LLM para sigla/nome exato, o campo `disciplina` dos horários e a preservação dos dados de faltas na resposta. O ensaio real de faltas com 4B Instruct acertou POO 1 e Gestão de Projetos; detalhes no diagnóstico acima.
